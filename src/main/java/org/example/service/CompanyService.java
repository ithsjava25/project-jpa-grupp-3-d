package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.entity.company.*;
import org.example.entity.user.User;
import org.example.exception.BusinessRuleException;
import org.example.exception.EntityNotFoundException;
import org.example.repository.CompanyRepository;
import org.example.repository.CompanyUserRepository;
import org.example.repository.UserRepository;

import java.util.UUID;

@Slf4j
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyUserRepository companyUserRepository;
    private final UserRepository userRepository;
    private final ValidationService validationService;

    public CompanyService(CompanyRepository companyRepository,
                          CompanyUserRepository companyUserRepository,
                          UserRepository userRepository,
                          ValidationService validationService) {
        this.companyRepository = companyRepository;
        this.companyUserRepository = companyUserRepository;
        this.userRepository = userRepository;
        this.validationService = validationService;
    }

    public CompanyDTO create(UUID creatorUserId, CreateCompanyDTO dto) {
        validationService.validateNotNull("creatorUserId", creatorUserId);
        validationService.validateNotEmpty("orgNum", dto.orgNum());
        validationService.validateNotEmpty("name", dto.name());
        validationService.validateOrgNum(dto.orgNum());
        validationService.validateCompanyName(dto.name());

        log.debug("Company creation started: orgNum={}, creatorUserId={}", dto.orgNum(), creatorUserId);

        User creator = userRepository.findById(creatorUserId)
            .orElseThrow(() -> {
                log.warn("Company creation failed: Creator user not found with id={}", creatorUserId);
                return new EntityNotFoundException("User", creatorUserId, "USER_NOT_FOUND");
            });

        if (companyRepository.existsByOrgNum(dto.orgNum())) {
            log.warn("Company creation failed: Company with orgNum={} already exists", dto.orgNum());
            throw new BusinessRuleException("Company with orgNum " + dto.orgNum() + " already exists", "ORG_NUM_EXISTS");
        }

        Company company = Company.fromDTO(dto);
        companyRepository.create(company);

        CompanyUser association = new CompanyUser(creator, company);
        companyUserRepository.create(association);

        log.info("Company created successfully with id={} by userId={}", company.getId(), creatorUserId);
        return CompanyDTO.fromEntity(company);
    }

    public CompanyDTO update(UpdateCompanyDTO dto) {
        validationService.validateNotNull("companyId", dto.companyId());

        log.debug("Company update started for id={}", dto.companyId());

        Company company = companyRepository.findById(dto.companyId())
            .orElseThrow(() -> {
                log.warn("Company update failed: Company not found with id={}", dto.companyId());
                return new EntityNotFoundException("Company", dto.companyId(), "COMPANY_NOT_FOUND");
            });

        company.update(dto);
        companyRepository.update(company);

        log.info("Company updated successfully with id={}", company.getId());
        return CompanyDTO.fromEntity(company);
    }

    public Company getCompanyEntity(UUID companyId) {
        validationService.validateNotNull("companyId", companyId);

        return companyRepository.findById(companyId)
            .orElseThrow(() -> new EntityNotFoundException("Company", companyId, "COMPANY_NOT_FOUND"));
    }

    public void deleteCompany(UUID companyId) {
        validationService.validateNotNull("companyId", companyId);

        log.debug("Company deletion requested for companyId={}", companyId);

        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> {
                log.warn("Company deletion failed: Company not found for id={}", companyId);
                return new EntityNotFoundException("Company", companyId, "COMPANY_NOT_FOUND");
            });

        companyRepository.delete(company);
        log.info("Company deleted successfully with id={}", companyId);
    }
}
