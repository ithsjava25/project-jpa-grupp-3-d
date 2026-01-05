package org.example.entity.user;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;


@Builder
public record UserDTO(
    UUID id,
    String firstName,
    String lastName,
    String email,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static UserDTO fromEntity (User user) {
        return UserDTO.builder()
            .id(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}
