package org.example.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record CompanyDTO(
        UUID id,
        String orgNum,
        String email,
        String phoneNumber,
        String name,
        String address,
        String city,
        String country,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}
