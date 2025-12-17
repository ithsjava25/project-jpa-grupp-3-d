package org.example.service;

import jakarta.transaction.Transactional;
import org.example.dto.CompanyDTO;
import org.example.entity.Company;
import org.example.repository.CompanyRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public class CompanyService {
    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional
    public CompanyDTO create(
        String orgNum,
        String email,
        String phoneNumber,
        String name,
        String address,
        String city,
        String country) {

        if (companyRepository.existsByOrgNum(orgNum)) {
            throw new IllegalArgumentException("Company with orgNum " + orgNum + " already exists");
        }
        if (companyRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Company with email " + email + " already exists");
        }

        Company company = Company.builder()
            .orgNum(orgNum)
            .email(email)
            .phoneNumber(phoneNumber)
            .name(name)
            .address(address)
            .city(city)
            .country(country)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        companyRepository.create(company);
        return toDto(company);
    }

    public CompanyDTO toDto(Company company) {
        return CompanyDTO.builder()
            .id(company.getId())
            .orgNum(company.getOrgNum())
            .email(company.getEmail())
            .phoneNumber(company.getPhoneNumber())
            .name(company.getName())
            .address(company.getAddress())
            .city(company.getCity())
            .country(company.getCountry())
            .createdAt(company.getCreatedAt())
            .updatedAt(company.getUpdatedAt())
            .build();
    }


    @Transactional
    public CompanyDTO update(
        UUID id,
        String name,
        String newOrgNum,
        String newEmail,
        String address,
        String city,
        String country,
        String phoneNumber) {

        Company company = companyRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + id));

        if (newOrgNum != null && !newOrgNum.equals(company.getOrgNum())) {
            if (companyRepository.existsByOrgNum(newOrgNum)) {
                throw new IllegalArgumentException("Company with orgNum " + newOrgNum + " already exists");
            }
        }

        if (newEmail != null && !newEmail.equals(company.getEmail())) {
            if (companyRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Company with email " + newEmail + " already exists");
            }
        }

        if (name != null) company.setName(name);
        if (newOrgNum != null) company.setOrgNum(newOrgNum);
        if (newEmail != null) company.setEmail(newEmail);
        if (address != null) company.setAddress(address);
        if (city != null) company.setCity(city);
        if (country != null) company.setCountry(country);
        if (phoneNumber != null) company.setPhoneNumber(phoneNumber);

        company.setUpdatedAt(LocalDateTime.now());

        companyRepository.update(company);
        return toDto(company);
    }
}
