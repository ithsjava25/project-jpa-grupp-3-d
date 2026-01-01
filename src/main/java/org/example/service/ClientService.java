package org.example.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.dto.ClientDTO;
import org.example.entity.Client;
import org.example.entity.Company;
import org.example.repository.ClientRepository;
import org.example.repository.CompanyRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ClientService {
    private final ClientRepository clientRepository;
    private final CompanyRepository companyRepository;

    public ClientService(ClientRepository clientRepository, CompanyRepository companyRepository) {
        this.clientRepository = clientRepository;
        this.companyRepository = companyRepository;
    }

    public Optional<Client> findById(UUID clientId) {
        return clientRepository.findById(clientId);
    }

    public List<ClientDTO> getClientsByCompany(UUID companyId) {
        return clientRepository.findByCompanyId(companyId).stream()
            .map(ClientDTO::fromEntity)
            .toList();
    }

    public ClientDTO createClient(UUID companyId,
                                  String firstName,
                                  String lastName,
                                  String email,
                                  String address,
                                  String city,
                                  String country,
                                  String phoneNumber) {

        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + companyId));

        Client client = Client.builder()
            .company(company)
            .firstName(firstName)
            .lastName(lastName)
            .email(email)
            .address(address)
            .city(city)
            .country(country)
            .phoneNumber(phoneNumber)
            .build();

        clientRepository.create(client);

        return ClientDTO.fromEntity(client);
    }

    public ClientDTO updateClient(UUID clientId,
                                  String firstName,
                                  String lastName,
                                  String email,
                                  String address,
                                  String city,
                                  String country,
                                  String phoneNumber) {

        Client client = clientRepository.findById(clientId)
            .orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + clientId));

        if (firstName != null) client.setFirstName(firstName);
        if (lastName != null) client.setLastName(lastName);
        if (email != null) client.setEmail(email);
        if (address != null) client.setAddress(address);
        if (city != null) client.setCity(city);
        if (country != null) client.setCountry(country);
        if (phoneNumber != null) client.setPhoneNumber(phoneNumber);

        clientRepository.update(client);
        return ClientDTO.fromEntity(client);
    }

    public void deleteClient(UUID clientId) {
        Client client = clientRepository.findById(clientId)
            .orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + clientId));

        clientRepository.delete(client);
    }
}
