package org.example.service;

import org.example.entity.client.ClientDTO;
import org.example.entity.client.Client;
import org.example.entity.company.Company;
import org.example.entity.client.CreateClientDTO;
import org.example.entity.client.UpdateClientDTO;
import org.example.exception.EntityNotFoundException;
import org.example.repository.ClientRepository;
import org.example.repository.CompanyRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ClientService {
    private final ClientRepository clientRepository;
    private final CompanyRepository companyRepository;
    private final ValidationService validationService;

    public ClientService(ClientRepository clientRepository,
                         CompanyRepository companyRepository,
                         ValidationService validationService) {
        this.clientRepository = clientRepository;
        this.companyRepository = companyRepository;
        this.validationService = validationService;
    }

    public Optional<Client> findById(UUID clientId) {
        validationService.validateNotNull("clientId", clientId);
        return clientRepository.findById(clientId);
    }

    public List<ClientDTO> getClientsByCompany(UUID companyId) {
        validationService.validateNotNull("companyId", companyId);
        return clientRepository.findByCompanyId(companyId).stream()
            .map(ClientDTO::fromEntity)
            .toList();
    }

    public ClientDTO createClient(CreateClientDTO dto) {
        validationService.validateNotNull("companyId", dto.companyId());
        validationService.validatePersonName("firstName", dto.firstName());
        validationService.validatePersonName("lastName", dto.lastName());
        validationService.validateEmail(dto.email());
        validationService.validateAddress("address", dto.address());
        validationService.validateAddress("city", dto.city());
        validationService.validateAddress("country", dto.country());
        validationService.validatePhoneNumber(dto.phoneNumber());

        Company company = companyRepository.findById(dto.companyId())
            .orElseThrow(() -> new EntityNotFoundException("Company", dto.companyId(), "COMPANY_NOT_FOUND"));

        Client client = Client.fromDTO(dto, company);
        clientRepository.create(client);

        return ClientDTO.fromEntity(client);
    }

    public ClientDTO updateClient(UpdateClientDTO dto) {
        validationService.validateNotNull("clientId", dto.clientId());

        Client client = clientRepository.findById(dto.clientId())
            .orElseThrow(() -> new EntityNotFoundException("Client", dto.clientId(), "CLIENT_NOT_FOUND"));

        if (dto.firstName() != null) {
            validationService.validatePersonName("firstName", dto.firstName());
            client.setFirstName(dto.firstName());
        }
        if (dto.lastName() != null) {
            validationService.validatePersonName("lastName", dto.lastName());
            client.setLastName(dto.lastName());
        }
        if (dto.email() != null) {
            validationService.validateEmail(dto.email());
            client.setEmail(dto.email());
        }
        if (dto.address() != null) {
            validationService.validateAddress("address", dto.address());
            client.setAddress(dto.address());
        }
        if (dto.city() != null) {
            validationService.validateAddress("city", dto.city());
            client.setCity(dto.city());
        }
        if (dto.country() != null) {
            validationService.validateAddress("country", dto.country());
            client.setCountry(dto.country());
        }
        if (dto.phoneNumber() != null) {
            validationService.validatePhoneNumber(dto.phoneNumber());
            client.setPhoneNumber(dto.phoneNumber());
        }

        clientRepository.update(client);
        return ClientDTO.fromEntity(client);
    }

    public void deleteClient(UUID clientId) {
        validationService.validateNotNull("clientId", clientId);

        Client client = clientRepository.findById(clientId)
            .orElseThrow(() -> new EntityNotFoundException("Client", clientId, "CLIENT_NOT_FOUND"));

        clientRepository.delete(client);
    }
}
