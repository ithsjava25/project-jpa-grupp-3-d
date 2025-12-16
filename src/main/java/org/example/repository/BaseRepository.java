package org.example.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.function.Function;

public abstract class BaseRepository <T, ID> {

    private final EntityManagerFactory emf;
    protected final Class <T> entityClass;

    protected BaseRepository(EntityManagerFactory emf, Class<T> entityClass) {
        this.emf = emf;
        this.entityClass = entityClass;
    }

    protected <R> R runInTransaction(Function<EntityManager, R> action) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            R result = action.apply(em);
            em.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Transaction failed for " + entityClass.getSimpleName(), e);
        } finally {
            em.close();
        }
    }

}
