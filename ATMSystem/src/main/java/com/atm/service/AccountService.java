package com.atm.service;

import com.atm.model.Account;

import java.util.HashMap;
import java.util.Map;

public class AccountService {
    private Map<String, Account> accounts = new HashMap<>();

    public void registerAccount(Account account) {
        accounts.put(account.getAccountNumber(), account);
    }

    public Account login(String accountNumber, String pin) {
        Account account = accounts.get(accountNumber);
        if (account != null && account.getPin().equals(pin)) {
            return account;
        }
        return null;
    }

    public void updateAccountInfo(String accountNumber, String email, String phoneNumber) {
        Account account = accounts.get(accountNumber);
        if (account != null) {
            account.setEmail(email);
            account.setPhoneNumber(phoneNumber);
        }
    }
    public void updateAddress(String accountNumber, String newAddress) {
        Account account = accounts.get(accountNumber);
        if (account != null) {
            account.setAddress(newAddress);
            System.out.println("Cập nhật địa chỉ thành công.");
        } else {
            System.out.println("Không tìm thấy tài khoản.");
        }
    }
}
