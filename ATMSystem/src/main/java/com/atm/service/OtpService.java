package com.atm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private String generateAndSendOtp(String phoneNumber) {
        // Tạo OTP ngẫu nhiên
        String otp = "123456"; // Hoặc sử dụng phương pháp tạo mã OTP thực tế
        logger.info("Sending OTP {} to phone number {}", otp, phoneNumber);
        // Logic gửi OTP tới số điện thoại (API SMS hoặc tích hợp khác)
        return otp;
    }
}
