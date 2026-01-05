package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.auth.PasswordEncoder;
import org.example.entity.user.CreateUserDTO;
import org.example.entity.user.UserDTO;
import org.example.entity.user.User;
import org.example.repository.UserRepository;
import org.example.util.LogUtil;

import java.util.UUID;


@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO register(CreateUserDTO dto) {

        log.debug("User registration started for email={}", LogUtil.maskEmail(dto.email()));

        boolean emailValid = dto.email() != null && dto.email().matches(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?(\\.[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?)*\\.[A-Za-z]{2,}$"
        );
        boolean passwordValid = dto.password() != null && dto.password().length() >= 8;

        if (!emailValid) {
            log.debug("Registration failed: invalid email format for email={}", LogUtil.maskEmail(dto.email()));
            log.warn("User registration failed due to invalid input");
            throw new IllegalArgumentException("Invalid registration data");
        }

        if (userRepository.existsByEmail(dto.email())) {
            log.debug("Registration failed: email already exists for email={}", LogUtil.maskEmail(dto.email()));
            log.warn("User registration failed due to invalid input");
            throw new IllegalArgumentException("Invalid registration data");
        }

        if (!passwordValid) {
            log.debug("Registration failed: password validation failed");
            log.warn("User registration failed due to invalid input");
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        User user = User.fromDTO(dto);
        user.setPassword(PasswordEncoder.hash(dto.password()));
        userRepository.create(user);

        log.info("User registered successfully with id={}", user.getId());
        return UserDTO.fromEntity(user);
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
}
