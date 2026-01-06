package org.example.service;

import org.example.entity.client.Client;
import org.example.entity.company.Company;
import org.example.entity.invoice.*;
import org.example.repository.ClientRepository;
import org.example.repository.CompanyRepository;
import org.example.repository.InvoiceRepository;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CompanyRepository companyRepository;
    private final ClientRepository clientRepository;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          CompanyRepository companyRepository,
                          ClientRepository clientRepository) {
        this.invoiceRepository = invoiceRepository;
        this.companyRepository = companyRepository;
        this.clientRepository = clientRepository;
    }

    public InvoiceDTO createInvoice(CreateInvoiceDTO dto) {
        if (invoiceRepository.findByInvoiceNumber(dto.number()).isPresent()) {
            log.warn("Invoice creation failed: Number {} is already in use for company {}", dto.number(), dto.companyId());
            throw new IllegalArgumentException("Invoice number already in use: " + dto.number());
        }

        Company company = companyRepository.findById(dto.companyId())
            .orElseThrow(() -> {
                log.warn("Create invoice failed: Company {} not found", dto.companyId());
                return new IllegalArgumentException("Company not found");
            });

        Client client = clientRepository.findById(dto.clientId())
            .orElseThrow(() -> {
                log.warn("Create invoice failed: Client {} not found", dto.clientId());
                return new IllegalArgumentException("Client not found");
            });

        Invoice invoice = Invoice.fromDTO(dto, company, client);

        Invoice saved = invoiceRepository.create(invoice);
        log.info("Successfully created invoice {} (ID: {}) for company {}", saved.getNumber(), saved.getId(), dto.companyId());

        return InvoiceDTO.fromEntity(saved);
    }


    public InvoiceDTO updateInvoice(UpdateInvoiceDTO dto) {
        log.info("Updating invoice ID: {}", dto.invoiceId());

        Invoice invoice = invoiceRepository.findByIdWithItems(dto.invoiceId())
            .orElseThrow(() -> {
                log.warn("Update failed: Invoice {} not found", dto.invoiceId());
                return new IllegalArgumentException("Invoice not found");
            });

        if (dto.dueDate() != null) invoice.setDueDate(dto.dueDate());
        if (dto.status() != null) invoice.setStatus(dto.status());

        if (dto.items() != null) {
            log.debug("Refreshing items for invoice {}. New item count: {}", dto.invoiceId(), dto.items().size());
            invoice.clearItems();
            dto.items().forEach(itemDTO -> {
                InvoiceItem item = new InvoiceItem();
                item.setQuantity(itemDTO.quantity());
                item.setUnitPrice(itemDTO.unitPrice());
                invoice.addItem(item);
            });
        }

        invoice.recalcTotals();
        Invoice updated = invoiceRepository.update(invoice);
        log.info("Successfully updated invoice {}. New total amount: {}", updated.getId(), updated.getAmount());
        return InvoiceDTO.fromEntity(updated);
    }


    public Optional<InvoiceDTO> getInvoiceById(UUID id) {
        return invoiceRepository.findByIdWithItems(id)
            .map(InvoiceDTO::fromEntity);
    }

    public void updateStatus(UUID id, InvoiceStatus newStatus) {
        Invoice invoice = invoiceRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Update failed: Invoice {} not found", id);
                return new IllegalArgumentException("Invoice not found");
            });
        invoice.setStatus(newStatus);
        invoiceRepository.update(invoice);
        log.info("Invoice {} status successfully updated to {}", id, newStatus);
    }

    public void deleteById(UUID id) {
        log.info("Attempting to delete invoice {}", id);
        if (!invoiceRepository.existsById(id)) {
            log.warn("Delete failed: Invoice {} does not exist", id);
            throw new IllegalArgumentException("Invoice not found");
        }
        invoiceRepository.deleteById(id);
    }

    public List<InvoiceDTO> getInvoicesByCompany(UUID companyId) {
        return invoiceRepository.findAllByCompanyId(companyId).stream()
            .map(InvoiceDTO::fromEntity)
            .toList();
    }

    public List<InvoiceDTO> getInvoicesByClient(UUID clientId) {
        return invoiceRepository.findAllByClientId(clientId).stream()
            .map(InvoiceDTO::fromEntity)
            .toList();
    }
}
