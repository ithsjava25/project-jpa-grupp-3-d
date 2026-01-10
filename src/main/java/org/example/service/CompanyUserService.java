package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.entity.company.Company;
import org.example.entity.company.CompanyUser;
import org.example.entity.company.CompanyUserId;
import org.example.entity.user.User;
import org.example.exception.BusinessRuleException;
import org.example.exception.EntityNotFoundException;
import org.example.exception.ValidationException;
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

    public CompanyUserService(
        UserRepository userRepository,
        CompanyUserRepository companyUserRepository,
        CompanyRepository companyRepository
    ) {
        this.userRepository = userRepository;
        this.companyUserRepository = companyUserRepository;
        this.companyRepository = companyRepository;
    }

    public void addUserToCompanyByEmail(UUID companyId, String email) {

        log.debug("Add user to company requested: companyId={}, email={}",
            companyId, LogUtil.maskEmail(email));

        if (companyId == null) {
            log.warn("Add user failed: companyId is null");
            throw new ValidationException("Company id cannot be null");
        }

        if (email == null || email.isBlank()) {
            log.warn("Add user failed: email is null or blank");
            throw new ValidationException("Email cannot be null or blank");
        }

        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> {
                log.warn("Add user failed: company not found with id={}", companyId);
                return new EntityNotFoundException("Company", companyId);
            });

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.warn("Add user failed: user not found with email={}",
                    LogUtil.maskEmail(email));
                return new EntityNotFoundException("User", email);
            });

        CompanyUserId id = new CompanyUserId(user.getId(), companyId);

        if (companyUserRepository.findById(id).isPresent()) {
            log.warn("Add user failed: user {} already associated with company {}",
                user.getId(), companyId);
            throw new BusinessRuleException("User is already associated with this company");
        }

        CompanyUser association = new CompanyUser(user, company);
        companyUserRepository.create(association);

        log.info("User {} added to company {} successfully",
            user.getId(), companyId);
    }

    public void deleteUserFromCompany(UUID companyId, UUID userId) {

        log.debug("Delete user from company requested: companyId={}, userId={}",
            companyId, userId);

        if (companyId == null || userId == null) {
            log.warn("Delete user failed: companyId or userId is null");
            throw new ValidationException("Company id and user id cannot be null");
        }

        CompanyUserId id = new CompanyUserId(userId, companyId);

        CompanyUser companyUser = companyUserRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Delete user failed: user {} not part of company {}",
                    userId, companyId);
                return new EntityNotFoundException(
                    "CompanyUser association",
                    String.format("userId=%s, companyId=%s", userId, companyId)
                );
            });

        companyUserRepository.delete(companyUser);

        log.info("User {} removed from company {} successfully",
            userId, companyId);
    }

    public List<CompanyUser> getCompanyUsers(UUID companyId) {

        log.debug("Fetching users for companyId={}", companyId);

        if (companyId == null) {
            log.warn("Get company users failed: companyId is null");
            throw new ValidationException("Company id cannot be null");
        }

        return companyUserRepository.findByCompanyId(companyId);
    }

    public List<CompanyUser> getUserCompanies(UUID userId) {

        log.debug("Fetching companies for userId={}", userId);

        if (userId == null) {
            log.warn("Get user companies failed: userId is null");
            throw new ValidationException("User id cannot be null");
        }

        return companyUserRepository.findByUserId(userId);
    }
}
