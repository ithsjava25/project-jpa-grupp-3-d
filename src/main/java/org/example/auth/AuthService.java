package org.example.auth;

import org.example.entity.User;
import org.example.repository.UserRepository;


public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalStateException("Invalid email or password"));

        if (!PasswordEncoder.matches(password, user.getPassword())) {
            throw new IllegalStateException("Invalid email or password");
        }

        return user;
    }
}
