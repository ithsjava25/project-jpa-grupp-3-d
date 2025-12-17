package org.example.repository;

import jakarta.persistence.EntityManagerFactory;
import org.example.entity.InvoiceItem;

import java.util.UUID;

public class InvoiceItemRepository extends BaseRepository<InvoiceItem, UUID> {
    protected InvoiceItemRepository(EntityManagerFactory emf, Class<InvoiceItem> entityClass) {
        super(emf, entityClass);
    }

    public InvoiceItem create(InvoiceItem invoiceItem) {
        return runInTransaction(em -> {
            em.persist(invoiceItem);
            return invoiceItem;
        });
    }

    public InvoiceItem update(InvoiceItem invoiceItem) {
        return runInTransaction(em -> {
            em.merge(invoiceItem);
            return invoiceItem;
        });
    }



}
