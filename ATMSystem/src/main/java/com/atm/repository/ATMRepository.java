package com.atm.repository;

import com.atm.model.ATM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ATMRepository extends JpaRepository<ATM, Long> {
    // Tìm ATM hiện tại (giả sử chỉ có một máy ATM trong hệ thống)
    default ATM findATM() {
        return findById(1L).orElseThrow(() -> new RuntimeException("No ATM found."));
    }

//    // Lấy trạng thái của ATM
//    default String findATMStatus() {
//        return findATM().getStatus();
//    }
}