package com.atm.dto;


public class LoginRequest {
    private String accountNumber;
    private String pin;

    public LoginRequest(String username, String password) {
        this.accountNumber = username;
        this.pin = password;
    }

    // Getter & Setter

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getPin() {
        return pin;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
