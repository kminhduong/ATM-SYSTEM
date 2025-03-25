package com.atm.controller;

import com.atm.model.Account;
import com.atm.service.AccountService;

public class AccountController {
    private AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }
    public void register(Account account) {
        accountService.registerAccount(account);
    }

    public Account login(String accountNumber, String pin) {
        return accountService.login(accountNumber, pin);
    }

    public void updateAccountInfo(String accountNumber, String email, String phoneNumber) {
        accountService.updateAccountInfo(accountNumber, email, phoneNumber);
    }
}
