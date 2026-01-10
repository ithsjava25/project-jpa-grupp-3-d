package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.entity.company.*;
import org.example.entity.user.User;
import org.example.exception.BusinessRuleException;
import org.example.exception.EntityNotFoundException;
import org.example.exception.ValidationException;
import org.example.repository.CompanyRepository;
import org.example.repository.CompanyUserRepository;
import org.example.repository.UserRepository;

import java.util.UUID;

@Slf4j
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyUserRepository companyUserRepository;
    private final UserRepository userRepository;

    public CompanyService(
        CompanyRepository companyRepository,
        CompanyUserRepository companyUserRepository,
        UserRepository userRepository
    ) {
        this.companyRepository = companyRepository;
        this.companyUserRepository = companyUserRepository;
        this.userRepository = userRepository;
    }

    public CompanyDTO create(UUID creatorUserId, CreateCompanyDTO dto) {

        log.debug("Company creation started: creatorUserId={}", creatorUserId);

        if (creatorUserId == null) {
            log.warn("Company creation failed: creatorUserId is null");
            throw new ValidationException("Creator user id cannot be null");
        }

        if (dto == null) {
            log.warn("Company creation failed: dto is null");
            throw new ValidationException("CreateCompanyDTO cannot be null");
        }

        if (dto.orgNum() == null || dto.orgNum().isBlank()) {
            log.warn("Company creation failed: orgNum is null or blank");
            throw new ValidationException("Organization number cannot be null or blank");
        }

        if (dto.name() == null || dto.name().isBlank()) {
            log.warn("Company creation failed: company name is null or blank");
            throw new ValidationException("Company name cannot be null or blank");
        }

        User creator = userRepository.findById(creatorUserId)
            .orElseThrow(() -> {
                log.warn("Company creation failed: creator user not found with id={}", creatorUserId);
                return new EntityNotFoundException("User", creatorUserId);
            });

        if (companyRepository.existsByOrgNum(dto.orgNum())) {
            log.warn("Company creation failed: company with orgNum={} already exists", dto.orgNum());
            throw new BusinessRuleException(
                "Company with organization number already exists"
            );
        }

        Company company = Company.fromDTO(dto);
        companyRepository.create(company);

        CompanyUser association = new CompanyUser(creator, company);
        companyUserRepository.create(association);

        log.info(
            "Company created successfully with id={} by userId={}",
            company.getId(),
            creatorUserId
        );

        return CompanyDTO.fromEntity(company);
    }

    public CompanyDTO update(UpdateCompanyDTO dto) {

        log.debug("Company update requested");

        if (dto == null) {
            log.warn("Company update failed: dto is null");
            throw new ValidationException("UpdateCompanyDTO cannot be null");
        }

        if (dto.companyId() == null) {
            log.warn("Company update failed: companyId is null");
            throw new ValidationException("Company id cannot be null");
        }

        Company company = companyRepository.findById(dto.companyId())
            .orElseThrow(() -> {
                log.warn("Company update failed: company not found with id={}", dto.companyId());
                return new EntityNotFoundException("Company", dto.companyId());
            });

        company.update(dto);
        companyRepository.update(company);

        log.info("Company updated successfully with id={}", company.getId());
        return CompanyDTO.fromEntity(company);
    }

    public Company getCompanyEntity(UUID companyId) {

        log.debug("Fetching company entity for id={}", companyId);

        if (companyId == null) {
            log.warn("Get company failed: companyId is null");
            throw new ValidationException("Company id cannot be null");
        }

        return companyRepository.findById(companyId)
            .orElseThrow(() -> {
                log.warn("Get company failed: company not found with id={}", companyId);
                return new EntityNotFoundException("Company", companyId);
            });
    }

    public void deleteCompany(UUID companyId) {

        log.debug("Company deletion requested for companyId={}", companyId);

        if (companyId == null) {
            log.warn("Company deletion failed: companyId is null");
            throw new ValidationException("Company id cannot be null");
        }

        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> {
                log.warn("Company deletion failed: company not found with id={}", companyId);
                return new EntityNotFoundException("Company", companyId);
            });

        companyRepository.delete(company);

        log.info("Company deleted successfully with id={}", companyId);
    }
}
