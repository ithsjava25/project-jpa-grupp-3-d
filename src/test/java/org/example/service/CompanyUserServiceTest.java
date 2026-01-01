package org.example.service;

import org.example.entity.Company;
import org.example.entity.CompanyUser;
import org.example.entity.CompanyUserId;
import org.example.entity.User;
import org.example.repository.CompanyRepository;
import org.example.repository.CompanyUserRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyUserRepository companyUserRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyUserService companyUserService;

    @Test
    void addUserToCompanyByEmail_success() {
        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String email = "test@email.com";

        Company company = new Company();
        company.setId(companyId);

        User user = new User();
        user.setId(userId);
        user.setEmail(email);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(companyUserRepository.findById(new CompanyUserId(userId, companyId)))
            .thenReturn(Optional.empty());

        companyUserService.addUserToCompanyByEmail(companyId, email);

        verify(companyUserRepository, times(1)).create(any(CompanyUser.class));
    }

    @Test
    void addUserToCompanyByEmail_companyNotFound() {
        UUID companyId = UUID.randomUUID();
        String email = "test@email.com";

        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class,
            () -> companyUserService.addUserToCompanyByEmail(companyId, email));

        assertTrue(ex.getMessage().contains("Company not found"));
    }

    @Test
    void deleteUserFromCompany() {
    }

    @Test
    void isUserAssociatedWithCompany() {
    }

    @Test
    void getCompanyUsers() {
    }

    @Test
    void getUserCompanies() {
    }
}
