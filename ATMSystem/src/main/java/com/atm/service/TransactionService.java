package com.atm.service;

import com.atm.model.Transaction;
import com.atm.model.Account;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

public class TransactionService {
    private AccountService accountService;
    private List<Transaction> transactions = new ArrayList<>();

    // Constructor đúng
    public TransactionService(AccountService accountService) {
        this.accountService = accountService; // Gán giá trị tránh null
    }
    // Tạo ID giao dịch duy nhất
    private String generateTransactionId() {
        return UUID.randomUUID().toString(); // Sinh ID ngẫu nhiên
    }

    // Rút tiền
    public void withdraw(String accountNumber, double amount) {
        Account account = accountService.getAccount(accountNumber);
        if (account == null) {
            System.out.println("Account does not exist.");
            return;
        }

        if (amount > account.getBalance()) {
            System.out.println("Insufficient balance.");
            return;
        }

        account.setBalance(account.getBalance() - amount);
        transactions.add(new Transaction(accountNumber, amount, "withdrawal"));
        System.out.println("Withdrawal successful! New balance: $" + account.getBalance());
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
