package com.atm.controller;

import com.atm.model.Transaction;
import com.atm.service.TransactionService;

import java.util.List;

public class TransactionController {
    private TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void withdraw(String accountNumber, double amount) {
        transactionService.withdraw(accountNumber, amount);
    }
}
