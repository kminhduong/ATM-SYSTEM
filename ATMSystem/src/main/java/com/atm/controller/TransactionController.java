package com.atm.controller;

import com.atm.dto.ApiResponse;
import com.atm.dto.WithdrawOtpRequest;
import com.atm.model.Account;
import com.atm.model.Transaction;
import com.atm.model.TransactionType;
import com.atm.repository.AccountRepository;
import com.atm.repository.TransactionRepository;
import com.atm.service.AccountService;
import com.atm.service.TransactionService;
import com.atm.dto.WithdrawRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.atm.util.JwtUtil;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final JwtUtil jwtUtil;
    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    public TransactionController(AccountService accountService,
                                 AccountRepository accountRepository,
                                 TransactionRepository transactionRepository,
                                 TransactionService transactionService,
                                 JwtUtil jwtUtil) {
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
        this.jwtUtil = jwtUtil;
    }

    // ThreadLocal để đảm bảo mỗi luồng có một currentUser riêng
//    private static final ThreadLocal<Account> currentUser = new ThreadLocal<>();

    // Lấy user đang đăng nhập trong luồng hiện tại
//    private Account getCurrentUser() {
//        Account user = currentUser.get();
//        if (user == null) {
//            throw new RuntimeException("User not logged in");
//        }
//        return user;
//    }
    private String getCurrentAccountNumber(String token) {
        return jwtUtil.validateToken(token); // Trả về accountNumber từ token
    }

    // API Đăng nhập
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

    // API Đăng xuất
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            transactionService.logout(token);
            return ResponseEntity.ok("Logout successful. Token invalidated.");
        }

        return ResponseEntity.badRequest().body("Invalid token.");
    }

    public ApiResponse<String> withdraw(String token, double amount, TransactionType transactionType) {
        String accountNumber = jwtUtil.validateToken(token);
        if (accountNumber == null) {
            return new ApiResponse<>("Invalid or expired token", null);
        }

        Account account = accountService.getAccount(accountNumber);
        if (account == null) {
            return new ApiResponse<>("Account not found", null);
        }

        if (amount > account.getBalance()) {
            return new ApiResponse<>("Insufficient funds", null);
        }

        synchronized (account) {
            account.setBalance(account.getBalance() - amount);
            account.setLastUpdated(LocalDateTime.now());
            accountRepository.save(account);
        }

        Transaction transaction = new Transaction(accountNumber, amount, transactionType, new Date());
        transactionRepository.save(transaction);

        return new ApiResponse<>("Withdraw successful.", null);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<String>> withdraw(@RequestHeader("Authorization") String authHeader, @RequestBody WithdrawRequest request) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("Missing or invalid Authorization header", null));
        }

        String token = authHeader.substring(7);

        // Kiểm tra nếu token đã bị vô hiệu hóa
        if (transactionService.isTokenBlacklisted(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>("Token has been logged out", null));
        }

        // Lấy thông tin tài khoản từ token
        String accountNumber = jwtUtil.validateToken(token);
        if (accountNumber == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>("Token không hợp lệ hoặc hết hạn", null));
        }

        // Kiểm tra và thực hiện rút tiền
        ApiResponse<String> response = transactionService.withdraw(token, request.getAmount(), TransactionType.WITHDRAWAL);
        if (response != null && response.getMessage().equals("Giao dịch rút tiền thành công")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // API Rút tiền qua OTP
    @PostMapping("/withdraw/otp")
    public ResponseEntity<ApiResponse<String>> withdrawWithOtp(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody WithdrawOtpRequest request) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("Missing or invalid Authorization header", null));
        }

        String token = authHeader.substring(7);
        String accountNumber = jwtUtil.validateToken(token);
        if (accountNumber == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("Invalid or expired token", null));
        }

        if (request.getPhoneNumber() == null || request.getOtp() == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("Phone number and OTP are required.", null));
        }

        boolean otpValid = transactionService.validateOtp(accountNumber, request.getPhoneNumber(), request.getOtp());
        if (!otpValid) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("Invalid OTP.", null));
        }

        ApiResponse<String> response = transactionService.withdraw(accountNumber, request.getAmount(), TransactionType.WITHDRAWAL_OTP);
        if (response != null && "Withdrawal successful".equals(response.getMessage())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // API Lấy lịch sử giao dịch
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<Transaction>>> getTransactionHistory(@RequestParam("token") String token) {
        log.debug("Token nhận được: {}", token);

        String accountNumber = jwtUtil.validateToken(token);
        if (accountNumber == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("Token không hợp lệ hoặc hết hạn", null));
        }

        return ResponseEntity.ok(transactionService.getTransactionHistory(accountNumber));
    }
}
