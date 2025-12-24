package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.auth.PasswordEncoder;
import org.example.dto.UserDTO;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.util.LogUtil;

import java.util.UUID;


@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO register(String firstName, String lastName, String email, String password) {

        log.debug("User registration started for email={}", LogUtil.maskEmail(email));

        boolean emailValid = email != null && email.matches(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?(\\.[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?)*\\.[A-Za-z]{2,}$"
        );
        boolean passwordValid = password != null && password.length() >= 8;

        if (!emailValid) {
            log.debug("Registration failed: invalid email format for email={}", LogUtil.maskEmail(email));
            log.warn("User registration failed due to invalid input");
            throw new IllegalArgumentException("Invalid registration data");
        }

        if (userRepository.existsByEmail(email)) {
            log.debug("Registration failed: email already exists for email={}", LogUtil.maskEmail(email));
            log.warn("User registration failed due to invalid input");
            throw new IllegalArgumentException("Invalid registration data");
        }

        if (!passwordValid) {
            log.debug("Registration failed: password validation failed");
            log.warn("User registration failed due to invalid input");
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(PasswordEncoder.hash(password));

        userRepository.create(user);

        log.info("User registered successfully with id={}", user.getId());
        return toDto(user);
    }


    public void deleteUser(UUID userId) {

        log.debug("User deletion requested for userId={}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.warn("User deletion failed: user not found for userId={}", userId);
                return new IllegalArgumentException("User not found");
            });

        userRepository.delete(user);
        log.info("User deleted successfully with userId={}", userId);
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
