package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.entity.Company;
import org.example.entity.User;
import org.example.entity.CompanyUser;
import org.example.entity.CompanyUserId;
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


    public CompanyUserService(UserRepository userRepository, CompanyUserRepository companyUserRepository, CompanyRepository companyRepository) {
        this.userRepository = userRepository;
        this.companyUserRepository = companyUserRepository;
        this.companyRepository = companyRepository;
    }

    public void addUserToCompanyByEmail(UUID companyId, String email) {
        log.debug("Add user to company requested: companyId={}, email={}", companyId, LogUtil.maskEmail(email));

        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> {
                log.warn("Add user failed: Company not found with id={}", companyId);
                return new IllegalArgumentException("Company not found with id: " + companyId);
            });

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.warn("Add user failed: User not found with email={}", LogUtil.maskEmail(email));
                return new IllegalArgumentException("User not found with email: " + email);
            });

        UUID userId = user.getId();
        CompanyUserId id = new CompanyUserId(userId, companyId);

        if (companyUserRepository.findById(id).isPresent()) {
            log.warn("Add user failed: User {} already associated with company {}", userId, companyId);
            throw new IllegalArgumentException("User is already associated with this company");
        }

        CompanyUser association = new CompanyUser(user, company);
        companyUserRepository.create(association);

        log.info("User {} added to company {} successfully", userId, companyId);
    }

    public void deleteUserFromCompany(UUID companyId, UUID userId) {
        log.debug("Delete user from company requested: companyId={}, userId={}", companyId, userId);

        CompanyUserId id = new CompanyUserId(userId, companyId);

        CompanyUser companyUser = companyUserRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Delete user failed: User {} is not part of company {}", userId, companyId);
                return new IllegalArgumentException("User is not part of company");
            });

        companyUserRepository.delete(companyUser);

        log.info("User {} removed from company {} successfully", userId, companyId);
    }

    public boolean isUserAssociatedWithCompany(UUID userId, UUID companyId) {
        log.debug("Check if user {} is associated with company {}", userId, companyId);
        CompanyUserId id = new CompanyUserId(userId, companyId);
        boolean associated = companyUserRepository.findById(id).isPresent();
        log.debug("User {} association with company {}: {}", userId, companyId, associated);
        return associated;
    }

    public List<CompanyUser> getCompanyUsers(UUID companyId) {
        log.debug("Fetching all users for company {}", companyId);
        return companyUserRepository.findByCompanyId(companyId);
    }

    public List<CompanyUser> getUserCompanies(UUID userId) {
        log.debug("Fetching all companies for user {}", userId);
        return companyUserRepository.findByUserId(userId);
    }
}
