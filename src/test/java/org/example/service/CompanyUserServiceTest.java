package org.example.service;

import org.example.entity.company.Company;
import org.example.entity.company.CompanyUser;
import org.example.entity.company.CompanyUserId;
import org.example.entity.user.User;
import org.example.repository.CompanyRepository;
import org.example.repository.CompanyUserRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
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

    private UUID companyId;
    private UUID userId;
    private String email;
    private Company company;
    private User user;

    @BeforeEach
    void setup() {
        companyId = UUID.randomUUID();
        userId = UUID.randomUUID();
        email = "test@email.com";

        company = new Company();
        company.setId(companyId);

        user = new User();
        user.setId(userId);
        user.setEmail(email);
    }

    @Test
    @DisplayName("Should add user to company successfully")
    void addUserToCompanyByEmail_success() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(companyUserRepository.findById(new CompanyUserId(userId, companyId)))
            .thenReturn(Optional.empty());

        companyUserService.addUserToCompanyByEmail(companyId, email);

        verify(companyUserRepository).create(argThat(cu ->
            cu.getUser().equals(user) &&
                cu.getCompany().equals(company)
        ));
        verifyNoMoreInteractions(userRepository, companyRepository, companyUserRepository);
    }

    @Test
    @DisplayName("Should throw exception if company not found")
    void addUserToCompanyByEmail_companyNotFound() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class,
            () -> companyUserService.addUserToCompanyByEmail(companyId, email));

        assertTrue(ex.getMessage().contains("Company not found"));
        verify(companyRepository).findById(companyId);
        verifyNoMoreInteractions(userRepository, companyRepository, companyUserRepository);
    }

    @Test
    @DisplayName("Should throw exception if user not found")
    void addUserToCompanyByEmail_userNotFound() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class,
            () -> companyUserService.addUserToCompanyByEmail(companyId, email));

        assertTrue(ex.getMessage().contains("User not found"));
        verify(companyRepository).findById(companyId);
        verify(userRepository).findByEmail(email);
        verifyNoMoreInteractions(userRepository, companyRepository, companyUserRepository);
    }

    @Test
    @DisplayName("Should throw exception if user already associated")
    void addUserToCompanyByEmail_userAlreadyAssociated() {
        CompanyUserId id = new CompanyUserId(userId, companyId);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(companyUserRepository.findById(id)).thenReturn(Optional.of(new CompanyUser()));

        Exception ex = assertThrows(IllegalArgumentException.class,
            () -> companyUserService.addUserToCompanyByEmail(companyId, email));

        assertTrue(ex.getMessage().contains("already associated"));
        verify(companyRepository).findById(companyId);
        verify(userRepository).findByEmail(email);
        verify(companyUserRepository).findById(id);
        verifyNoMoreInteractions(userRepository, companyRepository, companyUserRepository);
    }

    @Test
    @DisplayName("Should throw exception for null companyId")
    void addUserToCompanyByEmail_nullCompanyId_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> companyUserService.addUserToCompanyByEmail(null, email));
    }

    @Test
    @DisplayName("Should throw exception for null email")
    void addUserToCompanyByEmail_nullEmail_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> companyUserService.addUserToCompanyByEmail(companyId, null));
    }

    @Test
    @DisplayName("Should delete user from company successfully")
    void deleteUserFromCompany_success() {
        CompanyUserId id = new CompanyUserId(userId, companyId);
        CompanyUser cu = new CompanyUser();
        when(companyUserRepository.findById(id)).thenReturn(Optional.of(cu));

        companyUserService.deleteUserFromCompany(companyId, userId);

        verify(companyUserRepository).delete(argThat(u -> u.equals(cu)));
        verifyNoMoreInteractions(userRepository, companyRepository, companyUserRepository);
    }

    @Test
    @DisplayName("Should throw exception if user not part of company")
    void deleteUserFromCompany_notFound() {
        CompanyUserId id = new CompanyUserId(userId, companyId);
        when(companyUserRepository.findById(id)).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class,
            () -> companyUserService.deleteUserFromCompany(companyId, userId));

        assertTrue(ex.getMessage().contains("User is not part of company"));
        verify(companyUserRepository).findById(id);
        verifyNoMoreInteractions(userRepository, companyRepository, companyUserRepository);
    }

    @Test
    @DisplayName("Should return true if user associated with company")
    void isUserAssociatedWithCompany_true() {
        CompanyUserId id = new CompanyUserId(userId, companyId);
        when(companyUserRepository.findById(id)).thenReturn(Optional.of(new CompanyUser()));

        boolean result = companyUserService.isUserAssociatedWithCompany(userId, companyId);

        assertTrue(result);
        verify(companyUserRepository).findById(id);
    }

    @Test
    @DisplayName("Should return false if user not associated with company")
    void isUserAssociatedWithCompany_false() {
        CompanyUserId id = new CompanyUserId(userId, companyId);
        when(companyUserRepository.findById(id)).thenReturn(Optional.empty());

        boolean result = companyUserService.isUserAssociatedWithCompany(userId, companyId);

        assertFalse(result);
        verify(companyUserRepository).findById(id);
    }

    @Test
    @DisplayName("Should return all users of a company")
    void getCompanyUsers_success() {
        List<CompanyUser> users = List.of(new CompanyUser(), new CompanyUser());
        when(companyUserRepository.findByCompanyId(companyId)).thenReturn(users);

        List<CompanyUser> result = companyUserService.getCompanyUsers(companyId);

        assertEquals(2, result.size());
        assertTrue(result.containsAll(users));
        verify(companyUserRepository).findByCompanyId(companyId);
    }

    @Test
    @DisplayName("Should return all companies of a user")
    void getUserCompanies_success() {
        List<CompanyUser> companies = List.of(new CompanyUser(), new CompanyUser());
        when(companyUserRepository.findByUserId(userId)).thenReturn(companies);

        List<CompanyUser> result = companyUserService.getUserCompanies(userId);

        assertEquals(2, result.size());
        assertTrue(result.containsAll(companies));
        verify(companyUserRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("Should return empty list if user is not part of any company")
    void getUserCompanies_empty() {
        when(companyUserRepository.findByUserId(userId)).thenReturn(List.of());

        List<CompanyUser> result = companyUserService.getUserCompanies(userId);

        assertTrue(result.isEmpty());
        verify(companyUserRepository).findByUserId(userId);
        verifyNoMoreInteractions(userRepository, companyRepository, companyUserRepository);
    }
}
