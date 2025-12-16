package org.example.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record InvoiceItemDTO (
    UUID id,
    int quantity,
    BigDecimal unitPrice
) {}
