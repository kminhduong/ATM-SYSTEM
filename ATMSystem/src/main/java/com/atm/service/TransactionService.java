package com.atm.service;

import com.atm.dto.AccountDTO;
import com.atm.dto.ApiResponse;
import com.atm.dto.WithdrawOtpRequest;
import com.atm.model.*;
import com.atm.repository.AccountRepository;
import com.atm.repository.TransactionRepository;
import com.atm.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import com.atm.util.JwtUtil;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final JwtUtil jwtUtil;
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();
    private final CredentialService credentialService;
    private final BalanceService balanceService;
    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);



    @Autowired
    public TransactionService(AccountRepository accountRepository,
                              TransactionRepository transactionRepository,
                              JwtUtil jwtUtil,
                              CredentialService credentialService,
                              BalanceService balanceService,
                              UserRepository userRepository) {  // Inject passwordEncoder vào constructor
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.jwtUtil = jwtUtil;
        this.credentialService=credentialService;
        this.balanceService=balanceService;
        this.userRepository = userRepository;
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
        // Xác minh token và quyền
        String accountNumber = jwtUtil.validateToken(token);
        if (accountNumber == null) {
            return new ApiResponse<>("Token is invalid or expired", null);
        }

        String role = jwtUtil.getRoleFromToken(token);
        if (!"USER".equals(role)) {
            return new ApiResponse<>("You do not have permission to perform this transaction.", null);
        }

        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty()) {
            return new ApiResponse<>("Account not found", null);
        }

        Account account = accountOpt.get();

        // Thực hiện giao dịch
        return switch (transactionType) {
            case WITHDRAWAL -> handleWithdraw(account, amount);
            case DEPOSIT -> handleDeposit(account, amount);
            case TRANSFER -> handleTransfer(account, targetAccountNumber, amount);
            default -> new ApiResponse<>("Invalid transaction type", null);
        };
    }

    private ApiResponse<String> handleWithdraw(Account account, double amount) {
        if (amount > account.getBalance()) {
            return new ApiResponse<>("Insufficient balance to make transaction", null);
        }

        // Tạo DTO để cập nhật số dư
        AccountDTO withdrawalDTO = new AccountDTO();
        withdrawalDTO.setBalance(amount);
        balanceService.updateBalance(withdrawalDTO, account, TransactionType.WITHDRAWAL);

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
            // Thay thế printStackTrace bằng ghi log
            logger.error("Unable to save transaction to database. Error details: ", e);
            return new ApiResponse<>("Unable to save transaction to database", null);
        }

        return new ApiResponse<>("Withdraw successfully", String.valueOf(account.getBalance()));
    }

    public ApiResponse<String> handleDeposit(Account account, double amount) {
        // Tạo DTO để cập nhật số dư
        AccountDTO depositDTO = new AccountDTO();
        depositDTO.setBalance(amount);
        balanceService.updateBalance(depositDTO, account, TransactionType.DEPOSIT);

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
            // Thay printStackTrace bằng logger.error
            logger.error("Unable to save transaction to database. Error details: ", e);
            return new ApiResponse<>("Unable to save transaction to database", null);
        }

        return new ApiResponse<>("Deposit successfully", String.valueOf(account.getBalance()));
    }

    private ApiResponse<String> handleTransfer(Account sourceAccount, String targetAccountNumber, double amount) {
        if (amount > sourceAccount.getBalance()) {
            return new ApiResponse<>("Insufficient balance to make money transfer", null);
        }

        Optional<Account> targetAccountOpt = accountRepository.findByAccountNumber(targetAccountNumber);
        if (targetAccountOpt.isEmpty()) {
            return new ApiResponse<>("Invalid receiving account", null);
        }

        Account targetAccount = targetAccountOpt.get();

        // Tạo DTO để trừ tiền tài khoản nguồn
        AccountDTO transferSourceDTO = new AccountDTO();
        transferSourceDTO.setBalance(amount);
        balanceService.updateBalance(transferSourceDTO, sourceAccount, TransactionType.WITHDRAWAL);

        // Tạo DTO để cộng tiền tài khoản đích
        AccountDTO transferTargetDTO = new AccountDTO();
        transferTargetDTO.setBalance(amount);
        balanceService.updateBalance(transferTargetDTO, targetAccount, TransactionType.DEPOSIT);

        // Lưu giao dịch
        Transaction transactionSource = new Transaction(
                sourceAccount.getAccountNumber(),
                amount,
                TransactionType.TRANSFER,
                new Date()
        );

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
            // Thay thế printStackTrace bằng logger.error
            logger.error("Unable to save transaction to database. Error details: ", e);
            return new ApiResponse<>("Unable to save transaction to database", null);
        }

        return new ApiResponse<>("Transfer successful", String.valueOf(sourceAccount.getBalance()));
    }

    public ApiResponse<String> sendOtpForWithdrawal(String accountNumber){
        // 1. Kiểm tra accountNumber đầu vào
        if (accountNumber == null || accountNumber.isEmpty()) {
            return new ApiResponse<>("Account number is required.", null);
        }

        // 2. Tìm tài khoản từ cơ sở dữ liệu
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty()) {
            return new ApiResponse<>("No account found with the provided account number", null);
        }
        Account account = accountOpt.get();

        // 3. Tìm số điện thoại từ bảng User
        Optional<User> userOpt = userRepository.findByUserId(account.getUser().getUserId()); // Liên kết account với userId
        if (userOpt.isEmpty()) {
            return new ApiResponse<>("No user information found for this account.", null);
        }
        String phoneNumber = userOpt.get().getPhone();

        // 4. Gửi OTP
        String generatedOtp = generateAndSendOtp(phoneNumber);
        return new ApiResponse<>("OTP has been sent to your phone number.", generatedOtp);
    }

    public ApiResponse<String> processWithdrawWithOtp(WithdrawOtpRequest request) {
        // Kiểm tra đầu vào
        if (request.getAccountNumber() == null || request.getAccountNumber().isEmpty()) {
            return new ApiResponse<>("Account number is required.", null);
        }
        if (request.getOtp() == null || request.getOtp().isEmpty()) {
            return new ApiResponse<>("OTP is required.", null);
        }
        if (request.getAmount() == null || request.getAmount() <= 0) {
            return new ApiResponse<>("The amount to be withdrawn must be greater than 0.", null);
        }

        // Xác thực OTP
        if (!"123456".equals(request.getOtp())) {
            return new ApiResponse<>("Invalid OTP. Please try again.", null);
        }

        // Lấy tài khoản từ cơ sở dữ liệu
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(request.getAccountNumber());
        if (accountOpt.isEmpty()) {
            return new ApiResponse<>("No account found with the provided account number.", null);
        }
        Account account = accountOpt.get();

        // Lấy thông tin User để kiểm tra số điện thoại
        Optional<User> userOpt = userRepository.findByUserId(account.getUser().getUserId());
        if (userOpt.isEmpty()) {
            return new ApiResponse<>("No user information found for this account.", null);
        }

        // Kiểm tra số dư tài khoản
        if (request.getAmount() > account.getBalance()) {
            return new ApiResponse<>("Insufficient balance to complete transaction.", null);
        }

        // Thực hiện rút tiền
        synchronized (account) {
            account.setBalance(account.getBalance() - request.getAmount());
            account.setLastUpdated(LocalDateTime.now());
            accountRepository.save(account);
        }

        // Lưu giao dịch
        Transaction transaction = new Transaction(
                request.getAccountNumber(),
                request.getAmount(),
                TransactionType.fromString("WITHDRAWAL_OTP"), // Bảo đảm không phân biệt chữ hoa/thường
                new Date()
        );
        transactionRepository.save(transaction);

        // Trả kết quả
        return new ApiResponse<>("Withdrawal successful.", "Remaining balance: " + account.getBalance());
    }

    private String generateAndSendOtp(String phoneNumber) {
        // Tạo OTP ngẫu nhiên
        String otp = "123456"; // Hoặc sử dụng phương pháp tạo mã OTP thực tế
        logger.info("Sending OTP {} to phone number {}", otp, phoneNumber);
        // Logic gửi OTP tới số điện thoại (API SMS hoặc tích hợp khác)
        return otp;
    }

    public ApiResponse<List<Transaction>> getTransactionHistory(String accountNumber) {
        // Kiểm tra đầu vào
        if (accountNumber == null || accountNumber.isEmpty()) {
            return new ApiResponse<>("Account number cannot be blank", null);
        }

        // Lấy lịch sử giao dịch từ cơ sở dữ liệu
        List<Transaction> transactions;
        try {
            transactions = transactionRepository.findByAccountNumber(accountNumber);
        } catch (DataAccessException e) {
            System.err.println("Lỗi cơ sở dữ liệu: " + e.getMessage());
            return new ApiResponse<>("Lỗi khi truy xuất lịch sử giao dịch từ cơ sở dữ liệu", null);
        } catch (Exception e) {
            System.err.println("Lỗi không xác định: " + e.getMessage());
            return new ApiResponse<>("Lỗi không xác định xảy ra", null);
        }

        // Kiểm tra nếu không có giao dịch nào
        if (transactions == null || transactions.isEmpty()) {
            return new ApiResponse<>("Không tìm thấy lịch sử giao dịch nào cho tài khoản này", null);
        }

        // Trả kết quả
        return new ApiResponse<>("Lịch sử giao dịch", transactions);
    }

    public  ApiResponse<List<Transaction>> getTransactionHistoryByUser(String userId) {
        if (userId == null || userId.isEmpty()) {
            return new ApiResponse<>("Input không hợp lệ", null);
        }

        List<Transaction> transactions;
        try {
            transactions = transactionRepository.findByUserId(userId);
        } catch (DataAccessException e) {
            System.err.println("Lỗi cơ sở dữ liệu: " + e.getMessage());
            return new ApiResponse<>("Lỗi khi truy xuất lịch sử giao dịch từ cơ sở dữ liệu", null);
        } catch (Exception e) {
            System.err.println("Lỗi không xác định: " + e.getMessage());
            return new ApiResponse<>("Lỗi không xác định xảy ra", null);
        }

        // Kiểm tra nếu không có giao dịch nào
        if (transactions == null || transactions.isEmpty()) {
            return new ApiResponse<>("Không tìm thấy lịch sử giao dịch nào cho tài khoản này", null);
        }

        // Trả kết quả
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