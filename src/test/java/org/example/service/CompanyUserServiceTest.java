package org.example.service;

import org.example.entity.company.*;
import org.example.entity.user.User;
import org.example.exception.BusinessRuleException;
import org.example.exception.EntityNotFoundException;
import org.example.exception.ValidationException;
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
    @DisplayName("Should throw EntityNotFoundException if company not found")
    void addUserToCompanyByEmail_companyNotFound() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> companyUserService.addUserToCompanyByEmail(companyId, email)
        );

        verify(companyRepository).findById(companyId);
        verifyNoMoreInteractions(userRepository, companyRepository, companyUserRepository);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException if user not found")
    void addUserToCompanyByEmail_userNotFound() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> companyUserService.addUserToCompanyByEmail(companyId, email)
        );

        verify(companyRepository).findById(companyId);
        verify(userRepository).findByEmail(email);
        verifyNoMoreInteractions(userRepository, companyRepository, companyUserRepository);
    }

    @Test
    @DisplayName("Should throw BusinessRuleException if user already associated")
    void addUserToCompanyByEmail_userAlreadyAssociated() {
        CompanyUserId id = new CompanyUserId(userId, companyId);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(companyUserRepository.findById(id)).thenReturn(Optional.of(new CompanyUser()));

        assertThrows(BusinessRuleException.class,
            () -> companyUserService.addUserToCompanyByEmail(companyId, email)
        );

        verify(companyRepository).findById(companyId);
        verify(userRepository).findByEmail(email);
        verify(companyUserRepository).findById(id);
        verifyNoMoreInteractions(userRepository, companyRepository, companyUserRepository);
    }

    @Test
    @DisplayName("Should throw ValidationException for null companyId")
    void addUserToCompanyByEmail_nullCompanyId_throws() {
        assertThrows(ValidationException.class,
            () -> companyUserService.addUserToCompanyByEmail(null, email)
        );
    }

    @Test
    @DisplayName("Should throw ValidationException for null or blank email")
    void addUserToCompanyByEmail_nullEmail_throws() {
        assertThrows(ValidationException.class,
            () -> companyUserService.addUserToCompanyByEmail(companyId, null)
        );
        assertThrows(ValidationException.class,
            () -> companyUserService.addUserToCompanyByEmail(companyId, "")
        );
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
    @DisplayName("Should throw EntityNotFoundException if user not part of company")
    void deleteUserFromCompany_notFound() {
        CompanyUserId id = new CompanyUserId(userId, companyId);
        when(companyUserRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> companyUserService.deleteUserFromCompany(companyId, userId)
        );

        verify(companyUserRepository).findById(id);
        verifyNoMoreInteractions(userRepository, companyRepository, companyUserRepository);
    }

    @Test
    @DisplayName("Should throw ValidationException if companyId or userId is null")
    void deleteUserFromCompany_nullIds_throws() {
        assertThrows(ValidationException.class,
            () -> companyUserService.deleteUserFromCompany(null, userId)
        );
        assertThrows(ValidationException.class,
            () -> companyUserService.deleteUserFromCompany(companyId, null)
        );
        assertThrows(ValidationException.class,
            () -> companyUserService.deleteUserFromCompany(null, null)
        );
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
    @DisplayName("Should throw ValidationException if companyId is null when fetching users")
    void getCompanyUsers_nullCompanyId_throws() {
        assertThrows(ValidationException.class,
            () -> companyUserService.getCompanyUsers(null)
        );
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
    @DisplayName("Should throw ValidationException if userId is null when fetching companies")
    void getUserCompanies_nullUserId_throws() {
        assertThrows(ValidationException.class,
            () -> companyUserService.getUserCompanies(null)
        );
    }
}
