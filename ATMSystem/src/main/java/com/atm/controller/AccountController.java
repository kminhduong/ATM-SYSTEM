package com.atm.controller;

import com.atm.dto.AccountDTO;
import com.atm.model.Account;
import com.atm.model.User;
import com.atm.repository.UserRepository;
import com.atm.service.AccountService;
import com.atm.service.BalanceService;
import com.atm.service.TransactionService;
import com.atm.service.UserService;
import com.atm.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final TransactionService transactionService;
    private final UserRepository userRepository; // 🔹 Thêm biến này
    private final BalanceService balanceService;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AccountController.class);

    @Autowired
    public AccountController(JwtUtil jwtUtil, AccountService accountService,
                             UserRepository userRepository, TransactionService transactionService,
                             UserService userService, BalanceService balanceService) {
        this.jwtUtil = jwtUtil;
        this.accountService = accountService;
        this.userRepository = userRepository; // 🔹 Inject vào constructor
        this.transactionService = transactionService;
        this.userService = userService;
        this.balanceService = balanceService;
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
                userService.createUser(user);
            } else {
                logger.info("User with userId: {} already exists.", userId);
            }

            // Chuyển đổi DTO thành Account entity và đăng ký tài khoản
            Account account = accountDTO.toAccount(userRepository);  // Chuyển từ DTO thành Account entity
            accountService.createAccount(account);

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
        String token = transactionService.login(loginRequest.get("accountNumber"), loginRequest.get("pin"));
        if (token != null) {
            return ResponseEntity.ok(Map.of("message", "Login successful", "token", token));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid account number or PIN."));
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateAccount(@RequestBody AccountDTO accountDTO, @RequestHeader("Authorization") String authHeader) {
        String accountNumber = jwtUtil.validateToken(authHeader.substring(7));
        if (accountNumber == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token không hợp lệ!");
        }
        accountService.updateAccount(accountDTO, accountNumber);
        return ResponseEntity.ok("Cập nhật tài khoản thành công!");
    }

    @GetMapping("/balance")
    public ResponseEntity<Double> getBalance() {
        String accountNumber = balanceService.getLoggedInAccountNumber();
        return accountNumber != null ? ResponseEntity.ok(balanceService.getBalance(accountNumber))
                : ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }

    @GetMapping("/customers")
    public ResponseEntity<List<AccountDTO>> getAllCustomers(@RequestHeader("Authorization") String authHeader) {
        String accountNumber = jwtUtil.validateToken(authHeader.substring(7));
        return accountNumber != null ? ResponseEntity.ok(accountService.getAllCustomers().stream().map(AccountDTO::fromAccount).toList())
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        if (jwtUtil.isTokenValid(token)) {
            jwtUtil.generateToken(jwtUtil.validateToken(token), "USER", 1);
        }
        return ResponseEntity.ok("Đăng xuất thành công!");
    }
}
