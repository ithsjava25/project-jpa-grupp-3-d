package org.example.dto;

import org.example.entity.Company;

import java.time.LocalDateTime;
import java.util.UUID;

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
) {
    public static CompanyDTO fromEntity(Company company) {
        return new CompanyDTO(
            company.getId(),
            company.getOrgNum(),
            company.getEmail(),
            company.getPhoneNumber(),
            company.getName(),
            company.getAddress(),
            company.getCity(),
            company.getCountry(),
            company.getCreatedAt(),
            company.getUpdatedAt()
        );
    }
}
