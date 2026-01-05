package org.example.entity.client;

import java.util.UUID;

public record UpdateClientDTO(
    UUID clientId,
    String firstName,
    String lastName,
    String email,
    String address,
    String country,
    String city,
    String phoneNumber
) {
}
