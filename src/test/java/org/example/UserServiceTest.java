package org.example;


import org.example.dto.UserDTO;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test
    void testRegisterUser() {
        String email = "test2@email.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);
        UserDTO userDTO = userService.register(
            "test",
            "test",
            email,
            "password"
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

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.register("test", "test", email, "pass");
        });

        assertEquals("Invalid registration data", exception.getMessage());
        verify(userRepository, never()).create(any());
    }

    @Test
    void testRegisterUserWithEncryptedPassword() {
        when(userRepository.existsByEmail(any())).thenReturn(false);

        UserDTO dto = userService.register(
            "test",
            "test",
            "test@email.com",
            "password"
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

        Exception exception = assertThrows(IllegalStateException.class,
            () -> userService.deleteUser(userId));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void testRegisterUserWithInvalidEmail() {
        String invalidEmail = "invalid-email";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.register("John", "Doe", invalidEmail, "password123");
        });

        assertEquals("Invalid registration data", exception.getMessage());
        verify(userRepository, never()).create(any());
    }

    @Test
    void testRegisterUserWithInvalidPassword() {
        String email = "test@email.com";
        String shortPassword = "123"; // too short

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.register("John", "Doe", email, shortPassword);
        });

        assertEquals("Password must be at least 8 characters", exception.getMessage());
        verify(userRepository, never()).create(any());
    }
}
