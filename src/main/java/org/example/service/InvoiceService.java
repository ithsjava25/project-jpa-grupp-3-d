package org.example.service;

import org.example.entity.client.Client;
import org.example.entity.company.Company;
import org.example.entity.invoice.*;
import org.example.exception.BusinessRuleException;
import org.example.exception.EntityNotFoundException;
import org.example.exception.ValidationException;
import org.example.repository.ClientRepository;
import org.example.repository.CompanyRepository;
import org.example.repository.InvoiceRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final CompanyRepository companyRepository;
    private final ClientRepository clientRepository;
    private final ValidationService validationService;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          CompanyRepository companyRepository,
                          ClientRepository clientRepository,
                          ValidationService validationService) {
        this.invoiceRepository = invoiceRepository;
        this.companyRepository = companyRepository;
        this.clientRepository = clientRepository;
        this.validationService = validationService;
    }

    public InvoiceDTO createInvoice(CreateInvoiceDTO dto) {
        validationService.validateNotNull("companyId", dto.companyId());
        validationService.validateNotNull("clientId", dto.clientId());
        validationService.validateNotEmpty("invoiceNumber", dto.number());
        validationService.validateInvoiceNumber(dto.number());

        if (invoiceRepository.findByInvoiceNumber(dto.number()).isPresent()) {
            throw new BusinessRuleException("Invoice number already in use: " + dto.number(), "INVOICE_NUMBER_EXISTS");
        }

        Company company = companyRepository.findById(dto.companyId())
            .orElseThrow(() -> new EntityNotFoundException("Company", dto.companyId(), "COMPANY_NOT_FOUND"));

        Client client = clientRepository.findById(dto.clientId())
            .orElseThrow(() -> new EntityNotFoundException("Client", dto.clientId(), "CLIENT_NOT_FOUND"));

        if (dto.items() == null || dto.items().isEmpty()) {
            throw new ValidationException("items", "Invoice must have at least one item", "INVOICE_ITEMS_REQUIRED");
        }

        Invoice invoice = Invoice.fromDTO(dto, company, client);
        Invoice saved = invoiceRepository.create(invoice);

        return InvoiceDTO.fromEntity(saved);
    }

    public InvoiceDTO updateInvoice(UpdateInvoiceDTO dto) {
        validationService.validateNotNull("invoiceId", dto.invoiceId());

        Invoice invoice = invoiceRepository.findByIdWithItems(dto.invoiceId())
            .orElseThrow(() -> new EntityNotFoundException("Invoice", dto.invoiceId(), "INVOICE_NOT_FOUND"));

        if (dto.dueDate() != null) {
            invoice.setDueDate(dto.dueDate());
        }
        if (dto.status() != null) {
            invoice.setStatus(dto.status());
        }

        if (dto.items() != null) {
            if (dto.items().isEmpty()) {
                throw new ValidationException("items", "Invoice must have at least one item", "INVOICE_ITEMS_REQUIRED");
            }

            invoice.clearItems();
            dto.items().forEach(itemDTO -> {
                validationService.validatePositive("quantity", itemDTO.quantity());
                validationService.validatePositive("unitPrice", itemDTO.unitPrice());

                InvoiceItem item = new InvoiceItem();
                item.setQuantity(itemDTO.quantity());
                item.setUnitPrice(itemDTO.unitPrice());
                invoice.addItem(item);
            });
        }

        invoice.recalcTotals();
        Invoice updated = invoiceRepository.update(invoice);
        return InvoiceDTO.fromEntity(updated);
    }


    public Optional<InvoiceDTO> getInvoiceById(UUID id) {
        validationService.validateNotNull("invoiceId", id);
        return invoiceRepository.findByIdWithItems(id)
            .map(InvoiceDTO::fromEntity);
    }

    public void updateStatus(UUID id, InvoiceStatus newStatus) {
        validationService.validateNotNull("invoiceId", id);
        validationService.validateNotNull("status", newStatus);

        Invoice invoice = invoiceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Invoice", id, "INVOICE_NOT_FOUND"));

        invoice.setStatus(newStatus);
        invoiceRepository.update(invoice);
    }

    public void deleteById(UUID id) {
        validationService.validateNotNull("invoiceId", id);

        if (!invoiceRepository.existsById(id)) {
            throw new EntityNotFoundException("Invoice", id, "INVOICE_NOT_FOUND");
        }

        invoiceRepository.deleteById(id);
    }

    public List<InvoiceDTO> getInvoicesByCompany(UUID companyId) {
        validationService.validateNotNull("companyId", companyId);
        return invoiceRepository.findAllByCompanyId(companyId).stream()
            .map(InvoiceDTO::fromEntity)
            .toList();
    }

    public List<InvoiceDTO> getInvoicesByClient(UUID clientId) {
        validationService.validateNotNull("clientId", clientId);
        return invoiceRepository.findAllByClientId(clientId).stream()
            .map(InvoiceDTO::fromEntity)
            .toList();
    }
}
