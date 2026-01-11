package org.example.entity.invoice;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record InvoiceDTO(
    UUID id,
    UUID companyId,
    UUID clientId,
    String number,
    BigDecimal amount,
    BigDecimal vatRate,
    BigDecimal vatAmount,
    LocalDateTime dueDate,
    LocalDateTime createdAt,
    InvoiceStatus status,
    List<InvoiceItemDTO> items
) {
    public static InvoiceDTO fromEntity(Invoice invoice) {
        List<InvoiceItemDTO> itemDTOs = invoice.getInvoiceItems().stream()
            .map(InvoiceItemDTO::fromEntity)
            .toList();

        return InvoiceDTO.builder()
            .id(invoice.getId())
            .companyId(invoice.getCompany().getId())
            .clientId(invoice.getClient().getId())
            .number(invoice.getNumber())
            .amount(invoice.getAmount())
            .vatRate(invoice.getVatRate())
            .vatAmount(invoice.getVatAmount())
            .dueDate(invoice.getDueDate())
            .createdAt(invoice.getCreatedAt())
            .status(invoice.getStatus())
            .items(itemDTOs)
            .build();
    }
}
