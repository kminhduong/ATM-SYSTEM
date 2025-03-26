package com.atm.controller;

import com.atm.model.Account;
import com.atm.model.Transaction;
import com.atm.model.TransactionType;
import com.atm.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // ThreadLocal để đảm bảo mỗi luồng có một currentUser riêng
    private static final ThreadLocal<Account> currentUser = new ThreadLocal<>();

    // Lấy user đang đăng nhập trong luồng hiện tại
    private Account getCurrentUser() {
        Account user = currentUser.get();
        if (user == null) {
            throw new RuntimeException("User not logged in");
        }
        return user;
    }

    // Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Account loginRequest) {
        String token = transactionService.login(loginRequest.getAccountNumber(), loginRequest.getPin());
        if (token != null) {
            return ResponseEntity.ok("Login successful. Token: " + token);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid credentials: Please check your account number and PIN.");
        }
    }

    // Đăng xuất
    @GetMapping("/logout")
    public ResponseEntity<String> logout() {
        currentUser.remove();
        return ResponseEntity.ok("Logout successful");
    }

    // Rút tiền
    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> payload) {
        double amount = ((Number) payload.get("amount")).doubleValue();
        boolean success = transactionService.withdraw(token, amount, TransactionType.WITHDRAWAL);
        if (success) {
            return ResponseEntity.ok("Withdrawal successful");
        } else {
            return ResponseEntity.badRequest().body("Insufficient balance or invalid token.");
        }
    }

    // Rút tiền qua OTP
    @PostMapping("/withdraw/otp")
    public ResponseEntity<String> withdrawWithOtp(@RequestBody Map<String, Object> payload) {
        try {
            Account user = getCurrentUser();
            String accountNumber = user.getAccountNumber();
            String phoneNumber = (String) payload.get("phoneNumber");
            double amount = ((Number) payload.get("amount")).doubleValue();
            String otp = (String) payload.get("otp");

            boolean otpValid = transactionService.validateOtp(accountNumber, phoneNumber, otp);
            if (!otpValid) {
                return ResponseEntity.badRequest().body("Invalid OTP.");
            }

            boolean success = transactionService.withdrawWithOtp(accountNumber, phoneNumber, amount, TransactionType.WITHDRAWAL_OTP);
            if (success) {
                return ResponseEntity.ok("Withdrawal successful with OTP");
            } else {
                return ResponseEntity.badRequest().body("Insufficient balance or invalid account/phone number.");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // Lấy lịch sử giao dịch
    @GetMapping("/{accountNumber}")
    public ResponseEntity<List<Transaction>> getTransactionHistory(@PathVariable String accountNumber) {
        try {
            getCurrentUser();  // Kiểm tra đăng nhập
            List<Transaction> transactions = transactionService.getTransactionHistory(accountNumber);
            return ResponseEntity.ok(transactions);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(List.of());
        }
    }
}
