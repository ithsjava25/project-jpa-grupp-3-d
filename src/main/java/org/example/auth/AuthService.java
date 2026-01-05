package org.example.auth;

import lombok.extern.slf4j.Slf4j;
import org.example.entity.user.UserDTO;
import org.example.entity.user.User;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.example.util.LogUtil;


@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final UserService userService;

    public AuthService(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public UserDTO authenticate(String email, String password) {

        log.debug("Authentication attempt for email: {}", LogUtil.maskEmail(email));

        User user = userRepository.findByEmail(email)

            .orElseThrow(() -> {
                log.debug("Authentication failed: user not found for email={}", LogUtil.maskEmail(email));
                return new IllegalStateException("Invalid email or password");
            });

        if (!PasswordEncoder.matches(password, user.getPassword())) {
            log.debug(
                "Authentication failed: invalid credentials for email={}",
                LogUtil.maskEmail(email)
            );
            throw new IllegalStateException("Invalid email or password");
        }

        log.info("Authentication successful for userId={}", user.getId());
        return UserDTO.fromEntity(user);
    }
}
