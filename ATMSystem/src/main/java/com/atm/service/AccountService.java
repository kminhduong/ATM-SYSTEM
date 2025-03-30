package com.atm.service;

import com.atm.dto.AccountDTO;
import com.atm.model.*;
import com.atm.repository.AccountRepository;
import com.atm.repository.BalanceRepository;
import com.atm.repository.CredentialRepository;
import com.atm.repository.UserRepository;
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
    private final CredentialService credentialService;
    private final UserService userService;

    @Autowired
    public AccountService(AccountRepository accountRepository,
                          CredentialRepository credentialRepository,
                          BalanceRepository balanceRepository,
                          UserRepository userRepository,
                          JdbcTemplate jdbcTemplate,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder,
                          BalanceService balanceService,
                          CredentialService credentialService,
                          UserService userService) {
        this.accountRepository = accountRepository;
        this.credentialRepository = credentialRepository;
        this.balanceRepository = balanceRepository;
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.balanceService = balanceService;
        this.credentialService = credentialService;
        this.userService = userService;
    }

    /**
     * Đăng ký tài khoản dựa trên thông tin từ AccountDTO.
     * Chuyển đổi dữ liệu và gọi hàm createAccount để xử lý logic tạo tài khoản.
     *
     * @param accountDTO Đối tượng chứa thông tin tài khoản cần đăng ký.
     */
    public void registerAccount(AccountDTO accountDTO) {
        // Chuyển đổi AccountDTO thành đối tượng Account.
        Account account = accountDTO.toAccount(userRepository);

        // Ghi log để xác nhận thông tin đã nhận.
        logger.info("Account registered successfully for userId: {}", accountDTO.getUserId());

        // Gọi hàm createAccount để xử lý logic kiểm tra và lưu tài khoản.
        createAccount(account);
    }

    /**
     * Tạo tài khoản trong hệ thống.
     * Kiểm tra sự tồn tại của tài khoản và người dùng, đồng thời tạo các thực thể liên quan.
     *
     * @param account Đối tượng chứa thông tin tài khoản.
     * @return Tài khoản đã được tạo.
     */
    @Transactional
    public Account createAccount(Account account) {
        logger.info("🔍 Đang vào phương thức createAccount...");
        logger.info("Received request to register account: {}", account.getAccountNumber());

        // 1. Kiểm tra xem tài khoản đã tồn tại hay chưa.
        if (accountRepository.existsById(account.getAccountNumber())) {
            logger.error("Account already exists: {}", account.getAccountNumber());
            throw new IllegalArgumentException("Tài khoản đã tồn tại!");
        }

        // 2. Kiểm tra thông tin người dùng (User) của tài khoản.
        User user = account.getUser();
        if (user == null) {
            logger.info("User của tài khoản là null, kiểm tra lại từ DB...");
            user = userRepository.findByUserId(account.getUser().getUserId()).orElse(null);

            // Nếu User tồn tại nhưng tên không khớp, ném ngoại lệ.
            if (user != null && !user.getName().equals(account.getFullName())) {
                logger.error("User với ID {} đã tồn tại nhưng có tên khác: {}", account.getUser().getUserId(), user.getName());
                throw new IllegalArgumentException("Tên người dùng không khớp với userId!");
            }

            // Nếu không tìm thấy User, tạo User mới.
            if (user == null) {
                logger.info("Không tìm thấy User, tạo User mới...");
                user = new User();
                user.setUserId(account.getUser().getUserId());
                user.setName(account.getFullName());
                userRepository.save(user);
                logger.info("User mới được tạo với ID: {}", user.getUserId());
            }
        }
        account.setUser(user);

        // 3. Lưu tài khoản vào bảng Account.
        Account savedAccount = accountRepository.save(account);

        balanceService.createBalance(savedAccount);
        credentialService.createCredential(savedAccount);

        logger.info("Successfully created account: {}", savedAccount.getAccountNumber());
        return savedAccount;
    }

    @Transactional
    public void updateAccount(AccountDTO accountDTO, String accountNumber) {
        Optional<Account> optionalAccount = accountRepository.findById(accountDTO.getAccountNumber());

        if (optionalAccount.isEmpty()) {
            throw new RuntimeException("Tài khoản không tồn tại.");
        }

        Account account = optionalAccount.get();

        // Kiểm tra quyền
        checkUpdatePermission(accountNumber, accountDTO);

        // Cập nhật thông tin tài khoản
        updateAccountDetails(account, accountDTO);

        // Cập nhật số dư
        balanceService.updateBalance(accountDTO, account, TransactionType.DEPOSIT);
        // Cập nhật thông tin bảo mật (Credential)
        if (accountDTO.getPin() != null) {
            credentialService.changePIN(accountDTO);
        }

        // Cập nhật thông tin người dùng
        if (account.getUser() != null) {
            userService.updateUserDetails(account.getUser(), accountDTO);
        } else {
            throw new RuntimeException("Không tìm thấy người dùng liên kết với tài khoản này.");
        }

        // Lưu Account sau khi cập nhật
        accountRepository.save(account);
    }

    private void checkUpdatePermission(String accountNumber, AccountDTO accountDTO) {
        String userRole = getUserRole(accountNumber);
        if (!"ADMIN".equals(userRole) && !accountNumber.equals(accountDTO.getAccountNumber())) {
            throw new RuntimeException("Bạn không có quyền cập nhật tài khoản này!");
        }
    }

    private void updateAccountDetails(Account account, AccountDTO accountDTO) {
        if (accountDTO.getFullName() != null) {
            account.setFullName(accountDTO.getFullName());
        }
        if (accountDTO.getRole() != null && !accountDTO.getRole().isEmpty()) {
            account.setRole(accountDTO.getRole());
        }
    }

    // Lấy tất cả khách hàng (dành cho nhân viên ngân hàng)
    public List<Account> getAllCustomers() {
        return accountRepository.findAll();
    }
    public String getUserRole(String accountNumber) {
        return accountRepository.findRoleByAccountNumber(accountNumber);
    }

//    public Account getAccount(String accountNumber) {
//        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
//
//        if (accountOpt.isPresent()) {
//            Account account = accountOpt.get();
//            logger.info("🔍 Tài khoản tìm thấy: {}, Role: {}", account.getAccountNumber(), account.getRole());
//            return account;
//        }
//
//        logger.warn("⚠ Không tìm thấy tài khoản: {}", accountNumber);
//        return null;
//    }

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
    // Kiểm tra user có tồn tại không
//    public boolean isUserExists(String userId) {
//        String sql = "SELECT COUNT(*) FROM user WHERE user_id = ?";
//        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
//        return count != null && count > 0;
//    }
}