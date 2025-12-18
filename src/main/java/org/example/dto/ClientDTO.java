package org.example.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ClientDTO (
    UUID id,
    UUID companyId,
    String firstName,
    String lastName,
    String email,
    String adress,
    String country,
    String city,
    String phoneNumber,
    LocalDateTime createdAt,
    LocalDateTime updatedAt

){

}
