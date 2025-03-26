package com.atm.controller;

import com.atm.model.Transaction;
import com.atm.model.TransactionType; // Thêm import này
import com.atm.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*") // Cho phép truy cập từ mọi nguồn
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestBody Map<String, Object> payload) {
        String accountNumber = (String) payload.get("accountNumber");
        String pin = (String) payload.get("pin");
        double amount = ((Number) payload.get("amount")).doubleValue();

        if (amount <= 0) {
            return ResponseEntity.badRequest().body("Invalid withdrawal amount.");
        }

        // Gọi phương thức rút tiền thông thường với loại giao dịch là WITHDRAWAL
        boolean success = transactionService.withdraw(accountNumber, pin, amount, TransactionType.WITHDRAWAL);
        if (success) {
            return ResponseEntity.ok("Withdrawal successful");
        } else {
            return ResponseEntity.badRequest().body("Insufficient balance or invalid PIN.");
        }
    }

    @PostMapping("/withdraw/otp")
    public ResponseEntity<String> withdrawWithOtp(@RequestBody Map<String, Object> payload) {
        String accountNumber = (String) payload.get("accountNumber");
        String phoneNumber = (String) payload.get("phoneNumber");
        double amount = ((Number) payload.get("amount")).doubleValue();
        String otp = (String) payload.get("otp");

        if (amount <= 0) {
            return ResponseEntity.badRequest().body("Invalid withdrawal amount.");
        }

        // Kiểm tra OTP hợp lệ trước khi rút tiền
        boolean otpValid = transactionService.validateOtp(accountNumber, phoneNumber, otp);
        if (!otpValid) {
            return ResponseEntity.badRequest().body("Invalid OTP.");
        }

        // Gọi phương thức rút tiền với OTP, loại giao dịch là WITHDRAWAL_OTP
        boolean success = transactionService.withdrawWithOtp(accountNumber, phoneNumber, amount, TransactionType.WITHDRAWAL_OTP);
        if (success) {
            return ResponseEntity.ok("Withdrawal successful with OTP");
        } else {
            return ResponseEntity.badRequest().body("Insufficient balance or invalid account/phone number.");
        }
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<List<Transaction>> getTransactionHistory(@PathVariable String accountNumber) {
        List<Transaction> transactions = transactionService.getTransactionHistory(accountNumber);
        return ResponseEntity.ok(transactions);
    }
}
