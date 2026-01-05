package org.example.service;

import org.example.entity.company.Company;
import org.example.entity.company.CompanyUser;
import org.example.entity.company.CompanyUserId;
import org.example.entity.user.User;
import org.example.exception.EntityNotFoundException;
import org.example.exception.BusinessRuleException;
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

    @Mock
    private ValidationService validationService;

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
    void addUserToCompanyByEmail_Success() {
        doNothing().when(validationService).validateNotNull("companyId", companyId);
        doNothing().when(validationService).validateNotEmpty("email", email);
        doNothing().when(validationService).validateEmail(email);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(companyUserRepository.findById(new CompanyUserId(userId, companyId)))
            .thenReturn(Optional.empty());

        companyUserService.addUserToCompanyByEmail(companyId, email);

        verify(companyUserRepository).create(argThat(cu ->
            cu.getUser().equals(user) &&
                cu.getCompany().equals(company)
        ));
        verify(validationService).validateNotNull("companyId", companyId);
        verify(validationService).validateEmail(email);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException if company not found")
    void addUserToCompanyByEmail_CompanyNotFound_ThrowsEntityNotFoundException() {
        doNothing().when(validationService).validateNotNull("companyId", companyId);
        doNothing().when(validationService).validateNotEmpty("email", email);
        doNothing().when(validationService).validateEmail(email);

        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        Exception ex = assertThrows(EntityNotFoundException.class,
            () -> companyUserService.addUserToCompanyByEmail(companyId, email));

        assertTrue(ex.getMessage().contains("Company not found"));
        verify(companyRepository).findById(companyId);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException if user not found")
    void addUserToCompanyByEmail_UserNotFound_ThrowsEntityNotFoundException() {
        doNothing().when(validationService).validateNotNull("companyId", companyId);
        doNothing().when(validationService).validateNotEmpty("email", email);
        doNothing().when(validationService).validateEmail(email);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Exception ex = assertThrows(EntityNotFoundException.class,
            () -> companyUserService.addUserToCompanyByEmail(companyId, email));

        assertTrue(ex.getMessage().contains("User not found"));
        verify(companyRepository).findById(companyId);
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Should throw BusinessRuleException if user already associated")
    void addUserToCompanyByEmail_UserAlreadyAssociated_ThrowsBusinessRuleException() {
        CompanyUserId id = new CompanyUserId(userId, companyId);

        doNothing().when(validationService).validateNotNull("companyId", companyId);
        doNothing().when(validationService).validateNotEmpty("email", email);
        doNothing().when(validationService).validateEmail(email);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(companyUserRepository.findById(id)).thenReturn(Optional.of(new CompanyUser()));

        Exception ex = assertThrows(BusinessRuleException.class,
            () -> companyUserService.addUserToCompanyByEmail(companyId, email));

        assertTrue(ex.getMessage().contains("User is already associated with this company"));
        verify(companyRepository).findById(companyId);
        verify(userRepository).findByEmail(email);
        verify(companyUserRepository).findById(id);
    }

    @Test
    @DisplayName("Should delete user from company successfully")
    void deleteUserFromCompany_Success() {
        CompanyUserId id = new CompanyUserId(userId, companyId);
        CompanyUser cu = new CompanyUser();

        doNothing().when(validationService).validateNotNull("companyId", companyId);
        doNothing().when(validationService).validateNotNull("userId", userId);

        when(companyUserRepository.findById(id)).thenReturn(Optional.of(cu));

        companyUserService.deleteUserFromCompany(companyId, userId);

        verify(companyUserRepository).delete(argThat(u -> u.equals(cu)));
    }

    @Test
    @DisplayName("Should throw BusinessRuleException if user not part of company")
    void deleteUserFromCompany_UserNotPartOfCompany_ThrowsBusinessRuleException() {
        CompanyUserId id = new CompanyUserId(userId, companyId);

        doNothing().when(validationService).validateNotNull("companyId", companyId);
        doNothing().when(validationService).validateNotNull("userId", userId);

        when(companyUserRepository.findById(id)).thenReturn(Optional.empty());

        Exception ex = assertThrows(BusinessRuleException.class,
            () -> companyUserService.deleteUserFromCompany(companyId, userId));

        assertTrue(ex.getMessage().contains("User is not part of company"));
        verify(companyUserRepository).findById(id);
    }

    @Test
    @DisplayName("Should return all users of a company")
    void getCompanyUsers_Success() {
        List<CompanyUser> users = List.of(new CompanyUser(), new CompanyUser());

        doNothing().when(validationService).validateNotNull("companyId", companyId);
        when(companyUserRepository.findByCompanyId(companyId)).thenReturn(users);

        List<CompanyUser> result = companyUserService.getCompanyUsers(companyId);

        assertEquals(2, result.size());
        assertTrue(result.containsAll(users));
        verify(companyUserRepository).findByCompanyId(companyId);
        verify(validationService).validateNotNull("companyId", companyId);
    }

    @Test
    @DisplayName("Should return all companies of a user")
    void getUserCompanies_Success() {
        List<CompanyUser> companies = List.of(new CompanyUser(), new CompanyUser());

        doNothing().when(validationService).validateNotNull("userId", userId);
        when(companyUserRepository.findByUserId(userId)).thenReturn(companies);

        List<CompanyUser> result = companyUserService.getUserCompanies(userId);

        assertEquals(2, result.size());
        assertTrue(result.containsAll(companies));
        verify(companyUserRepository).findByUserId(userId);
        verify(validationService).validateNotNull("userId", userId);
    }
}
