package org.example;

import jakarta.persistence.EntityManagerFactory;
import org.example.dto.ClientDTO;
import org.example.dto.CompanyDTO;
import org.example.dto.UserDTO;
import org.example.entity.Company;
import org.example.entity.CompanyUser;
import org.example.repository.ClientRepository;
import org.example.repository.CompanyRepository;
import org.example.repository.CompanyUserRepository;
import org.example.repository.UserRepository;
import org.example.service.ClientService;
import org.example.service.CompanyService;
import org.example.service.CompanyUserService;
import org.example.service.UserService;
import org.example.auth.AuthService;
import org.example.util.JpaUtil;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * CLI Application for Invoice Management System
 * <p>
 * Workflow:
 * 1. User logs in or registers
 * 2. User creates or selects a company (creator is auto-associated)
 * 3. All operations are scoped to the current company
 * <p>
 * Note: Invoice and InvoiceItem functionality is commented out
 * as it's being worked on in a different branch.
 */
public class CliApp {
    private final EntityManagerFactory emf;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CompanyUserRepository companyUserRepository;
    private final ClientRepository clientRepository;

    private final UserService userService;
    private final AuthService authService;
    private final CompanyService companyService;
    private final CompanyUserService companyUserService;
    private final ClientService clientService;

    private final Scanner scanner;

    // Current session state
    private UUID currentUserId;
    private UUID currentCompanyId;
    private UserDTO currentUser;
    private CompanyDTO currentCompany;

    public CliApp() {
        this.emf = JpaUtil.getEntityManagerFactory();
        this.scanner = new Scanner(System.in);

        // Initialize repositories
        this.userRepository = new UserRepository(emf);
        this.companyRepository = new CompanyRepository(emf);
        this.companyUserRepository = new CompanyUserRepository(emf);
        this.clientRepository = new ClientRepository(emf);

        // Initialize services
        this.userService = new UserService(userRepository);
        this.authService = new AuthService(userRepository, userService);
        this.companyService = new CompanyService(companyRepository, companyUserRepository, userRepository);
        this.companyUserService = new CompanyUserService(userRepository, companyUserRepository, companyRepository);
        this.clientService = new ClientService(clientRepository, companyRepository);
    }

    public void run() {
        System.out.println("=== Invoice Management System ===\n");

        // Step 1: Authentication (with retry)
        while (!authenticate()) {
            System.out.println("\nWould you like to try again? (yes/no): ");
            String retry = scanner.nextLine().trim().toLowerCase();
            if (!"yes".equals(retry)) {
                System.out.println("Exiting...");
                return;
            }
        }

        // Step 2: Company setup (with retry)
        while (!setupCompany()) {
            System.out.println("\nWould you like to try again? (yes/no): ");
            String retry = scanner.nextLine().trim().toLowerCase();
            if (!"yes".equals(retry)) {
                System.out.println("Exiting...");
                return;
            }
        }

        // Step 3: Main menu loop
        mainMenu();

        scanner.close();
    }

    private boolean authenticate() {
        System.out.println("--- Authentication ---");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.print("Choose option (1-2): ");

        int choice = readInt();

        if (choice == 1) {
            return login();
        } else if (choice == 2) {
            return register();
        } else {
            System.out.println("Invalid choice.");
            return false;
        }
    }

    private boolean login() {
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = readPassword();

        try {
            currentUser = authService.authenticate(email, password);
            currentUserId = currentUser.id();
            System.out.println("✓ Login successful! Welcome, " + currentUser.firstName() + " " + currentUser.lastName());
            return true;
        } catch (Exception e) {
            System.out.println("✗ Login failed: " + e.getMessage());
            return false;
        }
    }

    private boolean register() {
        System.out.print("First Name: ");
        String firstName = scanner.nextLine().trim();

        System.out.print("Last Name: ");
        String lastName = scanner.nextLine().trim();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = readPassword();

        try {
            currentUser = userService.register(firstName, lastName, email, password);
            currentUserId = currentUser.id();
            System.out.println("✓ Registration successful! Welcome, " + currentUser.firstName() + " " + currentUser.lastName());
            return true;
        } catch (Exception e) {
            System.out.println("✗ Registration failed: " + e.getMessage());
            return false;
        }
    }

    private String readPassword() {
        return scanner.nextLine().trim();
    }

    private boolean setupCompany() {
        System.out.println("\n--- Company Setup ---");
        System.out.println("1. Create new company");
        System.out.println("2. Select existing company");
        System.out.print("Choose option (1-2): ");

        int choice = readInt();

        if (choice == 1) {
            return createCompany();
        } else if (choice == 2) {
            return selectCompany();
        } else {
            System.out.println("Invalid choice.");
            return false;
        }
    }

    private boolean createCompany() {
        System.out.println("\n--- Create Company ---");
        System.out.print("Organization Number: ");
        String orgNum = scanner.nextLine().trim();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Phone Number: ");
        String phoneNumber = scanner.nextLine().trim();

        System.out.print("Company Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Address: ");
        String address = scanner.nextLine().trim();

        System.out.print("City: ");
        String city = scanner.nextLine().trim();

        System.out.print("Country: ");
        String country = scanner.nextLine().trim();

        try {
            // Creator is automatically associated with the company
            currentCompany = companyService.create(
                currentUserId,
                orgNum,
                email,
                phoneNumber,
                name,
                address,
                city,
                country
            );
            currentCompanyId = currentCompany.id();
            System.out.println("✓ Company created successfully!");
            System.out.println("  Company: " + currentCompany.name() + " (" + currentCompany.orgNum() + ")");
            System.out.println("  You have been automatically associated with this company.");
            return true;
        } catch (Exception e) {
            System.out.println("✗ Company creation failed: " + e.getMessage());
            return false;
        }
    }

    private boolean selectCompany() {
        try {
            // Get all companies the user is associated with
            List<CompanyUser> userCompanies = companyUserService.getUserCompanies(currentUserId);

            if (userCompanies.isEmpty()) {
                System.out.println("✗ You are not associated with any companies.");
                System.out.println("Please create a new company first.");
                return false;
            }

            System.out.println("\n--- Your Companies ---");
            for (int i = 0; i < userCompanies.size(); i++) {
                CompanyUser cu = userCompanies.get(i);
                Company company = cu.getCompany();
                System.out.println((i + 1) + ". " + company.getName() + " (" + company.getOrgNum() + ")");
            }

            System.out.print("\nSelect company (1-" + userCompanies.size() + "): ");
            int choice = readInt();

            if (choice < 1 || choice > userCompanies.size()) {
                System.out.println("✗ Invalid selection.");
                return false;
            }

            Company selectedCompany = userCompanies.get(choice - 1).getCompany();
            currentCompany = CompanyDTO.fromEntity(selectedCompany);
            currentCompanyId = currentCompany.id();
            System.out.println("✓ Company selected: " + currentCompany.name() + " (" + currentCompany.orgNum() + ")");
            return true;
        } catch (Exception e) {
            System.out.println("✗ Failed to select company: " + e.getMessage());
            return false;
        }
    }

    private void mainMenu() {
        while (true) {
            System.out.println("\n=== Main Menu ===");
            System.out.println("Current Company: " + currentCompany.name() + " (" + currentCompany.orgNum() + ")");
            System.out.println("1. Client Management");
            System.out.println("2. Invoice Management");
            System.out.println("3. Company Users");
            System.out.println("4. Company Settings");
            System.out.println("5. Switch Company");
            System.out.println("6. Logout");
            System.out.print("Choose option (1-6): ");

            int choice = readInt();

            switch (choice) {
                case 1 -> clientMenu();
                case 2 -> invoiceMenu();
                case 3 -> companyUserMenu();
                case 4 -> companySettingsMenu();
                case 5 -> {
                    if (setupCompany()) {
                        continue;
                    } else {
                        return;
                    }
                }
                case 6 -> {
                    System.out.println("Logging out...");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void clientMenu() {
        while (true) {
            System.out.println("\n--- Client Management ---");
            System.out.println("1. List Clients");
            System.out.println("2. Create Client");
            System.out.println("3. Update Client");
            System.out.println("4. Delete Client");
            System.out.println("5. Back to Main Menu");
            System.out.print("Choose option (1-5): ");

            int choice = readInt();

            switch (choice) {
                case 1 -> listClients();
                case 2 -> createClient();
                case 3 -> updateClient();
                case 4 -> deleteClient();
                case 5 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void listClients() {
        try {
            List<ClientDTO> clients = clientService.getClientsByCompany(currentCompanyId);
            if (clients.isEmpty()) {
                System.out.println("No clients found.");
            } else {
                System.out.println("\nClients:");
                for (ClientDTO client : clients) {
                    System.out.println("  ID: " + client.id());
                    System.out.println("  Name: " + client.firstName() + " " + client.lastName());
                    System.out.println("  Email: " + client.email());
                    System.out.println("  City: " + client.city());
                    System.out.println("  ---");
                }
            }
        } catch (Exception e) {
            System.out.println("✗ Failed to list clients: " + e.getMessage());
        }
    }

    private void createClient() {
        System.out.println("\n--- Create Client ---");
        System.out.print("First Name: ");
        String firstName = scanner.nextLine().trim();

        System.out.print("Last Name: ");
        String lastName = scanner.nextLine().trim();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Address: ");
        String address = scanner.nextLine().trim();

        System.out.print("City: ");
        String city = scanner.nextLine().trim();

        System.out.print("Country: ");
        String country = scanner.nextLine().trim();

        System.out.print("Phone Number: ");
        String phoneNumber = scanner.nextLine().trim();

        try {
            ClientDTO client = clientService.createClient(
                currentCompanyId,
                firstName,
                lastName,
                email,
                address,
                city,
                country,
                phoneNumber
            );
            System.out.println("✓ Client created successfully!");
            System.out.println("  ID: " + client.id());
            System.out.println("  Name: " + client.firstName() + " " + client.lastName());
        } catch (Exception e) {
            System.out.println("✗ Client creation failed: " + e.getMessage());
        }
    }

    private void updateClient() {
        System.out.print("\nEnter Client ID: ");
        String clientIdStr = scanner.nextLine().trim();

        try {
            UUID clientId = UUID.fromString(clientIdStr);

            // Verify client belongs to current company
            var clientOpt = clientService.findById(clientId);
            if (clientOpt.isEmpty()) {
                System.out.println("✗ Client not found.");
                return;
            }

            var client = clientOpt.get();
            // Verify client belongs to current company
            if (!client.getCompany().getId().equals(currentCompanyId)) {
                System.out.println("✗ Client does not belong to current company.");
                return;
            }

            System.out.println("Leave blank to keep current value.");
            System.out.print("First Name [" + (client.getFirstName() != null ? client.getFirstName() : "") + "]: ");
            String firstName = scanner.nextLine().trim();

            System.out.print("Last Name [" + (client.getLastName() != null ? client.getLastName() : "") + "]: ");
            String lastName = scanner.nextLine().trim();

            System.out.print("Email [" + (client.getEmail() != null ? client.getEmail() : "") + "]: ");
            String email = scanner.nextLine().trim();

            System.out.print("Address [" + (client.getAddress() != null ? client.getAddress() : "") + "]: ");
            String address = scanner.nextLine().trim();

            System.out.print("City [" + (client.getCity() != null ? client.getCity() : "") + "]: ");
            String city = scanner.nextLine().trim();

            System.out.print("Country [" + (client.getCountry() != null ? client.getCountry() : "") + "]: ");
            String country = scanner.nextLine().trim();

            System.out.print("Phone Number [" + (client.getPhoneNumber() != null ? client.getPhoneNumber() : "") + "]: ");
            String phoneNumber = scanner.nextLine().trim();

            ClientDTO updated = clientService.updateClient(
                clientId,
                firstName.isEmpty() ? null : firstName,
                lastName.isEmpty() ? null : lastName,
                email.isEmpty() ? null : email,
                address.isEmpty() ? null : address,
                city.isEmpty() ? null : city,
                country.isEmpty() ? null : country,
                phoneNumber.isEmpty() ? null : phoneNumber
            );

            System.out.println("✓ Client updated successfully!");
            System.out.println("  Name: " + updated.firstName() + " " + updated.lastName());
        } catch (Exception e) {
            System.out.println("✗ Client update failed: " + e.getMessage());
        }
    }

    private void deleteClient() {
        System.out.print("\nEnter Client ID to delete: ");
        String clientIdStr = scanner.nextLine().trim();

        try {
            UUID clientId = UUID.fromString(clientIdStr);

            // Verify client belongs to current company
            var clientOpt = clientRepository.findById(clientId);
            if (clientOpt.isEmpty()) {
                System.out.println("✗ Client not found.");
                return;
            }

            var client = clientOpt.get();
            if (!client.getCompany().getId().equals(currentCompanyId)) {
                System.out.println("✗ Client does not belong to current company.");
                return;
            }

            System.out.print("Are you sure you want to delete this client? (yes/no): ");
            String confirm = scanner.nextLine().trim().toLowerCase();

            if ("yes".equals(confirm)) {
                clientService.deleteClient(clientId);
                System.out.println("✓ Client deleted successfully!");
            } else {
                System.out.println("Deletion cancelled.");
            }
        } catch (Exception e) {
            System.out.println("✗ Client deletion failed: " + e.getMessage());
        }
    }

    private void invoiceMenu() {
        // TODO: Invoice functionality is being worked on in a different branch
        // The following operations are commented out but show the intended flow

        System.out.println("\n--- Invoice Management ---");
        System.out.println("⚠️  Invoice functionality is currently under development");
        System.out.println("The following operations will be available:");
        System.out.println("1. List Invoices");
        System.out.println("2. Create Invoice");
        System.out.println("3. Update Invoice Status");
        System.out.println("4. Add Invoice Item");
        System.out.println("5. Remove Invoice Item");
        System.out.println("6. Update Invoice Item");
        System.out.println("7. Delete Invoice");
        System.out.println("8. Back to Main Menu");
        System.out.print("Choose option (8 to go back): ");

        int choice = readInt();
        if (choice == 8) {
            return;
        }

        System.out.println("This feature is not yet implemented.");

        /* TODO: Uncomment when InvoiceService and InvoiceItemService are implemented

        while (true) {
            System.out.println("\n--- Invoice Management ---");
            System.out.println("1. List Invoices");
            System.out.println("2. Create Invoice");
            System.out.println("3. Update Invoice Status");
            System.out.println("4. Invoice Items");
            System.out.println("5. Delete Invoice");
            System.out.println("6. Back to Main Menu");
            System.out.print("Choose option (1-6): ");

            int choice = readInt();

            switch (choice) {
                case 1 -> listInvoices();
                case 2 -> createInvoice();
                case 3 -> updateInvoiceStatus();
                case 4 -> invoiceItemMenu();
                case 5 -> deleteInvoice();
                case 6 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
        */
    }

    /* TODO: Uncomment when InvoiceService is implemented

    private void listInvoices() {
        try {
            // List<InvoiceDTO> invoices = invoiceService.getInvoicesByCompany(currentCompanyId);
            // Display invoices...
            System.out.println("List invoices functionality - to be implemented");
        } catch (Exception e) {
            System.out.println("✗ Failed to list invoices: " + e.getMessage());
        }
    }

    private void createInvoice() {
        System.out.println("\n--- Create Invoice ---");
        // 1. List clients for selection
        // 2. Select client
        // 3. Enter invoice number
        // 4. Enter due date
        // 5. Create invoice with status CREATED
        // 6. Add invoice items
        System.out.println("Create invoice functionality - to be implemented");
    }

    private void updateInvoiceStatus() {
        System.out.print("\nEnter Invoice ID: ");
        String invoiceIdStr = scanner.nextLine().trim();

        System.out.println("Available statuses: CREATED, SENT, PAID, OVERDUE, CANCELLED");
        System.out.print("Enter new status: ");
        String statusStr = scanner.nextLine().trim();

        try {
            // UUID invoiceId = UUID.fromString(invoiceIdStr);
            // InvoiceStatus status = InvoiceStatus.valueOf(statusStr);
            // invoiceService.updateInvoiceStatus(invoiceId, status);
            System.out.println("✓ Invoice status updated successfully!");
        } catch (Exception e) {
            System.out.println("✗ Failed to update invoice status: " + e.getMessage());
        }
    }

    private void invoiceItemMenu() {
        while (true) {
            System.out.println("\n--- Invoice Items ---");
            System.out.println("1. List Invoice Items");
            System.out.println("2. Add Invoice Item");
            System.out.println("3. Update Invoice Item");
            System.out.println("4. Remove Invoice Item");
            System.out.println("5. Back to Invoice Menu");
            System.out.print("Choose option (1-5): ");

            int choice = readInt();

            switch (choice) {
                case 1 -> listInvoiceItems();
                case 2 -> addInvoiceItem();
                case 3 -> updateInvoiceItem();
                case 4 -> removeInvoiceItem();
                case 5 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void listInvoiceItems() {
        System.out.print("\nEnter Invoice ID: ");
        String invoiceIdStr = scanner.nextLine().trim();

        try {
            // UUID invoiceId = UUID.fromString(invoiceIdStr);
            // List<InvoiceItemDTO> items = invoiceItemService.getItemsByInvoice(invoiceId);
            // Display items...
            System.out.println("List invoice items functionality - to be implemented");
        } catch (Exception e) {
            System.out.println("✗ Failed to list invoice items: " + e.getMessage());
        }
    }

    private void addInvoiceItem() {
        System.out.print("\nEnter Invoice ID: ");
        String invoiceIdStr = scanner.nextLine().trim();

        System.out.print("Quantity: ");
        int quantity = readInt();

        System.out.print("Unit Price: ");
        String unitPriceStr = scanner.nextLine().trim();

        try {
            // UUID invoiceId = UUID.fromString(invoiceIdStr);
            // BigDecimal unitPrice = new BigDecimal(unitPriceStr);
            // invoiceItemService.addItem(invoiceId, quantity, unitPrice);
            // invoiceService.recalculateTotal(invoiceId); // Recalculate invoice total
            System.out.println("✓ Invoice item added successfully!");
        } catch (Exception e) {
            System.out.println("✗ Failed to add invoice item: " + e.getMessage());
        }
    }

    private void updateInvoiceItem() {
        System.out.print("\nEnter Invoice Item ID: ");
        String itemIdStr = scanner.nextLine().trim();

        System.out.print("New Quantity: ");
        int quantity = readInt();

        System.out.print("New Unit Price: ");
        String unitPriceStr = scanner.nextLine().trim();

        try {
            // UUID itemId = UUID.fromString(itemIdStr);
            // BigDecimal unitPrice = new BigDecimal(unitPriceStr);
            // invoiceItemService.updateItem(itemId, quantity, unitPrice);
            // invoiceService.recalculateTotal(invoiceId); // Recalculate invoice total
            System.out.println("✓ Invoice item updated successfully!");
        } catch (Exception e) {
            System.out.println("✗ Failed to update invoice item: " + e.getMessage());
        }
    }

    private void removeInvoiceItem() {
        System.out.print("\nEnter Invoice Item ID to remove: ");
        String itemIdStr = scanner.nextLine().trim();

        try {
            // UUID itemId = UUID.fromString(itemIdStr);
            // invoiceItemService.removeItem(itemId);
            // invoiceService.recalculateTotal(invoiceId); // Recalculate invoice total
            System.out.println("✓ Invoice item removed successfully!");
        } catch (Exception e) {
            System.out.println("✗ Failed to remove invoice item: " + e.getMessage());
        }
    }

    private void deleteInvoice() {
        System.out.print("\nEnter Invoice ID to delete: ");
        String invoiceIdStr = scanner.nextLine().trim();

        System.out.print("Are you sure you want to delete this invoice? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if ("yes".equals(confirm)) {
            try {
                // UUID invoiceId = UUID.fromString(invoiceIdStr);
                // invoiceService.deleteInvoice(invoiceId);
                System.out.println("✓ Invoice deleted successfully!");
            } catch (Exception e) {
                System.out.println("✗ Failed to delete invoice: " + e.getMessage());
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
    }
    */

    private void companyUserMenu() {
        while (true) {
            System.out.println("\n--- Company Users ---");
            System.out.println("1. List Company Users");
            System.out.println("2. Add User to Company");
            System.out.println("3. Remove User from Company");
            System.out.println("4. Back to Main Menu");
            System.out.print("Choose option (1-4): ");

            int choice = readInt();

            switch (choice) {
                case 1 -> listCompanyUsers();
                case 2 -> addUserToCompany();
                case 3 -> removeUserFromCompany();
                case 4 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void listCompanyUsers() {
        try {
            List<CompanyUser> companyUsers = companyUserService.getCompanyUsers(currentCompanyId);
            if (companyUsers.isEmpty()) {
                System.out.println("No users associated with this company.");
            } else {
                System.out.println("\nCompany Users:");
                for (CompanyUser cu : companyUsers) {
                    System.out.println("  User ID: " + cu.getUser().getId());
                    System.out.println("  Name: " + cu.getUser().getFirstName() + " " + cu.getUser().getLastName());
                    System.out.println("  Email: " + cu.getUser().getEmail());
                    System.out.println("  ---");
                }
            }
        } catch (Exception e) {
            System.out.println("✗ Failed to list company users: " + e.getMessage());
        }
    }

    private void addUserToCompany() {
        System.out.print("\nEnter user email to invite: ");
        String email = scanner.nextLine().trim();

        try {
            companyUserService.addUserToCompanyByEmail(currentCompanyId, email);
            System.out.println("✓ User added to company successfully!");
        } catch (Exception e) {
            System.out.println("✗ Failed to add user: " + e.getMessage());
        }
    }

    private void removeUserFromCompany() {
        // List users first to make selection easier
        try {
            List<CompanyUser> companyUsers = companyUserService.getCompanyUsers(currentCompanyId);
            if (companyUsers.isEmpty()) {
                System.out.println("No users to remove.");
                return;
            }

            System.out.println("\n--- Company Users ---");
            for (int i = 0; i < companyUsers.size(); i++) {
                CompanyUser cu = companyUsers.get(i);
                System.out.println((i + 1) + ". " + cu.getUser().getFirstName() + " " + cu.getUser().getLastName() + " (" + cu.getUser().getEmail() + ")");
            }

            System.out.print("\nSelect user to remove (1-" + companyUsers.size() + "): ");
            int choice = readInt();

            if (choice < 1 || choice > companyUsers.size()) {
                System.out.println("✗ Invalid selection.");
                return;
            }

            UUID userId = companyUsers.get(choice - 1).getUser().getId();
            if (userId.equals(currentUserId)) {
                System.out.println("✗ Cannot remove yourself from the current company.");
                System.out.println("  Switch to another company first, or have another user remove you.");
                return;
                }

            companyUserService.deleteUserFromCompany(currentCompanyId, userId);
            System.out.println("✓ User removed from company successfully!");
        } catch (Exception e) {
            System.out.println("✗ Failed to remove user: " + e.getMessage());
        }
    }

    private void companySettingsMenu() {
        while (true) {
            System.out.println("\n--- Company Settings ---");
            System.out.println("Company: " + currentCompany.name());
            System.out.println("Org Num: " + currentCompany.orgNum());
            System.out.println("Email: " + currentCompany.email());
            System.out.println("1. Update Company");
            System.out.println("2. View Company Details");
            System.out.println("3. Back to Main Menu");
            System.out.print("Choose option (1-3): ");

            int choice = readInt();

            switch (choice) {
                case 1 -> updateCompany();
                case 2 -> viewCompanyDetails();
                case 3 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void updateCompany() {
        System.out.println("\n--- Update Company ---");
        System.out.println("Leave blank to keep current value.");
        System.out.print("Name [" + currentCompany.name() + "]: ");
        String name = scanner.nextLine().trim();

        System.out.print("Email [" + currentCompany.email() + "]: ");
        String email = scanner.nextLine().trim();

        System.out.print("Address [" + currentCompany.address() + "]: ");
        String address = scanner.nextLine().trim();

        System.out.print("City [" + currentCompany.city() + "]: ");
        String city = scanner.nextLine().trim();

        System.out.print("Country [" + currentCompany.country() + "]: ");
        String country = scanner.nextLine().trim();

        System.out.print("Phone Number [" + currentCompany.phoneNumber() + "]: ");
        String phoneNumber = scanner.nextLine().trim();

        try {
            currentCompany = companyService.update(
                currentCompanyId,
                name.isEmpty() ? null : name,
                null, // orgNum - typically shouldn't be changed
                email.isEmpty() ? null : email,
                address.isEmpty() ? null : address,
                city.isEmpty() ? null : city,
                country.isEmpty() ? null : country,
                phoneNumber.isEmpty() ? null : phoneNumber
            );
            System.out.println("✓ Company updated successfully!");
        } catch (Exception e) {
            System.out.println("✗ Company update failed: " + e.getMessage());
        }
    }

    private void viewCompanyDetails() {
        System.out.println("\n--- Company Details ---");
        System.out.println("ID: " + currentCompany.id());
        System.out.println("Name: " + currentCompany.name());
        System.out.println("Org Num: " + currentCompany.orgNum());
        System.out.println("Email: " + currentCompany.email());
        System.out.println("Phone: " + currentCompany.phoneNumber());
        System.out.println("Address: " + currentCompany.address());
        System.out.println("City: " + currentCompany.city());
        System.out.println("Country: " + currentCompany.country());
        System.out.println("Created: " + currentCompany.createdAt());
        System.out.println("Updated: " + currentCompany.updatedAt());
    }

    private int readInt() {
        try {
            String input = scanner.nextLine().trim();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

}
