package org.example.dto;

import lombok.Builder;
import org.example.entity.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record InvoiceDTO(
    UUID id,
    UUID companyId,
    UUID clientId,
    String number,
    BigDecimal amount,
    LocalDateTime dueDate,
    LocalDateTime createdAt,
    InvoiceStatus status
) {
}
