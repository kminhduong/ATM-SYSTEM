package com.atm.dto;

import com.atm.model.Account;
import com.atm.model.AccountType;
import com.atm.model.AccountStatus;
import com.atm.model.User;
import com.atm.repository.UserRepository;

public class AccountDTO {

    private String accountNumber;
    private String fullName;
    private String userId;
    private AccountType accountType;
    private AccountStatus status;
    private Double balance;
    private String pin;
    private String role;
    private String phone;
    private String email;

    // Constructor không tham số
    public AccountDTO() {
    }

    // Constructor đầy đủ
    public AccountDTO(String accountNumber,String fullName, String userId,
                      AccountType accountType, AccountStatus status, Double balance,
                      String pin, String role, String phone, String email) {
        this.accountNumber = accountNumber;
        this.fullName = fullName;
        this.userId = userId;
        this.accountType = accountType;
        this.status = status;
        this.balance = balance;
        this.pin = pin;
        this.role = role;
        this.phone = phone;
        this.email = email;
    }


    // Chuyển từ DTO thành Entity
    public Account toAccount(UserRepository userRepository) {
        User user = (this.userId != null) ? userRepository.findById(this.userId).orElse(null) : null;

        Account account = new Account(
                this.accountNumber != null ? this.accountNumber : "Unknown",
                this.fullName != null ? this.fullName : "Unknown Name",
                user,
                this.accountType != null ? this.accountType : AccountType.SAVINGS,
                this.status != null ? this.status : AccountStatus.ACTIVE,
                this.balance != null ? this.balance : 0.0,
                this.pin != null ? this.pin : "000000",
                this.role != null ? this.role : "USER"
        );

        // Nếu user không null, lấy phone và email từ User
        if (user != null) {
            this.phone = user.getPhone();
            this.email = user.getEmail();
        }

        return account;
    }

    // Chuyển từ Entity thành DTO
    public static AccountDTO fromAccount(Account account) {
        return new AccountDTO(
                account.getAccountNumber(),
                account.getFullName(),
                (account.getUser() != null) ? account.getUser().getUserId() : null,
                account.getAccountType(),
                account.getStatus(),
                account.getBalance(),
                (account.getCredential() != null) ? account.getCredential().getPin() : null,
                account.getRole(),
                (account.getUser() != null) ? account.getUser().getPhone() : null, // Lấy phone từ User
                (account.getUser() != null) ? account.getUser().getEmail() : null  // Lấy email từ User
        );
    }

    // Getters và Setters
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

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
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

    // Getter và Setter cho phone
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}