package com.atm.service;

import com.atm.dto.AccountDTO;
import com.atm.model.Account;
import com.atm.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    @Autowired
    private AccountRepository accountRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Account getAccount(String accountNumber) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        return accountOpt.orElse(null);
    }

    // Đăng ký tài khoản mới
    @Transactional
    public Account register(Account account) {
        logger.info("Checking if account exists with account number: " + account.getAccountNumber());
        if (accountRepository.existsById(account.getAccountNumber())) {
            throw new IllegalArgumentException("Tài khoản đã tồn tại!");
        }

        // Kiểm tra userId, nếu chưa có thì tạo mới user
        String userId = account.getUserId();
        logger.info("Checking userId: " + userId);
        if (userId == null || userId.isEmpty() || !isUserExists(userId)) {
            logger.info("User exists check for userId: " + userId);
            userId = createUser(account.getFullName());
            account.setUserId(userId);
            logger.info("Created new userId for account: " + userId);
        }

        // Đăng ký tài khoản mới
        logger.info("Registering account with account number: " + account.getAccountNumber());
        return accountRepository.save(account);
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
}