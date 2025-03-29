package com.atm;

import com.atm.dto.AccountDTO;
import com.atm.model.Account;
import com.atm.model.AccountType;
import com.atm.model.User;
import com.atm.service.AccountService;
import com.atm.repository.UserRepository; // Import UserRepository
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.atm.model.AccountStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@SpringBootApplication(scanBasePackages = "com.atm")
@EnableJpaRepositories(basePackages = "com.atm.repository") // Đảm bảo quét các repository
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            logger.info("Starting ATM System...");

            // Khởi chạy ứng dụng và lấy Spring Context
            ConfigurableApplicationContext context = SpringApplication.run(Main.class, args);

            // Lấy instance UserRepository từ Spring context
            UserRepository userRepository = context.getBean(UserRepository.class);

            // Gọi phương thức khởi tạo tài khoản admin
            initializeAdminAccount(context.getBean(AccountService.class), userRepository);

            logger.info("ATM System started successfully.");
        } catch (Exception e) {
            logger.error("Failed to start ATM System: ", e);
        }
    }

    private static void initializeAdminAccount(AccountService accountService, UserRepository userRepository) {
        logger.info("Starting admin account initialization...");

        // Kiểm tra xem tài khoản admin đã tồn tại trong bảng User chưa
        User existingUser = userRepository.findByEmail("admin@example.com");
        if (existingUser == null) {
            logger.info("Admin user does not exist, proceeding to create...");

            // Tạo User mới với 4 tham số: userId, name, email, phone
            String userId = "123456789012";  // Giả sử đây là userId hợp lệ
            User user = new User(userId, "Admin User", "admin@example.com", "1234567890");  // UUID và createAt sẽ tự động được thiết lập

            // Lưu User vào cơ sở dữ liệu
            userRepository.save(user);  // Lưu vào DB
            logger.info("Admin user created successfully!");

            // Tạo tài khoản admin mới sử dụng AccountDTO
            AccountDTO adminDTO = new AccountDTO(
                    "9999999999",           // accountNumber
                    "admin",                // username
                    "Default Admin",        // fullName
                    user.getUserId(),       // userId từ User mới tạo
                    AccountType.SAVINGS,    // accountType
                    AccountStatus.ACTIVE,   // status
                    0.0,                    // balance
                    "123456",               // pin
                    "ADMIN"                 // role
            );

            // Chuyển đổi AccountDTO thành Account entity và lưu vào cơ sở dữ liệu
            Account account = adminDTO.toAccount(userRepository); // Truyền UserRepository vào đây

            // Đăng ký tài khoản admin
            accountService.register(account); // Gọi phương thức register để lưu tài khoản admin
            logger.info("Admin account has been created successfully!");
        } else {
            logger.info("Admin user already exists, skipping user creation.");
        }
    }
}
