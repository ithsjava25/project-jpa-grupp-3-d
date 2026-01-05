package org.example.repository;

import jakarta.persistence.EntityManagerFactory;
import org.example.entity.client.Client;

import java.util.List;
import java.util.UUID;

public class ClientRepository extends BaseRepository<Client, UUID> {
    public ClientRepository(EntityManagerFactory emf) {
        super(emf, Client.class);
    }

    public List<Client> findByCompanyId(UUID companyId) {
        return executeRead(em ->
            em.createQuery("SELECT c FROM Client c WHERE c.company.id = :companyId", Client.class)
                .setParameter("companyId", companyId)
                .getResultList()
        );
    }
}
