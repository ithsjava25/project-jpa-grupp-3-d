package org.example.entity.invoice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CreateInvoiceDTO(
    @NotNull(message = "Company ID cannot be null")
    UUID companyId,

    @NotNull(message = "Client ID cannot be null")
    UUID clientId,

    @NotBlank(message = "Invoice number cannot be blank")
    String number,

    @NotNull(message = "Due date cannot be null")
    LocalDateTime dueDate,

    @NotNull(message = "Items cannot be null")
    @NotEmpty(message = "Invoice must have at least one item")
    @Valid
    List<InvoiceItemDTO> items
) {}
