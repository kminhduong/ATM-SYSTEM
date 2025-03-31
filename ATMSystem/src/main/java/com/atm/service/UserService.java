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

        String phone = accountDTO.getPhone(); // Lấy thông tin số điện thoại
        if (phone == null || phone.isEmpty()) {
            logger.error("Phone is required for user registration.");
            throw new IllegalArgumentException("Số điện thoại là bắt buộc.");
        }

        String email = accountDTO.getEmail(); // Lấy thông tin email
        if (email == null || email.isEmpty()) {
            logger.error("Email is required for user registration.");
            throw new IllegalArgumentException("Email là bắt buộc.");
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
                logger.error("User ID: {} đã tồn tại nhưng tên không khớp. Tên hiện tại trong DB: {}, Tên được yêu cầu: {}", userId, existingName, fullName);
                throw new IllegalArgumentException("User ID đã tồn tại nhưng tên không khớp.");
            }
            logger.info("User ID: {} đã tồn tại với tên khớp: {}", userId, existingName);
            return false; // Người dùng đã tồn tại với thông tin đúng
        }

        // Tạo mới người dùng
        String sqlInsert = "INSERT INTO `User` (user_id, name, phone, email) VALUES (?, ?, ?, ?)";
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
            throw new RuntimeException("Có lỗi khi lưu thông tin người dùng: " + e.getMessage());
        }
    }
}
