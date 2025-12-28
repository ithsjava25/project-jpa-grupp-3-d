package org.example.service;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.example.dto.InvoiceDTO;
import org.example.dto.InvoiceItemDTO;
import org.example.entity.Client;
import org.example.entity.Invoice;
import org.example.entity.InvoiceItem;
import org.example.entity.InvoiceStatus;
import org.example.repository.ClientRepository;
import org.example.repository.CompanyRepository;
import org.example.repository.InvoiceItemRepository;
import org.example.repository.InvoiceRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

//One Service-class which is an Aggregate Root, An invoice and its lines/items are logically connected.
// One line/item has no reason to exist without an invoice.
// ie.  the method createInvoiceWithItems is more secure when both the lines/items and invoice are saved in the same transaction
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    //fields to be able to connect invoices to clients and companies
    private final CompanyService companyService;
    private final CompanyUserService companyUserService;
    private final ClientService clientService;


    public InvoiceService( ClientService clientService,
                           CompanyUserService companyUserService,
                           InvoiceRepository invoiceRepository,
                            CompanyService companyService) {
        this.invoiceRepository = invoiceRepository;
        this.companyService=companyService;
        this.companyUserService = companyUserService;
        this.clientService = clientService;
    }
//user sends us a draftDTO with no ID no created_at and total:amount not confirmed
    // returns a DTO to the frontend:
    //input DTO: "I want to create this"
    //Method: Translating dto to entity
    //Return DTO: Confirmation: this has been created with the definitive details)
    //we don't want to return entities because of safety or LazyInitializationException
    public InvoiceDTO createInvoice(InvoiceDTO dto, UUID userId) {
        //validation:
        validateUserAccess(userId, dto.companyId());

        invoiceRepository.findByInvoiceNumber(dto.number()).ifPresent(existing -> {
            throw new IllegalArgumentException("Invoice number " + dto.number() + " already in use.");
        });
        // Translating DTO to Entity (to save in DB)
        Invoice invoice = mapToEntity(dto);

        // creates the entity (DB creates ID and timestamp)
        Invoice savedInvoice = invoiceRepository.create(invoice);

        // translate entity to DTO (to give the user a complete receipt)
        return mapToDTO(savedInvoice);
    }



// method to find an invoice by ID
    //if invoice is found, it gets mapped from entity to DTO.
    // the user receives the actual total amount since calculate total is integrated here also
    public Optional<InvoiceDTO> getInvoiceById(UUID id, UUID userId, UUID companyId) {

        validateUserAccess(userId, companyId);

        return invoiceRepository.findByIdWithItems(id)
            .map(invoice -> {
                if (!invoice.getCompany().getId().equals(companyId)) {
                    throw new SecurityException("Access denied: Invoice does not belong to the specified company.");
                }
                return mapToDTO(invoice);
            });

    }



    public void updateStatus(UUID id, InvoiceStatus newStatus, UUID userId, UUID companyId) {
        validateUserAccess(userId, companyId);

        Invoice invoice=invoiceRepository.findById(id)
            .orElseThrow(()->new EntityNotFoundException("Invoice not found"));

        if (!invoice.getCompany().getId().equals(companyId)) {
            throw new SecurityException("Unauthorized access to this invoice.");
        }

        //updates the status of the entity
        invoice.setStatus(newStatus);

        //saves the update to the database
        invoiceRepository.update(invoice);

    }

    public void deleteById(UUID id, UUID userId, UUID companyId) {
        validateUserAccess(userId, companyId);

        invoiceRepository.findById(id).ifPresent(invoice -> {
            if (!invoice.getCompany().getId().equals(companyId)) {
                throw new SecurityException("Unauthorized");
            }
            invoiceRepository.delete(invoice);
        });

    }


    //method to update items on an existing invoice
    public InvoiceDTO updateInvoiceItems(UUID id, Set<InvoiceItemDTO> newItemDtos, UUID userId, UUID companyId) {
        validateUserAccess(userId, companyId);

        Invoice invoice=invoiceRepository.findByIdWithItems(id)
            .orElseThrow(()->new EntityNotFoundException("Invoice not found"));

        if (!invoice.getCompany().getId().equals(companyId)) {
            throw new SecurityException("Unauthorized");
        }

        //clears the current set to handle deletions (orphan removal handles SQL)
        invoice.getItems().clear();

        //Maps the new DTOs to entities and adds them
        for (InvoiceItemDTO itemDto : newItemDtos) {
            InvoiceItem item = new InvoiceItem();
            item.setQuantity(itemDto.quantity());
            item.setUnitPrice(itemDto.unitPrice());
            item.setInvoice(invoice); // Viktigt för @ManyToOne-kopplingen
            invoice.getItems().add(item);
        }
        invoice.setAmount(calculateTotal(invoice.getItems()));
        //saves the complete Invoice
        Invoice updatedInvoice=invoiceRepository.update(invoice);

        //returns the updated invoice as DTO
        return mapToDTO(updatedInvoice);
    }
    public List<InvoiceDTO> getInvoicesByClientForCompany(UUID clientId, UUID companyId, UUID userId) {
        //check if user is connected to the company
        validateUserAccess(userId, companyId);

        clientService.validateClientAccess(clientId, companyId);

        return invoiceRepository.findAllByClientId(clientId).stream()
            .map(this::mapToDTO)
            .toList();
    }


    public List<InvoiceDTO> getCompanyInvoices(UUID userId, UUID companyId) {
        validateUserAccess(userId, companyId);

        return invoiceRepository.findAllByCompanyId(companyId).stream()
            .map(this::mapToDTO)
            .toList();
    }


    public List<InvoiceDTO> getInvoicesByStatusForCompany(UUID userId, UUID companyId, InvoiceStatus status) {
        validateUserAccess(userId, companyId);

        return invoiceRepository.findAllByStatusAndCompany(status, companyId).stream()
            .map(this::mapToDTO)
            .toList();
    }

    private void validateUserAccess(UUID userId, UUID companyId) {
        if (!companyUserService.isUserAssociatedWithCompany(userId, companyId)) {
            throw new SecurityException("User " + userId + " is not authorized to access company " + companyId);
        }
    }


    private BigDecimal calculateTotal(Set<InvoiceItem> items) {
        if (items == null) return BigDecimal.ZERO;
        return items.stream()
            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }



//help method to convert from DTO to entity and connect each invoice to the client or company
    private Invoice mapToEntity(InvoiceDTO dto) {
        Invoice invoice = new Invoice();
        invoice.setNumber(dto.number());
        invoice.setDueDate(dto.dueDate());
        invoice.setStatus(InvoiceStatus.CREATED);


        if (dto.clientId() != null ) {
            invoice.setClient(clientService.getClientEntity(dto.clientId()));
        }

        //connection to Company
        if (dto.companyId() != null) {
            invoice.setCompany(companyService.getCompanyEntity(dto.companyId()));
        }

        //handling of missing values to avoid SQL Constraint Violation
        if (dto.amount() != null) {
            invoice.setAmount(dto.amount());
        } else {
            // An invoice cannot be null i DB, sets 0.00 as standard
            invoice.setAmount(BigDecimal.ZERO);
        }

        return invoice;
    }



    //method that converts data from entity to DTO (easy for user)
    private InvoiceDTO mapToDTO(Invoice invoice) {
        return InvoiceDTO.builder()
            .id(invoice.getId())
            .number(invoice.getNumber())
            .status(invoice.getStatus())
            .dueDate(invoice.getDueDate())
            .createdAt(invoice.getCreatedAt())
            // Calculate the total amount based on the number of items
            .amount(calculateTotal(invoice.getItems()))
            .build();
    }




}
