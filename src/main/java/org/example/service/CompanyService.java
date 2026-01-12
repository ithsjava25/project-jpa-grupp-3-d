package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.entity.company.*;
import org.example.entity.user.User;
import org.example.exception.BusinessRuleException;
import org.example.exception.EntityNotFoundException;
import org.example.repository.CompanyRepository;
import org.example.repository.CompanyUserRepository;
import org.example.repository.UserRepository;
import org.example.util.ValidationUtil;

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
        ValidationUtil.validate(dto);

        log.debug(
            "Company creation started: orgNum={}, name={}, creatorUserId={}",
            dto.orgNum(),
            dto.name(),
            creatorUserId
        );

        User creator = userRepository.findById(creatorUserId)
            .orElseThrow(() -> {
                log.warn("Company creation failed: creator user not found id={}", creatorUserId);
                return new EntityNotFoundException("User", creatorUserId);
            });

        if (companyRepository.existsByOrgNum(dto.orgNum())) {
            log.warn("Company creation failed: orgNum={} already exists", dto.orgNum());
            throw new BusinessRuleException(
                "Company with organization number already exists"
            );
        }

        Company company = Company.fromDTO(dto);
        companyRepository.create(company);

        CompanyUser association = new CompanyUser(creator, company);
        companyUserRepository.create(association);

        log.info(
            "Company created successfully id={} orgNum={} creatorUserId={}",
            company.getId(),
            dto.orgNum(),
            creatorUserId
        );

        return CompanyDTO.fromEntity(company);
    }

    public CompanyDTO update(UpdateCompanyDTO dto) {
        ValidationUtil.validate(dto);

        log.debug("Company update requested: companyId={}", dto.companyId());

        Company company = companyRepository.findById(dto.companyId())
            .orElseThrow(() -> {
                log.warn("Company update failed: company not found id={}", dto.companyId());
                return new EntityNotFoundException("Company", dto.companyId());
            });

        company.update(dto);
        companyRepository.update(company);

        log.info("Company updated successfully id={}", company.getId());
        return CompanyDTO.fromEntity(company);
    }

    public Company getCompanyEntity(UUID companyId) {

        log.debug("Fetching company entity id={}", companyId);

        return companyRepository.findById(companyId)
            .orElseThrow(() -> {
                log.warn("Get company failed: company not found id={}", companyId);
                return new EntityNotFoundException("Company", companyId);
            });
    }

    public void deleteCompany(UUID companyId) {

        log.debug("Company deletion requested id={}", companyId);

        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> {
                log.warn("Company deletion failed: company not found id={}", companyId);
                return new EntityNotFoundException("Company", companyId);
            });

        companyRepository.delete(company);

        log.info("Company deleted successfully id={}", companyId);
    }
}
