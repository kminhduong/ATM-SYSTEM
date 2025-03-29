package com.atm.service;

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

    public void createUser(User user) {
        logger.info("Creating user with id: {}", user.getUserId());

        String sqlCheck = "SELECT user_id FROM `User` WHERE user_id = ?";
        String existingUserId = null;

        try {
            existingUserId = jdbcTemplate.queryForObject(sqlCheck, String.class, user.getUserId());
        } catch (EmptyResultDataAccessException e) {
            // Nếu không tìm thấy, tiếp tục tạo mới
        }

        if (existingUserId != null) {
            logger.info("User already exists with ID: {}", existingUserId);
            return; // Hoặc bạn có thể ném ra ngoại lệ nếu cần
        } else {
            logger.info("User does not exist, creating user with id: {}", user.getUserId());

            // Chèn user mới vào cơ sở dữ liệu
            String sqlInsert = "INSERT INTO `User` (user_id, name) VALUES (?, ?)";
            int rows = jdbcTemplate.update(sqlInsert, user.getUserId(), user.getName());

            if (rows > 0) {
                logger.info("User created with ID: {}", user.getUserId());
            } else {
                logger.error("Failed to create user with id: {}", user.getUserId());
                throw new RuntimeException("Failed to create user");
            }
        }
    }
}
