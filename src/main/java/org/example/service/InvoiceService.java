package org.example.service;

import jakarta.persistence.EntityManagerFactory;
import org.example.repository.InvoiceItemRepository;
import org.example.repository.InvoiceRepository;

//One Service-class which is an Aggregate Root, An invoice and its lines/items are logically connected.
// One line/item has no reason to exist without an invoice.
// ie.  the method createInvoiceWithItems is more secure when both the lines/items and invoice are saved in the same transaction
public class InvoiceService {

    private final InvoiceItemRepository invoiceItemRepository;
    private final InvoiceRepository invoiceRepository;


    public InvoiceService(InvoiceItemRepository invoiceItemRepository, InvoiceRepository invoiceRepository) {
        this.invoiceItemRepository = invoiceItemRepository;
        this.invoiceRepository = invoiceRepository;
    }



}
