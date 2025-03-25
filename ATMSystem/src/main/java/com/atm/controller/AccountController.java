package com.atm.controller;

import com.atm.model.Account;
import com.atm.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    // Đăng ký tài khoản
    @PostMapping("/register")
    public ResponseEntity<Account> register(@RequestBody Account account) {
        accountService.registerAccount(account);
        return ResponseEntity.ok(account);
    }

    // Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String accountNumber, @RequestParam String pin) {
        Optional<Account> account = Optional.ofNullable(accountService.login(accountNumber, pin));
        return account.isPresent() ? ResponseEntity.ok("Login successful") : ResponseEntity.status(401).body("Invalid credentials");
    }

    // Cập nhật thông tin tài khoản
    @PutMapping("/update")
    public ResponseEntity<String> updateAccountInfo(
            @RequestParam String accountNumber,
            @RequestParam String email,
            @RequestParam String phoneNumber) {
        accountService.updateAccountInfo(accountNumber, email, phoneNumber);
        return ResponseEntity.ok("Account updated successfully");
    }
}
