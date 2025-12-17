package org.example.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ClientDTO (
    UUID companyId,
    String firstName,
    String lastName,
    String email,
    String address,
    String country,
    String city,
    LocalDateTime createdAt,
    LocalDateTime updatedAt

){

}
