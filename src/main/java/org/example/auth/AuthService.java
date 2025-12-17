package org.example.auth;

import org.example.dto.UserDTO;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.service.UserService;


public class AuthService {
    private final UserRepository userRepository;
    private final UserService userService;

    public AuthService(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public UserDTO authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalStateException("Invalid email or password"));

        if (!PasswordEncoder.matches(password, user.getPassword())) {
            throw new IllegalStateException("Invalid email or password");
        }

        return userService.toDto(user);
    }
}
