package com.atm.repository;

import com.atm.model.Account;
import com.atm.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    Optional<Account> findByAccountNumber(String accountNumber);

    Optional<Account> findByAccountNumberAndPassword(String accountNumber, String password);

    @Query("SELECT a.role FROM Account a WHERE a.accountNumber = :accountNumber")
    String findRoleByAccountNumber(@Param("accountNumber") String accountNumber);

    // ✅ Thêm phương thức tìm account theo User
    List<Account> findByUser(User user);
}

