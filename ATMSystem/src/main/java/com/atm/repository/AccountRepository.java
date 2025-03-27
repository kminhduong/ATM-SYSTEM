package com.atm.repository;

import com.atm.model.Account;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    Optional<Account> findByAccountNumber(String accountNumber);

    // Thêm phương thức kiểm tra tài khoản và mật khẩu
    Optional<Account> findByAccountNumberAndPassword(String accountNumber, String password);
    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.fullName = :fullName WHERE a.accountNumber = :accountNumber")
    void updateFullName(@Param("accountNumber") String accountNumber,
                        @Param("fullName") String fullName);

    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.phoneNumber = :phoneNumber WHERE a.accountNumber = :accountNumber")
    void updatePhoneNumber(@Param("accountNumber") String accountNumber,
                           @Param("phoneNumber") String phoneNumber);

    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.balance = :balance WHERE a.accountNumber = :accountNumber")
    void updateBalance(@Param("accountNumber") String accountNumber,
                       @Param("balance") Double balance);

    @Query("SELECT a.role FROM Account a WHERE a.accountNumber = :accountNumber")
    String findRoleByAccountNumber(@Param("accountNumber") String accountNumber);

}