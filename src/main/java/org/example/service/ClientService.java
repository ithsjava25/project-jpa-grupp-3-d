package org.example.service;

import org.example.dto.ClientDTO;
import org.example.entity.Client;
import org.example.entity.Company;
import org.example.repository.ClientRepository;
import org.example.repository.CompanyRepository;

import java.util.List;
import java.util.UUID;

public class ClientService {
    private final ClientRepository clientRepository;
    private final CompanyRepository companyRepository;

    public ClientService(ClientRepository clientRepository, CompanyRepository companyRepository) {
        this.clientRepository = clientRepository;
        this.companyRepository = companyRepository;
    }

    public List<ClientDTO> getClientsByCompany(UUID companyId) {
        List<Client> clients = clientRepository.findByCompanyId(companyId);
        return clients.stream()
            .map(this::toDto)
            .toList();
    }

    public ClientDTO createClient(UUID companyId,
                                   String firstName,
                                   String lastName,
                                   String email,
                                   String address,
                                   String city,
                                   String country,
                                   String phoneNumber
                     ) {

        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + companyId));

        Client client = new Client();
        client.setCompany(company);
        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setEmail(email);
        client.setAddress(address);
        client.setCity(city);
        client.setCountry(country);
        client.setPhoneNumber(phoneNumber);

        clientRepository.create(client);

        return toDto(client);
    }


    public void deleteClient(UUID clientId) {
        Client client = clientRepository.findById(clientId)
            .orElseThrow(() -> new IllegalArgumentException("Client not found with id: " + clientId));

        clientRepository.delete(client);
    }



    public ClientDTO update(
        UUID clientId,
        String firstName,
        String lastName,
        String email,
        String address,
        String city,
        String country,
        String phoneNumber) {

        Client client = clientRepository.findById(clientId)
            .orElseThrow(() -> new IllegalArgumentException("Client not found with id: " + clientId));


        if (firstName != null) client.setFirstName(firstName);
        if (lastName != null) client.setLastName(lastName);
        if (email != null) client.setEmail(email);
        if (address != null) client.setAddress(address);
        if (city != null) client.setCity(city);
        if (country != null) client.setCountry(country);
        if (phoneNumber != null) client.setPhoneNumber(phoneNumber);

        clientRepository.update(client);
        return toDto(client);
    }

    public ClientDTO toDto(Client client) {
        return ClientDTO.builder()
            .id(client.getId())
            .companyId(client.getCompany().getId())
            .firstName(client.getFirstName())
            .lastName(client.getLastName())
            .email(client.getEmail())
            .address(client.getAddress())
            .city(client.getCity())
            .country(client.getCountry())
            .phoneNumber(client.getPhoneNumber())
            .createdAt(client.getCreatedAt())
            .updatedAt(client.getUpdatedAt())
            .build();
    }
}
