package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.entity.company.*;
import org.example.entity.user.User;
import org.example.repository.CompanyRepository;
import org.example.repository.CompanyUserRepository;
import org.example.repository.UserRepository;

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

    public CompanyDTO create(UUID creatorUserId, CreateCompanyDTO dto) {
        if (creatorUserId == null) throw new IllegalArgumentException("Creator userId cannot be null");
        if (dto.orgNum() == null || dto.orgNum().isBlank()) throw new IllegalArgumentException("OrgNum cannot be null or blank");
        if (dto.name() == null || dto.name().isBlank()) throw new IllegalArgumentException("Company name cannot be null or blank");

        log.debug("Company creation started: orgNum={}, creatorUserId={}", dto.orgNum(), creatorUserId);

        User creator = userRepository.findById(creatorUserId)
            .orElseThrow(() -> {
                log.warn("Company creation failed: Creator user not found with id={}", creatorUserId);
                return new IllegalArgumentException("Creator user not found with id: " + creatorUserId);
            });

        if (companyRepository.existsByOrgNum(dto.orgNum())) {
            log.warn("Company creation failed: Company with orgNum={} already exists", dto.orgNum());
            throw new IllegalArgumentException("Company with orgNum " + dto.orgNum() + " already exists");
        }

        Company company = Company.fromDTO(dto);
        companyRepository.create(company);

        CompanyUser association = new CompanyUser(creator, company);
        companyUserRepository.create(association);

        log.info("Company created successfully with id={} by userId={}", company.getId(), creatorUserId);

        return CompanyDTO.fromEntity(company);
    }

    public CompanyDTO update(UpdateCompanyDTO dto) {
        if (dto.companyId() == null) throw new IllegalArgumentException("Company id cannot be null");

        log.debug("Company update started for id={}", dto.companyId());

        Company company = companyRepository.findById(dto.companyId())
            .orElseThrow(() -> {
                log.warn("Company update failed: Company not found with id={}", dto.companyId());
                return new IllegalArgumentException("Company not found with id: " + dto.companyId());
            });

        company.update(dto);

        companyRepository.update(company);

        log.info("Company updated successfully with id={}", company.getId());
        return CompanyDTO.fromEntity(company);
    }

    public Company getCompanyEntity(UUID companyId) {
        return companyRepository.findById(companyId)
            .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + companyId));
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
}
