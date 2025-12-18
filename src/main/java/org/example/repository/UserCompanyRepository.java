package org.example.repository;

import jakarta.persistence.EntityManagerFactory;
import org.example.entity.UserCompany;
import org.example.entity.UserCompanyId;


public class UserCompanyRepository extends BaseRepository<UserCompany, UserCompanyId> {
    public UserCompanyRepository(EntityManagerFactory emf) {
        super(emf, UserCompany.class);
    }
}
