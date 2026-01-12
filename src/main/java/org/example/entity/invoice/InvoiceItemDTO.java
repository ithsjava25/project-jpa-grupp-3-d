package org.example.entity.invoice;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record InvoiceItemDTO(
    UUID id,

    @Min(value = 1, message = "Quantity must be at least 1")
    int quantity,

    @NotNull(message = "Unit price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
    BigDecimal unitPrice
) {
    public static InvoiceItemDTO fromEntity(InvoiceItem item) {
        return InvoiceItemDTO.builder()
            .id(item.getId())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .build();
    }
}
