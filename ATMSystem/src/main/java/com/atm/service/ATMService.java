package com.atm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.atm.model.ATM;
import com.atm.repository.ATMRepository;

@Service
public class ATMService {
    private final ATMRepository atmRepository;

    @Autowired
    public ATMService(ATMRepository atmRepository) {
        this.atmRepository = atmRepository;
    }

//    // Kiểm tra trạng thái của máy ATM
//    public String checkATMStatus() {
//        return atmRepository.findATMStatus(); // Giả sử hàm này trả về trạng thái của máy ATM
//    }

//    // Cập nhật trạng thái máy ATM
//    public void updateATMStatus(String new_status) {
//        ATM atm = atmRepository.findATM();
//        atm.setStatus(new_status); // Cập nhật trạng thái mới
//        atmRepository.save(atm); // Lưu thay đổi vào cơ sở dữ liệu
//    }
//
//    // Kiểm tra xem máy ATM có đủ tiền mặt hay không
//    public boolean checkCashAvailability() {
//        ATM atm = atmRepository.findATM();
//        return atm.getTotalCash() > 0; // Kiểm tra tổng tiền mặt trong máy ATM
//    }

    // Nạp tiền vào máy ATM
    public void refillCash(int cash_500, int cash_200, int cash_100, int cash_50) {
        ATM atm = atmRepository.findATM();
        atm.setCash500(atm.getCash500() + cash_500);
        atm.setCash200(atm.getCash200() + cash_200);
        atm.setCash100(atm.getCash100() + cash_100);
        atm.setCash50(atm.getCash50() + cash_50);
        atmRepository.save(atm); // Lưu thông tin mới vào cơ sở dữ liệu
    }
}