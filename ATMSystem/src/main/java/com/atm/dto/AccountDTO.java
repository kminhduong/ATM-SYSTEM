package com.atm.dto;

import com.atm.model.Account;
import com.atm.model.AccountType;
import com.atm.model.AccountStatus;

public class AccountDTO {

    private String accountNumber;
    private String username;
    private String password;
    private String fullName;
    private String userId;
    private AccountType accountType;
    private AccountStatus status;
    private double balance;
    private String pin;
    private String role;
    private String phoneNumber;

    // Constructor không tham số
    public AccountDTO() {
    }

    // Constructor đầy đủ
    public AccountDTO(String accountNumber, String username, String password, String fullName, String userId, AccountType accountType, AccountStatus status, double balance, String pin, String role) {
        this.accountNumber = accountNumber;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.userId = userId;
        this.accountType = accountType;
        this.status = status;
        this.balance = balance;
        this.pin = pin;
        this.role = role;
    }

    // Chuyển từ DTO thành Entity
    public Account toAccount() {
        return new Account(
                this.accountNumber != null ? this.accountNumber : "Unknown",
                this.username != null ? this.username : "DefaultUsername",
                this.password != null ? this.password : "DefaultPassword",
                this.fullName != null ? this.fullName : "Unknown Name",
                this.userId,
                this.accountType != null ? this.accountType : AccountType.SAVINGS,
                this.status != null ? this.status : AccountStatus.ACTIVE,
                0.0, // Số dư mặc định
                "000000", // PIN mặc định
                this.role
        );
    }

    // Chuyển từ Entity thành DTO
    public static AccountDTO fromAccount(Account account) {
        return new AccountDTO(
                account.getAccountNumber(),
                account.getUsername(),
                account.getPassword(),
                account.getFullName(),
                account.getUserId(),
                account.getAccountType(),
                account.getStatus(),
                account.getBalance(),
                account.getPin(),  // Include pin
                account.getRole()   // Include role
        );
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

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
    public String getPin() {
        return pin;
    }
    public void setPin(String pin) {
        this.pin = pin;
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