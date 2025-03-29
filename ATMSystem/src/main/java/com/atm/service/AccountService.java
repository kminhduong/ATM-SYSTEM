package com.atm.service;

import com.atm.dto.AccountDTO;
import com.atm.model.Account;
import com.atm.model.Credential;
import com.atm.model.User;
import com.atm.repository.AccountRepository;
import com.atm.repository.BalanceRepository;
import com.atm.repository.CredentialRepository;
import com.atm.repository.UserRepository;
import com.atm.model.Balance;
import com.atm.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final CredentialRepository credentialRepository;
    private final BalanceRepository balanceRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final BalanceService balanceService;

    @Autowired
    public AccountService(AccountRepository accountRepository,
                          CredentialRepository credentialRepository,
                          BalanceRepository balanceRepository,
                          UserRepository userRepository,
                          JdbcTemplate jdbcTemplate,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder, BalanceService balanceService) {
        this.accountRepository = accountRepository;
        this.credentialRepository = credentialRepository;
        this.balanceRepository = balanceRepository;
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.balanceService = balanceService;
    }

    public Account getAccount(String accountNumber) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);

        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            logger.info("🔍 Tài khoản tìm thấy: {}, Role: {}", account.getAccountNumber(), account.getRole());
            return account;
        }

        logger.warn("⚠ Không tìm thấy tài khoản: {}", accountNumber);
        return null;
    }

    // Đăng ký tài khoản mới
    @Transactional
    public Account createAccount(Account account) {
        logger.info("🔍 Đang vào phương thức register...");
        logger.info("Received request to register account: {}", account.getAccountNumber());

        // Kiểm tra xem tài khoản đã tồn tại hay chưa
        if (accountRepository.existsById(account.getAccountNumber())) {
            logger.error("Account already exists: {}", account.getAccountNumber());
            throw new IllegalArgumentException("Tài khoản đã tồn tại!");
        }

        // Kiểm tra User của tài khoản
        User user = account.getUser();
        if (user == null) {
            // Nếu không có thông tin người dùng trong account, lấy thông tin người dùng từ DB
            logger.info("User của tài khoản là null, kiểm tra lại từ DB...");
            user = userRepository.findByUserId(account.getUser().getUserId()).orElse(null);

            if (user != null) {
                // Kiểm tra ràng buộc 1 userId chỉ có 1 name
                if (!user.getName().equals(account.getFullName())) {
                    logger.error("User with ID {} already exists with a different name: {}", account.getUser().getUserId(), user.getName());
                    throw new IllegalArgumentException("Tên người dùng không khớp với userId!");
                }
            }
        }

        if (user == null) {
            // Nếu không tìm thấy User trong DB, tạo User mới và gán cho tài khoản
            logger.info("Không tìm thấy User, tạo User mới...");

            // Lấy full name từ tài khoản
            String fullName = account.getFullName();  // Tên người dùng từ Account

            // Lấy userId người dùng nhập vào (nếu có)
            String userId = account.getUser().getUserId();  // Giả sử người dùng đã nhập userId khi tạo account

            // Kiểm tra tính hợp lệ của userId (CCCD phải là 12 số)
            if (userId == null || !userId.matches("\\d{12}")) {
                logger.error("Invalid userId: {}", userId);
                throw new IllegalArgumentException("userId phải là 12 số (CCCD)");
            }

            // Tạo user mới từ userId và tên người dùng
            user = new User();
            user.setUserId(userId);  // Lưu userId người dùng nhập vào
            user.setName(fullName);  // Lưu tên người dùng nhập vào (tương ứng với full_name trong Account)

            // Lưu User mới vào DB
            userRepository.save(user);
            logger.info("User mới được tạo với ID: {}", user.getUserId());

            // Gán User cho tài khoản
            account.setUser(user);
        } else {
            logger.info("User đã tồn tại: {}", user.getUserId());
        }

        // Lưu tài khoản vào bảng Account
        Account savedAccount = accountRepository.save(account);
        accountRepository.flush(); // Đảm bảo tài khoản được commit vào DB

        // Tạo và lưu thông tin Balance
        Balance balance = new Balance();
        balance.setAccount(savedAccount); // Liên kết Balance với tài khoản
        balance.setBalance(0.0); // Số dư mặc định
        balance.setLastUpdated(LocalDateTime.now());
        balanceRepository.save(balance);
        logger.info("Balance record created for account: {}", savedAccount.getAccountNumber());

        // Tạo thông tin Credential với PIN mặc định
        Credential credential = new Credential();
        credential.setAccount(savedAccount);
        credential.setPin(passwordEncoder.encode("000000")); // Mã hóa PIN mặc định
        credential.setFailedAttempts(0);
        credential.setLockTime(null);
        credential.setUpdateAt(LocalDateTime.now());
        credentialRepository.save(credential);

        logger.info("Successfully registered account: {}", savedAccount.getAccountNumber());
        return savedAccount;
    }

    // Kiểm tra user có tồn tại không
    public boolean isUserExists(String userId) {
        String sql = "SELECT COUNT(*) FROM user WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null && count > 0;
    }

    @Transactional
    public void updateAccount(AccountDTO accountDTO, String accountNumber) {
        Optional<Account> optionalAccount = accountRepository.findById(accountDTO.getAccountNumber());

        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();

            // Kiểm tra quyền cập nhật
            String userRole = getUserRole(accountNumber);
            if (!"ADMIN".equals(userRole) && !accountNumber.equals(accountDTO.getAccountNumber())) {
                throw new RuntimeException("Bạn không có quyền cập nhật tài khoản này!");
            }

            // Cập nhật thông tin Account nếu có thay đổi
            if (accountDTO.getFullName() != null) {
                account.setFullName(accountDTO.getFullName());
            }

            // Cập nhật Balance thông qua hàm tách riêng
            balanceService.updateBalance(accountDTO, account);

            // Cập nhật Pin nếu có thay đổi
            if (accountDTO.getPin() != null) {
                Optional<Credential> optionalCredential = credentialRepository.findById(accountDTO.getAccountNumber());
                if (optionalCredential.isPresent()) {
                    Credential credential = optionalCredential.get();
                    credential.setPin(passwordEncoder.encode(accountDTO.getPin())); // Mã hóa pin mới
                    credential.setUpdateAt(LocalDateTime.now());
                    credentialRepository.save(credential);  // Lưu Credential đã cập nhật
                } else {
                    throw new RuntimeException("Không tìm thấy thông tin Credential cho tài khoản này.");
                }
            }

            // Cập nhật Role nếu có thay đổi
            if (accountDTO.getRole() != null && !accountDTO.getRole().isEmpty()) {
                account.setRole(accountDTO.getRole());
            }

            // Cập nhật User nếu có thay đổi
            if (account.getUser() != null) {
                User user = account.getUser();

                // Cập nhật số điện thoại trong User nếu có thay đổi
                if (accountDTO.getPhoneNumber() != null && !accountDTO.getPhoneNumber().equals(user.getPhone())) {
                    user.setPhone(accountDTO.getPhoneNumber());
                }

                // Cập nhật tên đầy đủ trong User nếu có thay đổi
                if (accountDTO.getFullName() != null && !accountDTO.getFullName().equals(user.getName())) {
                    user.setName(accountDTO.getFullName());
                }

                // Lưu thông tin User đã cập nhật
                try {
                    userRepository.save(user);  // Lưu thông tin User đã cập nhật
                } catch (Exception e) {
                    throw new RuntimeException("Có lỗi khi lưu thông tin người dùng: " + e.getMessage());
                }
            } else {
                throw new RuntimeException("Không tìm thấy người dùng liên kết với tài khoản này.");
            }

            // Lưu Account (Hibernate sẽ tự động lưu Balance khi Account được lưu)
            accountRepository.save(account);

        } else {
            throw new RuntimeException("Tài khoản không tồn tại.");
        }
    }

    // Lấy tất cả khách hàng (dành cho nhân viên ngân hàng)
    public List<Account> getAllCustomers() {
        return accountRepository.findAll();
    }
    public String getUserRole(String accountNumber) {
        return accountRepository.findRoleByAccountNumber(accountNumber);
    }

//    public void updateAccountStatus(String new_status) {
//        // Danh sách trạng thái hợp lệ
//        List<String> validStatuses = Arrays.asList("ACTIVE", "CLOSED", "FROZEN", "BLOCKED", "PENDING");
//
//        // Kiểm tra tính hợp lệ của trạng thái
//        if (!validStatuses.contains(new_status)) {
//            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + new_status);
//        }
//
//        // Cập nhật trạng thái
//        account.setStatus(new_status);
//        accountRepository.save(account); // Lưu vào cơ sở dữ liệu
//
//        System.out.println("Trạng thái tài khoản đã được cập nhật thành: " + new_status);
//    }

//    public String checkAccountStatus() {
//        // Assume 'status' is a field in your Account class
//        switch (account.getStatus()) {
//            case "ACTIVE":
//                return "The account is active.";
//            case "CLOSED":
//                return "The account has been closed.";
//            case "FROZEN":
//                return "The account is frozen.";
//            case "BLOCKED":
//                return "The account is blocked.";
//            case "PENDING":
//                return "The account is pending.";
//            default:
//                return "Unknown status.";
//        }
//    }
}