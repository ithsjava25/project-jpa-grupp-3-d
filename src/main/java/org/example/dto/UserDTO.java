package org.example.dto;

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
}
