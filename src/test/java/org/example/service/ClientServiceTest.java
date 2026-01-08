package org.example.service;

import org.example.entity.client.ClientDTO;
import org.example.entity.client.Client;
import org.example.entity.client.CreateClientDTO;
import org.example.entity.client.UpdateClientDTO;
import org.example.entity.company.Company;
import org.example.exception.EntityNotFoundException;
import org.example.repository.ClientRepository;
import org.example.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ClientServiceTest {

    private ClientRepository clientRepository;
    private ClientService clientService;
    private CompanyRepository companyRepository;

    private UUID companyId;
    private final Company company = new Company();

    @BeforeEach
    void setUp() {
        clientRepository = mock(ClientRepository.class);
        companyRepository = mock(CompanyRepository.class);
        clientService = new ClientService(clientRepository, companyRepository);
        companyId = UUID.randomUUID();
    }

    @Test
    void shouldNotAllowClientCreationIfNoValidCompany() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        CreateClientDTO dto = new CreateClientDTO(
            companyId, "John", "Doe", "john.doe@email.com",
            "Client Street 1", "Country", "City", "0701234567"
        );

        assertThrows(EntityNotFoundException.class, () -> clientService.createClient(dto));
    }

    @Test
    void shouldAllowClientCreationIfValidCompany() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        CreateClientDTO dto = new CreateClientDTO(
            companyId, "John", "Doe", "john.doe@email.com",
            "Client Street 1", "Country", "City", "0701234567"
        );

        assertDoesNotThrow(() -> clientService.createClient(dto));

        verify(clientRepository).create(any(Client.class));
    }

    @Test
    void shouldUpdateClient() {
        UUID clientId = UUID.randomUUID();
        Client client = Client.builder()
            .id(clientId)
            .company(company)
            .firstName("Old")
            .lastName("Name")
            .email("old@email.com")
            .build();

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        UpdateClientDTO dto = new UpdateClientDTO(
            clientId, "New", "Name", "new@email.com",
            null, null, null, null
        );

        ClientDTO updated = clientService.updateClient(dto);

        assertEquals("New", updated.firstName());
        assertEquals("Name", updated.lastName());
        assertEquals("new@email.com", updated.email());
        verify(clientRepository).update(client);
    }

    @Test
    void shouldDeleteClient() {
        UUID clientId = UUID.randomUUID();
        Client client = Client.builder().id(clientId).company(company).build();

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        assertDoesNotThrow(() -> clientService.deleteClient(clientId));
        verify(clientRepository).delete(client);
    }

    @Test
    void shouldGetClientsByCompany() {
        Client client1 = Client.builder().company(company).firstName("A").build();
        Client client2 = Client.builder().company(company).firstName("B").build();

        when(clientRepository.findByCompanyId(companyId)).thenReturn(List.of(client1, client2));

        List<ClientDTO> clients = clientService.getClientsByCompany(companyId);

        assertEquals(2, clients.size());
        assertEquals("A", clients.get(0).firstName());
        assertEquals("B", clients.get(1).firstName());
    }

    @Test
    void shouldThrowOnUpdateNonExistentClient() {
        UUID clientId = UUID.randomUUID();
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        UpdateClientDTO dto = new UpdateClientDTO(
            clientId, "New", null, null, null, null, null, null
        );

        assertThrows(EntityNotFoundException.class, () -> clientService.updateClient(dto));
    }

    @Test
    void shouldThrowOnDeleteNonExistentClient() {
        UUID clientId = UUID.randomUUID();
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> clientService.deleteClient(clientId));
    }
}
