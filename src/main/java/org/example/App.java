package org.example;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.entity.User;
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
        User user = new User(
            "test", "test", "test@email.com", "password", "123456-0000"
        );
        userService.create(user);
    }
}
