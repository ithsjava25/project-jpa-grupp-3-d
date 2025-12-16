package org.example.dto;

import lombok.Builder;

import java.util.UUID;


@Builder
public record UserDTO(
    UUID id,
    String first_name,
    String last_name,
    String email
) {
}
