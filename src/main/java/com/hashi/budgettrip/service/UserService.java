package com.hashi.budgettrip.service;

import com.hashi.budgettrip.model.User;
import com.hashi.budgettrip.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Signup: hash password and save
    public User registerUser(User user) {
        String hashed = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashed);
        return userRepository.save(user);
    }

    // Login: check email and password
    public User loginUser(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(u -> BCrypt.checkpw(password, u.getPassword()))
                .orElse(null); // null if invalid credentials
    }
}
