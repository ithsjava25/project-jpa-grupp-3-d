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
    void addUserToCompanyByEmail_userNotFound() {
        UUID companyId = UUID.randomUUID();
        String email = "test@email.com";
        Company company = new Company();
        company.setId(companyId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class,
            () -> companyUserService.addUserToCompanyByEmail(companyId, email));

        assertTrue(ex.getMessage().contains("User not found"));
    }

    @Test
    void addUserToCompanyByEmail_userAlreadyAssociated() {
        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String email = "test@email.com";
        Company company = new Company();
        company.setId(companyId);
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        CompanyUserId id = new CompanyUserId(userId, companyId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(companyUserRepository.findById(id)).thenReturn(Optional.of(new CompanyUser()));

        Exception ex = assertThrows(IllegalArgumentException.class,
            () -> companyUserService.addUserToCompanyByEmail(companyId, email));

        assertTrue(ex.getMessage().contains("already associated"));
    }

    @Test
    void deleteUserFromCompany_success() {
        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CompanyUserId id = new CompanyUserId(userId, companyId);
        CompanyUser cu = new CompanyUser();
        when(companyUserRepository.findById(id)).thenReturn(Optional.of(cu));

        companyUserService.deleteUserFromCompany(companyId, userId);

        verify(companyUserRepository, times(1)).delete(cu);
    }

    @Test
    void deleteUserFromCompany_notFound() {
        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CompanyUserId id = new CompanyUserId(userId, companyId);

        when(companyUserRepository.findById(id)).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class,
            () -> companyUserService.deleteUserFromCompany(companyId, userId));

        assertTrue(ex.getMessage().contains("User is not part of company"));
    }

    @Test
    void isUserAssociatedWithCompany_true() {
        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CompanyUserId id = new CompanyUserId(userId, companyId);
        when(companyUserRepository.findById(id)).thenReturn(Optional.of(new CompanyUser()));

        boolean result = companyUserService.isUserAssociatedWithCompany(userId, companyId);

        assertTrue(result);
    }

    @Test
    void isUserAssociatedWithCompany_false() {
        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CompanyUserId id = new CompanyUserId(userId, companyId);
        when(companyUserRepository.findById(id)).thenReturn(Optional.empty());

        boolean result = companyUserService.isUserAssociatedWithCompany(userId, companyId);

        assertFalse(result);
    }

    @Test
    void getCompanyUsers_success() {
        UUID companyId = UUID.randomUUID();
        List<CompanyUser> users = List.of(new CompanyUser(), new CompanyUser());
        when(companyUserRepository.findByCompanyId(companyId)).thenReturn(users);

        List<CompanyUser> result = companyUserService.getCompanyUsers(companyId);

        assertEquals(2, result.size());
    }

    @Test
    void getUserCompanies_success() {
        UUID userId = UUID.randomUUID();
        List<CompanyUser> companies = List.of(new CompanyUser(), new CompanyUser(), new CompanyUser());
        when(companyUserRepository.findByUserId(userId)).thenReturn(companies);

        List<CompanyUser> result = companyUserService.getUserCompanies(userId);

        assertEquals(3, result.size());
    }
}
