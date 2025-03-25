package com.atm.service;

import com.atm.model.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionService {
    private List<Transaction> transactions = new ArrayList<>();

    // Rút tiền
    public void withdraw(String accountNumber, double amount) {
        if (amount > 0) {
            Transaction transaction = new Transaction(accountNumber, amount, "withdrawal");
            transactions.add(transaction);
            System.out.println("Withdrawal successful: " + transaction);
        } else {
            System.out.println("Invalid withdrawal amount.");
        }
    }

    // Gửi tiền
    public void deposit(String accountNumber, double amount) {
        if (amount > 0) {
            Transaction transaction = new Transaction(accountNumber, amount, "deposit");
            transactions.add(transaction);
            System.out.println("Deposit successful: " + transaction);
        } else {
            System.out.println("Invalid deposit amount.");
        }
    }

    // Lấy lịch sử giao dịch theo số tài khoản
    public List<Transaction> getTransactionHistory(String accountNumber) {
        return transactions.stream()
                .filter(t -> t.getAccountNumber().equals(accountNumber))
                .collect(Collectors.toList());
    }
}
