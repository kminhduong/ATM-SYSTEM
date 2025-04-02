package com.atm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

import com.atm.model.Account;
import com.atm.model.Credential;
import com.atm.repository.CredentialRepository;

@Service
public class CredentialService {
    private final PasswordEncoder passwordEncoder;
    private final CredentialRepository credentialRepository;
    private final BalanceService balanceService;

    @Autowired
    public CredentialService(PasswordEncoder passwordEncoder,
                             CredentialRepository credentialRepository,
                             BalanceService balanceService) {
        this.passwordEncoder = passwordEncoder;
        this.credentialRepository = credentialRepository;
        this.balanceService = balanceService;
    }

    // Phương thức kiểm tra mã PIN (sử dụng mã hóa)
    public boolean validatePIN(String rawPin, String encodedPin) {
        return passwordEncoder.matches(rawPin, encodedPin); // So sánh mã PIN thô với mã PIN đã mã hóa
    }

    public void createCredential(Account account) {
        Credential credential = new Credential();
        credential.setAccount(account);
        credential.setPin(passwordEncoder.encode("000000"));
        credential.setFailedAttempts(0);
        credential.setLockTime(null);
        credential.setUpdateAt(LocalDateTime.now());
        credentialRepository.save(credential);
    }

    public void changePIN(String oldPin, String newPin, String confirmNewPin) {
        // Lấy account_number từ token đăng nhập
        String loggedInAccountNumber = balanceService.getLoggedInAccountNumber();
        if (loggedInAccountNumber == null) {
            throw new RuntimeException("No users are logged in.");
        }

        System.out.println("🔍 Account currently logged in: " + loggedInAccountNumber);

        // Yêu cầu kiểm tra tính hợp lệ của PIN mới
        if (!newPin.equals(confirmNewPin)) {
            throw new RuntimeException("New PIN and Confirmation PIN do not match.");
        }

        // Tìm Credential của tài khoản
        Optional<Credential> optionalCredential = credentialRepository.findById(loggedInAccountNumber);
        if (optionalCredential.isPresent()) {
            Credential credential = optionalCredential.get();

            // Kiểm tra mã PIN cũ
            if (!passwordEncoder.matches(oldPin, credential.getPin())) {
                throw new RuntimeException("Old PIN is incorrect.");
            }

            // Cập nhật mã PIN mới
            credential.setPin(passwordEncoder.encode(newPin));
            credential.setUpdateAt(LocalDateTime.now());
            credentialRepository.save(credential);
            System.out.println("✅ PIN code has been changed successfully.");
        } else {
            throw new RuntimeException("Credential information not found for this account.");
        }
    }

//    // Tăng số lần đăng nhập thất bại
//    public void incrementFailedAttempts(User user) {
//        user.setFailedAttempts(user.getFailedAttempts() + 1);
//    }
//
//    // Đặt lại số lần đăng nhập thất bại
//    public void resetFailedAttempts(User user) {
//        user.setFailedAttempts(0);
//    }
//
//    // Kiểm tra xem tài khoản có bị khóa hay không
//    public boolean isAccountLocked(User user) {
//        return user.isLocked();
//    }
//
//    // Khóa tài khoản người dùng
//    public void lockAccount(User user) {
//        user.setLocked(true);
//    }
}