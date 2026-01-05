package org.example.service;

import org.example.entity.company.*;
import org.example.entity.user.User;
import org.example.repository.CompanyRepository;
import org.example.repository.CompanyUserRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyUserRepository companyUserRepository;

    @InjectMocks
    private CompanyService companyService;

    @Test
    @DisplayName("Should create company successfully")
    void createCompanySuccess() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        CreateCompanyDTO dto = new CreateCompanyDTO(
            "1234567890",
            "company@email.com",
            "0701234567",
            "TestCo",
            "Street 1",
            "City",
            "Country"
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(companyRepository.existsByOrgNum("1234567890")).thenReturn(false);

        doAnswer(invocation -> {
            Company c = invocation.getArgument(0);
            c.setId(UUID.randomUUID());
            return null;
        }).when(companyRepository).create(any(Company.class));

        CompanyDTO result = companyService.create(userId, dto);

        assertNotNull(result);
        assertEquals("TestCo", result.name());
        assertEquals("1234567890", result.orgNum());

        verify(companyRepository).create(any(Company.class));
        verify(companyUserRepository).create(any(CompanyUser.class));
    }

    @Test
    @DisplayName("Should create company with null optional fields")
    void createCompanyWithNullOptionalFields() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        CreateCompanyDTO dto = new CreateCompanyDTO(
            "1234567890",
            null,
            null,
            "TestCo",
            null,
            null,
            null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(companyRepository.existsByOrgNum("1234567890")).thenReturn(false);

        doAnswer(invocation -> {
            Company c = invocation.getArgument(0);
            c.setId(UUID.randomUUID());
            return null;
        }).when(companyRepository).create(any(Company.class));

        CompanyDTO result = companyService.create(userId, dto);

        assertNotNull(result);
        assertNull(result.email());
        assertNull(result.phoneNumber());
        assertNull(result.address());
    }

    @Test
    @DisplayName("Should throw exception if creator user not found")
    void createCompanyUserNotFound() {
        UUID userId = UUID.randomUUID();

        CreateCompanyDTO dto = new CreateCompanyDTO(
            "1234567890",
            "email@test.com",
            null,
            "TestCo",
            null,
            null,
            null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> companyService.create(userId, dto)
        );

        verify(companyRepository, never()).create(any());
        verify(companyUserRepository, never()).create(any());
    }

    @Test
    @DisplayName("Should throw exception if orgNum already exists")
    void createCompanyOrgNumAlreadyExists() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        CreateCompanyDTO dto = new CreateCompanyDTO(
            "1234567890",
            "email@test.com",
            null,
            "TestCo",
            null,
            null,
            null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(companyRepository.existsByOrgNum("1234567890")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
            () -> companyService.create(userId, dto)
        );

        verify(companyRepository, never()).create(any());
    }

    @Test
    @DisplayName("Should associate CompanyUser correctly")
    void createCompanyAssociatesCompanyUserCorrectly() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        CreateCompanyDTO dto = new CreateCompanyDTO(
            "1234567890",
            "email@test.com",
            null,
            "TestCo",
            null,
            null,
            null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(companyRepository.existsByOrgNum("1234567890")).thenReturn(false);

        doAnswer(invocation -> {
            Company c = invocation.getArgument(0);
            c.setId(UUID.randomUUID());
            return null;
        }).when(companyRepository).create(any());

        doAnswer(invocation -> {
            CompanyUser cu = invocation.getArgument(0);
            assertEquals(userId, cu.getUser().getId());
            assertNotNull(cu.getCompany());
            return null;
        }).when(companyUserRepository).create(any());

        companyService.create(userId, dto);
    }

    @Test
    @DisplayName("Should update company successfully")
    void updateCompanySuccess() {
        UUID companyId = UUID.randomUUID();
        Company company = new Company();
        company.setId(companyId);
        company.setName("OldName");
        company.setEmail("old@email.com");

        UpdateCompanyDTO dto = new UpdateCompanyDTO(
            companyId,
            "new@email.com",
            null,
            "NewName",
            null,
            null,
            null
        );

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        CompanyDTO result = companyService.update(dto);

        assertEquals("NewName", result.name());
        assertEquals("new@email.com", result.email());

        verify(companyRepository).update(company);
    }

    @Test
    @DisplayName("Should update company without overwriting with null values")
    void updateCompanyWithNullFields() {
        UUID companyId = UUID.randomUUID();
        Company company = new Company();
        company.setId(companyId);
        company.setName("OldName");
        company.setEmail("old@email.com");

        UpdateCompanyDTO dto = new UpdateCompanyDTO(
            companyId,
            null,
            null,
            null,
            null,
            null,
            null
        );

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        CompanyDTO result = companyService.update(dto);

        assertEquals("OldName", result.name());
        assertEquals("old@email.com", result.email());
    }

    @Test
    @DisplayName("Should throw exception if company not found on update")
    void updateCompanyNotFound() {
        UUID companyId = UUID.randomUUID();

        UpdateCompanyDTO dto = new UpdateCompanyDTO(
            companyId,
            "email@test.com",
            null,
            "Name",
            null,
            null,
            null
        );

        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> companyService.update(dto)
        );
    }

    @Test
    @DisplayName("Should delete company successfully")
    void deleteCompanySuccess() {
        UUID companyId = UUID.randomUUID();
        Company company = new Company();
        company.setId(companyId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        companyService.deleteCompany(companyId);

        verify(companyRepository).delete(company);
    }

    @Test
    @DisplayName("Should throw exception if company not found on delete")
    void deleteCompanyNotFound() {
        UUID companyId = UUID.randomUUID();

        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> companyService.deleteCompany(companyId)
        );
    }
}
