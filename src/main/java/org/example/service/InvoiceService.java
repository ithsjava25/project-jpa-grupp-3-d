package org.example.service;

import org.example.entity.client.Client;
import org.example.entity.company.Company;
import org.example.entity.invoice.*;
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

    public InvoiceService(InvoiceRepository invoiceRepository,
                          CompanyRepository companyRepository,
                          ClientRepository clientRepository) {
        this.invoiceRepository = invoiceRepository;
        this.companyRepository = companyRepository;
        this.clientRepository = clientRepository;
    }

    public InvoiceDTO createInvoice(CreateInvoiceDTO dto) {
        if (invoiceRepository.findByInvoiceNumber(dto.number()).isPresent()) {
            throw new IllegalArgumentException("Invoice number already in use: " + dto.number());
        }

        Company company = companyRepository.findById(dto.companyId())
            .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        Client client = clientRepository.findById(dto.clientId())
            .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        Invoice invoice = Invoice.fromDTO(dto, company, client);

        Invoice saved = invoiceRepository.create(invoice);
        return InvoiceDTO.fromEntity(saved);
    }


    public InvoiceDTO updateInvoice(UpdateInvoiceDTO dto) {
        Invoice invoice = invoiceRepository.findByIdWithItems(dto.invoiceId())
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        if (dto.dueDate() != null) invoice.setDueDate(dto.dueDate());
        if (dto.status() != null) invoice.setStatus(dto.status());

        if (dto.items() != null) {
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
        return InvoiceDTO.fromEntity(updated);
    }


    public Optional<InvoiceDTO> getInvoiceById(UUID id) {
        return invoiceRepository.findByIdWithItems(id)
            .map(InvoiceDTO::fromEntity);
    }

    public void updateStatus(UUID id, InvoiceStatus newStatus) {
        Invoice invoice = invoiceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
        invoice.setStatus(newStatus);
        invoiceRepository.update(invoice);
    }

    public void deleteById(UUID id) {
        if (!invoiceRepository.existsById(id)) {
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
