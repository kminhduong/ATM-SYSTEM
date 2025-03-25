package com.atm.model;

import java.util.Date;
// Tạo tự động id
import java.util.UUID;

public class Transaction {
    private String transactionId;
    private String accountNumber;
    private double amount;
    private Date date;
    private String type; // "withdrawal" hoặc "deposit"

    // Constructor
    public Transaction(String accountNumber, double amount, String type) {
        this.transactionId = UUID.randomUUID().toString(); // Tạo ID ngẫu nhiên
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.date = new Date(); // Thời gian giao dịch hiện tại
        this.type = type;
    }

    // Getters
    public String getTransactionId() {
        return transactionId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public double getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    // Setters (nếu cần)
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setType(String type) {
        this.type = type;
    }

    // Hiển thị thông tin giao dịch
    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", amount=" + amount +
                ", date=" + date +
                ", type='" + type + '\'' +
                '}';
    }
}
