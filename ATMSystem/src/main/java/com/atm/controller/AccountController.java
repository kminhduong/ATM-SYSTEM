package com.atm.controller;

import com.atm.model.Account;
import com.atm.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "*") // Nếu cần kết nối với frontend
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/register")
    public ResponseEntity<Account> register(@Valid @RequestBody Account account) {
        Account createdAccount = accountService.registerAccount(account); // Gọi phương thức trả về Account
        return ResponseEntity.ok(createdAccount); // Trả về đối tượng Account đã được tạo
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Account loginRequest) {
        Account account = accountService.login(loginRequest.getAccountNumber(), loginRequest.getPin());
        if (account != null) {
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateAccountInfo(@Valid @RequestBody Account account) {
        accountService.updateAccountInfo(account.getAccountNumber(), account.getEmail(), account.getPhoneNumber());
        return ResponseEntity.ok("Account updated successfully");
    }

    @GetMapping
    public ResponseEntity<?> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    // Lấy thông tin tài khoản theo số tài khoản
    @GetMapping("/{accountNumber}")
    public ResponseEntity<?> getAccountByNumber(@PathVariable String accountNumber) {
        Optional<Account> account = accountService.getAccountByAccountNumber(accountNumber);
        return account.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<Double> getBalance(@PathVariable String accountNumber) {
        try {
            Double balance = accountService.getBalance(accountNumber);
            return ResponseEntity.ok(balance);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}