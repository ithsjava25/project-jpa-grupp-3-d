package org.example.repository;

import jakarta.persistence.EntityManagerFactory;
import org.example.entity.Client;

import java.util.UUID;

public class ClientRepository extends BaseRepository<Client, UUID> {
    public ClientRepository(EntityManagerFactory emf) {
        super(emf, Client.class);
    }
}
