package org.example.entity.invoice;

import jakarta.persistence.*;
import lombok.*;
import org.example.entity.company.Company;
import org.example.entity.client.Client;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table (name="invoices")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Invoice {

    @Id
    @GeneratedValue (strategy= GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column (name= "number", nullable = false, unique = true)
    private String number;

    @Column(name= "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "vat_rate", precision = 5, scale = 4)
    private BigDecimal vatRate;

    private BigDecimal vatAmount;

    @Column(name= "due_date")
    private LocalDateTime dueDate;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> invoiceItems = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    public void addItem(InvoiceItem item) {
        invoiceItems.add(item);
        item.setInvoice(this);
        recalcTotals();
    }

    public void clearItems() {
        invoiceItems.clear();
        recalcTotals();
    }

    public void recalcTotals() {
        BigDecimal subTotal = invoiceItems.stream()
            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal vat = (vatRate != null ? vatRate : BigDecimal.ZERO)
            .multiply(subTotal);
        this.vatAmount = vat;
        this.amount = subTotal.add(vat);
    }

    public static Invoice fromDTO(CreateInvoiceDTO dto, Company company, Client client) {
        if (dto.vatRate() != null) {
            if (dto.vatRate().compareTo(BigDecimal.ZERO) < 0 || dto.vatRate().compareTo(BigDecimal.ONE) > 0) {
                throw new IllegalArgumentException("VAT rate must be between 0.0 and 1.0 (e.g., 0.25 for 25%)");
            }
        }
        Invoice invoice = Invoice.builder()
            .company(company)
            .client(client)
            .number(dto.number())
            .dueDate(dto.dueDate())
            .status(InvoiceStatus.CREATED)
            .invoiceItems(new ArrayList<>())
            .amount(BigDecimal.ZERO)
            .vatRate(dto.vatRate())
            .build();

        if (dto.items() != null) {
            dto.items().forEach(itemDTO -> {
                InvoiceItem item = new InvoiceItem();
                item.setName(itemDTO.name());
                item.setQuantity(itemDTO.quantity());
                item.setUnitPrice(itemDTO.unitPrice());
                invoice.addItem(item);
            });
        }

        invoice.recalcTotals();
        return invoice;
    }
}
