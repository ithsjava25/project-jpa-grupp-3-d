package org.example.service;

import org.example.auth.PasswordEncoder;
import org.example.dto.UserDTO;
import org.example.entity.User;
import org.example.repository.UserRepository;

import java.time.LocalDateTime;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO register(String firstName, String lastName, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Email already in use");
        }

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(PasswordEncoder.hash(password));
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);
        return toDto(user);
    }

    public UserDTO toDto(User user) {
        return UserDTO.builder()
            .id(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .build();
    }
}
