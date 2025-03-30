package com.atm.model;

public enum TransactionType {
    WITHDRAWAL,  // Rút tiền
    DEPOSIT,     // Nạp tiền
    TRANSFER,     // Chuyển khoản (bổ sung cho đúng CSDL)
    WITHDRAWAL_OTP;
    public static TransactionType fromString(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Transaction type cannot be null or empty");
        }
        return TransactionType.valueOf(value.toUpperCase()); // Không phân biệt hoa/thường
    }

    public TransactionType getTypeFromDatabase(String dbValue) {
        try {
            return TransactionType.fromString(dbValue);
        } catch (IllegalArgumentException e) {
            System.err.println("Giá trị không hợp lệ trong cơ sở dữ liệu: " + dbValue);
            throw e; // Hoặc xử lý lỗi nếu cần
        }
    }
}
