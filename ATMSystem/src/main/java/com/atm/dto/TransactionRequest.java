package com.atm.dto;

import com.atm.model.TransactionType;

// TransactionRequest.java
public class TransactionRequest {
    private double amount;
    private TransactionType transactionType;

    // Getters và Setters
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }
}