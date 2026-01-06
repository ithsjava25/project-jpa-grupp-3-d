package org.example.repository;

import jakarta.persistence.EntityManagerFactory;
import org.example.entity.invoice.Invoice;
import org.example.entity.invoice.InvoiceStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InvoiceRepository extends BaseRepository<Invoice, UUID>{
    public InvoiceRepository(EntityManagerFactory emf) {
        super(emf, Invoice.class);
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


    //fetches both the invoice and items in one question thanks to join fetch.  also rprevents lazyInitialixationException
    public Optional<Invoice> findByIdWithItems(UUID id) {
        return executeRead(em -> {
            return em.createQuery(
                    "SELECT i FROM Invoice i LEFT JOIN FETCH i.items WHERE i.id = :id", Invoice.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
        });
    }

    //Fetch all invoices by a certain company
    public List<Invoice> findAllByCompanyId(UUID companyId) {
        return executeRead(em->{
            return em.createQuery(
                "SELECT i FROM Invoice i WHERE i.company.id = :companyId",  Invoice.class)
                    .setParameter("companyId", companyId)
                .getResultList();
        });
    }

    //Fetch all invoices for a certain client
    public List<Invoice> findAllByClientId (UUID clientId) {
        return executeRead(em->{
            return em.createQuery(
                "SELECT i FROM Invoice i WHERE i.client.id = :clientId",  Invoice.class)
                .setParameter("clientId", clientId)
                .getResultList();

        });
    }

    //find which state an invoice is in
    public List<Invoice> findAllByStatusAndCompany(InvoiceStatus status, UUID companyId) {
        return executeRead(em -> {
            return em.createQuery(
                    "SELECT i FROM Invoice i WHERE i.status = :status AND i.company.id = :companyId", Invoice.class)
                .setParameter("status", status)
                .setParameter("companyId", companyId)
                .getResultList();
        });
    }
}
