package org.example.service;

import org.example.entity.Company;
import org.example.entity.User;
import org.example.entity.CompanyUser;
import org.example.entity.CompanyUserId;
import org.example.repository.CompanyRepository;
import org.example.repository.CompanyUserRepository;
import org.example.repository.UserRepository;

import java.util.List;
import java.util.UUID;

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
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + companyId));

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        UUID userId = user.getId();

        // Check if association already exists
        CompanyUserId id = new CompanyUserId(userId, companyId);
        if (companyUserRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException("User is already associated with this company");
        }

        CompanyUser association = new CompanyUser(user, company);
        companyUserRepository.create(association);
    }

    public void deleteUserFromCompany(UUID companyId, UUID userId) {
        CompanyUserId id = new CompanyUserId(userId, companyId);

        CompanyUser companyUser = companyUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User is not part of company"));

        companyUserRepository.delete(companyUser);
    }

    public boolean isUserAssociatedWithCompany(UUID userId, UUID companyId) {
        CompanyUserId id = new CompanyUserId(userId, companyId);
        return companyUserRepository.findById(id).isPresent();
    }

    public List<CompanyUser> getCompanyUsers(UUID companyId) {
        return companyUserRepository.findByCompanyId(companyId);
    }

    public List<CompanyUser> getUserCompanies(UUID userId) {
        return companyUserRepository.findByUserId(userId);
    }
}
