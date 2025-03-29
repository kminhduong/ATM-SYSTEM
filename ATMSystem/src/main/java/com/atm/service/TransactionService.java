package com.atm.service;

import com.atm.dto.ApiResponse;
import com.atm.dto.WithdrawOtpRequest;
import com.atm.model.Credential;
import com.atm.model.Transaction;
import com.atm.model.Account;
import com.atm.repository.AccountRepository;
import com.atm.repository.TransactionRepository;
import jakarta.transaction.Transactional;
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
    private final CredentialService credentialService;


    @Autowired
    public TransactionService(AccountService accountService,
                              AccountRepository accountRepository,
                              TransactionRepository transactionRepository,
                              JwtUtil jwtUtil,
                              PasswordEncoder passwordEncoder,CredentialService credentialService ) {  // Inject passwordEncoder vào constructor
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;  // Gán giá trị cho passwordEncoder
        this.credentialService=credentialService;
    }

    // 📌 Đăng nhập và trả về token JWT
    public String login(String accountNumber, String pin) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);

        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            Credential credential = account.getCredential();

            if (credential != null && credentialService.validatePIN(pin, credential.getPin())) {  // So sánh pin với Credential
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

    @Transactional
    public ApiResponse<String> recordTransaction(String token, double amount, TransactionType transactionType, String targetAccountNumber) {
        // Xác minh token và kiểm tra quyền
        String accountNumber = jwtUtil.validateToken(token);
        if (accountNumber == null) {
            return new ApiResponse<>("Token không hợp lệ hoặc hết hạn", null);
        }

        String role = jwtUtil.getRoleFromToken(token);
        if (!"USER".equals(role)) {
            return new ApiResponse<>("Bạn không có quyền thực hiện giao dịch này", null);
        }

        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty()) {
            return new ApiResponse<>("Không tìm thấy tài khoản", null);
        }

        Account account = accountOpt.get();

        synchronized (account) {
            switch (transactionType) {
                case WITHDRAWAL:
                    return handleWithdraw(account, amount);
                case DEPOSIT:
                    return handleDeposit(account, amount);
                case TRANSFER:
                    return handleTransfer(account, targetAccountNumber, amount);
                default:
                    return new ApiResponse<>("Loại giao dịch không hợp lệ", null);
            }
        }
    }

    private ApiResponse<String> handleWithdraw(Account account, double amount) {
        if (amount > account.getBalance()) {
            return new ApiResponse<>("Số dư không đủ để thực hiện giao dịch", null);
        }

        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);

        // Lưu giao dịch
        Transaction transaction = new Transaction(
                account.getAccountNumber(),
                amount,
                TransactionType.WITHDRAWAL,
                new Date()
        );
        try {
            transactionRepository.save(transaction);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>("Không thể lưu giao dịch vào cơ sở dữ liệu", null);
        }

        return new ApiResponse<>("Rút tiền thành công", String.valueOf(account.getBalance()));
    }

    private ApiResponse<String> handleDeposit(Account account, double amount) {
        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);

        // Lưu giao dịch
        Transaction transaction = new Transaction(
                account.getAccountNumber(),
                amount,
                TransactionType.DEPOSIT,
                new Date()
        );
        try {
            transactionRepository.save(transaction);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>("Không thể lưu giao dịch vào cơ sở dữ liệu", null);
        }

        return new ApiResponse<>("Nạp tiền thành công", String.valueOf(account.getBalance()));
    }

    private ApiResponse<String> handleTransfer(Account sourceAccount, String targetAccountNumber, double amount) {
        if (amount > sourceAccount.getBalance()) {
            return new ApiResponse<>("Số dư không đủ để thực hiện giao dịch chuyển tiền", null);
        }

        Optional<Account> targetAccountOpt = accountRepository.findByAccountNumber(targetAccountNumber);
        if (targetAccountOpt.isEmpty()) {
            return new ApiResponse<>("Tài khoản nhận không hợp lệ", null);
        }

        Account targetAccount = targetAccountOpt.get();

        // Trừ tiền tài khoản nguồn
        sourceAccount.setBalance(sourceAccount.getBalance() - amount);
        accountRepository.save(sourceAccount);

        // Cộng tiền tài khoản đích
        targetAccount.setBalance(targetAccount.getBalance() + amount);
        accountRepository.save(targetAccount);

        // Lưu giao dịch tài khoản nguồn
        Transaction transactionSource = new Transaction(
                sourceAccount.getAccountNumber(),
                amount,
                TransactionType.TRANSFER,
                new Date()
        );

        // Lưu giao dịch tài khoản đích
        Transaction transactionTarget = new Transaction(
                targetAccount.getAccountNumber(),
                amount,
                TransactionType.DEPOSIT,
                new Date()
        );

        try {
            transactionRepository.save(transactionSource);
            transactionRepository.save(transactionTarget);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>("Không thể lưu giao dịch vào cơ sở dữ liệu", null);
        }

        return new ApiResponse<>("Chuyển tiền thành công", String.valueOf(sourceAccount.getBalance()));
    }

    public ApiResponse<String> processWithdrawWithOtp(WithdrawOtpRequest request) {
        // 1. Kiểm tra thông tin đầu vào
        if (request.getPhoneNumber() == null || request.getOtp() == null || request.getAccountNumber() == null) {
            return new ApiResponse<>("Số điện thoại, OTP và số tài khoản là bắt buộc.", null);
        }

        // 2. Xác thực OTP (sử dụng mã cố định)
        if (!"123456".equals(request.getOtp())) {
            return new ApiResponse<>("OTP không hợp lệ. Vui lòng thử lại.", null);
        }

        // 3. Lấy tài khoản từ cơ sở dữ liệu
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(request.getAccountNumber());
        if (accountOpt.isEmpty()) {
            return new ApiResponse<>("Không tìm thấy tài khoản với số tài khoản đã cung cấp.", null);
        }

        Account account = accountOpt.get();

        // 4. Kiểm tra số dư tài khoản
        if (request.getAmount() > account.getBalance()) {
            return new ApiResponse<>("Số dư không đủ để thực hiện giao dịch.", null);
        }

        // 5. Trừ tiền và cập nhật tài khoản
        synchronized (account) {
            account.setBalance(account.getBalance() - request.getAmount());
            account.setLastUpdated(LocalDateTime.now());
            accountRepository.save(account);
        }

        // 6. Lưu thông tin giao dịch vào cơ sở dữ liệu
        Transaction transaction = new Transaction(
                request.getAccountNumber(),
                request.getAmount(),
                TransactionType.WITHDRAWAL_OTP, // Truyền trực tiếp giá trị enum
                new Date()
        );
        transactionRepository.save(transaction);

        // 7. Trả kết quả giao dịch thành công
        return new ApiResponse<>("Giao dịch rút tiền thành công.", String.valueOf(account.getBalance()));
    }

    public ApiResponse<List<Transaction>> getTransactionHistory(String accountNumber) {
        // 1. Kiểm tra tham số đầu vào
        if (accountNumber == null || accountNumber.isEmpty()) {
            return new ApiResponse<>("Số tài khoản không được để trống", null);
        }

        // 2. Lấy lịch sử giao dịch từ cơ sở dữ liệu
        List<Transaction> transactions;
        try {
            transactions = transactionRepository.findByAccountNumber(accountNumber);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>("Lỗi khi truy xuất lịch sử giao dịch", null);
        }

        // 3. Kiểm tra nếu không có giao dịch nào
        if (transactions.isEmpty()) {
            return new ApiResponse<>("Không tìm thấy lịch sử giao dịch nào cho tài khoản này", transactions);
        }

        // 4. Trả kết quả
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