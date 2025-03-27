package com.atm.service;

import com.atm.dto.AccountDTO;
import com.atm.model.Account;
import com.atm.model.Credential;
import com.atm.repository.AccountRepository;
import com.atm.repository.BalanceRepository;
import com.atm.repository.CredentialRepository;
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
        logger.info("Received request to register account: {}", account.getAccountNumber());

        if (accountRepository.existsById(account.getAccountNumber())) {
            logger.error("Account already exists: {}", account.getAccountNumber());
            throw new IllegalArgumentException("Tài khoản đã tồn tại!");
        }

        String userId = account.getUserId();
        logger.info("Checking user existence for userId: {}", userId);

        if (userId == null || userId.isEmpty() || !isUserExists(userId)) {
            logger.info("User does not exist, creating new user for fullName: {}", account.getFullName());
            userId = createUser(account.getFullName());
            account.setUserId(userId);
            logger.info("Created new user with userId: {}", userId);
        }

        // Lưu tài khoản vào bảng Account
        Account savedAccount = accountRepository.save(account);

        // Tạo và lưu thông tin Balance
        Balance balance = new Balance();
        balance.setAccountNumber(savedAccount.getAccountNumber());
        balance.setAvailableBalance(0.0); // Số dư mặc định
        balance.setLastUpdated(LocalDateTime.now());
        balanceRepository.save(balance);
        logger.info("Balance record created for account: {}", savedAccount.getAccountNumber());

        // Tạo thông tin Credential
        Credential credential = new Credential();
        credential.setAccountNumber(savedAccount.getAccountNumber());
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

    @Transactional
    public void updateAccount(AccountDTO accountDTO) {
        Optional<Account> optionalAccount = accountRepository.findById(accountDTO.getAccountNumber());

        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();
            account.setPin(accountDTO.getPin());
            account.setPhoneNumber(accountDTO.getPhoneNumber());
            account.setFullName(accountDTO.getFullName());
            account.setBalance(accountDTO.getBalance());

//            accountRepository.save(account);
            accountRepository.updateFullName(accountDTO.getAccountNumber(), accountDTO.getFullName());
            accountRepository.updatePhoneNumber(accountDTO.getAccountNumber(), accountDTO.getPhoneNumber());
        } else {
            throw new RuntimeException("Tài khoản không tồn tại.");
        }
    }

//    public Double getBalance(String accountNumber) {
//        return accountRepository.findByAccountNumber(accountNumber)
//                .map(Account::getBalance)
//                .orElse(null);
//    }
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

    // Tạo user mới và trả về userId (giả sử user_id là UUID hoặc bạn tự sinh chuỗi)
    public String createUser(String fullName) {
        String userId = java.util.UUID.randomUUID().toString();
        String sql = "INSERT INTO `User` (user_id, name) VALUES (?, ?)";
        int rows = jdbcTemplate.update(sql, userId, fullName);
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