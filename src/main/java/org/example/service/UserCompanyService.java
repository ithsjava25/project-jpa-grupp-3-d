package org.example.service;

import org.example.entity.Company;
import org.example.entity.User;
import org.example.entity.UserCompany;
import org.example.entity.UserCompanyId;
import org.example.repository.CompanyRepository;
import org.example.repository.UserCompanyRepository;
import org.example.repository.UserRepository;

import java.util.UUID;

public class UserCompanyService {
    private final UserRepository userRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final CompanyRepository companyRepository;


    public UserCompanyService(UserRepository userRepository, UserCompanyRepository userCompanyRepository, CompanyRepository companyRepository) {
        this.userRepository = userRepository;
        this.userCompanyRepository = userCompanyRepository;
        this.companyRepository = companyRepository;
    }

    public void addUserToCompany(UUID companyId, UUID userId) {
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + companyId));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + userId));

        UserCompany association = new UserCompany(user, company);

        userCompanyRepository.create(association);
    }

    public void deleteUserFromCompany(UUID companyId, UUID userId) {
        UserCompanyId id = new UserCompanyId(userId, companyId);

        UserCompany userCompany = userCompanyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User is not part of company"));

        userCompanyRepository.delete(userCompany);
    }
}
