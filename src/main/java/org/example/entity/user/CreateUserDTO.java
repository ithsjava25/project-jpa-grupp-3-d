package org.example.entity.user;

public record CreateUserDTO(
    String firstName,
    String lastName,
    String email,
    String password
) {
}
