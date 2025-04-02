package com.atm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Balance")
public class Balance {
    @Id
    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Column(name = "balance", nullable = false, columnDefinition = "DECIMAL(15,2) DEFAULT 0.00") // Added default value
    private Double balance;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @OneToOne
    @MapsId
    @JoinColumn(name = "account_number") // Liên kết với account_number trong bảng Account
    private Account account;

    // No-arg constructor (Hibernate cần)
    public Balance() {}

    public Balance(Account account, double balance, LocalDateTime lastUpdated) {
        this.account = account;
        this.balance = balance;
        this.lastUpdated = lastUpdated;
    }

    // Getters và Setters
    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
