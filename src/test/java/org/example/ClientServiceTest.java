package org.example;

import org.example.entity.Client;
import org.example.repository.ClientRepository;
import org.example.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class ClientServiceTest {
    private ClientRepository clientRepository;
    private ClientService clientService;

    @BeforeEach
    void setUp() {
        clientRepository = mock(ClientRepository.class);
        clientService = new ClientService(clientRepository);
    }

    @Test
    void testDeleteClient() {
        UUID clientId = UUID.randomUUID();
        Client client = new Client();
        client.setId(clientId);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        clientService.deleteById(clientId);

        verify(clientRepository, times(1)).delete(client);
    }

    @Test
    void testDeleteClientNotFound() {
        UUID clientId = UUID.randomUUID();
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalStateException.class,
            () -> clientService.deleteById(clientId));

        assertEquals("Client not found", exception.getMessage());
    }
}
