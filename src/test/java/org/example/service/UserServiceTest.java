package org.example.service;

import org.example.entity.user.CreateUserDTO;
import org.example.entity.user.UserDTO;
import org.example.entity.user.User;
import org.example.exception.ValidationException;
import org.example.exception.BusinessRuleException;
import org.example.exception.EntityNotFoundException;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ValidationService validationService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should register user successfully with valid input")
    void registerUser_Success() {
        String email = "test2@email.com";

        doNothing().when(validationService).validateEmail(email);
        doNothing().when(validationService).validatePassword("password");
        doNothing().when(validationService).validatePersonName("firstName", "test");
        doNothing().when(validationService).validatePersonName("lastName", "test");

        when(userRepository.existsByEmail(email)).thenReturn(false);

        UserDTO userDTO = userService.register(
            new CreateUserDTO("test", "test", email, "password")
        );

        assertNotNull(userDTO);
        assertEquals(email, userDTO.email());
        assertEquals("test", userDTO.firstName());

        verify(userRepository).create(any(User.class));
        verify(validationService).validateEmail(email);
        verify(validationService).validatePassword("password");
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when email already exists")
    void registerUser_EmailAlreadyExists_ThrowsBusinessRuleException() {
        String email = "exists@email.com";

        doNothing().when(validationService).validateEmail(email);
        doNothing().when(validationService).validatePassword("pass");
        doNothing().when(validationService).validatePersonName("firstName", "test");
        doNothing().when(validationService).validatePersonName("lastName", "test");

        when(userRepository.existsByEmail(email)).thenReturn(true);

        Exception exception = assertThrows(BusinessRuleException.class, () -> {
            userService.register(new CreateUserDTO("test", "test", email, "pass"));
        });

        assertTrue(exception.getMessage().contains("Email already registered"));
        verify(userRepository, never()).create(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when email format is invalid")
    void registerUser_InvalidEmail_ThrowsValidationException() {
        String invalidEmail = "invalid-email";

        doThrow(new ValidationException("email", "Invalid email format"))
            .when(validationService).validateEmail(invalidEmail);

        Exception exception = assertThrows(ValidationException.class, () -> {
            userService.register(new CreateUserDTO("John", "Doe", invalidEmail, "password123"));
        });

        assertEquals("email", ((ValidationException) exception).getFieldName());
        verify(userRepository, never()).create(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when password is too short")
    void registerUser_ShortPassword_ThrowsValidationException() {
        String email = "test@email.com";
        String shortPassword = "123";

        doNothing().when(validationService).validateEmail(email);
        doThrow(new ValidationException("password", "Password must be at least 8 characters"))
            .when(validationService).validatePassword(shortPassword);

        Exception exception = assertThrows(ValidationException.class, () -> {
            userService.register(new CreateUserDTO("John", "Doe", email, shortPassword));
        });

        assertEquals("password", ((ValidationException) exception).getFieldName());
        verify(userRepository, never()).create(any());
    }

    @Test
    @DisplayName("Should register user with encrypted password")
    void registerUser_EncryptedPassword_Success() {
        String email = "test@email.com";

        doNothing().when(validationService).validateEmail(email);
        doNothing().when(validationService).validatePassword("password");
        doNothing().when(validationService).validatePersonName("firstName", "test");
        doNothing().when(validationService).validatePersonName("lastName", "test");

        when(userRepository.existsByEmail(email)).thenReturn(false);

        userService.register(new CreateUserDTO("test", "test", email, "password"));

        verify(userRepository).create(argThat(user ->
            !user.getPassword().equals("password")
        ));
    }

    @Test
    @DisplayName("Should delete user successfully when user exists")
    void deleteUser_Success() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        doNothing().when(validationService).validateNotNull("userId", userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userRepository).delete(user);
        verify(validationService).validateNotNull("userId", userId);
    }


    @Test
    @DisplayName("Should throw EntityNotFoundException when deleting non-existent user")
    void deleteUser_UserNotFound_ThrowsEntityNotFoundException() {
        UUID userId = UUID.randomUUID();

        doNothing().when(validationService).validateNotNull("userId", userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
            () -> userService.deleteUser(userId));

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository, never()).delete(any());
    }
}
