package org.example;

import org.example.entity.invoice.Invoice;
import org.example.entity.company.Company;
import org.example.entity.invoice.*;
import org.example.entity.client.Client;
import org.example.repository.ClientRepository;
import org.example.repository.CompanyRepository;
import org.example.repository.InvoiceRepository;
import org.example.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InvoiceServiceTest {

    private InvoiceRepository invoiceRepository;
    private CompanyRepository companyRepository;
    private ClientRepository clientRepository;
    private InvoiceService invoiceService;

    @BeforeEach
    void setUp() {
        invoiceRepository = mock(InvoiceRepository.class);
        companyRepository = mock(CompanyRepository.class);
        clientRepository = mock(ClientRepository.class);

        invoiceService = new InvoiceService(
            invoiceRepository,
            companyRepository,
            clientRepository
        );
    }

    @Test
    void testCreateInvoice_Success() {
        // Arrange
        UUID companyId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();

        CreateInvoiceDTO createDto = new CreateInvoiceDTO(
            companyId,
            clientId,
            "INV-001",
            LocalDateTime.now().plusDays(14),
            List.of()
        );

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(new Company()));
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(new Client()));
        when(invoiceRepository.findByInvoiceNumber("INV-001")).thenReturn(Optional.empty());


        when(invoiceRepository.create(any(Invoice.class))).thenAnswer(i -> {
            Invoice inv = i.getArgument(0);
            inv.setId(UUID.randomUUID());
            return inv;
        });

        // Act
        InvoiceDTO result = invoiceService.createInvoice(createDto);

        // Assert
        assertNotNull(result);
        assertEquals("INV-001", result.number());
        verify(invoiceRepository).create(any(Invoice.class));
    }

    @Test
    void testCreateInvoice_NumberAlreadyExists() {
        // Arrange
        UUID companyId = UUID.randomUUID();
        CreateInvoiceDTO createDto = new CreateInvoiceDTO(companyId, UUID.randomUUID(), "INV-EXIST", LocalDateTime.now(), List.of());


        when(invoiceRepository.findByInvoiceNumber("INV-EXIST")).thenReturn(Optional.of(new Invoice()));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> invoiceService.createInvoice(createDto));
        verify(invoiceRepository, never()).create(any());
    }

    @Test
    void testUpdateInvoice_Success() {
        // Arrange
        UUID invoiceId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();

        Invoice existingInvoice = new Invoice();
        existingInvoice.setId(invoiceId);
        existingInvoice.setNumber("INV-123");
        existingInvoice.setAmount(BigDecimal.ZERO);

        Company company = new Company();
        company.setId(companyId);
        existingInvoice.setCompany(company);

        Client client = new Client();
        client.setId(clientId);
        existingInvoice.setClient(client);

        UpdateInvoiceDTO updateDto = new UpdateInvoiceDTO(
            invoiceId,
            LocalDateTime.now().plusDays(30),
            List.of(new InvoiceItemDTO(null, 2, new BigDecimal("500.00"))),
            InvoiceStatus.SENT
        );

        when(invoiceRepository.findByIdWithItems(invoiceId)).thenReturn(Optional.of(existingInvoice));
        when(invoiceRepository.update(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        InvoiceDTO result = invoiceService.updateInvoice(updateDto);

        // Assert
        assertEquals(InvoiceStatus.SENT, result.status());
        verify(invoiceRepository).update(existingInvoice);
    }

    @Test
    void testGetInvoiceById_Success() {
        // Arrange
        UUID id = UUID.randomUUID();
        Invoice invoice = new Invoice();
        invoice.setId(id);
        invoice.setNumber("INV-180");

        Company company = new Company();
        company.setId(UUID.randomUUID());
        invoice.setCompany(company);

        Client client = new Client();
        client.setId(UUID.randomUUID());
        invoice.setClient(client);

        when(invoiceRepository.findByIdWithItems(id)).thenReturn(Optional.of(invoice));

        // Act
        Optional<InvoiceDTO> result = invoiceService.getInvoiceById(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("INV-180", result.get().number());
    }

    @Test
    void testDeleteById_Success() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(invoiceRepository.existsById(id)).thenReturn(true);

        // Act
        invoiceService.deleteById(id);

        // Assert
        verify(invoiceRepository).deleteById(id);
    }

    @Test
    void testDeleteById_NotFound() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(invoiceRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> invoiceService.deleteById(id));
        verify(invoiceRepository, never()).deleteById(any());
    }
}
