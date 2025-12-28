package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.CompanyDTO;
import org.example.entity.Company;
import org.example.entity.CompanyUser;
import org.example.entity.User;
import org.example.repository.CompanyRepository;
import org.example.repository.CompanyUserRepository;
import org.example.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
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

        log.debug("Company creation started: orgNum={}, creatorUserId={}", orgNum, creatorUserId);

        User creator = userRepository.findById(creatorUserId)
            .orElseThrow(() -> {
                log.warn("Company creation failed: Creator user not found with id={}", creatorUserId);
                return new IllegalArgumentException("Creator user not found with id: " + creatorUserId);
            });

        if (companyRepository.existsByOrgNum(orgNum)) {
            log.warn("Company creation failed: Company with orgNum={} already exists", orgNum);
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

        CompanyUser creatorAssociation = new CompanyUser(creator, company);
        companyUserRepository.create(creatorAssociation);

        log.info("Company created successfully with id={} by userId={}", company.getId(), creatorUserId);

        return toDto(company);
    }

    public CompanyDTO update(UUID id,
                             String name,
                             String orgNum,
                             String email,
                             String address,
                             String city,
                             String country,
                             String phoneNumber) {

        log.debug("Company update started for id={}", id);

        Company company = companyRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Company update failed: Company not found with id={}", id);
                return new IllegalArgumentException("Company not found with id: " + id);
            });

        if (orgNum != null && !orgNum.equals(company.getOrgNum()) && companyRepository.existsByOrgNum(orgNum)) {
            log.warn("Company update failed: OrgNum {} already exists", orgNum);
            throw new IllegalArgumentException("Company with orgNum " + orgNum + " already exists");
        }

        if (name != null) company.setName(name);
        if (orgNum != null) company.setOrgNum(orgNum);
        if (email != null) company.setEmail(email);
        if (address != null) company.setAddress(address);
        if (city != null) company.setCity(city);
        if (country != null) company.setCountry(country);
        if (phoneNumber != null) company.setPhoneNumber(phoneNumber);

        company.setUpdatedAt(LocalDateTime.now());

        companyRepository.update(company);

        log.info("Company updated successfully with id={}", company.getId());

        return toDto(company);
    }

    public void deleteCompany(UUID companyId) {
        log.debug("Company deletion requested for companyId={}", companyId);

        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> {
                log.warn("Company deletion failed: Company not found for id={}", companyId);
                return new IllegalArgumentException("Company not found with id: " + companyId);
            });

        companyRepository.delete(company);

        log.info("Company deleted successfully with id={}", companyId);
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
}
