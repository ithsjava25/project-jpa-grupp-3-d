package org.example.entity.company;

import java.util.UUID;

public record UpdateCompanyDTO(
    UUID companyId,
    String email,
    String phoneNumber,
    String name,
    String address,
    String city,
    String country
) {}
