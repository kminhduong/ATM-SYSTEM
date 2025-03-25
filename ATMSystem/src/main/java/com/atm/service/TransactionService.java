package com.atm.service;

import com.atm.model.Transaction;
import com.atm.model.Account;
import com.atm.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.atm.model.TransactionType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final AccountService accountService;
    private final AccountRepository accountRepository;  // Thêm repository để lưu cập nhật
    private List<Transaction> transactions = new ArrayList<>();

    @Autowired
    public TransactionService(AccountService accountService, AccountRepository accountRepository) {
        this.accountService = accountService;
        this.accountRepository = accountRepository;
    }

    public boolean validateOtp(String accountNumber, String phoneNumber, String otp) {
        return "123456".equals(otp);  // Giả sử OTP đúng là "123456"
    }

    public boolean withdraw(String accountNumber, String pin, double amount) {
        Account account = accountService.getAccount(accountNumber);
        if (account == null || !account.getPin().equals(pin) || amount > account.getBalance()) {
            return false;
        }

        // Thực hiện giao dịch rút tiền
        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);  // Lưu thay đổi vào database
        transactions.add(new Transaction(accountNumber, amount, TransactionType.WITHDRAWAL));
        return true;
    }

    public boolean withdrawWithOtp(String accountNumber, String phoneNumber, double amount) {
        Account account = accountService.getAccount(accountNumber);
        if (account == null || amount > account.getBalance()) {
            return false;
        }

        // Thực hiện giao dịch rút tiền
        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);  // Lưu thay đổi vào database
        transactions.add(new Transaction(accountNumber, amount, TransactionType.WITHDRAWAL));
        return true;
    }

    public List<Transaction> getTransactionHistory(String accountNumber) {
        return transactions.stream()
                .filter(t -> t.getAccountNumber().equals(accountNumber))
                .collect(Collectors.toList());
    }
}