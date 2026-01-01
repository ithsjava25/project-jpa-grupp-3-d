package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.dto.InvoiceDTO;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table (name="invoices")
@Getter
@Setter
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
        amount = invoiceItems.stream()
            .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static Invoice fromDTO(InvoiceDTO dto, Company company, Client client) {
        Invoice invoice = new Invoice();
        invoice.setCompany(company);
        invoice.setClient(client);
        invoice.setNumber(dto.number());
        invoice.setDueDate(dto.dueDate());
        invoice.setStatus(dto.status() != null ? dto.status() : InvoiceStatus.CREATED);
        invoice.amount = dto.amount() != null ? dto.amount() : BigDecimal.ZERO;
        return invoice;
    }
}
