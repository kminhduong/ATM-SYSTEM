package com.atm.repository;

import com.atm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // Tìm người dùng theo userId và nạp tài khoản của người dùng
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.accounts WHERE u.userId = :userId")
    Optional<User> findUserWithAccountsByUserId(@Param("userId") String userId);

    // Tìm người dùng theo email
    User findByEmail(String email);

    // Tìm người dùng theo userId
    Optional<User> findByUserId(String userId);
}
