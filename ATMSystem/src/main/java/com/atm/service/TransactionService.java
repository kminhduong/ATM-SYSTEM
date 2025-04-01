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
import org.springframework.security.crypto.password.PasswordEncoder;
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

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final JwtUtil jwtUtil;
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();
    private final PasswordEncoder passwordEncoder;
    private final CredentialService credentialService;
    private final BalanceService balanceService;
    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);



    @Autowired
    public TransactionService(AccountService accountService,
                              AccountRepository accountRepository,
                              TransactionRepository transactionRepository,
                              JwtUtil jwtUtil,
                              PasswordEncoder passwordEncoder,
                              CredentialService credentialService,
                              BalanceService balanceService,
                              UserRepository userRepository) {  // Inject passwordEncoder vào constructor
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;  // Gán giá trị cho passwordEncoder
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

        // Thực hiện giao dịch
        switch (transactionType) {
            case Withdrawal:
                return handleWithdraw(account, amount);

            case Deposit:
                return handleDeposit(account, amount);

            case TRANSFER:
                return handleTransfer(account, targetAccountNumber, amount);

            default:
                return new ApiResponse<>("Loại giao dịch không hợp lệ", null);
        }
    }

    private ApiResponse<String> handleWithdraw(Account account, double amount) {
        if (amount > account.getBalance()) {
            return new ApiResponse<>("Số dư không đủ để thực hiện giao dịch", null);
        }

        // Tạo DTO để cập nhật số dư
        AccountDTO withdrawalDTO = new AccountDTO();
        withdrawalDTO.setBalance(amount);
        balanceService.updateBalance(withdrawalDTO, account, TransactionType.Withdrawal);

        // Lưu giao dịch
        Transaction transaction = new Transaction(
                account.getAccountNumber(),
                amount,
                TransactionType.Withdrawal,
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

    public ApiResponse<String> handleDeposit(Account account, double amount) {
        // Tạo DTO để cập nhật số dư
        AccountDTO depositDTO = new AccountDTO();
        depositDTO.setBalance(amount);
        balanceService.updateBalance(depositDTO, account, TransactionType.Deposit);

        // Lưu giao dịch
        Transaction transaction = new Transaction(
                account.getAccountNumber(),
                amount,
                TransactionType.Deposit,
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

        // Tạo DTO để trừ tiền tài khoản nguồn
        AccountDTO transferSourceDTO = new AccountDTO();
        transferSourceDTO.setBalance(amount);
        balanceService.updateBalance(transferSourceDTO, sourceAccount, TransactionType.Withdrawal);

        // Tạo DTO để cộng tiền tài khoản đích
        AccountDTO transferTargetDTO = new AccountDTO();
        transferTargetDTO.setBalance(amount);
        balanceService.updateBalance(transferTargetDTO, targetAccount, TransactionType.Deposit);

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
                TransactionType.Deposit,
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

    public ApiResponse<String> sendOtpForWithdrawal(String accountNumber){
        // 1. Kiểm tra accountNumber đầu vào
        if (accountNumber == null || accountNumber.isEmpty()) {
            return new ApiResponse<>("Số tài khoản là bắt buộc.", null);
        }

        // 2. Tìm tài khoản từ cơ sở dữ liệu
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty()) {
            return new ApiResponse<>("Không tìm thấy tài khoản với số tài khoản đã cung cấp.", null);
        }
        Account account = accountOpt.get();

        // 3. Tìm số điện thoại từ bảng User
        Optional<User> userOpt = userRepository.findByUserId(account.getUser().getUserId()); // Liên kết account với userId
        if (userOpt.isEmpty()) {
            return new ApiResponse<>("Không tìm thấy thông tin người dùng cho tài khoản này.", null);
        }
        String phoneNumber = userOpt.get().getPhone();

        // 4. Gửi OTP
        String generatedOtp = generateAndSendOtp(phoneNumber);
        return new ApiResponse<>("OTP đã được gửi đến số điện thoại của bạn.", generatedOtp);
    }

    public ApiResponse<String> processWithdrawWithOtp(WithdrawOtpRequest request) {
        // Kiểm tra đầu vào
        if (request.getAccountNumber() == null || request.getAccountNumber().isEmpty()) {
            return new ApiResponse<>("Số tài khoản là bắt buộc.", null);
        }
        if (request.getOtp() == null || request.getOtp().isEmpty()) {
            return new ApiResponse<>("OTP là bắt buộc.", null);
        }
        if (request.getAmount() == null || request.getAmount() <= 0) {
            return new ApiResponse<>("Số tiền muốn rút phải lớn hơn 0.", null);
        }

        // Xác thực OTP
        if (!"123456".equals(request.getOtp())) {
            return new ApiResponse<>("OTP không hợp lệ. Vui lòng thử lại.", null);
        }

        // Lấy tài khoản từ cơ sở dữ liệu
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(request.getAccountNumber());
        if (accountOpt.isEmpty()) {
            return new ApiResponse<>("Không tìm thấy tài khoản với số tài khoản đã cung cấp.", null);
        }
        Account account = accountOpt.get();

        // Lấy thông tin User để kiểm tra số điện thoại
        Optional<User> userOpt = userRepository.findByUserId(account.getUser().getUserId());
        if (userOpt.isEmpty()) {
            return new ApiResponse<>("Không tìm thấy thông tin người dùng cho tài khoản này.", null);
        }
        String phoneNumber = userOpt.get().getPhone();

        // Kiểm tra số dư tài khoản
        if (request.getAmount() > account.getBalance()) {
            return new ApiResponse<>("Số dư không đủ để thực hiện giao dịch.", null);
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
        return new ApiResponse<>("Giao dịch rút tiền thành công.", "Số dư còn lại: " + account.getBalance());
    }

    private String generateAndSendOtp(String phoneNumber) {
        // Tạo OTP ngẫu nhiên
        String otp = "123456"; // Hoặc sử dụng phương pháp tạo mã OTP thực tế
        logger.info("Đang gửi OTP {} tới số điện thoại {}", otp, phoneNumber);
        // Logic gửi OTP tới số điện thoại (API SMS hoặc tích hợp khác)
        return otp;
    }

    public ApiResponse<List<Transaction>> getTransactionHistory(String accountNumber) {
        // Kiểm tra đầu vào
        if (accountNumber == null || accountNumber.isEmpty()) {
            return new ApiResponse<>("Số tài khoản không được để trống", null);
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