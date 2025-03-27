package com.atm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "balance")
public class Balance {

    @Id
    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Column(name = "available_balance")
    private Double availableBalance;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    // Getters và Setters
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Double getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(Double availableBalance) {
        this.availableBalance = availableBalance;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
