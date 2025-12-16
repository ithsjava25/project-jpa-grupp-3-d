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

        companyRepository.save(company);

        return toDto(company);
    }

    public CompanyDTO toDto(Company company) {
        return CompanyDTO.builder()
           //  .id(company.getId()) saknas???
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
        String orgNum,
        String email,
        String address,
        String city,
        String country,
        String phoneNumber) {
        //TODO: Implement
        return null;
    }
}
