package com.atm.controller;

import com.atm.model.Transaction;
import com.atm.service.TransactionService;

import java.util.List;

public class TransactionController {
    private TransactionService transactionService = new TransactionService();

    public void withdraw(String accountNumber, double amount) {
        transactionService.withdraw(accountNumber, amount);
    }

    public List<Transaction> getTransactionHistory(String accountNumber) {
        return transactionService.getTransactionHistory(accountNumber);
    }
}
