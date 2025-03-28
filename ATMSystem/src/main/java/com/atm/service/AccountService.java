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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CredentialRepository credentialRepository;
    @Autowired
    private BalanceRepository balanceRepository;
    @Autowired
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private JwtUtil jwtUtil;

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
    public Account register(Account account) {
        logger.info("🔍 Đang vào phương thức register...");
        logger.info("Received request to register account: {}", account.getAccountNumber());

        // Kiểm tra xem tài khoản đã tồn tại hay chưa
        if (accountRepository.existsById(account.getAccountNumber())) {
            logger.error("Account already exists: {}", account.getAccountNumber());
            throw new IllegalArgumentException("Tài khoản đã tồn tại!");
        }

        User user = account.getUser();
        logger.info("Checking user existence for userId: {}", user);

        // Kiểm tra User của tài khoản
        if (user == null) {
            logger.info("User của tài khoản là null, kiểm tra lại từ DB...");
            user = userRepository.findById(account.getUser().getId()).orElse(null);
        }

        if (user == null) {
            logger.info("Không tìm thấy User, tạo User mới...");
            String newUserId = createUser(account.getFullName());
            user = userRepository.findById(newUserId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy User mới tạo với ID: " + newUserId));
            account.setUser(user);
        } else {
            logger.info("User đã tồn tại: {}", user.getId());
        }

        // Lưu tài khoản vào bảng Account trước
        Account savedAccount = accountRepository.save(account);
        accountRepository.flush(); // 🚀 Đảm bảo Account được commit trước khi dùng trong Credential

        // Tạo và lưu thông tin Balance
        Balance balance = new Balance();
        balance.setAccount(savedAccount); // Liên kết Balance với Account
        balance.setBalance(0.0); // Số dư mặc định
        balance.setLastUpdated(LocalDateTime.now());
        balanceRepository.save(balance);
        logger.info("Balance record created for account: {}", savedAccount.getAccountNumber());

        // Tạo thông tin Credential
        Credential credential = new Credential();
        credential.setAccount(savedAccount); // ✅ Không cần set accountNumber nữa vì @MapsId tự xử lý
        credential.setPin(passwordEncoder.encode("000000")); // Mã hóa PIN mặc định
        credential.setFailedAttempts(0);
        credential.setLockTime(null);
        credential.setUpdateAt(LocalDateTime.now());
        credentialRepository.save(credential);

        logger.info("Successfully registered account: {}", savedAccount.getAccountNumber());
        return savedAccount;
    }

    // Đăng nhập (Authenticate)
    public boolean authenticate(String accountNumber, String password) {
        Optional<Account> account = accountRepository.findByAccountNumber(accountNumber);

        if (account.isPresent()) {
            // So sánh trực tiếp mật khẩu
            return password.equals(account.get().getPassword());
        }
        return false;
    }

    public String getUserRole(String accountNumber) {
        return accountRepository.findRoleByAccountNumber(accountNumber);
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

            // Cập nhật Balance nếu tồn tại, hoặc tạo mới
            if (accountDTO.getBalance() != null) {
                if (account.getBalanceEntity() == null) {
                    // Tạo mới Balance nếu chưa có
                    Balance newBalance = new Balance();
                    newBalance.setBalance(accountDTO.getBalance());  // Sử dụng balance thay vì available_balance
                    newBalance.setAccount(account);  // Liên kết Balance với Account
                    account.setBalanceEntity(newBalance);
                    balanceRepository.save(newBalance);  // Lưu Balance mới
                } else {
                    // Cập nhật Balance nếu đã tồn tại
                    account.getBalanceEntity().setBalance(accountDTO.getBalance());
                    balanceRepository.save(account.getBalanceEntity());  // Lưu Balance đã cập nhật
                }
            }

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

    public Double getBalance(String accountNumber) {
        // Lấy tài khoản đang đăng nhập
        String loggedInAccountNumber = getLoggedInAccountNumber();

        // Kiểm tra xem tài khoản yêu cầu có phải của người dùng đang đăng nhập hay không
        if (!accountNumber.equals(loggedInAccountNumber)) {
            throw new SecurityException("Bạn không có quyền truy cập số dư của tài khoản này.");
        }

        return accountRepository.findByAccountNumber(accountNumber)
                .map(Account::getBalance)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại."));
    }

    // Hàm lấy số tài khoản của người dùng hiện tại
    public String getLoggedInAccountNumber() {
        System.out.println("🔍 Kiểm tra SecurityContextHolder: " + SecurityContextHolder.getContext().getAuthentication());

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            System.out.println("❌ SecurityContextHolder is NULL!");
            return null;
        }

        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    // Lấy tất cả khách hàng (dành cho nhân viên ngân hàng)
    public List<Account> getAllCustomers() {
        return accountRepository.findAll();
    }

    // Kiểm tra user có tồn tại không
    public boolean isUserExists(String userId) {
        String sql = "SELECT COUNT(*) FROM user WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null && count > 0;
    }

    public boolean isAdminAccountExists(String userId) {
        String sql = "SELECT COUNT(*) FROM account WHERE user_id = ? AND role = 'ADMIN'";
        logger.info("Checking admin existence for userId: " + userId);

        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, new Object[]{userId});
            logger.info("Admin account count: " + count);
            return count != null && count > 0;
        } catch (Exception e) {
            logger.error("Error checking admin account existence: ", e);
            return false;
        }
    }

    // Tạo user mới và trả về userId (giả sử user_id là UUID hoặc bạn tự sinh chuỗi)
    public String createUser(String fullName) {
        String sqlCheck = "SELECT user_id FROM `User` WHERE name = ?";
        List<String> existingUsers = jdbcTemplate.queryForList(sqlCheck, String.class, fullName);

        if (!existingUsers.isEmpty()) {
            System.out.println("User already exists with ID: " + existingUsers.get(0));
            return existingUsers.get(0); // Trả về userId của User đã tồn tại
        }

        // Nếu không tìm thấy, tạo User mới
        String userId = java.util.UUID.randomUUID().toString();
        String sqlInsert = "INSERT INTO `User` (user_id, name) VALUES (?, ?)";
        int rows = jdbcTemplate.update(sqlInsert, userId, fullName);

        if (rows > 0) {
            System.out.println("User created with ID: " + userId);
            return userId;
        } else {
            throw new RuntimeException("Failed to create user");
        }
    }

    public String authenticateAndGenerateToken(String accountNumber, String password) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);

        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();

            // Kiểm tra mật khẩu đã mã hóa
            if (passwordEncoder.matches(password, account.getPassword())) {
                String role = account.getRole(); // Lấy role trực tiếp từ entity
                logger.info("🔍 Role từ DB khi đăng nhập: {}", role);

                // Tạo JWT với role từ DB
                return jwtUtil.generateToken(accountNumber, role, 86400000); // Token hết hạn sau 1 ngày
            } else {
                throw new IllegalArgumentException("Sai mật khẩu!");
            }
        } else {
            throw new IllegalArgumentException("Tài khoản không tồn tại!");
        }
    }
    public Optional<Account> getAccountByNumberAndPassword(String accountNumber, String password) {
        return accountRepository.findByAccountNumberAndPassword(accountNumber, password);
    }
}