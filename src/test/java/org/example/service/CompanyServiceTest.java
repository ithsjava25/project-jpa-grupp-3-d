package org.example.service;

import org.example.dto.CompanyDTO;
import org.example.entity.Company;
import org.example.entity.CompanyUser;
import org.example.entity.User;
import org.example.repository.CompanyRepository;
import org.example.repository.CompanyUserRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyUserRepository companyUserRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyService companyService;

    @Test
    @DisplayName("Should create company successfully")
    void createCompanySuccess() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(companyRepository.existsByOrgNum("1234567890")).thenReturn(false);

        when(companyRepository.create(any(Company.class))).thenAnswer(invocation -> {
            Company c = invocation.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        CompanyDTO dto = companyService.create(
            userId, "1234567890", "company@email.com",
            "0701234567", "TestCo", "Street 1", "City", "Country"
        );

        assertNotNull(dto);
        assertEquals("TestCo", dto.name());
        assertEquals("1234567890", dto.orgNum());

        verify(companyRepository, times(1)).create(any(Company.class));
        verify(companyUserRepository, times(1)).create(any(CompanyUser.class));
    }


    @Test
    @DisplayName("Should create company with null optional fields")
    void createCompanyWithNullOptionalFields() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(companyRepository.existsByOrgNum("1234567890")).thenReturn(false);

        when(companyRepository.create(any(Company.class))).thenAnswer(invocation -> {
            Company c = invocation.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        CompanyDTO dto = companyService.create(
            userId, "1234567890", null, null,
            "TestCo", null, null, null
        );

        assertNotNull(dto);
        assertEquals("TestCo", dto.name());
        assertNull(dto.email());
        assertNull(dto.phoneNumber());
        assertNull(dto.address());
        assertNull(dto.city());
        assertNull(dto.country());

        verify(companyRepository, times(1)).create(any(Company.class));
        verify(companyUserRepository, times(1)).create(any(CompanyUser.class));
    }

    @Test
    @DisplayName("Should update company and set updatedAt")
    void updateCompanySetsUpdatedAt() {
        UUID companyId = UUID.randomUUID();
        Company company = new Company();
        company.setId(companyId);
        company.setName("OldName");
        company.setOrgNum("1111111111");

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyRepository.existsByOrgNum("2222222222")).thenReturn(false);

        doAnswer(invocation -> {
            Company c = invocation.getArgument(0);
            c.setUpdatedAt(LocalDateTime.now());
            return null;
        }).when(companyRepository).update(any(Company.class));

        LocalDateTime beforeUpdate = LocalDateTime.now();
        companyService.update(
            companyId, "NewName", "2222222222",
            "new@email.com", "New Street", "New City",
            "New Country", "0709999999"
        );
        LocalDateTime afterUpdate = LocalDateTime.now();

        assertNotNull(company.getUpdatedAt());
        assertTrue(!company.getUpdatedAt().isBefore(beforeUpdate));
        assertTrue(!company.getUpdatedAt().isAfter(afterUpdate));

        verify(companyRepository, times(1)).update(company);
    }



    @Test
    @DisplayName("Should throw exception if creator user not found")
    void createCompanyUserNotFound() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            companyService.create(userId, "1234567890", "company@email.com",
                "0701234567", "TestCo", "Street 1", "City", "Country")
        );

        assertTrue(exception.getMessage().contains("Creator user not found"));

        verify(companyRepository, never()).create(any());
        verify(companyUserRepository, never()).create(any());
    }

    @Test
    @DisplayName("Should throw exception if company orgNum already exists")
    void createCompanyOrgNumAlreadyExists() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(companyRepository.existsByOrgNum("1234567890")).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            companyService.create(userId, "1234567890", "company@email.com",
                "0701234567", "TestCo", "Street 1", "City", "Country")
        );

        assertTrue(exception.getMessage().contains("already exists"));

        verify(companyRepository, never()).create(any());
        verify(companyUserRepository, never()).create(any());
    }

    @Test
    @DisplayName("Should associate CompanyUser correctly when creating a company")
    void createCompanyAssociatesCompanyUserCorrectly() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(companyRepository.existsByOrgNum("1234567890")).thenReturn(false);


        doAnswer(invocation -> {
            Company company = invocation.getArgument(0);
            company.setId(UUID.randomUUID());
            company.setOrgNum("1234567890");
            return null;
        }).when(companyRepository).create(any(Company.class));

        doAnswer(invocation -> {
            CompanyUser cu = invocation.getArgument(0);
            assertNotNull(cu.getUser());
            assertEquals(userId, cu.getUser().getId());
            assertNotNull(cu.getCompany());
            assertEquals("1234567890", cu.getCompany().getOrgNum());
            return null;
        }).when(companyUserRepository).create(any(CompanyUser.class));

        companyService.create(
            userId,
            "1234567890",
            "company@email.com",
            "0701234567",
            "TestCo",
            "Street 1",
            "City",
            "Country"
        );

        verify(companyRepository, times(1)).create(any(Company.class));
        verify(companyUserRepository, times(1)).create(any(CompanyUser.class));
    }


    @Test
    @DisplayName("Should update company successfully")
    void updateCompanySuccess() {
        UUID companyId = UUID.randomUUID();
        Company company = new Company();
        company.setId(companyId);
        company.setName("OldName");
        company.setOrgNum("1111111111");

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyRepository.existsByOrgNum("2222222222")).thenReturn(false);

        CompanyDTO dto = companyService.update(
            companyId, "NewName", "2222222222",
            "new@email.com", "New Street", "New City",
            "New Country", "0709999999"
        );

        assertEquals("NewName", dto.name());
        assertEquals("2222222222", dto.orgNum());

        verify(companyRepository, times(1)).update(company);
    }

    @Test
    @DisplayName("Should update company with null fields without overwriting")
    void updateCompanyWithNullFields() {
        UUID companyId = UUID.randomUUID();
        Company company = new Company();
        company.setId(companyId);
        company.setName("OldName");
        company.setOrgNum("1111111111");
        company.setEmail("old@email.com");

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        CompanyDTO dto = companyService.update(
            companyId, null, null, "new@email.com", null, null, null, null
        );

        assertEquals("OldName", dto.name());
        assertEquals("1111111111", dto.orgNum());
        assertEquals("new@email.com", dto.email());

        verify(companyRepository, times(1)).update(company);
    }

    @Test
    @DisplayName("Should throw exception if company not found for update")
    void updateCompanyNotFound() {
        UUID companyId = UUID.randomUUID();

        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            companyService.update(companyId, "Name", null, null, null, null, null, null)
        );

        assertTrue(exception.getMessage().contains("Company not found"));

        verify(companyRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should throw exception if orgNum already exists on update")
    void updateCompanyOrgNumAlreadyExists() {
        UUID companyId = UUID.randomUUID();
        Company existingCompany = new Company();
        existingCompany.setId(companyId);
        existingCompany.setName("OldName");
        existingCompany.setOrgNum("1111111111");

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(existingCompany));
        when(companyRepository.existsByOrgNum("2222222222")).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            companyService.update(companyId, "NewName", "2222222222", "new@email.com",
                "New Street", "New City", "New Country", "0709999999")
        );

        assertTrue(exception.getMessage().contains("already exists"));

        verify(companyRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should delete company successfully")
    void deleteCompanySuccess() {
        UUID companyId = UUID.randomUUID();
        Company company = new Company();
        company.setId(companyId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        companyService.deleteCompany(companyId);

        verify(companyRepository, times(1)).delete(company);
    }

    @Test
    @DisplayName("Should throw exception if company not found for deletion")
    void deleteCompanyNotFound() {
        UUID companyId = UUID.randomUUID();
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            companyService.deleteCompany(companyId));

        assertTrue(exception.getMessage().contains("Company not found"));
        verify(companyRepository, never()).delete(any());
    }
}
