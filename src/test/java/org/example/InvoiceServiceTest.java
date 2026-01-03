package org.example;

import org.example.dto.InvoiceDTO;
import org.example.entity.Company;
import org.example.entity.Invoice;
import org.example.entity.InvoiceStatus;
import org.example.repository.InvoiceRepository;
import org.example.service.ClientService;
import org.example.service.CompanyService;
import org.example.service.CompanyUserService;
import org.example.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InvoiceServiceTest {

    private InvoiceRepository invoiceRepository;
    private ClientService clientService;
    private CompanyService companyService;
    private CompanyUserService companyUserService;
    private InvoiceService invoiceService;

    @BeforeEach
    void setUp() {
        invoiceRepository = mock(InvoiceRepository.class);
        clientService = mock(ClientService.class);
        companyService = mock(CompanyService.class);
        companyUserService = mock(CompanyUserService.class);


        invoiceService = new InvoiceService(
            clientService,
            companyUserService,
            invoiceRepository,
            companyService
        );
    }

    @Test
    void testCreateInvoice_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("1200.00");
        LocalDateTime now = LocalDateTime.now();

        InvoiceDTO inputDto = new InvoiceDTO(
            null,           // id (UUID)
            companyId,      // companyId (UUID)
            null,           // clientId (UUID)
            "INV-001",      // number (String)
            amount,         // amount (BigDecimal)
            now,            // dueDate (LocalDateTime)
            now,            // createdAt (LocalDateTime)
            InvoiceStatus.CREATED // status (InvoiceStatus)
        );

        // Simulates that the user has access to the company
        when(companyUserService.isUserAssociatedWithCompany(userId, companyId)).thenReturn(true);

        when(invoiceRepository.findByInvoiceNumber("INV-001")).thenReturn(Optional.empty());
        // simulates the save to the database
        when(invoiceRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        InvoiceDTO result = invoiceService.createInvoice(inputDto, userId);

        // Assert
        assertNotNull(result);
        assertEquals("INV-001", result.number());
        verify(invoiceRepository, times(1)).create(any());
    }

    @Test
    void testCreateInvoice_NumberAlreadyExists() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        InvoiceDTO inputDto = new InvoiceDTO(null, companyId, null, "INV-EXIST", BigDecimal.ZERO, null, null, null);

        when(companyUserService.isUserAssociatedWithCompany(userId, companyId)).thenReturn(true);
        //simulates that the number already exists in the database
        when(invoiceRepository.findByInvoiceNumber("INV-EXIST")).thenReturn(Optional.of(new Invoice()));

        // Act & Assert
        // were expecting an IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            invoiceService.createInvoice(inputDto, userId);
        });

        // verify that "create" was never called
        verify(invoiceRepository, never()).create(any());
    }

    @Test
    void testGetInvoiceById_SecurityException() {
        // Arrange
        UUID invoiceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID(); // users company
        UUID otherCompanyId = UUID.randomUUID(); // the invoices real company

        Invoice invoice = new Invoice();
        Company otherCompany = new Company();
        otherCompany.setId(otherCompanyId);
        invoice.setCompany(otherCompany);

        when(companyUserService.isUserAssociatedWithCompany(userId, companyId)).thenReturn(true);
        when(invoiceRepository.findByIdWithItems(invoiceId)).thenReturn(Optional.of(invoice));

        // Act & Assert
        // expecting a SecurityException because companyId does not match
        assertThrows(SecurityException.class, () -> {
            invoiceService.getInvoiceById(invoiceId, userId, companyId);
        });

        // verify that we did not make it to the mapping stage
        verify(invoiceRepository).findByIdWithItems(invoiceId);
    }

    @Test
    void testDeleteInvoice_Success() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        Invoice invoice = new Invoice();
        Company company = new Company();
        company.setId(companyId);
        invoice.setCompany(company);

        when(companyUserService.isUserAssociatedWithCompany(userId, companyId)).thenReturn(true);
        when(invoiceRepository.findById(id)).thenReturn(Optional.of(invoice));

        // Act
        invoiceService.deleteById(id, userId, companyId);

        // Assert
        verify(invoiceRepository, times(1)).delete(invoice);
    }

    @Test
    void testCreateInvoice_UserNotAssociatedWithCompany() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        InvoiceDTO inputDto = new InvoiceDTO(null, companyId, null, "INV-001", BigDecimal.ZERO, null, null, null);

        // simulates that the user does not have access
        when(companyUserService.isUserAssociatedWithCompany(userId, companyId)).thenReturn(false);

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            invoiceService.createInvoice(inputDto, userId);
        });

        // verify that we never checked if the invoice exists
        verify(invoiceRepository, never()).findByInvoiceNumber(anyString());
    }
}
