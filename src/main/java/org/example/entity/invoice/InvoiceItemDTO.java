package org.example.entity.invoice;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record InvoiceItemDTO(
    UUID id,
    String name,
    int quantity,
    BigDecimal unitPrice
) {
    public static InvoiceItemDTO fromEntity(InvoiceItem item) {
        return InvoiceItemDTO.builder()
            .id(item.getId())
            .name(item.getName())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .build();
    }
}
