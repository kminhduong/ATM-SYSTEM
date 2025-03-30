package com.atm.service;

import com.atm.dto.AccountDTO;
import com.atm.model.User;
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

    @Autowired
    public CredentialService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    @Autowired
    private CredentialRepository credentialRepository; // Đường dẫn đúng tới CredentialRepository

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

    public void changePIN(AccountDTO accountDTO) {
        Optional<Credential> optionalCredential = credentialRepository.findById(accountDTO.getAccountNumber());
        if (optionalCredential.isPresent()) {
            Credential credential = optionalCredential.get();
            credential.setPin(passwordEncoder.encode(accountDTO.getPin())); // Mã hóa pin mới
            credential.setUpdateAt(LocalDateTime.now());
            credentialRepository.save(credential); // Lưu Credential đã cập nhật
        } else {
            throw new RuntimeException("Không tìm thấy thông tin Credential cho tài khoản này.");
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