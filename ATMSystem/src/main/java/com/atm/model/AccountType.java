package com.atm.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AccountType {
    SAVINGS, CHECKING;

    @JsonCreator
    public static AccountType fromString(String value) {
        try {
            // Chuyển đổi sang chữ hoa trước khi tìm kiếm enum
            return AccountType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            // Xử lý nếu không có hằng số phù hợp
            throw new IllegalArgumentException("Invalid account type: " + value);
        }
    }
}