package com.atm;

import com.atm.dto.AccountDTO;
import com.atm.model.Account;
import com.atm.model.AccountType;
import com.atm.service.AccountService;
import com.atm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.atm.model.AccountStatus;


@SpringBootApplication(scanBasePackages = "com.atm")
@EnableJpaRepositories(basePackages = "com.atm.repository") // Đảm bảo quét các repository
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            logger.info("Starting ATM System...");

            // Khởi chạy ứng dụng và lấy Spring Context
            ConfigurableApplicationContext context = SpringApplication.run(Main.class, args);

            // Lấy các bean từ context
            AccountService accountService = context.getBean(AccountService.class);
            UserRepository userRepository = context.getBean(UserRepository.class); // ✅ Lấy UserRepository từ context

            // Gọi phương thức khởi tạo tài khoản admin
            initializeAdminAccount(accountService, userRepository);

            logger.info("ATM System started successfully.");
        } catch (Exception e) {
            logger.error("Failed to start ATM System: ", e);
        }
    }

    private static void initializeAdminAccount(AccountService accountService, UserRepository userRepository) {
        logger.info("Starting admin account initialization...");
        if (!accountService.isUserExists("admin")) {
            logger.info("Admin account does not exist, proceeding to create...");

            // Tạo tài khoản admin mới
            AccountDTO admin = new AccountDTO(
                    "9999999999",           // accountNumber
                    "admin",                // username
                    "secureAdminPass",      // password
                    "Default Admin",        // fullName
                    "admin",                // userId
                    AccountType.SAVINGS,    // accountType
                    AccountStatus.ACTIVE,   // status
                    0.0,                    // balance (hoặc giá trị hợp lý khác)
                    "1234",                 // pin
                    "ADMIN"                 // role
            );

            // Chuyển đổi DTO thành Entity và lưu vào cơ sở dữ liệu
            Account account = admin.toAccount(userRepository); // ✅ Truyền UserRepository vào
            accountService.register(account);
            logger.info("Admin account has been created successfully!");
        } else {
            logger.info("Admin account already exists.");
        }
    }
}