package org.example.service;

import org.example.auth.PasswordEncoder;
import org.example.dto.UserDTO;
import org.example.entity.User;
import org.example.repository.UserRepository;

import java.util.UUID;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO register(String firstName, String lastName, String email, String password) {
        boolean emailValid = email != null && email.matches(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?(\\.[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?)*\\.[A-Za-z]{2,}$"
        );
        boolean passwordValid = password != null && password.length() >= 8;

        if (!emailValid || userRepository.existsByEmail(email)) {
            // Generic error message to prevent user enumeration
            throw new IllegalArgumentException("Invalid registration data");
        }

        if (!passwordValid) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(PasswordEncoder.hash(password));

        userRepository.create(user);
        return toDto(user);
    }

    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        userRepository.delete(user);
    }

    public UserDTO toDto(User user) {
        return UserDTO.builder()
            .id(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}
