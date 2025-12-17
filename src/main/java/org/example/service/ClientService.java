package org.example.service;

import org.example.dto.ClientDTO;
import org.example.entity.Client;
import org.example.entity.Company;
import org.example.repository.ClientRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public class ClientService {
    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }


    public ClientDTO create(Company company,
                            String firstName,
                            String lastName,
                            String email,
                            String address,
                            String city,
                            String country) {

        if (company == null) {
            throw new IllegalArgumentException("Company must not be null");
        }

        if (firstName == null || firstName.isBlank()) throw new IllegalArgumentException("First name required");
        if (lastName == null || lastName.isBlank()) throw new IllegalArgumentException("Last name required");
        if (email == null || !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format");
        }

        Client client = new Client();
        client.setCompany(company);
        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setEmail(email);
        client.setAddress(address);
        client.setCity(city);
        client.setCountry(country);
        client.setCreatedAt(LocalDateTime.now());
        client.setUpdatedAt(LocalDateTime.now());

        clientRepository.save(client);
        return toDto(client);
    }

    public ClientDTO update(Client client,
                            String firstName,
                            String lastName,
                            String email,
                            String address,
                            String city,
                            String country) {

        if (firstName != null) client.setFirstName(firstName);
        if (lastName != null) client.setLastName(lastName);
        if (email != null) client.setEmail(email);
        if (address != null) client.setAddress(address);
        if (city != null) client.setCity(city);
        if (country != null) client.setCountry(country);
        client.setUpdatedAt(LocalDateTime.now());

        clientRepository.save(client);
        return toDto(client);
    }

    public void deleteById(UUID clientId) {
        Client client = clientRepository.findById(clientId)
            .orElseThrow(() -> new IllegalStateException("Client not found"));
        clientRepository.delete(client);
    }


    public ClientDTO toDto(Client client) {
        return ClientDTO.builder()
            .companyId(client.getCompany().getId())
            .firstName(client.getFirstName())
            .lastName(client.getLastName())
            .email(client.getEmail())
            .address(client.getAddress())
            .city(client.getCity())
            .country(client.getCountry())
            .createdAt(client.getCreatedAt())
            .updatedAt(client.getUpdatedAt())
            .build();
    }
}
