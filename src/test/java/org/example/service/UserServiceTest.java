package org.example.service;

import org.example.entity.user.CreateUserDTO;
import org.example.entity.user.UserDTO;
import org.example.entity.user.User;
import org.example.exception.BusinessRuleException;
import org.example.exception.EntityNotFoundException;
import org.example.exception.ValidationException;
import org.example.repository.CompanyUserRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserRepository userRepository;
    private CompanyUserRepository companyUserRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        companyUserRepository = mock(CompanyUserRepository.class);
        userService = new UserService(userRepository, companyUserRepository);
    }

    @Test
    void testRegisterUser() {
        String email = "test2@email.com";
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
    }

    @Test
    void testRegisterUserEmailAlreadyExists() {
        String email = "exists@email.com";

        when(userRepository.existsByEmail(email)).thenReturn(true);

        Exception exception = assertThrows(BusinessRuleException.class, () -> {
            userService.register(
                new CreateUserDTO(
                    "test", "test", email, "password123"
                ));
        });

        assertEquals("Invalid registration data", exception.getMessage());
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

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void testDeleteUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
            () -> userService.deleteUser(userId));

        assertEquals("User not found with identifier: " + userId, exception.getMessage());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void testRegisterUserWithInvalidEmail() {
        String invalidEmail = "invalid-email";

        Exception exception = assertThrows(ValidationException.class, () -> {
            userService.register(
                new CreateUserDTO(
                    "John", "Doe", invalidEmail, "password123"
                ));
        });

        assertTrue(exception.getMessage().contains("email: Email must be a valid email address"));
        verify(userRepository, never()).create(any());
    }

    @Test
    void testRegisterUserWithInvalidPassword() {
        String email = "test@email.com";
        String shortPassword = "123";

        Exception exception = assertThrows(ValidationException.class, () -> {
            userService.register(
                new CreateUserDTO(
                    "John", "Doe", email, shortPassword
                ));
        });

        assertTrue(exception.getMessage().contains("password: Password must be at least 8 characters"));
        verify(userRepository, never()).create(any());
    }
}
