package org.example.repository;

import jakarta.persistence.EntityManagerFactory;
import org.example.entity.Invoice;

import java.util.Optional;
import java.util.UUID;

public class InvoiceRepository extends BaseRepository<Invoice, UUID>{
    protected InvoiceRepository(EntityManagerFactory emf) {
        super(emf, Invoice.class);
    }

    public Invoice createInvoice(Invoice invoice) {
        return runInTransaction(em -> {
            em.persist(invoice);
            return invoice;
        });

    }

    public Invoice updateInvoice(Invoice invoice) {
        return runInTransaction(em -> {
             return em.merge(invoice);

        });
    }

    public void deleteById(UUID id) {
        findById(id).ifPresent(invoice -> {
            delete(invoice);
        });
    }

    public Optional<Invoice> findByInvoiceNumber(String number) {
        return executeRead(em -> {
            return em.createQuery(
                    "SELECT i FROM Invoice i WHERE i.number = :num", Invoice.class)
                .setParameter("num", number)
                .getResultStream()
                .findFirst();
        });
    }


}
