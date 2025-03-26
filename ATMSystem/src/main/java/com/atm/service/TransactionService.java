package com.atm.service;

import com.atm.model.Transaction;
import com.atm.model.Account;
import com.atm.repository.AccountRepository;
import com.atm.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.atm.model.TransactionType;
import com.atm.util.JwtUtil;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public TransactionService(AccountService accountService, AccountRepository accountRepository, TransactionRepository transactionRepository, JwtUtil jwtUtil) {
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.jwtUtil = jwtUtil;
    }

    // 📌 Đăng nhập và trả về token JWT
    public String login(String accountNumber, String pin) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isPresent() && verifyPin(pin, accountOpt.get().getPin())) {
            // Lấy vai trò của tài khoản, giả sử là ADMIN hoặc USER (hoặc bất kỳ giá trị nào bạn sử dụng)
            String role = accountOpt.get().getRole(); // Bạn cần đảm bảo rằng Account có phương thức getRole() hoặc tương tự
            return jwtUtil.generateToken(accountNumber, role); // Truyền cả accountNumber và role vào
        }
        return null;
    }

    // 📌 Kiểm tra mã PIN
    private boolean verifyPin(String inputPin, String actualPin) {
        return inputPin.equals(actualPin);
    }

    // 📌 Rút tiền
    public boolean withdraw(String token, double amount, TransactionType transactionType) {
        String accountNumber = jwtUtil.validateToken(token);
        if (accountNumber == null) return false;

        Account account = accountService.getAccount(accountNumber);
        if (account == null || amount > account.getBalance()) {
            return false;
        }

        // Trừ tiền và cập nhật tài khoản
        account.setBalance(account.getBalance() - amount);
        account.setLastUpdated(LocalDateTime.now());
        accountRepository.save(account);

        // Lưu giao dịch
        Transaction transaction = new Transaction(accountNumber, amount, transactionType, new Date());
        transactionRepository.save(transaction);
        return true;
    }

    // 📌 Rút tiền qua OTP
    public boolean withdrawWithOtp(String accountNumber, String phoneNumber, double amount, TransactionType transactionType) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty()) return false;

        Account account = accountOpt.get();
        if (amount > account.getBalance()) return false;

        // Trừ tiền và cập nhật tài khoản
        account.setBalance(account.getBalance() - amount);
        account.setLastUpdated(LocalDateTime.now());
        accountRepository.save(account);

        // Lưu giao dịch
        Transaction transaction = new Transaction(accountNumber, amount, transactionType, new Date());
        transactionRepository.save(transaction);
        return true;
    }

    // 📌 Xác thực OTP (Giả lập - nên dùng dịch vụ OTP thực tế)
    public boolean validateOtp(String accountNumber, String phoneNumber, String otp) {
        // Thêm logic kiểm tra accountNumber và phoneNumber nếu cần
        return "123456".equals(otp);  // Tạm thời hard-code, nên thay thế bằng giải pháp OTP thực tế
    }

    // 📌 Xem lịch sử giao dịch
    public List<Transaction> getTransactionHistory(String accountNumber) {
        return transactionRepository.findByAccountNumber(accountNumber);
    }
}