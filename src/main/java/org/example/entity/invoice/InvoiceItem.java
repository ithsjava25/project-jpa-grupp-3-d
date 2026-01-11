package org.example.entity.invoice;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "invoice_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "name", nullable = false)
    private String name;

    @Column (name= "quantity", nullable = false)
    private int quantity;

    @Column ( name= "unit_price", nullable = false)
    private BigDecimal unitPrice;
}
