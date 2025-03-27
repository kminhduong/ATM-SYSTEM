package com.atm.repository;

import com.atm.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    Optional<Account> findByAccountNumber(String accountNumber);

    // Thêm phương thức kiểm tra tài khoản và mật khẩu
    Optional<Account> findByAccountNumberAndPassword(String accountNumber, String password);
}