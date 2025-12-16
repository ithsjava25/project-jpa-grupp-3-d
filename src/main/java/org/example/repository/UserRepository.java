package org.example.repository;

import jakarta.persistence.EntityManagerFactory;
import org.example.entity.User;

import java.util.UUID;

public class UserRepository extends BaseRepository<User, UUID> {

    public UserRepository(EntityManagerFactory emf) {
        super(emf, User.class);
    }
}
