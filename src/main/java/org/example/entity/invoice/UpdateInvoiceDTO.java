package org.example.entity.invoice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UpdateInvoiceDTO(
    UUID invoiceId,
    LocalDateTime dueDate,
    BigDecimal vatRate,
    List<InvoiceItemDTO> items,
    InvoiceStatus status
) {}
