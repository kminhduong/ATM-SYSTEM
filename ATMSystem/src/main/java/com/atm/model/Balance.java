package com.atm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "balance")
public class Balance {

    @Id
    @Column(name = "account_number", length = 50)
    private String accountNumber; // Sử dụng @MapsId để ánh xạ từ Account

    @Column(name = "balance", nullable = false)
    private Double balance;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @OneToOne
    @MapsId
    @JoinColumn(name = "account_number") // Liên kết với account_number trong bảng Account
    private Account account;

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
