package com.atm.service;

import com.atm.model.Transaction;
import com.atm.model.Account;
import com.atm.repository.AccountRepository;
import com.atm.repository.TransactionRepository;  // Import TransactionRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.atm.model.TransactionType;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;  // Thêm field cho TransactionRepository

    // Constructor tiêm tất cả các dependency vào service
    @Autowired
    public TransactionService(AccountService accountService, AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;  // Tiêm dependency TransactionRepository
    }

    public boolean validateOtp(String accountNumber, String phoneNumber, String otp) {
        return "123456".equals(otp);  // Giả sử OTP đúng là "123456"
    }

    public boolean withdraw(String accountNumber, String pin, double amount, TransactionType transactionType) {
        Account account = accountService.getAccount(accountNumber);
        if (account == null || !account.getPin().equals(pin) || amount > account.getBalance()) {
            return false;
        }

        // Thực hiện giao dịch rút tiền
        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);  // Lưu thay đổi vào database

        // Lưu giao dịch vào database
        Transaction transaction = new Transaction(accountNumber, amount, transactionType);
        transactionRepository.save(transaction);  // Lưu giao dịch vào bảng transactions
        return true;
    }

    public boolean withdrawWithOtp(String accountNumber, String phoneNumber, double amount, TransactionType transactionType) {
        Account account = accountService.getAccount(accountNumber);
        if (account == null || amount > account.getBalance()) {
            return false;
        }

        // Thực hiện giao dịch rút tiền
        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);  // Lưu thay đổi vào database

        // Lưu giao dịch vào database
        Transaction transaction = new Transaction(accountNumber, amount, transactionType);
        transactionRepository.save(transaction);  // Lưu giao dịch vào bảng transactions
        return true;
    }

    public List<Transaction> getTransactionHistory(String accountNumber) {
        List<Transaction> transactions = transactionRepository.findByAccountNumber(accountNumber);
        if (transactions.isEmpty()) {
            System.out.println("Không có giao dịch cho tài khoản này.");
        } else {
            System.out.println("Lịch sử giao dịch: " + transactions);
        }
        return transactions;
    }
}
