package org.example.repository;

import jakarta.persistence.EntityManager;
import org.example.entity.User;

import java.util.UUID;

public class UserRepository {
    private final EntityManager em;

    public UserRepository(EntityManager em) {
        this.em = em;
    }

    public User getUserById(UUID id) {
        return em.find(User.class, id);
    }

    public User getUserByUsername(String username) {
        return em.find(User.class, username);
    }


    public void save(User user) {
        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();
    }

    public void delete(User user) {
        em.getTransaction().begin();
        em.remove(user);
        em.getTransaction().commit();
    }
}
