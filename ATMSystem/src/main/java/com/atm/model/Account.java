package com.atm.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

@Entity // Đánh dấu lớp này là một thực thể JPA
@Table(name = "accounts") // Tên bảng trong cơ sở dữ liệu
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Tạo ID tự động tăng
    private Long id;

    @Column(unique = true, nullable = false, length = 12) // Giới hạn số tài khoản 12 ký tự
    private String accountNumber;

    @Column(nullable = false, length = 6) // Giới hạn PIN 6 ký tự
    private String pin;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "#,##0.00")
    private double balance;
    private String email;

    @Column(length = 10)
    private String phoneNumber;

    @Column(length = 255)
    private String address;

    // Constructor mặc định
    public Account() {}

    // Constructor đầy đủ
    public Account(String accountNumber, String pin, double balance, String email, String phoneNumber, String address) {
        this.accountNumber = accountNumber;
        this.pin = pin;
        this.balance = balance;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", accountNumber='" + accountNumber + '\'' +
                ", balance=" + balance +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}