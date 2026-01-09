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

    public ClientDTO createClient(CreateClientDTO dto) {

        Company company = companyRepository.findById(dto.companyId())
            .orElseThrow(() -> new EntityNotFoundException("Company", dto.companyId()));

        Client client = Client.fromDTO(dto, company);

        clientRepository.create(client);

        return ClientDTO.fromEntity(client);
    }

    public ClientDTO updateClient(UpdateClientDTO dto) {

        Client client = clientRepository.findById(dto.clientId())
            .orElseThrow(() -> new EntityNotFoundException("Client", dto.clientId()));

        if (dto.firstName() != null) {
            client.setFirstName(dto.firstName());
        }
        if (dto.lastName() != null) {
            client.setLastName(dto.lastName());
        }
        if (dto.email() != null) {
            client.setEmail(dto.email());
        }
        if (dto.address() != null) {
            client.setAddress(dto.address());
        }
        if (dto.city() != null) {
            client.setCity(dto.city());
        }
        if (dto.country() != null) {
            client.setCountry(dto.country());
        }
        if (dto.phoneNumber() != null) {
            client.setPhoneNumber(dto.phoneNumber());
        }

        clientRepository.update(client);
        return ClientDTO.fromEntity(client);
    }


    public void deleteClient(UUID clientId) {
        Client client = clientRepository.findById(clientId)
            .orElseThrow(() -> new EntityNotFoundException("Client", clientId));

        clientRepository.delete(client);
    }
}
