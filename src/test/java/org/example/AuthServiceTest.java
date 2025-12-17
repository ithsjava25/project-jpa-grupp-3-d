package org.example;

import org.example.auth.AuthService;
import org.example.dto.UserDTO;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    private UserRepository userRepository;
    private UserService userService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
        authService = new AuthService(userRepository, userService);
    }

    @Test
    void testAuthenticateUserSuccess() {
        String email = "user@email.com";
        String rawPassword = "password";
        User user = new User();
        user.setEmail(email);
        user.setPassword(org.example.auth.PasswordEncoder.hash(rawPassword));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDTO dto = authService.authenticate(email, rawPassword);

        assertEquals(email, dto.email());
    }

    @Test
    void testAuthenticateUserInvalidPassword() {
        String email = "user@email.com";
        User user = new User();
        user.setEmail(email);
        user.setPassword(org.example.auth.PasswordEncoder.hash("password"));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Exception exception = assertThrows(IllegalStateException.class,
            () -> authService.authenticate(email, "wrongpass"));

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void testAuthenticateUserNotFound() {
        String email = "unknown@email.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalStateException.class,
            () -> authService.authenticate(email, "password"));

        assertEquals("Invalid email or password", exception.getMessage());
    }
}
