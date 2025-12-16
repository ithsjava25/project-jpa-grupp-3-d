package org.example.dto;

import lombok.Builder;

import java.util.UUID;


@Builder
public record UserDTO(
    UUID id,
    String firstName,
    String lastName,
    String email
) {
}
