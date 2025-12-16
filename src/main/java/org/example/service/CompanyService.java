package org.example.service;

import jakarta.transaction.Transactional;
import org.example.dto.CompanyDTO;
import org.example.entity.Company;
import org.example.repository.CompanyRepository;

import java.time.LocalDateTime;

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

        companyRepository.save(company);

        return toDto(company);
    }

    public CompanyDTO toDto(Company company) {
        return CompanyDTO.builder()
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

}
