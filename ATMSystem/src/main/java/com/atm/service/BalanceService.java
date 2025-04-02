package com.atm.service;

import com.atm.dto.AccountDTO;
import com.atm.model.Account;
import com.atm.model.Balance;
import com.atm.repository.AccountRepository;
import com.atm.repository.BalanceRepository;
import com.atm.model.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BalanceService {
    private final AccountRepository accountRepository;
    private final BalanceRepository balanceRepository;

    @Autowired
    public BalanceService(AccountRepository accountRepository,BalanceRepository balanceRepository) {
        this.accountRepository = accountRepository;
        this.balanceRepository = balanceRepository;
    }

    public Double getBalance(String accountNumber) {
        // Lấy tài khoản đang đăng nhập
        String loggedInAccountNumber = getLoggedInAccountNumber();

        // Kiểm tra xem tài khoản yêu cầu có phải của người dùng đang đăng nhập hay không
        if (!accountNumber.equals(loggedInAccountNumber)) {
            throw new SecurityException("Bạn không có quyền truy cập số dư của tài khoản này.");
        }

        return accountRepository.findByAccountNumber(accountNumber)
                .map(Account::getBalance)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại."));
    }

    public void updateBalance(AccountDTO accountDTO, Account account, TransactionType transactionType) {
        // Kiểm tra nếu số dư mới không tồn tại hoặc không hợp lệ
        if (accountDTO.getBalance() == null || accountDTO.getBalance() <= 0) {
            throw new IllegalArgumentException("Số dư phải lớn hơn 0.");
        }

        // Kiểm tra nếu Balance chưa tồn tại
        Balance balance = account.getBalanceEntity();
        if (balance == null) {
            // Sử dụng hàm createBalance để tạo Balance mới
            createBalance(account);
            balance = account.getBalanceEntity(); // Lấy Balance vừa tạo
        }

        // Lấy số dư hiện tại và chuẩn bị cập nhật
        double currentBalance = balance.getBalance();
        double updatedBalance = currentBalance;

        // Xử lý logic dựa trên loại giao dịch
        switch (transactionType) {
            case Deposit:
                // Nạp tiền
                updatedBalance += accountDTO.getBalance();
                break;

            case Withdrawal:
                // Rút tiền
                if (currentBalance < accountDTO.getBalance()) {
                    throw new IllegalArgumentException("Số dư không đủ để thực hiện giao dịch rút tiền.");
                }
                updatedBalance -= accountDTO.getBalance();
                break;

            case WITHDRAWAL_OTP:
                // Rút tiền OTP
                if (currentBalance < accountDTO.getBalance()) {
                    throw new IllegalArgumentException("Số dư không đủ để thực hiện giao dịch rút tiền OTP.");
                }
                updatedBalance -= accountDTO.getBalance();
                // Logic bổ sung như xác thực OTP có thể được thêm tại đây
                break;

            case TRANSFER:
                // Logic chuyển khoản cần được xử lý riêng
                throw new UnsupportedOperationException("Chức năng chuyển khoản cần xử lý riêng cho tài khoản nguồn và đích.");

            default:
                throw new IllegalArgumentException("Loại giao dịch không hợp lệ.");
        }

        // Cập nhật số dư mới
        balance.setBalance(updatedBalance);
        balance.setLastUpdated(LocalDateTime.now()); // Cập nhật thời gian sửa đổi
        balanceRepository.save(balance); // Lưu vào cơ sở dữ liệu
    }

    // Hàm lấy số tài khoản của người dùng hiện tại
    public String getLoggedInAccountNumber() {
        System.out.println("🔍 Kiểm tra SecurityContextHolder: " + SecurityContextHolder.getContext().getAuthentication());

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            System.out.println("❌ SecurityContextHolder is NULL!");
            return null;
        }

        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public void createBalance(Account account) {
        Balance balance = new Balance();
        balance.setAccount(account);
        balance.setBalance(0.0);
        balance.setLastUpdated(LocalDateTime.now());
        balanceRepository.save(balance);
    }
}
