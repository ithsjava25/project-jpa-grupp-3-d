package org.example;

import org.example.entity.invoice.InvoiceDTO;
import org.example.entity.company.Company;
import org.example.entity.client.Client;
import org.example.entity.invoice.*;
import org.example.exception.BusinessRuleException;
import org.example.exception.EntityNotFoundException;
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

    //Success cases

    @Test
    void testCreateInvoice_Success() {
        UUID companyId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        CreateInvoiceDTO createDto = new CreateInvoiceDTO(companyId, clientId, "INV-001", LocalDateTime.now().plusDays(14), new BigDecimal("0.25"), List.of());

        Company company = new Company(); company.setId(companyId);
        Client client = new Client(); client.setId(clientId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(invoiceRepository.findByInvoiceNumber("INV-001")).thenReturn(Optional.empty());
        when(invoiceRepository.create(any(Invoice.class))).thenAnswer(i -> {
            Invoice inv = i.getArgument(0);
            inv.setId(UUID.randomUUID());
            inv.setCompany(company);
            inv.setClient(client);
            return inv;
        });

        InvoiceDTO result = invoiceService.createInvoice(createDto);

        assertNotNull(result);
        assertEquals("INV-001", result.number());
        verify(invoiceRepository).create(any(Invoice.class));
    }

    @Test
    void testUpdateStatus_Success() {
        UUID id = UUID.randomUUID();
        Invoice invoice = createFullInvoice(id, "INV-100");

        when(invoiceRepository.findById(id)).thenReturn(Optional.of(invoice));

        invoiceService.updateStatus(id, InvoiceStatus.PAID);

        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
        verify(invoiceRepository).update(invoice);
    }

    @Test
    void testGetInvoicesByCompany_Success() {
        UUID companyId = UUID.randomUUID();
        Invoice inv = createFullInvoice(UUID.randomUUID(), "INV-COMP");
        inv.getCompany().setId(companyId);

        when(invoiceRepository.findAllByCompanyId(companyId)).thenReturn(List.of(inv));

        List<InvoiceDTO> result = invoiceService.getInvoicesByCompany(companyId);

        assertEquals(1, result.size());
        assertEquals("INV-COMP", result.get(0).number());
    }

    @Test
    void testGetInvoicesByClient_Success() {
        UUID clientId = UUID.randomUUID();
        Invoice inv = createFullInvoice(UUID.randomUUID(), "INV-CLI");
        inv.getClient().setId(clientId);

        when(invoiceRepository.findAllByClientId(clientId)).thenReturn(List.of(inv));

        List<InvoiceDTO> result = invoiceService.getInvoicesByClient(clientId);

        assertEquals(1, result.size());
        assertEquals("INV-CLI", result.get(0).number());
    }

    @Test
    void testUpdateInvoice_Success() {
        UUID invoiceId = UUID.randomUUID();
        Invoice existingInvoice = createFullInvoice(invoiceId, "INV-123");
        UpdateInvoiceDTO updateDto = new UpdateInvoiceDTO(invoiceId, LocalDateTime.now().plusDays(30), new BigDecimal("0.25"), List.of(new InvoiceItemDTO(null, "name", 2, new BigDecimal("500.00"))), InvoiceStatus.SENT);

        when(invoiceRepository.findByIdWithItems(invoiceId)).thenReturn(Optional.of(existingInvoice));
        when(invoiceRepository.update(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

        InvoiceDTO result = invoiceService.updateInvoice(updateDto);

        assertNotNull(result);
        assertEquals(InvoiceStatus.SENT, result.status());
        verify(invoiceRepository).update(existingInvoice);
    }

    @Test
    void testGetInvoiceById_Success() {
        UUID id = UUID.randomUUID();
        Invoice invoice = createFullInvoice(id, "INV-180");
        when(invoiceRepository.findByIdWithItems(id)).thenReturn(Optional.of(invoice));

        Optional<InvoiceDTO> result = invoiceService.getInvoiceById(id);

        assertTrue(result.isPresent());
        assertEquals("INV-180", result.get().number());
    }

    @Test
    void testDeleteById_Success() {
        UUID id = UUID.randomUUID();
        when(invoiceRepository.existsById(id)).thenReturn(true);
        invoiceService.deleteById(id);
        verify(invoiceRepository).deleteById(id);
    }


    @Test
    void testCreateInvoice_CompanyNotFound() {
        UUID companyId = UUID.randomUUID();
        CreateInvoiceDTO dto = new CreateInvoiceDTO(companyId, UUID.randomUUID(), "INV-X", LocalDateTime.now(), new BigDecimal("0.25"), List.of());

        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> invoiceService.createInvoice(dto));
        verify(invoiceRepository, never()).create(any());
    }

    @Test
    void testCreateInvoice_ClientNotFound() {
        UUID companyId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        CreateInvoiceDTO dto = new CreateInvoiceDTO(companyId, clientId, "INV-X", LocalDateTime.now(), new BigDecimal("0.25"), List.of());

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(new Company()));
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> invoiceService.createInvoice(dto));
        verify(invoiceRepository, never()).create(any());
    }

    @Test
    void testUpdateInvoice_InvoiceNotFound() {
        UUID id = UUID.randomUUID();
        UpdateInvoiceDTO dto = new UpdateInvoiceDTO(id, LocalDateTime.now(), new BigDecimal("0.25"), List.of(), InvoiceStatus.SENT);

        when(invoiceRepository.findByIdWithItems(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> invoiceService.updateInvoice(dto));
        verify(invoiceRepository, never()).update(any());
    }

    @Test
    void testUpdateStatus_InvoiceNotFound() {
        UUID id = UUID.randomUUID();
        when(invoiceRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> invoiceService.updateStatus(id, InvoiceStatus.PAID));
        verify(invoiceRepository, never()).update(any());
    }


    @Test
    void testDeleteById_NotFound() {
        UUID id = UUID.randomUUID();
        when(invoiceRepository.existsById(id)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> invoiceService.deleteById(id));
        verify(invoiceRepository, never()).deleteById(any());
    }

    @Test
    void testCreateInvoice_NumberAlreadyExists() {
        CreateInvoiceDTO createDto = new CreateInvoiceDTO(UUID.randomUUID(), UUID.randomUUID(), "INV-EXIST", LocalDateTime.now(), new BigDecimal("0.25"), List.of());
        when(invoiceRepository.findByInvoiceNumber("INV-EXIST")).thenReturn(Optional.of(new Invoice()));
        assertThrows(BusinessRuleException.class, () -> invoiceService.createInvoice(createDto));
    }

    private Invoice createFullInvoice(UUID id, String number) {
        Invoice invoice = new Invoice();
        invoice.setId(id);
        invoice.setNumber(number);
        invoice.setAmount(BigDecimal.ZERO);
        invoice.setStatus(InvoiceStatus.CREATED);
        invoice.setCreatedAt(LocalDateTime.now());

        Company company = new Company(); company.setId(UUID.randomUUID());
        invoice.setCompany(company);

        Client client = new Client(); client.setId(UUID.randomUUID());
        invoice.setClient(client);

        return invoice;
    }
}
