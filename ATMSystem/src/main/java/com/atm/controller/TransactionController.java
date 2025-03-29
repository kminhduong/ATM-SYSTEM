package com.atm.controller;

import com.atm.dto.*;
import com.atm.dto.ApiResponse;
import com.atm.model.Transaction;
import com.atm.model.TransactionType;
import com.atm.repository.AccountRepository;
import com.atm.repository.TransactionRepository;
import com.atm.service.AccountService;
import com.atm.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.atm.util.JwtUtil;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<String>> withdraw(@RequestHeader("Authorization") String authHeader, @RequestBody TransactionRequest request) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("Missing or invalid Authorization header", null));
        }

        String token = authHeader.substring(7);

        if (transactionService.isTokenBlacklisted(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>("Token has been logged out", null));
        }

        ApiResponse<String> response = transactionService.recordTransaction(token, request.getAmount(), TransactionType.WITHDRAWAL, null);
        return buildResponse(response);
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<String>> deposit(@RequestHeader("Authorization") String authHeader, @RequestBody TransactionRequest request) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("Missing or invalid Authorization header", null));
        }

        String token = authHeader.substring(7);

        if (transactionService.isTokenBlacklisted(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>("Token has been logged out", null));
        }

        ApiResponse<String> response = transactionService.recordTransaction(token, request.getAmount(), TransactionType.DEPOSIT, null);
        return buildResponse(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<String>> transfer(@RequestHeader("Authorization") String authHeader, @RequestBody TransferRequest request) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("Missing or invalid Authorization header", null));
        }

        String token = authHeader.substring(7);

        if (transactionService.isTokenBlacklisted(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>("Token has been logged out", null));
        }

        ApiResponse<String> response = transactionService.recordTransaction(token, request.getAmount(), TransactionType.TRANSFER, request.getTargetAccountNumber());
        return buildResponse(response);
    }

    private ResponseEntity<ApiResponse<String>> buildResponse(ApiResponse<String> response) {
        if (response != null && response.getMessage().contains("thành công")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // API Rút tiền qua OTP
    @PostMapping("/withdraw/otp")
    public ResponseEntity<ApiResponse<String>> withdrawWithOtp(@RequestBody WithdrawOtpRequest request) {
        // Gọi service để xử lý toàn bộ logic nghiệp vụ
        ApiResponse<String> response = transactionService.processWithdrawWithOtp(request);

        // Trả kết quả về cho client
        if ("Giao dịch rút tiền thành công".equals(response.getMessage())) {
            return ResponseEntity.ok(response); // Thành công
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // Lỗi
        }
    }

    // API Lấy lịch sử giao dịch
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<Transaction>>> getTransactionHistory(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("Missing or invalid Authorization header", null));
        }

        String token = authHeader.substring(7);
        String accountNumber = jwtUtil.validateToken(token);

        if (accountNumber == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>("Token không hợp lệ hoặc hết hạn", null));
        }

        ApiResponse<List<Transaction>> response = transactionService.getTransactionHistory(accountNumber);
        return ResponseEntity.ok(response);
    }
}
