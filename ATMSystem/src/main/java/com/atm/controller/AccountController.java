package com.atm.controller;

import com.atm.dto.AccountDTO;
import com.atm.dto.ApiResponse;
import com.atm.model.Account;
import com.atm.service.AccountService;
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

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AccountController.class);

    @Autowired
    public AccountController(JwtUtil jwtUtil,
                             AccountService accountService,
                             TransactionService transactionService,
                             UserService userService) {
        this.jwtUtil = jwtUtil;
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AccountDTO accountDTO) {
        try {
            logger.info("Skipping authorization check for testing.");

            // Kiểm tra và tạo người dùng nếu cần
            userService.createUser(accountDTO);

            // Đăng ký tài khoản
            accountService.registerAccount(accountDTO);

            return ResponseEntity.ok("The account has been successfully registered!");
        } catch (IllegalArgumentException e) {
            logger.error("Error while registering account: " + e.getMessage());
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred when registering an account!");
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid tokens!");
        }
        accountService.updateAccount(accountDTO, accountNumber);
        return ResponseEntity.ok("Account update successful!");
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
        return ResponseEntity.ok("Log out successfully!");
    }

    // API lấy lịch sử giao dịch theo user
    @RequestMapping(value="/get-by-user/{userId}", method = RequestMethod.GET)
    public ResponseEntity<ApiResponse<List<Account>>> getAccountsByUser(@PathVariable("userId") String userId) {

        if (userId == null) {
            return ResponseEntity.internalServerError().body(null);
        }
        ApiResponse<List<Account>> response = accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(response);

    }
}
