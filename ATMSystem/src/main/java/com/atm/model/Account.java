package com.atm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Account")
public class Account {

    @Id
    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "full_name", length = 100, nullable = false)
    private String fullName;

    @Column(name = "user_id", length = 50, nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type")
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status;

    @Column(name = "balance", nullable = false)
    private double balance; // 🆕 Thêm số dư tài khoản

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "pin", length = 6, nullable = false)
    private String pin;
    private String role;
    private String phoneNumber;


    // ✅ Constructor không tham số (BẮT BUỘC cho JPA)
    public Account() {
    }

    // ✅ Constructor đầy đủ
    public Account(String accountNumber, String username, String password, String fullName, String userId, AccountType accountType, AccountStatus status, double balance, String pin, String role) {
        this.accountNumber = accountNumber;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.userId = userId;
        this.accountType = accountType;
        this.status = status;
        this.balance = balance;
        this.pin = pin;
        this.lastUpdated = LocalDateTime.now();
        this.role = role; // Gán giá trị cho thuộc tính role
    }

    // Getters và Setters
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(String type) {
        this.accountType = AccountType.fromString(type);
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public double getBalance() { // 🆕 Getter balance
        return balance;
    }

    public void setBalance(double balance) { // 🆕 Setter balance
        this.balance = balance;
    }

    // Getter và Setter
    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}