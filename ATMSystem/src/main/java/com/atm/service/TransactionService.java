package com.atm.service;

import com.atm.model.Credential;
import com.atm.model.Transaction;
import com.atm.model.Account;
import com.atm.repository.AccountRepository;
import com.atm.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.atm.model.TransactionType;
import com.atm.util.JwtUtil;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public TransactionService(AccountService accountService,
                              AccountRepository accountRepository,
                              TransactionRepository transactionRepository,
                              JwtUtil jwtUtil,
                              PasswordEncoder passwordEncoder) {  // Inject passwordEncoder vào constructor
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;  // Gán giá trị cho passwordEncoder
    }

    // 📌 Đăng nhập và trả về token JWT
    public String login(String accountNumber, String pin) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);

        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            Credential credential = account.getCredential();

            if (credential != null && verifyPin(pin, credential.getPin())) {  // So sánh pin với Credential
                if (account.getRole() == null) {
                    account.setRole("USER");
                    accountRepository.save(account);
                }

                long expirationTime = 3600000; // 1 giờ
                String token = jwtUtil.generateToken(accountNumber, account.getRole(), expirationTime);

                System.out.println("Generated Token: " + token);

                return token;
            }
        }

        return null;
    }

    // Phương thức kiểm tra pin (sử dụng mã hóa)
    private boolean verifyPin(String rawPin, String encodedPin) {
        return passwordEncoder.matches(rawPin, encodedPin);  // So sánh pin nhập vào với pin đã mã hóa
    }

    // 📌 Rút tiền
    public boolean withdraw(String token, double amount, TransactionType transactionType) {
        String accountNumber = jwtUtil.validateToken(token);
        System.out.println("DEBUG: Account extracted from token -> " + accountNumber);
        if (accountNumber == null) {
            System.out.println("ERROR: Token không hợp lệ hoặc hết hạn");
            return false;
        }

        Account account = accountService.getAccount(accountNumber);
        if (account == null || amount > account.getBalance()) {
            System.out.println("ERROR: Không tìm thấy tài khoản hoặc số dư không đủ");
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

    public void logout(String token) {
        if (token != null) {
            blacklistedTokens.add(token);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        return token != null && blacklistedTokens.contains(token);
    }
}