package com.atm.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user")
public class User {

    @Id
    @Column(name = "user_id", length = 12, updatable = false, nullable = false, unique = true)
    @Pattern(regexp = "\\d{12}", message = "userId phải là 12 số (CCCD)")
    private String userId; // Ràng buộc userId phải là 12 chữ số (CCCD)

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "email", length = 100, unique = true, nullable = false)
    private String email;

    @Column(name = "phone", length = 20, unique = true, nullable = false)
    private String phone;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Account> accounts;

    // No-arg constructor (Hibernate cần)
    public User() {}

    // Constructor yêu cầu nhập userId
    public User(String userId, String name, String email, String phone) {
        this.setUserId(userId);  // Sử dụng setter để kiểm tra userId có phải là 12 số không
        this.name = name.toUpperCase();  // Chuyển tên thành chữ in hoa
        this.email = email;
        this.phone = phone;
        this.createAt = LocalDateTime.now();
    }

    // Getter và Setter
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        // Kiểm tra userId có phải là CCCD với 12 số không
        if (userId != null && !userId.matches("\\d{12}")) {
            throw new IllegalArgumentException("userId phải là 12 số (CCCD)");
        }
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        // Đảm bảo tên viết in hoa
        this.name = name.toUpperCase();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", createAt=" + createAt +
                '}';
    }
}
