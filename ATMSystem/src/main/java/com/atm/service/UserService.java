package com.atm.service;

import com.atm.model.User;
import com.atm.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public User getUserById(String userId) {
        return userRepository.findUserWithAccountsByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + userId));
    }
}
