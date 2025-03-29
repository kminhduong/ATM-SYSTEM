package com.atm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Account")
public class Account {

    @Id
    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @OneToOne
    @JoinColumn(name = "account_number", referencedColumnName = "account_number", insertable = false, updatable = false)
    private Credential credential;

    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    @Column(name = "full_name", length = 100, nullable = false)
    private String fullName;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private Balance balanceEntity;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column
    private String role;  // Không ràng buộc, có thể NULL

    // ✅ Constructor không tham số (BẮT BUỘC cho JPA)
    public Account() {}

    // ✅ Constructor đầy đủ
    public Account(String accountNumber, String username, String fullName, User user, AccountType accountType, AccountStatus status, double balance, String pin, String role) {
        this.accountNumber = accountNumber;
        this.username = username;
        this.fullName = fullName;
        this.user = user; // ✅ Sửa lại từ userId thành user
        this.accountType = accountType;
        this.status = status;
        this.lastUpdated = LocalDateTime.now();
        this.role = role;  // Có thể NULL
    }

    // ✅ Getter & Setter sửa lại
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Credential getCredential() {
        return credential;
    }

    public void setCredential(Credential credential) {
        this.credential = credential;
    }

    public String getPin() {
        return this.credential != null ? this.credential.getPin() : null;
    }

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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(String type) {
        try {
            this.accountType = AccountType.valueOf(type);  // Chuyển từ String thành Enum
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid account type: " + type);
        }
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public Balance getBalanceEntity() {
        return balanceEntity;
    }

    public void setBalanceEntity(Balance balanceEntity) {
        this.balanceEntity = balanceEntity;
    }

    public double getBalance() {
        return (balanceEntity != null) ? balanceEntity.getBalance() : 0.0;
    }

    public void setBalance(double balance) {
        if (balanceEntity == null) {
            balanceEntity = new Balance();
            balanceEntity.setAccount(this);
        }
        balanceEntity.setBalance(balance);
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
        this.role = role;  // Có thể NULL
    }
}
