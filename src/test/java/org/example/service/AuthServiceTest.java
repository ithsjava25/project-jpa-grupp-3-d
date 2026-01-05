package org.example.service;

import org.example.auth.AuthService;
import org.example.auth.PasswordEncoder;
import org.example.entity.user.UserDTO;
import org.example.entity.user.User;
import org.example.exception.AuthenticationException;
import org.example.exception.ValidationException;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    private UserRepository userRepository;
    private ValidationService validationService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        validationService = mock(ValidationService.class);
        authService = new AuthService(userRepository, validationService);
    }

    @Test
    void testAuthenticateUserSuccess() {
        String email = "user@email.com";
        String rawPassword = "password";
        User user = new User();
        user.setEmail(email);
        user.setId(java.util.UUID.randomUUID());
        user.setPassword(PasswordEncoder.hash(rawPassword));

        doNothing().when(validationService).validateNotEmpty("email", email);
        doNothing().when(validationService).validateNotEmpty("password", rawPassword);
        doNothing().when(validationService).validateEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDTO dto = authService.authenticate(email, rawPassword);

        assertEquals(email, dto.email());
        verify(validationService).validateEmail(email);
        verify(validationService).validateNotEmpty("email", email);
        verify(validationService).validateNotEmpty("password", rawPassword);
    }

    @Test
    void testAuthenticateUserInvalidPassword() {
        String email = "user@email.com";
        User user = new User();
        user.setEmail(email);
        user.setId(java.util.UUID.randomUUID());
        user.setPassword(PasswordEncoder.hash("correctpassword"));

        doNothing().when(validationService).validateNotEmpty("email", email);
        doNothing().when(validationService).validateNotEmpty("password", "wrongpass");
        doNothing().when(validationService).validateEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Exception exception = assertThrows(AuthenticationException.class,
            () -> authService.authenticate(email, "wrongpass"));

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void testAuthenticateUserNotFound() {
        String email = "unknown@email.com";

        doNothing().when(validationService).validateNotEmpty("email", email);
        doNothing().when(validationService).validateNotEmpty("password", "password");
        doNothing().when(validationService).validateEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Exception exception = assertThrows(AuthenticationException.class,
            () -> authService.authenticate(email, "password"));

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void testAuthenticateUserInvalidEmailFormat() {
        String invalidEmail = "not-an-email";
        String password = "password";

        doNothing().when(validationService).validateNotEmpty("email", invalidEmail);
        doNothing().when(validationService).validateNotEmpty("password", password);
        doThrow(new ValidationException("email", "Invalid email format"))
            .when(validationService).validateEmail(invalidEmail);

        Exception exception = assertThrows(ValidationException.class,
            () -> authService.authenticate(invalidEmail, password));

        assertEquals("email", ((ValidationException) exception).getFieldName());
    }
}
