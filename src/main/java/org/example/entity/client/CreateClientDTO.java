package org.example.entity.client;

import java.util.UUID;

public record CreateClientDTO(
    UUID companyId,
    String firstName,
    String lastName,
    String email,
    String address,
    String country,
    String city,
    String phoneNumber
){
}
