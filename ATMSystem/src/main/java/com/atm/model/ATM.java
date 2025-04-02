package com.atm.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "ATM")
public class ATM {

    @Id
    @Column(name = "atm_id", length = 50, updatable = false, nullable = false)
    private String atmId;

    @Column(name = "cash_500", nullable = false)
    private int cash500;

    @Column(name = "cash_200", nullable = false)
    private int cash200;

    @Column(name = "cash_100", nullable = false)
    private int cash100;

    @Column(name = "cash_50", nullable = false)
    private int cash50;

    @Column(name = "total_amount", insertable = false, updatable = false) // Generated column
    private double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ATMStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_updated", nullable = false)
    private Date lastUpdated;

    // No-arg constructor (Hibernate cần)
    public ATM() {
        this.lastUpdated = new Date(); // Mặc định thời gian cập nhật
    }

    // Constructor đầy đủ
    public ATM(int cash500, int cash200, int cash100, int cash50, ATMStatus status) {
        this.cash500 = cash500;
        this.cash200 = cash200;
        this.cash100 = cash100;
        this.cash50 = cash50;
        this.status = status;
        this.lastUpdated = new Date();
    }

    // Method to insert default ATM record
    public static void insertDefaultATM(EntityManager entityManager) {
        ATM defaultATM = new ATM(50, 100, 200, 300, ATMStatus.ACTIVE);
        entityManager.persist(defaultATM);
    }

    // Getters and Setters
    public String getAtmId() {
        return atmId;
    }

    public int getCash500() {
        return cash500;
    }

    public void setCash500(int cash500) {
        this.cash500 = cash500;
    }

    public int getCash200() {
        return cash200;
    }

    public void setCash200(int cash200) {
        this.cash200 = cash200;
    }

    public int getCash100() {
        return cash100;
    }

    public void setCash100(int cash100) {
        this.cash100 = cash100;
    }

    public int getCash50() {
        return cash50;
    }

    public void setCash50(int cash50) {
        this.cash50 = cash50;
    }

    public double getTotalAmount() {
        return totalAmount; // Chỉ đọc từ cơ sở dữ liệu
    }

    public ATMStatus getStatus() {
        return status;
    }

    public void setStatus(ATMStatus status) {
        this.status = status;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = new Date(); // Cập nhật thời gian khi có thay đổi
    }

    @Override
    public String toString() {
        return "ATM{" +
                "atmId='" + atmId + '\'' +
                ", cash500=" + cash500 +
                ", cash200=" + cash200 +
                ", cash100=" + cash100 +
                ", cash50=" + cash50 +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}