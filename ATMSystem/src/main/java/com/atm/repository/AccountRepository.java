package com.atm.repository;

import com.atm.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {

    Optional<Account> findByAccountNumber(String accountNumber);

    // Thêm phương thức kiểm tra tài khoản và mật khẩu
    Optional<Account> findByAccountNumberAndPassword(String accountNumber, String password);
}