package com.atm.service;

import com.atm.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CredentialService {
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CredentialService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    // Phương thức kiểm tra mã PIN (sử dụng mã hóa)
    public boolean validatePIN(String rawPin, String encodedPin) {
        return passwordEncoder.matches(rawPin, encodedPin); // So sánh mã PIN thô với mã PIN đã mã hóa
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
//
//    // Thay đổi mã PIN
//    public boolean changePIN(String oldPin, String newPin, User user) {
//        if (passwordEncoder.matches(oldPin, user.getStoredPIN())) {
//            user.setStoredPIN(passwordEncoder.encode(newPin)); // Mã hóa và lưu mã PIN mới
//            return true; // Đổi mã PIN thành công
//        }
//        return false; // Đổi mã PIN thất bại
//    }
}