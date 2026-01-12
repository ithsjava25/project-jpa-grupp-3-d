package org.example.entity.invoice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UpdateInvoiceDTO(
    @NotNull(message = "Invoice ID cannot be null")
    UUID invoiceId,
    
    LocalDateTime dueDate,
    
    @Valid
    List<InvoiceItemDTO> items,
    
    InvoiceStatus status
) {}
