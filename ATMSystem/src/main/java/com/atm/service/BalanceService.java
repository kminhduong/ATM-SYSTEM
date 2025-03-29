package com.atm.service;

import com.atm.dto.AccountDTO;
import com.atm.model.Account;
import com.atm.model.Balance;
import com.atm.repository.AccountRepository;
import com.atm.repository.BalanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

    public void updateBalance(AccountDTO accountDTO, Account account) {
        if (accountDTO.getBalance() != null) {
            if (account.getBalanceEntity() == null) {
                // Nếu chưa có Balance, tạo mới
                Balance newBalance = new Balance();
                newBalance.setBalance(accountDTO.getBalance()); // Cập nhật số dư mới
                newBalance.setAccount(account); // Liên kết Balance với Account
                account.setBalanceEntity(newBalance); // Gắn Balance vào Account
                balanceRepository.save(newBalance); // Lưu Balance mới vào cơ sở dữ liệu
            } else {
                // Nếu đã có Balance, chỉ cập nhật giá trị số dư
                Balance existingBalance = account.getBalanceEntity();
                existingBalance.setBalance(accountDTO.getBalance());
                balanceRepository.save(existingBalance); // Lưu Balance đã cập nhật
            }
        }
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
}
