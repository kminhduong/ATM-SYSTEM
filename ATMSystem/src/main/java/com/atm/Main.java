package com.atm;

import com.atm.controller.AccountController;
import com.atm.controller.TransactionController;
import com.atm.model.Account;
import com.atm.service.AccountService;
import com.atm.service.TransactionService;

public class Main {
    public static void main(String[] args) {

//        System.out.println("Hello, World!");

// Tạo AccountService
        AccountService accountService = new AccountService();

// Tạo TransactionService và truyền AccountService vào
        TransactionService transactionService = new TransactionService(accountService);

// Tạo AccountController
        AccountController accountController = new AccountController(accountService);

// Tạo TransactionController
        TransactionController transactionController = new TransactionController(transactionService);

        // Đăng ký tài khoản mới
        Account newAccount = new Account();
        newAccount.setAccountNumber("123456789");
        newAccount.setPin("1234");
        newAccount.setBalance(1000.0);
        newAccount.setEmail("user@example.com");
        newAccount.setPhoneNumber("0123456789");
        accountController.register(newAccount);

        // Đăng nhập
        Account loggedInAccount = accountController.login("123456789", "1234");
        if (loggedInAccount != null) {
            System.out.println("Login successful!");

            // Kiểm tra số dư
            System.out.println("Account balance: " + loggedInAccount.getBalance());

            // Rút tiền
            transactionController.withdraw(loggedInAccount.getAccountNumber(), 200.0);
            System.out.println("Withdrawal successful!");

            // Kiểm tra số dư sau khi rút
            System.out.println("Account balance after withdrawal: " + loggedInAccount.getBalance());

            // Xem lịch sử giao dịch
            // List<Transaction> history = transactionController.getTransactionHistory(loggedInAccount.getAccountNumber());
            // history.forEach(transaction -> System.out.println(transaction));
        } else {
            System.out.println("Login failed!");
        }
    }
}