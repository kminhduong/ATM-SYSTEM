package com.atm.service;

import com.atm.model.Account;
import com.atm.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account registerAccount(Account account) {
        Optional<Account> existingAccount = accountRepository.findByAccountNumber(account.getAccountNumber());
        if (existingAccount.isPresent()) {
            throw new RuntimeException("Account number already exists"); // Báo lỗi nếu tài khoản đã tồn tại
        }
        return accountRepository.save(account); // Lưu tài khoản mới nếu chưa tồn tại
    }

    public Account login(String accountNumber, String pin) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        return accountOpt.filter(account -> account.getPin().equals(pin)).orElse(null);
    }

    public void updateAccountInfo(String accountNumber, String email, String phoneNumber) {
        Optional<Account> optionalAccount = accountRepository.findByAccountNumber(accountNumber);
        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();
            account.setEmail(email);
            account.setPhoneNumber(phoneNumber);
            accountRepository.save(account);
        }
    }

    public Account getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber).orElse(null);
    }
    public Double getBalance(String accountNumber) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isPresent()) {
            return accountOpt.get().getBalance();
        } else {
            throw new RuntimeException("Account not found");
        }
    }
}