package com.atm.service;

import com.atm.dto.AccountDTO;
import com.atm.model.User;
import com.atm.repository.AccountRepository;
import com.atm.repository.BalanceRepository;
import com.atm.repository.CredentialRepository;
import com.atm.repository.UserRepository;
import com.atm.util.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;


    @Autowired
    public UserService(UserRepository userRepository,
                          JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public User getUserInfo(String userId) {
        return userRepository.findUserWithAccountsByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + userId));
    }

    public boolean createUser(AccountDTO accountDTO) {
        String userId = accountDTO.getUserId();
        if (userId == null || userId.isEmpty()) {
            logger.error("User ID is required.");
            throw new IllegalArgumentException("User ID là bắt buộc.");
        }

        String fullName = accountDTO.getFullName();
        if (fullName == null || fullName.isEmpty()) {
            logger.error("Full name is required for user registration.");
            throw new IllegalArgumentException("Họ tên là bắt buộc.");
        }

        // Kiểm tra trong cơ sở dữ liệu
        String sqlCheck = "SELECT user_id FROM `User` WHERE user_id = ?";
        String existingUserId = null;
        try {
            existingUserId = jdbcTemplate.queryForObject(sqlCheck, String.class, userId);
        } catch (EmptyResultDataAccessException e) {
            // Không tìm thấy, tiếp tục tạo mới
        }

        if (existingUserId != null) {
            logger.info("User already exists with ID: {}", existingUserId);
            return false; // Người dùng đã tồn tại
        }

        // Tạo mới người dùng
        String sqlInsert = "INSERT INTO `User` (user_id, name) VALUES (?, ?)";
        int rows = jdbcTemplate.update(sqlInsert, userId, fullName);
        if (rows > 0) {
            logger.info("User created with ID: {}", userId);
            return true; // Người dùng được tạo mới
        } else {
            logger.error("Failed to create user with ID: {}", userId);
            throw new RuntimeException("Failed to create user");
        }
    }

    public void updateUserDetails(User user, AccountDTO accountDTO) {
        if (accountDTO.getPhoneNumber() != null && !accountDTO.getPhoneNumber().equals(user.getPhone())) {
            user.setPhone(accountDTO.getPhoneNumber());
        }
        if (accountDTO.getFullName() != null && !accountDTO.getFullName().equals(user.getName())) {
            user.setName(accountDTO.getFullName());
        }

        try {
            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi khi lưu thông tin người dùng: " + e.getMessage());
        }
    }
}
