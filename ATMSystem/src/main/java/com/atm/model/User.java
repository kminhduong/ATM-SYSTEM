package com.atm.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user")
public class User {

    @Id
    @Column(name = "user_id", length = 50, updatable = false, nullable = false)
    private String userId; // Nếu cần dùng UUID, đổi sang UUID userId;

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

    // ✅ No-arg constructor (Hibernate cần)
    public User() {}

    // ✅ Constructor tự động tạo userId
    public User(String name, String email, String phone) {
        this.userId = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.createAt = LocalDateTime.now();
    }

    // ✅ Getters và Setters
    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    private String id;

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
