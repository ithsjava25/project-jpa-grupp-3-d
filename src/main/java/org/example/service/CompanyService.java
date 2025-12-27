package org.example.service;

import org.example.dto.CompanyDTO;
import org.example.entity.Company;
import org.example.entity.CompanyUser;
import org.example.entity.User;
import org.example.repository.CompanyRepository;
import org.example.repository.CompanyUserRepository;
import org.example.repository.UserRepository;

import java.util.UUID;

public class CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyUserRepository companyUserRepository;
    private final UserRepository userRepository;

    public CompanyService(CompanyRepository companyRepository, CompanyUserRepository companyUserRepository, UserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.companyUserRepository = companyUserRepository;
        this.userRepository = userRepository;
    }

    public CompanyDTO create(
        UUID creatorUserId,
        String orgNum,
        String email,
        String phoneNumber,
        String name,
        String address,
        String city,
        String country) {

        // Validate creator exists
        User creator = userRepository.findById(creatorUserId)
            .orElseThrow(() -> new IllegalArgumentException("Creator user not found with id: " + creatorUserId));

        if (companyRepository.existsByOrgNum(orgNum)) {
            throw new IllegalArgumentException("Company with orgNum " + orgNum + " already exists");
        }

        Company company = Company.builder()
            .orgNum(orgNum)
            .email(email)
            .phoneNumber(phoneNumber)
            .name(name)
            .address(address)
            .city(city)
            .country(country)
            .build();

        companyRepository.create(company);

        // Automatically associate creator with the company
        CompanyUser creatorAssociation = new CompanyUser(creator, company);
        companyUserRepository.create(creatorAssociation);

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

    public CompanyDTO update(
        UUID id,
        String name,
        String orgNum,
        String email,
        String address,
        String city,
        String country,
        String phoneNumber) {

        Company company = companyRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + id));

        if (orgNum != null && !orgNum.equals(company.getOrgNum())) {
            if (companyRepository.existsByOrgNum(orgNum)) {
                throw new IllegalArgumentException("Company with orgNum " + orgNum + " already exists");
            }
        }

        if (name != null) company.setName(name);
        if (orgNum != null) company.setOrgNum(orgNum);
        if (email != null) company.setEmail(email);
        if (address != null) company.setAddress(address);
        if (city != null) company.setCity(city);
        if (country != null) company.setCountry(country);
        if (phoneNumber != null) company.setPhoneNumber(phoneNumber);

        companyRepository.update(company);
        return toDto(company);
    }

    public Company getCompanyEntity(UUID companyId) {
        return companyRepository.findById(companyId)
            .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + companyId));
    }

    public void deleteCompany(UUID companyId) {
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + companyId));
        companyRepository.delete(company);
    }
}
