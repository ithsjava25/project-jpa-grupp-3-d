package org.example;

import jakarta.persistence.EntityManagerFactory;
import org.example.dto.UserDTO;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.example.util.JpaUtil;

public class App {
    public static void main(String[] args) {

        // Static EMF for whole application
        EntityManagerFactory emf = JpaUtil.getEntityManagerFactory();

        // Repository initialization
        UserRepository userRepository = new UserRepository(emf);
        // InvoiceRepository invoiceRepository = new InvoiceRepository(emf);
        // etc...

        // Service initialization
        UserService userService = new UserService(userRepository);
        // InvoiceService invoiceService = new InvoiceService(emf);


        // Test User example implementation
        UserDTO testUser = userService.register(
            "test",
            "test",
            "test@email.com",
            "password"
        );

        System.out.println(testUser);

    }
}
