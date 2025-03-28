package com.atm.controller;

import com.atm.model.Account;
import com.atm.model.Transaction;
import com.atm.model.TransactionType;
import com.atm.service.TransactionService;
import com.atm.dto.WithdrawRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.atm.util.JwtUtil;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private com.atm.util.JwtUtil jwtUtil;

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

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestHeader("Authorization") String authHeader, @RequestBody WithdrawRequest request) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // Kiểm tra nếu token đã bị vô hiệu hóa
            if (transactionService.isTokenBlacklisted(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token has been logged out.");
            }

            // Lấy thông tin tài khoản từ token
            String accountNumber = jwtUtil.validateToken(token);
            if (accountNumber == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid or expired token.");
            }

            // Kiểm tra và thực hiện rút tiền
            boolean success = transactionService.withdraw(token, request.getAmount(), TransactionType.WITHDRAWAL); // Sử dụng WITHDRAWAL
            if (success) {
                return ResponseEntity.ok("Withdraw successful.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient funds or transaction failed.");
            }
        }

        return ResponseEntity.badRequest().body("Missing Authorization header.");
    }

    // API Rút tiền qua OTP
    @PostMapping("/withdraw/otp")
    public ResponseEntity<String> withdrawWithOtp(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> payload) {
        String accountNumber = jwtUtil.validateToken(token);
        String phoneNumber = (String) payload.get("phoneNumber");
        double amount = ((Number) payload.get("amount")).doubleValue();
        String otp = (String) payload.get("otp");

        if (accountNumber == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token.");
        }

        if (phoneNumber == null || otp == null) {
            return ResponseEntity.badRequest().body("Phone number and OTP are required.");
        }

        boolean otpValid = transactionService.validateOtp(accountNumber, phoneNumber, otp);
        if (!otpValid) {
            return ResponseEntity.badRequest().body("Invalid OTP.");
        }

        boolean success = transactionService.withdrawWithOtp(accountNumber, phoneNumber, amount, TransactionType.WITHDRAWAL_OTP);
        if (success) {
            return ResponseEntity.ok("Withdrawal successful with OTP.");
        } else {
            return ResponseEntity.badRequest().body("Insufficient balance or ATM funds.");
        }
    }

    // API Lấy lịch sử giao dịch
    @GetMapping("/{accountNumber}")
    public ResponseEntity<List<Transaction>> getTransactionHistory(@RequestHeader("Authorization") String token, @PathVariable String accountNumber) {
        String authenticatedAccountNumber = jwtUtil.validateToken(token);

        if (authenticatedAccountNumber == null || !authenticatedAccountNumber.equals(accountNumber)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        List<Transaction> transactions = transactionService.getTransactionHistory(accountNumber);
        return ResponseEntity.ok(transactions);
    }
}
