package com.atm.model;

public enum TransactionType {
    WITHDRAWAL,  // Rút tiền
    DEPOSIT,     // Nạp tiền
    TRANSFER,     // Chuyển khoản (bổ sung cho đúng CSDL)
    WITHDRAWAL_OTP
}
