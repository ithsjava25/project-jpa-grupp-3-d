package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.entity.company.Company;
import org.example.entity.user.User;
import org.example.entity.company.CompanyUser;
import org.example.entity.company.CompanyUserId;
import org.example.exception.BusinessRuleException;
import org.example.exception.EntityNotFoundException;
import org.example.repository.CompanyRepository;
import org.example.repository.CompanyUserRepository;
import org.example.repository.UserRepository;
import org.example.util.LogUtil;

import java.util.List;
import java.util.UUID;

@Slf4j
public class CompanyUserService {
    private final UserRepository userRepository;
    private final CompanyUserRepository companyUserRepository;
    private final CompanyRepository companyRepository;
    private final ValidationService validationService;

    public CompanyUserService(UserRepository userRepository,
                              CompanyUserRepository companyUserRepository,
                              CompanyRepository companyRepository,
                              ValidationService validationService) {
        this.userRepository = userRepository;
        this.companyUserRepository = companyUserRepository;
        this.companyRepository = companyRepository;
        this.validationService = validationService;
    }

    public void addUserToCompanyByEmail(UUID companyId, String email) {
        validationService.validateNotNull("companyId", companyId);
        validationService.validateNotEmpty("email", email);
        validationService.validateEmail(email);

        log.debug("Add user to company requested: companyId={}, email={}", companyId, LogUtil.maskEmail(email));

        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> {
                log.warn("Add user failed: Company not found with id={}", companyId);
                return new EntityNotFoundException("Company", companyId, "COMPANY_NOT_FOUND");
            });

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.warn("Add user failed: User not found with email={}", LogUtil.maskEmail(email));
                return new EntityNotFoundException("User", email, "USER_NOT_FOUND");
            });

        CompanyUserId id = new CompanyUserId(user.getId(), companyId);
        if (companyUserRepository.findById(id).isPresent()) {
            log.warn("Add user failed: User {} already associated with company {}", user.getId(), companyId);
            throw new BusinessRuleException("User is already associated with this company", "USER_ALREADY_ASSOCIATED");
        }

        CompanyUser association = new CompanyUser(user, company);
        companyUserRepository.create(association);

        log.info("User {} added to company {} successfully", user.getId(), companyId);
    }

    public void deleteUserFromCompany(UUID companyId, UUID userId) {
        validationService.validateNotNull("companyId", companyId);
        validationService.validateNotNull("userId", userId);

        log.debug("Delete user from company requested: companyId={}, userId={}", companyId, userId);

        CompanyUserId id = new CompanyUserId(userId, companyId);

        CompanyUser companyUser = companyUserRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Delete user failed: User {} is not part of company {}", userId, companyId);
                return new BusinessRuleException("User is not part of company", "USER_NOT_IN_COMPANY");
            });

        companyUserRepository.delete(companyUser);
        log.info("User {} removed from company {} successfully", userId, companyId);
    }

    public List<CompanyUser> getCompanyUsers(UUID companyId) {
        validationService.validateNotNull("companyId", companyId);

        log.debug("Fetching all users for company {}", companyId);
        return companyUserRepository.findByCompanyId(companyId);
    }

    public List<CompanyUser> getUserCompanies(UUID userId) {
        validationService.validateNotNull("userId", userId);

        log.debug("Fetching all companies for user {}", userId);
        return companyUserRepository.findByUserId(userId);
    }
}
