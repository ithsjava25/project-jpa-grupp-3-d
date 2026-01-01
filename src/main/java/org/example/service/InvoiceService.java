package org.example.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.dto.InvoiceDTO;
import org.example.dto.InvoiceItemDTO;
import org.example.entity.*;
import org.example.repository.ClientRepository;
import org.example.repository.CompanyRepository;
import org.example.repository.InvoiceRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    public InvoiceDTO createInvoice(InvoiceDTO dto) {
        if (invoiceRepository.findByInvoiceNumber(dto.number()).isPresent()) {
            throw new IllegalArgumentException("Invoice number already in use " + dto.number());
        }

        Company company = companyRepository.findById(dto.companyId())
            .orElseThrow(() -> new EntityNotFoundException("Company not found"));

        Client client = clientRepository.findById(dto.clientId())
            .orElseThrow(() -> new EntityNotFoundException("Client not found"));

        Invoice invoice = Invoice.fromDTO(dto, company, client);
        Invoice saved = invoiceRepository.create(invoice);
        return InvoiceDTO.fromEntity(saved);
    }

    public Optional<InvoiceDTO> getInvoiceById(UUID id) {
        return invoiceRepository.findByIdWithItems(id)
            .map(InvoiceDTO::fromEntity);
    }

    public void updateStatus(UUID id, InvoiceStatus newStatus) {
        Invoice invoice = invoiceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Invoice not found"));
        invoice.setStatus(newStatus);
        invoiceRepository.update(invoice);
    }

    public void deleteById(UUID id) {
        if (!invoiceRepository.existsById(id)) {
            throw new EntityNotFoundException("Invoice not found");
        }
        invoiceRepository.deleteById(id);
    }

    public InvoiceDTO updateInvoiceItems(UUID id, Set<InvoiceItemDTO> items) {
        Invoice invoice = invoiceRepository.findByIdWithItems(id)
            .orElseThrow(() -> new EntityNotFoundException("Invoice not found"));

        invoice.clearItems();
        items.forEach(dto -> {
            InvoiceItem item = new InvoiceItem();
            item.setQuantity(dto.quantity());
            item.setUnitPrice(dto.unitPrice());
            invoice.addItem(item);
        });

        Invoice updated = invoiceRepository.update(invoice);
        return InvoiceDTO.fromEntity(updated);
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
