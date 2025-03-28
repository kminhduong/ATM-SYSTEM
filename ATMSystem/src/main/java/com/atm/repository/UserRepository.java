package com.atm.repository;

import com.atm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.accounts WHERE u.userId = :userId")
    Optional<User> findByIdWithAccounts(@Param("userId") String userId);

    User findByEmail(String email);  // Tìm user theo email
}
