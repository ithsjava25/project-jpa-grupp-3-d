package org.example;

import jakarta.persistence.EntityManagerFactory;
import org.example.dto.CompanyDTO;
import org.example.dto.UserDTO;
import org.example.entity.User;
import org.example.repository.CompanyRepository;
import org.example.repository.UserRepository;
import org.example.service.CompanyService;
import org.example.service.UserService;
import org.example.util.JpaUtil;

import java.time.LocalDateTime;

public class App {
    public static void main(String[] args) {

        // Static EMF for whole application
        EntityManagerFactory emf = JpaUtil.getEntityManagerFactory();

        // Repository initialization
        UserRepository userRepository = new UserRepository(emf);
        CompanyRepository companyRepository = new CompanyRepository(emf);

        // Service initialization
        UserService userService = new UserService(userRepository);
        CompanyService companyService = new CompanyService(companyRepository);


        // Test User example implementation
        UserDTO testUser = userService.create(
            "test",
            "test",
            "test@email.com",
            "password"
        );

        System.out.println(testUser);


        // Test Company Example implementation

        CompanyDTO testCompany = companyService.create(
            "1234567-0000",
            "billing@test.com",
            "0701234567",
            "Test AB",
            "Testgatan 1",
            "Gothenburg",
            "Sweden"
        );

        System.out.println(testCompany);

    }
}
