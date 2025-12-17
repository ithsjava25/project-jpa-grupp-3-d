package org.example.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ClientDTO (
    String company,
    String firstName,
    String lastName,
    String email,
    String adress,
    String country,
    String city,
    LocalDateTime createdAt,
    LocalDateTime updatedAt

){

}
