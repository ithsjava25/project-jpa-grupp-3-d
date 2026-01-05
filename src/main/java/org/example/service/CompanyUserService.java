package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.entity.company.Company;
import org.example.entity.user.User;
import org.example.entity.company.CompanyUser;
import org.example.entity.company.CompanyUserId;
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
        if (companyId == null) throw new IllegalArgumentException("Company id cannot be null");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email cannot be null or blank");

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

        CompanyUserId id = new CompanyUserId(user.getId(), companyId);
        if (companyUserRepository.findById(id).isPresent()) {
            log.warn("Add user failed: User {} already associated with company {}", user.getId(), companyId);
            throw new IllegalArgumentException("User is already associated with this company");
        }

        CompanyUser association = new CompanyUser(user, company);
        companyUserRepository.create(association);

        log.info("User {} added to company {} successfully", user.getId(), companyId);
    }

    public void deleteUserFromCompany(UUID companyId, UUID userId) {
        if (companyId == null || userId == null) throw new IllegalArgumentException("Company id and user id cannot be null");

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

    public List<CompanyUser> getCompanyUsers(UUID companyId) {
        if (companyId == null) throw new IllegalArgumentException("Company id cannot be null");

        log.debug("Fetching all users for company {}", companyId);
        return companyUserRepository.findByCompanyId(companyId);
    }

    public List<CompanyUser> getUserCompanies(UUID userId) {
        if (userId == null) throw new IllegalArgumentException("User id cannot be null");

        log.debug("Fetching all companies for user {}", userId);
        return companyUserRepository.findByUserId(userId);
    }
}
