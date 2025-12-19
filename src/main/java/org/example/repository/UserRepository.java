package org.example.repository;

import jakarta.persistence.EntityManagerFactory;
import org.example.entity.User;

import java.util.Optional;
import java.util.UUID;

public class UserRepository extends BaseRepository<User, UUID> {

    public UserRepository(EntityManagerFactory emf) {
        super(emf, User.class);
    }

    public boolean existsByEmail(String email) {
        return executeRead(em ->
            !em.createQuery(
                    "SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", email)
                .getResultList()
                .isEmpty()
        );
    }

    public Optional<User> findByEmail(String email) {
        return executeRead(em ->
            em.createQuery(
                    "SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", email)
                .getResultList()
                .stream()
                .findFirst()
        );
    }

}
