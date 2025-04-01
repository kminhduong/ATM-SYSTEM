package com.atm.repository;

import com.atm.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountNumber(String accountNumber);

    @Query(value = "SELECT t.transaction_id, t.atm_id, t.account_number, t.type, t.amount, t.create_at FROM Transaction t INNER JOIN Account ac ON ac.account_number = t.account_number INNER JOIN User u ON u.user_id = ac.user_id WHERE u.user_id = ?1",
    nativeQuery = true)
    List<Transaction> findByUserId(String userId);
}
