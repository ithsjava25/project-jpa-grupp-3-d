package org.example.service;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.example.dto.InvoiceDTO;
import org.example.entity.Invoice;
import org.example.entity.InvoiceItem;
import org.example.entity.InvoiceStatus;
import org.example.repository.InvoiceItemRepository;
import org.example.repository.InvoiceRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

//One Service-class which is an Aggregate Root, An invoice and its lines/items are logically connected.
// One line/item has no reason to exist without an invoice.
// ie.  the method createInvoiceWithItems is more secure when both the lines/items and invoice are saved in the same transaction
public class InvoiceService {

    private final InvoiceItemRepository invoiceItemRepository;
    private final InvoiceRepository invoiceRepository;


    public InvoiceService(InvoiceItemRepository invoiceItemRepository, InvoiceRepository invoiceRepository) {
        this.invoiceItemRepository = invoiceItemRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public InvoiceDTO createInvoice(InvoiceDTO invoiceDTO) {}

    public Optional<InvoiceDTO> getInvoiceById(UUID id) {}

    public void updateStatus(UUID id, InvoiceStatus newStatus) {
        Invoice invoice=invoiceRepository.findById(id)
            .orElseThrow(()->new EntityNotFoundException("Invoice not found"));

        //updates the status of the entity
        invoice.setStatus(newStatus);

        //saves the update to the database
        invoiceRepository.update(invoice);

    }

    public void deleteInvoice(UUID id) {
        invoiceRepository.deleteById(id);
    }



    //method that converts data from entity to DTO (easy for user)
    private InvoiceDTO mapToDTO(Invoice invoice) {
        return InvoiceDTO.builder()
            .id(invoice.getId())
            .number(invoice.getNumber())
            .status(invoice.getStatus())
            .dueDate(invoice.getDueDate())
            .createdAt(invoice.getCreatedAt())
            // Calculate the total amount based on the number of items
            .amount(calculateTotal(invoice.getItems()))
            .build();
    }

    private BigDecimal calculateTotal(Set<InvoiceItem> items) {
        if (items == null) return BigDecimal.ZERO;
        return items.stream()
            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }








}
