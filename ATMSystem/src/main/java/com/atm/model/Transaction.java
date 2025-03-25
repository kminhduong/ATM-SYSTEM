package com.atm.model;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity // Đánh dấu lớp này là một thực thể JPA
@Table(name = "transactions") // Tên bảng trong cơ sở dữ liệu
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Tạo ID tự động tăng
    private Long id;

    private String transactionId;
    private String accountNumber;
    private double amount;
    private Date date;
    private String type; // "withdrawal" hoặc "deposit"

    // Constructor
    public Transaction() {
        this.transactionId = UUID.randomUUID().toString(); // Tạo ID ngẫu nhiên
        this.date = new Date(); // Thời gian giao dịch hiện tại
    }

    public Transaction(String accountNumber, double amount, String type) {
        this();
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.type = type;
    }

    // Getters và Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTransactionId() { return transactionId; }
    public String getAccountNumber() { return accountNumber; }
    public double getAmount() { return amount; }
    public Date getDate() { return date; }
    public String getType() { return type; }

    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setType(String type) { this.type = type; }

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