package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.auth.PasswordEncoder;
import org.example.entity.user.CreateUserDTO;
import org.example.entity.user.UserDTO;
import org.example.entity.user.User;
import org.example.exception.BusinessRuleException;
import org.example.exception.EntityNotFoundException;
import org.example.repository.UserRepository;
import org.example.util.LogUtil;

import java.util.UUID;

@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ValidationService validationService;

    public UserService(UserRepository userRepository, ValidationService validationService) {
        this.userRepository = userRepository;
        this.validationService = validationService;
    }

    public UserDTO register(CreateUserDTO dto) {

        log.debug("User registration started for email={}", LogUtil.maskEmail(dto.email()));

        validationService.validateEmail(dto.email());
        validationService.validatePassword(dto.password());
        validationService.validatePersonName("firstName", dto.firstName());
        validationService.validatePersonName("lastName", dto.lastName());

        if (userRepository.existsByEmail(dto.email())) {
            log.debug("Registration failed: email already exists for email={}", LogUtil.maskEmail(dto.email()));
            throw new BusinessRuleException("Email already registered: " + dto.email(), "EMAIL_ALREADY_EXISTS");
        }

        User user = User.fromDTO(dto);
        user.setPassword(PasswordEncoder.hash(dto.password()));
        userRepository.create(user);

        log.info("User registered successfully with id={}", user.getId());
        return UserDTO.fromEntity(user);
    }

    public void deleteUser(UUID userId) {
        log.debug("User deletion requested for userId={}", userId);

        validationService.validateNotNull("userId", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.warn("User deletion failed: user not found for userId={}", userId);
                return new EntityNotFoundException("User", userId, "USER_NOT_FOUND");
            });

        userRepository.delete(user);
        log.info("User deleted successfully with userId={}", userId);
    }
}
