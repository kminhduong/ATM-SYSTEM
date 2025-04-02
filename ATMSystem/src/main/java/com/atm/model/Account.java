package com.atm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Account")
public class Account {

    @Id
    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "user_id", nullable = false) // Ánh xạ chính xác với cột user_id trong cơ sở dữ liệu
    @JsonIgnore
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    @JsonIgnore
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @JsonIgnore
    private AccountStatus status;

    @Column(name = "full_name", length = 100, nullable = false)
    private String fullName;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }

    @Column
    private String role;  // Không ràng buộc, có thể NULL

    @OneToOne
    @JoinColumn(name = "account_number", referencedColumnName = "account_number", insertable = false, updatable = false)
    @JsonIgnore
    private Credential credential;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Balance balanceEntity;

    // ✅ Constructor không tham số (BẮT BUỘC cho JPA)
    public Account() {}

    // ✅ Constructor đầy đủ
    public Account(String accountNumber,
                   String fullName, User user, AccountType accountType,
                   AccountStatus status, double balance, String pin, String role) {
        this.accountNumber = accountNumber;
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
            balanceEntity = new Balance(); // Khởi tạo đối tượng Balance nếu chưa tồn tại
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
