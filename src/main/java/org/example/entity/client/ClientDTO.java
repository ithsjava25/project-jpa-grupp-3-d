package org.example.entity.client;

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
    String address,
    String country,
    String city,
    String phoneNumber,
    LocalDateTime createdAt,
    LocalDateTime updatedAt

){


    public static ClientDTO fromEntity(Client client) {
        return ClientDTO.builder()
            .id(client.getId())
            .companyId(client.getCompany().getId())
            .firstName(client.getFirstName())
            .lastName(client.getLastName())
            .email(client.getEmail())
            .address(client.getAddress())
            .country(client.getCountry())
            .city(client.getCity())
            .phoneNumber(client.getPhoneNumber())
            .createdAt(client.getCreatedAt())
            .updatedAt(client.getUpdatedAt())
            .build();
    }
}
