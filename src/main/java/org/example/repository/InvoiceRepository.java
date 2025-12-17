package org.example.repository;

import jakarta.persistence.EntityManagerFactory;
import org.example.entity.Invoice;

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
        runInTransaction(em -> {
            Invoice invoice = em.find(Invoice.class, id);

            if (invoice != null) {
                em.remove(invoice);
            } else {
                System.out.println("Could not find Invoice with id: " + id );
            }

            return null;
        });
    }


}
