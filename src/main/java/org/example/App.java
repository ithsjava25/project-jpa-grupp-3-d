package org.example;

import jakarta.persistence.EntityManagerFactory;
import org.example.dto.CompanyDTO;
import org.example.dto.UserDTO;
import org.example.entity.Company;
import org.example.entity.User;
import org.example.repository.CompanyRepository;
import org.example.repository.UserCompanyRepository;
import org.example.repository.UserRepository;
import org.example.service.CompanyService;
import org.example.service.UserCompanyService;
import org.example.service.UserService;
import org.example.util.JpaUtil;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class App {
    public static void main(String[] args) {

        // Static EMF for whole application
        EntityManagerFactory emf = JpaUtil.getEntityManagerFactory();

        // Repository initialization
        UserRepository userRepository = new UserRepository(emf);
        CompanyRepository companyRepository = new CompanyRepository(emf);
        UserCompanyRepository userCompanyRepository = new UserCompanyRepository(emf);

        // Service initialization
        UserService userService = new UserService(userRepository);
        CompanyService companyService = new CompanyService(companyRepository);
        UserCompanyService userCompanyService = new UserCompanyService(userRepository, userCompanyRepository, companyRepository);


        System.out.println("=== USER REGISTRATION ===");

        UserDTO user1 = userService.register(
            "Alice",
            "Andersson",
            "alice@test.com",
            "password123"
        );

        UserDTO user2 = userService.register(
            "Bob",
            "Berg",
            "bob@test.com",
            "password123"
        );

        System.out.println("Created user: " + user1);
        System.out.println("Created user: " + user2);

        System.out.println("\n=== COMPANY CREATION ===");

        CompanyDTO company = companyService.create(
            "556677-8899",
            "info@acme.se",
            "0701112233",
            "Acme AB",
            "Main Street 1",
            "Stockholm",
            "Sweden"
        );

        System.out.println("Created company: " + company);

        UUID companyId = company.id();
        UUID user1Id = user1.id();
        UUID user2Id = user2.id();

        System.out.println("\n=== ADD USERS TO COMPANY ===");

        userCompanyService.addUserToCompany(companyId, user1Id);
        userCompanyService.addUserToCompany(companyId, user2Id);

        System.out.println("Users added to company");

        System.out.println("\n=== REMOVE USER FROM COMPANY ===");

        userCompanyService.deleteUserFromCompany(companyId, user2Id);
    }
}
