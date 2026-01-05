package org.example.service;

import org.example.dto.ClientDTO;
import org.example.entity.Client;
import org.example.entity.Company;
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
    void setUp(){
        this.clientRepository = mock(ClientRepository.class);
        this.companyRepository = mock(CompanyRepository.class);
        this.clientService = new ClientService(clientRepository, companyRepository);
        companyId = UUID.randomUUID();
    }


    @Test
    void shouldNotAllowClientCreationIfNoValidCompany() {
        // mock company does NOT exist
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.empty());


        assertThrows(IllegalArgumentException.class, () ->
            clientService.createClient(
                companyId,
                "John",
                "Doe",
                "john.doe@email.com",
                "Client Street 1",
                "City",
                "Country",
                "0701234567"
            )
        );
    }

    @Test
    void shouldAllowClientCreationIfValidCompany() {
        // mock valid company
        when(companyRepository.findById(companyId))
            .thenReturn(Optional.of(company));


        // no exception expected
        assertDoesNotThrow(() ->
            clientService.createClient(
                companyId,
                "John",
                "Doe",
                "john.doe@email.com",
                "Client Street 1",
                "City",
                "Country",
                "0701234567"
            )
        );

        // verify client was persisted
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

        ClientDTO updated = clientService.updateClient(
            clientId, "New", "Name", "new@email.com",
            null, null, null, null
        );

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

        assertThrows(Exception.class, () ->
            clientService.updateClient(clientId, "New", null, null, null, null, null, null)
        );
    }

    @Test
    void shouldThrowOnDeleteNonExistentClient() {
        UUID clientId = UUID.randomUUID();
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> clientService.deleteClient(clientId));
    }
}
