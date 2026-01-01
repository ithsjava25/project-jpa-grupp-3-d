package org.example;

import org.example.entity.Client;
import org.example.entity.Company;
import org.example.repository.ClientRepository;
import org.example.repository.CompanyRepository;
import org.example.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class ClientTest {

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
}
