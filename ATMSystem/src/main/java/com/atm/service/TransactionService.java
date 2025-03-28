package com.atm.service;

import com.atm.dto.ApiResponse;
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

                // Tạo JWT token
                long expirationTime = 3600000; // 1 giờ
                String token = jwtUtil.generateToken(accountNumber, account.getRole(), expirationTime);

                System.out.println("Generated Token: " + token);

                return token;
            }
        }

        return null; // Trả về null nếu tài khoản không hợp lệ
    }

    // Phương thức kiểm tra pin (sử dụng mã hóa)
    private boolean verifyPin(String rawPin, String encodedPin) {
        return passwordEncoder.matches(rawPin, encodedPin);  // So sánh pin nhập vào với pin đã mã hóa
    }

    public ApiResponse<String> withdraw(String token, double amount, TransactionType transactionType) {
        // Xác minh token và lấy số tài khoản từ token
        String accountNumber = jwtUtil.validateToken(token);
        if (accountNumber == null) {
            // Token không hợp lệ hoặc hết hạn
            return new ApiResponse<>("Token không hợp lệ hoặc hết hạn", null);
        }

        // Kiểm tra quyền của người dùng từ token
        String role = jwtUtil.getRoleFromToken(token);
        if (!"USER".equals(role)) {
            return new ApiResponse<>("Bạn không có quyền thực hiện giao dịch này", null);
        }

        // Lấy tài khoản từ accountService (hoặc từ DB)
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty()) {
            return new ApiResponse<>("Không tìm thấy tài khoản", null);
        }

        Account account = accountOpt.get();

        // Kiểm tra số dư có đủ để rút tiền không
        if (amount > account.getBalance()) {
            return new ApiResponse<>("Số dư không đủ để thực hiện giao dịch", null);
        }

        // Trừ tiền và cập nhật tài khoản
        synchronized (account) {
            account.setBalance(account.getBalance() - amount);
            account.setLastUpdated(LocalDateTime.now());
            accountRepository.save(account);
        }

        // Lưu giao dịch
        Transaction transaction = new Transaction(accountNumber, amount, transactionType, new Date());
        transactionRepository.save(transaction);

        // Giao dịch thành công, trả về thông báo và số dư dưới dạng String
        return new ApiResponse<>("Giao dịch rút tiền thành công", String.valueOf(account.getBalance()));
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
    public ApiResponse<List<Transaction>> getTransactionHistory(String accountNumber) {
        List<Transaction> transactions = transactionRepository.findByAccountNumber(accountNumber);
        return new ApiResponse<>("Lịch sử giao dịch", transactions);
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