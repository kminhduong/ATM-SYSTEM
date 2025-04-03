package com.atm.service;

import com.atm.dto.AccountDTO;
import com.atm.dto.ApiResponse;
import com.atm.model.*;
import com.atm.repository.AccountRepository;
import com.atm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final BalanceService balanceService;
    private final CredentialService credentialService;
    private final UserService userService;

    @Autowired
    public AccountService(AccountRepository accountRepository,
                          UserRepository userRepository,
                          BalanceService balanceService,
                          CredentialService credentialService,
                          UserService userService) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.balanceService = balanceService;
        this.credentialService = credentialService;
        this.userService = userService;
    }

    /**
     * Đăng ký tài khoản dựa trên thông tin từ AccountDTO.
     * Chuyển đổi dữ liệu và gọi hàm createAccount để xử lý logic tạo tài khoản.
     *
     * @param accountDTO Đối tượng chứa thông tin tài khoản cần đăng ký.
     */
    public void registerAccount(AccountDTO accountDTO) {
        // Chuyển đổi AccountDTO thành đối tượng Account.
        Account account = accountDTO.toAccount(userRepository);

        // Ghi log để xác nhận thông tin đã nhận.
        logger.info("Account registered successfully for userId: {}", accountDTO.getUserId());

        // Gọi hàm createAccount để xử lý logic kiểm tra và lưu tài khoản.
        createAccount(account);
    }

    /**
     * Tạo tài khoản trong hệ thống.
     * Kiểm tra sự tồn tại của tài khoản và người dùng, đồng thời tạo các thực thể liên quan.
     *
     * @param account Đối tượng chứa thông tin tài khoản.
     * @return Tài khoản đã được tạo.
     */
    @Transactional
    public Account createAccount(Account account) {
        logger.info("🔍 Entering the createAccount...");
        logger.info("Received request to register account: {}", account.getAccountNumber());

        // 1. Kiểm tra xem tài khoản đã tồn tại hay chưa.
        if (accountRepository.existsById(account.getAccountNumber())) {
            logger.error("Account already exists: {}", account.getAccountNumber());
            throw new IllegalArgumentException("The account already exists!");
        }

        // 2. Kiểm tra thông tin người dùng (User) của tài khoản.
        User user = account.getUser();
        if (user == null) {
            logger.info("The user of the account is null, check again from the DB...");
            user = userRepository.findByUserId(account.getUser().getUserId()).orElse(null);

            // Nếu User tồn tại nhưng tên không khớp, ném ngoại lệ.
            if (user != null && !user.getName().equals(account.getFullName())) {
                logger.error("User with ID {} already exists but has a different name: {}", account.getUser().getUserId(), user.getName());
                throw new IllegalArgumentException("Name doesn't match userId!");
            }

            // Nếu không tìm thấy User, tạo User mới.
            if (user == null) {
                logger.info("User not found, creating a new User...");
                user = new User();
                user.setUserId(account.getUser().getUserId());
                user.setName(account.getFullName());
                userRepository.save(user);
                logger.info("New User created with ID: {}", user.getUserId());
            }
        }
        account.setUser(user);

        // 3. Lưu tài khoản
        Account savedAccount = accountRepository.save(account);
        balanceService.createBalance(savedAccount);
        credentialService.createCredential(savedAccount);

        logger.info("Successfully created account: {}", savedAccount.getAccountNumber());
        return savedAccount;
    }

    @Transactional
    public void updateAccount(AccountDTO accountDTO, String accountNumber) {
        Optional<Account> optionalAccount = accountRepository.findById(accountDTO.getAccountNumber());

        if (optionalAccount.isEmpty()) {
            throw new RuntimeException("The account does not exist.");
        }

        Account account = optionalAccount.get();

        // Kiểm tra quyền thay đổi thông tin
        //checkUpdatePermission(accountNumber, accountDTO, false);

        // Cập nhật thông tin tài khoản
        updateAccountDetails(account, accountDTO);

        // Nếu cập nhật số dư
        if (accountDTO.getBalance() != null) {
            //checkUpdatePermission(accountNumber, accountDTO, true);
            balanceService.updateBalance(accountDTO, account, TransactionType.DEPOSIT);
        }

        // Cập nhật thông tin bảo mật (Credential)
        if (accountDTO.getPin() != null) {
            credentialService.changePINAdmin(
                    accountDTO.getAccountNumber(),
                    accountDTO.getPin()
            );
        }

        // Cập nhật thông tin người dùng
        if (account.getUser() != null) {
            userService.updateUserDetails(account.getUser(), accountDTO);
        } else {
            throw new RuntimeException("The user associated with this account was not found.");
        }

        // Lưu Account sau khi cập nhật
        accountRepository.save(account);
    }

    private void checkUpdatePermission(String accountNumber, AccountDTO accountDTO, boolean isBalanceUpdate) {
        String userRole = getUserRole(accountNumber);
        if ("ADMIN".equals(userRole)) {
            return; // ADMIN luôn có quyền
        }

        if (!accountNumber.equals(accountDTO.getAccountNumber())) {
            throw new RuntimeException("You do not have the right to update this account!");
        }

        if (isBalanceUpdate) {
            throw new RuntimeException("You do not have the right to change this account balance!");
        }
    }

    private void updateAccountDetails(Account account, AccountDTO accountDTO) {
        if (accountDTO.getFullName() != null) {
            account.setFullName(accountDTO.getFullName());
        }
        if (accountDTO.getRole() != null && !accountDTO.getRole().isEmpty()) {
            account.setRole(accountDTO.getRole());
        }

        if (accountDTO.getStatus() != null) {
            account.setStatus(accountDTO.getStatus());
        }

    }

    // Lấy tất cả khách hàng (dành cho nhân viên ngân hàng)
    public List<Account> getAllCustomers() {
        return accountRepository.findAll();
    }
    public String getUserRole(String accountNumber) {
        return accountRepository.findRoleByAccountNumber(accountNumber);
    }

//    public Account getAccount(String accountNumber) {
//        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
//
//        if (accountOpt.isPresent()) {
//            Account account = accountOpt.get();
//            logger.info("🔍 Accounts found: {}, Role: {}", account.getAccountNumber(), account.getRole());
//            return account;
//        }
//
//        logger.warn("⚠ No account found: {}", accountNumber);
//        return null;
//    }

//    public void updateAccountStatus(String new_status) {
//        // Danh sách trạng thái hợp lệ
//        List<String> validStatuses = Arrays.asList("ACTIVE", "CLOSED", "FROZEN", "BLOCKED", "PENDING");
//
//        // Kiểm tra tính hợp lệ của trạng thái
//        if (!validStatuses.contains(new_status)) {
//            throw new IllegalArgumentException("Invalid Status: " + new_status);
//        }
//
//        // Cập nhật trạng thái
//        account.setStatus(new_status);
//        accountRepository.save(account); // Lưu vào cơ sở dữ liệu
//
//        System.out.println("The account status has been updated to: " + new_status);
//    }

//    public String checkAccountStatus() {
//        // Assume 'status' is a field in your Account class
//        switch (account.getStatus()) {
//            case "ACTIVE":
//                return "The account is active.";
//            case "CLOSED":
//                return "The account has been closed.";
//            case "FROZEN":
//                return "The account is frozen.";
//            case "BLOCKED":
//                return "The account is blocked.";
//            case "PENDING":
//                return "The account is pending.";
//            default:
//                return "Unknown status.";
//        }
//    }
    // Kiểm tra user có tồn tại không
//    public boolean isUserExists(String userId) {
//        String sql = "SELECT COUNT(*) FROM user WHERE user_id = ?";
//        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
//        return count != null && count > 0;
//    }

    private <T> ApiResponse<T> handleError(Exception e, String message) {
        System.err.println(message + ": " + e.getMessage());
        return new ApiResponse<>(message, null);
    }

    public ApiResponse<List<Account>> getAccountsByUserId(String userId) {
        List<Account> accounts;
        try {
            accounts = accountRepository.findByUserId(userId);
        } catch (DataAccessException e) {
            return handleError(e, "Error retrieving translation history from database");
        } catch (Exception e) {
            return handleError(e, "An unknown error occurred");
        }

        // Kiểm tra nếu không có giao dịch nào
        if (accounts == null || accounts.isEmpty()) {
            return new ApiResponse<>("No accounts found for this user", null);
        }
        return new ApiResponse<>("success", accounts);
    }

    public Account getAccountById(String id) {
        Optional<Account> accountOptional = accountRepository.findById(id);
        if (accountOptional.isPresent()) {
            return accountOptional.get(); // Returns the User if found
        } else {
            throw new RuntimeException("Account not found with ID: " + id);
        }
    }


}