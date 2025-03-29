package com.atm.controller;

import com.atm.dto.AccountDTO;
import com.atm.model.Account;
import com.atm.model.User;
import com.atm.repository.UserRepository;
import com.atm.service.AccountService;
import com.atm.service.TransactionService;
import com.atm.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
    private final UserRepository userRepository; // Thêm UserRepository

    private final JwtUtil jwtUtil;
    private final TransactionService transactionService;

    @Autowired
    public AccountController(JwtUtil jwtUtil, AccountService accountService, UserRepository userRepository,TransactionService transactionService) {
        this.jwtUtil = jwtUtil;
        this.accountService = accountService;
        this.userRepository = userRepository;
        this.transactionService = transactionService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AccountDTO accountDTO) {
        try {
            logger.info("Skipping authorization check for testing.");

            // Lấy userId từ DTO
            String userId = accountDTO.getUserId();

            // Kiểm tra nếu userId chưa tồn tại, tự động tạo user nếu cần
            if (userId == null || userId.isEmpty() || !accountService.isUserExists(userId)) {
                logger.info("User with userId: {} does not exist, creating a new user.", userId);

                // Kiểm tra nếu fullName và các thông tin khác hợp lệ
                if (accountDTO.getFullName() == null || accountDTO.getFullName().isEmpty()) {
                    logger.error("Full name is required for user registration.");
                    return ResponseEntity.badRequest().body("Họ tên là bắt buộc.");
                }

                // Tạo người dùng mới nếu chưa tồn tại
                User user = new User(userId, accountDTO.getFullName(), accountDTO.getUsername(), accountDTO.getPhoneNumber());
                accountService.createUser(user);
            } else {
                logger.info("User with userId: {} already exists.", userId);
            }

            // Chuyển đổi DTO thành Account entity và đăng ký tài khoản
            Account account = accountDTO.toAccount(userRepository);  // Chuyển từ DTO thành Account entity
            accountService.register(account);

            logger.info("Account registered successfully for userId: {}", userId);
            return ResponseEntity.ok("Tài khoản đã được đăng ký thành công!");
        } catch (IllegalArgumentException e) {
            logger.error("Error while registering account: " + e.getMessage());
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi khi đăng ký tài khoản!");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> loginRequest) {
        String accountNumber = loginRequest.get("accountNumber");
        String pin = loginRequest.get("pin");

        String token = transactionService.login(accountNumber, pin);
        if (token != null) {
            Map<String, String> response = Map.of(
                    "message", "Login successful",
                    "token", token
            );
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid account number or PIN."));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateAccount(@RequestBody AccountDTO accountDTO,
                                                @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bạn cần đăng nhập trước!");
        }

        String token = authHeader.substring(7);
        String accountNumber = jwtUtil.validateToken(token); // Lấy accountNumber từ token

        if (accountNumber == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token không hợp lệ!");
        }

        try {
            accountService.updateAccount(accountDTO, accountNumber);
            return ResponseEntity.ok("Cập nhật tài khoản thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi khi cập nhật tài khoản.");
        }
    }

    @GetMapping("/balance")
    public ResponseEntity<Double> getBalance() {
        try {
            String loggedInAccountNumber = accountService.getLoggedInAccountNumber();
            System.out.println("🔹 Logged in Account: " + loggedInAccountNumber);

            if (loggedInAccountNumber == null) {
                System.out.println("❌ Authentication failed! SecurityContextHolder is NULL.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }

            Double balance = accountService.getBalance(loggedInAccountNumber);
            System.out.println("✅ Balance Retrieved: " + balance);

            return ResponseEntity.ok(balance);
        } catch (SecurityException e) {
            System.out.println("❌ SecurityException: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    // Xem toàn bộ khách hàng (dành cho nhân viên ngân hàng)
    @GetMapping("/customers")
    public ResponseEntity<List<AccountDTO>> getAllCustomers(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String token = authHeader.substring(7); // Loại bỏ tiền tố "Bearer "
        String accountNumber = jwtUtil.validateToken(token);

        if (accountNumber != null) {
            List<AccountDTO> customers = accountService.getAllCustomers().stream()
                    .map(AccountDTO::fromAccount)
                    .toList();
            return ResponseEntity.ok(customers);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bạn cần đăng nhập trước!");
        }

        String token = authHeader.substring(7);
        if (jwtUtil.isTokenValid(token)) {
            // Khi đăng xuất, tạo một token mới với thời gian hết hạn cực ngắn (1ms)
            jwtUtil.generateToken(jwtUtil.validateToken(token), "USER", 1);
        }

        return ResponseEntity.ok("Đăng xuất thành công!");
    }
    @GetMapping("/check-role")
    public ResponseEntity<String> checkRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("👤 User: " + authentication.getName());
        System.out.println("🔐 Authorities: " + authentication.getAuthorities());

        return ResponseEntity.ok("Check console for role details.");
    }

}