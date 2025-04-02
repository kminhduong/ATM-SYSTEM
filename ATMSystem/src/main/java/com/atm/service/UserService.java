package com.atm.service;

import com.atm.dto.AccountDTO;
import com.atm.model.User;
import com.atm.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

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
            throw new IllegalArgumentException("User ID is required.");
        }

        String fullName = accountDTO.getFullName();
        if (fullName == null || fullName.isEmpty()) {
            logger.error("Full name is required for user registration.");
            throw new IllegalArgumentException("Full name is required.");
        }

        String phone = accountDTO.getPhone(); // Lấy thông tin số điện thoại
        if (phone == null || phone.isEmpty()) {
            logger.error("Phone is required for user registration.");
            throw new IllegalArgumentException("Phone number is required.");
        }

        String email = accountDTO.getEmail(); // Lấy thông tin email
        if (email == null || email.isEmpty()) {
            logger.error("Email is required for user registration.");
            throw new IllegalArgumentException("Email is required.");
        }

        // Kiểm tra trong cơ sở dữ liệu dựa trên userId
        String sqlCheck = "SELECT name FROM `User` WHERE user_id = ?";
        String existingName = null;
        try {
            existingName = jdbcTemplate.queryForObject(sqlCheck, String.class, userId);
        } catch (EmptyResultDataAccessException e) {
            // Không tìm thấy userId, tiếp tục tạo mới
        }

        if (existingName != null) {
            // Kiểm tra nếu tên không khớp
            if (!existingName.equals(fullName)) {
                logger.error("User ID: {} already exists but name does not match. Current name in DB: {}, Requested name: {}", userId, existingName, fullName);
                throw new IllegalArgumentException("User ID already exists but name does not match.");
            }
            logger.info("User ID: {} already exists with matching name: {}", userId, existingName);
            return false; // Người dùng đã tồn tại với thông tin đúng
        }

        // Tạo mới người dùng
        String sqlInsert = "INSERT INTO `User` (user_id, name, phone, email, create_at)\n" +
                "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP);";
        int rows = jdbcTemplate.update(sqlInsert, userId, fullName, phone, email);
        if (rows > 0) {
            logger.info("User created with ID: {}", userId);
            return true; // Người dùng được tạo mới
        } else {
            logger.error("Failed to create user with ID: {}", userId);
            throw new RuntimeException("Failed to create user");
        }
    }

    public void updateUserDetails(User user, AccountDTO accountDTO) {
        if (accountDTO.getPhone() != null && !accountDTO.getPhone().equals(user.getPhone())) {
            user.setPhone(accountDTO.getPhone());
        }
        if (accountDTO.getFullName() != null && !accountDTO.getFullName().equals(user.getName())) {
            user.setName(accountDTO.getFullName());
        }

        try {
            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Error saving user information: " + e.getMessage());
        }
    }

    public List<User> getAllCustomers() {
        return userRepository.findAll();
    }

    public User getUserById(String userId) {
        return userRepository.findByUserId(userId).orElseThrow(() -> new EntityNotFoundException("User not found with id " + userId));
    }
}
