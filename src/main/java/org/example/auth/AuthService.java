package org.example.auth;

import lombok.extern.slf4j.Slf4j;
import org.example.entity.user.UserDTO;
import org.example.entity.user.User;
import org.example.exception.AuthenticationException;
import org.example.service.ValidationService;
import org.example.repository.UserRepository;
import org.example.util.LogUtil;


@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final ValidationService validationService;

    public AuthService(UserRepository userRepository, ValidationService validationService) {
        this.userRepository = userRepository;
        this.validationService = validationService;
    }

    public UserDTO authenticate(String email, String password) {
        validationService.validateNotEmpty("email", email);
        validationService.validateNotEmpty("password", password);
        validationService.validateEmail(email);

        log.debug("Authentication attempt for email: {}", LogUtil.maskEmail(email));

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.debug("Authentication failed: user not found for email={}", LogUtil.maskEmail(email));
                return new AuthenticationException("Invalid email or password", "INVALID_CREDENTIALS");
            });

        if (!PasswordEncoder.matches(password, user.getPassword())) {
            log.debug(
                "Authentication failed: invalid credentials for email={}",
                LogUtil.maskEmail(email)
            );
            throw new AuthenticationException("Invalid email or password", "INVALID_CREDENTIALS");
        }

        log.info("Authentication successful for userId={}", user.getId());
        return UserDTO.fromEntity(user);
    }
}
