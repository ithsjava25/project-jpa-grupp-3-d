package org.example.entity.company;

public record CreateCompanyDTO(
    String orgNum,
    String email,
    String phoneNumber,
    String name,
    String address,
    String city,
    String country
) {}
