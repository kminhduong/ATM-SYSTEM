package com.atm.dto;

import com.atm.model.Account;
import com.atm.model.AccountType;
import com.atm.model.AccountStatus;
import com.atm.model.User;
import com.atm.repository.UserRepository;

public class AccountDTO {

    private String accountNumber;
    private String username;
    private String fullName;
    private String userId;
    private AccountType accountType;
    private AccountStatus status;
    private Double balance;
    private String pin;
    private String role;
    private String phoneNumber;

    // Constructor không tham số
    public AccountDTO() {
    }

    // Constructor đầy đủ
    public AccountDTO(String accountNumber, String username, String fullName, String userId,
                      AccountType accountType, AccountStatus status, Double balance, String pin, String role) {
        this.accountNumber = accountNumber;
        this.username = username;
        this.fullName = fullName;
        this.userId = userId;
        this.accountType = accountType;
        this.status = status;
        this.balance = balance;
        this.pin = pin;
        this.role = role;
    }


    // Chuyển từ DTO thành Entity
    public Account toAccount(UserRepository userRepository) {
        // Đảm bảo userId hợp lệ và user tồn tại, hoặc log khi không tìm thấy
        User user = (this.userId != null) ? userRepository.findById(this.userId).orElse(null) : null;

        // Trả về một đối tượng Account, sử dụng giá trị mặc định khi cần thiết
        return new Account(
                this.accountNumber != null ? this.accountNumber : "Unknown", // Giá trị mặc định cho accountNumber
                this.username != null ? this.username : "DefaultUsername",   // Giá trị mặc định cho username
                this.fullName != null ? this.fullName : "Unknown Name",      // Giá trị mặc định cho fullName
                user,  // Chuyển đổi userId thành User
                this.accountType != null ? this.accountType : AccountType.SAVINGS,  // Giá trị mặc định cho accountType
                this.status != null ? this.status : AccountStatus.ACTIVE,    // Giá trị mặc định cho status
                this.balance != null ? this.balance : 0.0,  // Giá trị mặc định cho balance
                this.pin != null ? this.pin : "000000",     // Giá trị mặc định cho pin
                this.role != null ? this.role : "USER"       // Giá trị mặc định cho role
        );
    }

    // Chuyển từ Entity thành DTO
    public static AccountDTO fromAccount(Account account) {
        return new AccountDTO(
                account.getAccountNumber(),
                account.getUsername(),
                account.getFullName(),
                (account.getUser() != null) ? account.getUser().getUserId() : null,  // Sửa từ getId() thành getUserId()
                account.getAccountType(),
                account.getStatus(),
                account.getBalance(),
                (account.getCredential() != null) ? account.getCredential().getPin() : null,  // Lấy pin từ Credential
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}