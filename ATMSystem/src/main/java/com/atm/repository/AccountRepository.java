package com.atm.repository;

import com.atm.model.Account;
import com.atm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    // Tìm Account theo accountNumber
    Optional<Account> findByAccountNumber(String accountNumber);

    // Tìm role của Account theo accountNumber
    @Query("SELECT a.role FROM Account a WHERE a.accountNumber = :accountNumber")
    String findRoleByAccountNumber(@Param("accountNumber") String accountNumber);

    // Tìm Account theo User
    List<Account> findByUser(User user);

    @Query(value = "SELECT a.account_number, a.user_id, a.account_type, a.status, a.last_updated, a.full_name, a.role FROM Account a INNER JOIN User u ON u.user_id = a.user_id WHERE u.user_id = ?1", nativeQuery = true)
    List<Account> findByUserId(String userId);
}
