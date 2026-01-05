package org.example.service;

import org.example.entity.client.ClientDTO;
import org.example.entity.client.Client;
import org.example.entity.client.CreateClientDTO;
import org.example.entity.client.UpdateClientDTO;
import org.example.entity.company.Company;
import org.example.exception.ValidationException;
import org.example.exception.EntityNotFoundException;
import org.example.repository.ClientRepository;
import org.example.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private ValidationService validationService;

    @InjectMocks
    private ClientService clientService;

    private UUID companyId;
    private Company company;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        company = new Company();
        company.setId(companyId);
    }

    @Test
    @DisplayName("Should not allow client creation if no valid company")
    void createClient_CompanyNotFound_ThrowsEntityNotFoundException() {
        CreateClientDTO dto = new CreateClientDTO(
            companyId, "John", "Doe", "john.doe@email.com",
            "Client Street 1", "Country", "City", "0701234567"
        );

        doNothing().when(validationService).validateNotNull("companyId", companyId);
        doNothing().when(validationService).validatePersonName("firstName", "John");
        doNothing().when(validationService).validatePersonName("lastName", "Doe");
        doNothing().when(validationService).validateEmail("john.doe@email.com");
        doNothing().when(validationService).validateAddress("address", "Client Street 1");
        doNothing().when(validationService).validateAddress("city", "City");
        doNothing().when(validationService).validateAddress("country", "Country");
        doNothing().when(validationService).validatePhoneNumber("0701234567");

        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> clientService.createClient(dto));
    }

    @Test
    @DisplayName("Should allow client creation with valid company")
    void createClient_ValidCompany_Success() {
        CreateClientDTO dto = new CreateClientDTO(
            companyId, "John", "Doe", "john.doe@email.com",
            "Client Street 1", "Country", "City", "0701234567"
        );

        doNothing().when(validationService).validateNotNull("companyId", companyId);
        doNothing().when(validationService).validatePersonName("firstName", "John");
        doNothing().when(validationService).validatePersonName("lastName", "Doe");
        doNothing().when(validationService).validateEmail("john.doe@email.com");
        doNothing().when(validationService).validateAddress("address", "Client Street 1");
        doNothing().when(validationService).validateAddress("city", "City");
        doNothing().when(validationService).validateAddress("country", "Country");
        doNothing().when(validationService).validatePhoneNumber("0701234567");

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        assertDoesNotThrow(() -> clientService.createClient(dto));

        verify(clientRepository).create(any(Client.class));
        verify(validationService).validateEmail("john.doe@email.com");
        verify(validationService).validatePersonName("firstName", "John");
        verify(validationService).validatePersonName("lastName", "Doe");
    }

    @Test
    @DisplayName("Should update client successfully")
    void updateClient_Success() {
        UUID clientId = UUID.randomUUID();
        Client client = Client.builder()
            .id(clientId)
            .company(company)
            .firstName("Old")
            .lastName("Name")
            .email("old@email.com")
            .build();

        UpdateClientDTO dto = new UpdateClientDTO(
            clientId, "New", "Name", "new@email.com",
            null, null, null, null
        );

        doNothing().when(validationService).validateNotNull("clientId", clientId);
        doNothing().when(validationService).validatePersonName("firstName", "New");
        doNothing().when(validationService).validatePersonName("lastName", "Name");
        doNothing().when(validationService).validateEmail("new@email.com");

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        ClientDTO updated = clientService.updateClient(dto);

        assertEquals("New", updated.firstName());
        assertEquals("Name", updated.lastName());
        assertEquals("new@email.com", updated.email());
        verify(clientRepository).update(client);
        verify(validationService).validateEmail("new@email.com");
    }

    @Test
    @DisplayName("Should delete client successfully")
    void deleteClient_Success() {
        UUID clientId = UUID.randomUUID();
        Client client = Client.builder().id(clientId).company(company).build();

        doNothing().when(validationService).validateNotNull("clientId", clientId);
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        assertDoesNotThrow(() -> clientService.deleteClient(clientId));
        verify(clientRepository).delete(client);
        verify(validationService).validateNotNull("clientId", clientId);
    }

    @Test
    @DisplayName("Should get all clients for a company")
    void getClientsByCompany_Success() {
        Client client1 = Client.builder().company(company).firstName("A").build();
        Client client2 = Client.builder().company(company).firstName("B").build();

        doNothing().when(validationService).validateNotNull("companyId", companyId);
        when(clientRepository.findByCompanyId(companyId)).thenReturn(List.of(client1, client2));

        List<ClientDTO> clients = clientService.getClientsByCompany(companyId);

        assertEquals(2, clients.size());
        assertEquals("A", clients.get(0).firstName());
        assertEquals("B", clients.get(1).firstName());
        verify(validationService).validateNotNull("companyId", companyId);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when updating non-existent client")
    void updateClient_ClientNotFound_ThrowsEntityNotFoundException() {
        UUID clientId = UUID.randomUUID();
        UpdateClientDTO dto = new UpdateClientDTO(
            clientId, "New", null, null, null, null, null, null
        );

        doNothing().when(validationService).validateNotNull("clientId", clientId);
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> clientService.updateClient(dto));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when deleting non-existent client")
    void deleteClient_ClientNotFound_ThrowsEntityNotFoundException() {
        UUID clientId = UUID.randomUUID();

        doNothing().when(validationService).validateNotNull("clientId", clientId);
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> clientService.deleteClient(clientId));
    }

    @Test
    @DisplayName("Should throw ValidationException when creating client with invalid email")
    void createClient_InvalidEmail_ThrowsValidationException() {
        CreateClientDTO dto = new CreateClientDTO(
            companyId, "John", "Doe", "invalid-email",
            "Address", "Country", "City", "0701234567"
        );

        lenient().doNothing().when(validationService).validateNotNull("companyId", companyId);
        lenient().doNothing().when(validationService).validatePersonName("firstName", "John");
        lenient().doNothing().when(validationService).validatePersonName("lastName", "Doe");

        doThrow(new ValidationException("email", "Invalid email format"))
            .when(validationService).validateEmail("invalid-email");

        lenient().when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        assertThrows(ValidationException.class, () -> clientService.createClient(dto));
        verify(clientRepository, never()).create(any());
    }
}
