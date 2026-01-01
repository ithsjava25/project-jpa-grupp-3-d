package org.example.dto;

import lombok.Builder;
import org.example.entity.Invoice;
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

    public static InvoiceDTO fromEntity(Invoice invoice) {
        return InvoiceDTO.builder()
            .id(invoice.getId())
            .companyId(invoice.getCompany().getId())
            .clientId(invoice.getClient().getId())
            .number(invoice.getNumber())
            .amount(invoice.getAmount())
            .dueDate(invoice.getDueDate())
            .createdAt(invoice.getCreatedAt())
            .status(invoice.getStatus())
            .build();
    }
}
