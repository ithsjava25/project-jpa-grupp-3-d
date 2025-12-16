package org.example.repository;

import jakarta.persistence.EntityManagerFactory;
import org.example.entity.Company;

import java.util.UUID;

public class CompanyRepository extends BaseRepository<Company, UUID>{
    public CompanyRepository(EntityManagerFactory emf) {
        super(emf, Company.class);
    }
}
