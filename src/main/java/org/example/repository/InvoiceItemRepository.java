package org.example.repository;

import jakarta.persistence.EntityManagerFactory;
import org.example.entity.invoice.InvoiceItem;

import java.util.UUID;

public class InvoiceItemRepository extends BaseRepository<InvoiceItem, UUID> {
    public InvoiceItemRepository(EntityManagerFactory emf) {
        super(emf, InvoiceItem.class);
    }
}
