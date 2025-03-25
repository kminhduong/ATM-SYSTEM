package com.atm.service;

import com.atm.model.Transaction;
import com.atm.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final AccountService accountService;
    private List<Transaction> transactions = new ArrayList<>();

    @Autowired
    public TransactionService(AccountService accountService) {
        this.accountService = accountService;
    }

    // Phương thức xác thực OTP
    public boolean validateOtp(String accountNumber, String phoneNumber, String otp) {
        // Đây chỉ là một ví dụ về cách kiểm tra OTP.
        // Cần thay đổi với logic xác thực OTP thực tế (ví dụ như kiểm tra OTP trong cơ sở dữ liệu hoặc qua một dịch vụ OTP).
        return "123456".equals(otp);  // Giả sử OTP đúng là "123456"
    }

    public boolean withdraw(String accountNumber, String pin, double amount) {
        // Lấy tài khoản từ AccountService
        Account account = accountService.getAccount(accountNumber);
        if (account == null) {
            return false;  // Account does not exist.
        }

        // Kiểm tra mã PIN hợp lệ
        if (!account.getPin().equals(pin)) {
            return false;  // Invalid PIN.
        }

        // Kiểm tra số dư tài khoản
        if (amount > account.getBalance()) {
            return false;  // Insufficient balance.
        }

        // Thực hiện giao dịch rút tiền
        account.setBalance(account.getBalance() - amount);
        transactions.add(new Transaction(accountNumber, amount, "withdrawal"));
        return true;  // Giao dịch thành công
    }

    public boolean withdrawWithOtp(String accountNumber, String phoneNumber, double amount) {
        Account account = accountService.getAccount(accountNumber);

        if (account == null) {
            return false;  // Account does not exist.
        }

        if (amount > account.getBalance()) {
            return false;  // Insufficient balance.
        }

        // Thực hiện rút tiền sau khi OTP hợp lệ
        account.setBalance(account.getBalance() - amount);
        transactions.add(new Transaction(accountNumber, amount, "withdrawal (OTP)"));
        return true;
    }

    // Lấy lịch sử giao dịch theo số tài khoản
    public List<Transaction> getTransactionHistory(String accountNumber) {
        return transactions.stream()
                .filter(t -> t.getAccountNumber().equals(accountNumber))
                .collect(Collectors.toList());
    }
}