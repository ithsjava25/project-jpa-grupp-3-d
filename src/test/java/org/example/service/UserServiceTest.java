package org.example.service;

import org.example.entity.user.CreateUserDTO;
import org.example.entity.user.UserDTO;
import org.example.entity.user.User;
import org.example.exception.ValidationException;
import org.example.exception.BusinessRuleException;
import org.example.exception.EntityNotFoundException;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserRepository userRepository;
    private ValidationService validationService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        validationService = mock(ValidationService.class);
        userService = new UserService(userRepository, validationService);
    }

    @Test
    void testRegisterUser() {
        String email = "test2@email.com";

        doNothing().when(validationService).validateEmail(email);
        doNothing().when(validationService).validatePassword("password");
        doNothing().when(validationService).validatePersonName("firstName", "test");
        doNothing().when(validationService).validatePersonName("lastName", "test");

        when(userRepository.existsByEmail(email)).thenReturn(false);

        UserDTO userDTO = userService.register(
            new CreateUserDTO(
                "test", "test", email, "password"
            )
        );

        assertNotNull(userDTO);
        assertEquals(email, userDTO.email());
        assertEquals("test", userDTO.firstName());

        verify(userRepository, times(1)).create(any());
        verify(validationService).validateEmail(email);
        verify(validationService).validatePassword("password");
    }


    @Test
    void testRegisterUserEmailAlreadyExists() {
        String email = "exists@email.com";

        doNothing().when(validationService).validateEmail(email);
        doNothing().when(validationService).validatePassword("pass");
        doNothing().when(validationService).validatePersonName("firstName", "test");
        doNothing().when(validationService).validatePersonName("lastName", "test");

        when(userRepository.existsByEmail(email)).thenReturn(true);

        Exception exception = assertThrows(BusinessRuleException.class, () -> {
            userService.register(
                new CreateUserDTO(
                    "test", "test", email, "pass"
                ));
        });

        assertTrue(exception.getMessage().contains("Email already registered"));
        verify(userRepository, never()).create(any());
    }

    @Test
    void testRegisterUserWithInvalidEmail() {
        String invalidEmail = "invalid-email";

        doThrow(new ValidationException("email", "Invalid email format"))
            .when(validationService).validateEmail(invalidEmail);

        Exception exception = assertThrows(ValidationException.class, () -> {
            userService.register(
                new CreateUserDTO(
                    "John", "Doe", invalidEmail, "password123"
                ));
        });

        assertEquals("email", ((ValidationException) exception).getFieldName());
        verify(userRepository, never()).create(any());
    }

    @Test
    void testRegisterUserWithInvalidPassword() {
        String email = "test@email.com";
        String shortPassword = "123";

        doNothing().when(validationService).validateEmail(email);
        doNothing().when(validationService).validatePersonName("firstName", "John");
        doNothing().when(validationService).validatePersonName("lastName", "Doe");
        doThrow(new ValidationException("password", "Password must be at least 8 characters"))
            .when(validationService).validatePassword(shortPassword);

        Exception exception = assertThrows(ValidationException.class, () -> {
            userService.register(
                new CreateUserDTO(
                    "John", "Doe", email, shortPassword
                ));
        });

        assertEquals("password", ((ValidationException) exception).getFieldName());
        verify(userRepository, never()).create(any());
    }


    @Test
    void testRegisterUserWithEncryptedPassword() {
        when(userRepository.existsByEmail(any())).thenReturn(false);

        UserDTO dto = userService.register(
            new CreateUserDTO(
                "test", "test", "test@email.com", "password"
            )
        );

        verify(userRepository).create(argThat(user ->
            !user.getPassword().equals("password")
        ));
    }

    @Test
    void testDeleteUser() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        doNothing().when(validationService).validateNotNull("userId", userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userRepository, times(1)).delete(user);
        verify(validationService).validateNotNull("userId", userId);
    }


    @Test
    void testDeleteUserNotFound() {
        UUID userId = UUID.randomUUID();

        doNothing().when(validationService).validateNotNull("userId", userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
            () -> userService.deleteUser(userId));

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository, never()).delete(any());
    }
}
