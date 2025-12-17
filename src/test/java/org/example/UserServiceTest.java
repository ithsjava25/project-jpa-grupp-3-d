package org.example;


import org.example.dto.UserDTO;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


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

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void testRegisterUserEmailAlreadyExists() {
        String email = "exists@email.com";

        when(userRepository.existsByEmail(email)).thenReturn(true);

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            userService.register("test", "test", email, "pass");
        });

        assertEquals("Email already in use", exception.getMessage());
        verify(userRepository, never()).save(any());
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

        verify(userRepository).save(argThat(user ->
            !user.getPassword().equals("password")
        ));
    }

}
