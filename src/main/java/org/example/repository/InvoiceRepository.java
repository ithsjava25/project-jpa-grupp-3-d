package org.example.repository;

import jakarta.persistence.EntityManagerFactory;
import org.example.entity.Invoice;

import java.util.Optional;
import java.util.UUID;

public class InvoiceRepository extends BaseRepository<Invoice, UUID>{
    protected InvoiceRepository(EntityManagerFactory emf) {
        super(emf, Invoice.class);
    }
//keep this method just for readability (method is already defined in Baserepository)
    public Invoice createInvoice(Invoice invoice) {
        return runInTransaction(em -> {
            em.persist(invoice);
            return invoice;
        });

    }
    //keep this method just for readability (method is already defined in Baserepository)
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


    /*
    Method to fetch the whole Invoice as an aggregate with all of its lines and fields.
    This is done with a single database query and JOIN FETCH.
    For example when we want to see all the data from the invoice at once.
     */
    public Optional<Invoice> findByIdWithItems(UUID id) {
        return executeRead(em -> {
            return em.createQuery(
                    "SELECT i FROM Invoice i LEFT JOIN FETCH i.items WHERE i.id = :id", Invoice.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
        });
    }


}
