package com.atm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "credential")
public class Credential {

    @Id
    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @OneToOne
    @MapsId // ✅ Chỉ định accountNumber là khóa chính của Credential, lấy từ Account
    @JoinColumn(name = "account_number")
    private Account account;

    @Column(name = "pin", length = 6, nullable = false)
    private String pin;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;

    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    // No-arg constructor (Hibernate cần)
    public Credential() {}

    public Credential(Account account, String password, int failedAttempts, LocalDateTime lastLocked, LocalDateTime createdAt) {
        this.account = account;
        this.pin = password;
        this.failedAttempts = failedAttempts;
        this.lockTime = (lastLocked != null) ? lastLocked : LocalDateTime.now();
        this.updateAt = createdAt;
    }


    // Getters và Setters
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public LocalDateTime getLockTime() {
        return lockTime;
    }

    public void setLockTime(LocalDateTime lockTime) {
        this.lockTime = lockTime;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }
}