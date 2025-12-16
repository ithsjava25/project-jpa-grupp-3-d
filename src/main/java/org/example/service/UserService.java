package org.example.service;

import org.example.dto.UserDTO;
import org.example.entity.User;
import org.example.repository.UserRepository;

import java.time.LocalDateTime;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO create(String firstName, String lastName, String email, String password, String ssn) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Email already in use");
        }
        if (userRepository.existsBySsn(ssn)) {
            throw new IllegalStateException("SSN already in use");
        }

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(password);
        user.setSsn(ssn);
        user.setCreated_at(LocalDateTime.now());

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
