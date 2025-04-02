package com.atm.controller;

import com.atm.service.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/balance")
public class BalanceController {
    private final BalanceService balanceService;

    @Autowired
    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @GetMapping("/balance")
    public ResponseEntity<Double> getBalance() {
        String accountNumber = balanceService.getLoggedInAccountNumber();
        return accountNumber != null ? ResponseEntity.ok(balanceService.getBalance(accountNumber))
                : ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }
}
