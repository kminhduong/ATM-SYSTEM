package com.atm.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

@Entity
@Table(name = "transaction")
public class Transaction {

    @Id
    @Column(name = "transaction_id", length = 50)
    private String transactionId;

    @Column(name = "atm_id", length = 50, nullable = false)
    private String atmId;

    @Column(name = "account_number", length = 50, nullable = false)
    @NotNull(message = "Account number is mandatory")
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    @Column(name = "amount", nullable = false)
    private double amount;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_at", nullable = false)
    private Date createAt;

    // ✅ No-arg constructor (bắt buộc cho Hibernate)
    public Transaction() {}

    // ✅ Constructor đầy đủ
    public Transaction(String transactionId, String atmId, String accountNumber, String type, double amount) {
        this.transactionId = transactionId;
        this.atmId = atmId;
        this.accountNumber = accountNumber;
        setType(type);
        setAmount(amount);
        this.createAt = new Date();  // Tạo thời gian hiện tại khi khởi tạo
    }

    // ✅ Constructor theo yêu cầu (accountNumber, amount, type, createAt)
    public Transaction(String accountNumber, double amount, TransactionType type, Date createAt) {
        this.transactionId = java.util.UUID.randomUUID().toString(); // Tạo ID ngẫu nhiên
        this.atmId = "ATM001"; // Có thể thay đổi nếu cần
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.type = type;
        this.createAt = createAt != null ? createAt : new Date();
    }

    // ✅ Getters và Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAtmId() {
        return atmId;
    }

    public void setAtmId(String atmId) {
        this.atmId = atmId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public TransactionType getType() {
        return type;
    }

    // ✅ Ràng buộc cho type
    public void setType(String type) {
        if (type != null) {
            this.type = TransactionType.fromString(type);
        } else {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }
    }

    public double getAmount() {
        return amount;
    }

    // ✅ Ràng buộc cho amount (phải > 0)
    public void setAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        this.amount = amount;
    }

    // ✅ Getter cho createAt (không cho phép chỉnh sửa trực tiếp)
    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", atmId='" + atmId + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", type=" + type +
                ", amount=" + amount +
                ", createAt=" + createAt +
                '}';
    }
}