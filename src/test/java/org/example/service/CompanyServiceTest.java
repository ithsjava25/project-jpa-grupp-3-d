package org.example.service;

import org.example.dto.CompanyDTO;
import org.example.entity.Company;
import org.example.entity.CompanyUser;
import org.example.entity.User;
import org.example.repository.CompanyRepository;
import org.example.repository.CompanyUserRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompanyServiceTest {

    private UserRepository userRepository;
    private CompanyUserRepository companyUserRepository;
    private CompanyRepository companyRepository;
    private CompanyService companyService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        companyUserRepository = mock(CompanyUserRepository.class);
        companyRepository = mock(CompanyRepository.class);
        companyService = new CompanyService(companyRepository, companyUserRepository, userRepository);
    }

    @Test
    void testCreateCompanySuccess() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(companyRepository.existsByOrgNum("1234567890")).thenReturn(false);

        CompanyDTO dto = companyService.create(
            userId,
            "1234567890",
            "company@email.com",
            "0701234567",
            "TestCo",
            "Street 1",
            "City",
            "Country"
        );

        assertNotNull(dto);
        assertEquals("TestCo", dto.name());
        assertEquals("1234567890", dto.orgNum());

        verify(companyRepository, times(1)).create(any(Company.class));
        verify(companyUserRepository, times(1)).create(any(CompanyUser.class));
    }

    @Test
    void testCreateCompanyWithNullOptionalFields() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(companyRepository.existsByOrgNum("1234567890")).thenReturn(false);

        CompanyDTO dto = companyService.create(
            userId,
            "1234567890",
            null,
            null,
            "TestCo",
            null,
            null,
            null
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
    void testCreateCompanyUserNotFound() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            companyService.create(
                userId,
                "1234567890",
                "company@email.com",
                "0701234567",
                "TestCo",
                "Street 1",
                "City",
                "Country"
            )
        );

        assertTrue(exception.getMessage().contains("Creator user not found"));

        verify(companyRepository, never()).create(any());
        verify(companyUserRepository, never()).create(any());
    }

    @Test
    void testCreateCompanyOrgNumAlreadyExists() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(companyRepository.existsByOrgNum("1234567890")).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            companyService.create(
                userId,
                "1234567890",
                "company@email.com",
                "0701234567",
                "TestCo",
                "Street 1",
                "City",
                "Country"
            )
        );

        assertTrue(exception.getMessage().contains("already exists"));

        verify(companyRepository, never()).create(any());
        verify(companyUserRepository, never()).create(any());
    }

    @Test
    void testUpdateCompanySuccess() {
        UUID companyId = UUID.randomUUID();

        Company company = new Company();
        company.setId(companyId);
        company.setName("OldName");
        company.setOrgNum("1111111111");

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyRepository.existsByOrgNum("2222222222")).thenReturn(false);

        CompanyDTO dto = companyService.update(
            companyId,
            "NewName",
            "2222222222",
            "new@email.com",
            "New Street",
            "New City",
            "New Country",
            "0709999999"
        );

        assertEquals("NewName", dto.name());
        assertEquals("2222222222", dto.orgNum());

        verify(companyRepository, times(1)).update(company);
    }

    @Test
    void testUpdateCompanyNotFound() {
        UUID companyId = UUID.randomUUID();

        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            companyService.update(
                companyId,
                "Name",
                null,
                null,
                null,
                null,
                null,
                null
            )
        );

        assertTrue(exception.getMessage().contains("Company not found"));

        verify(companyRepository, never()).update(any());
    }

    @Test
    void testUpdateCompanyOrgNumAlreadyExists() {
        UUID companyId = UUID.randomUUID();

        Company existingCompany = new Company();
        existingCompany.setId(companyId);
        existingCompany.setName("OldName");
        existingCompany.setOrgNum("1111111111");

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(existingCompany));

        when(companyRepository.existsByOrgNum("2222222222")).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            companyService.update(
                companyId,
                "NewName",
                "2222222222",
                "new@email.com",
                "New Street",
                "New City",
                "New Country",
                "0709999999"
            )
        );

        assertTrue(exception.getMessage().contains("already exists"));

        verify(companyRepository, never()).update(any());
    }

    @Test
    void testDeleteCompanySuccess() {
        UUID companyId = UUID.randomUUID();

        Company company = new Company();
        company.setId(companyId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        companyService.deleteCompany(companyId);

        verify(companyRepository, times(1)).delete(company);
    }

    @Test
    void testDeleteCompanyNotFound() {
        UUID companyId = UUID.randomUUID();

        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> companyService.deleteCompany(companyId));

        assertTrue(exception.getMessage().contains("Company not found"));

        verify(companyRepository, never()).delete(any());
    }
}
