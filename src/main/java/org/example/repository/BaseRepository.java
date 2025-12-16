package org.example.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.Optional;
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

    protected <R> R executeRead(Function<EntityManager, R> action) {
        EntityManager em = emf.createEntityManager();
        try {
            return action.apply(em);
        } finally {
            em.close();
        }
    }

    public void save(T entity) {
        runInTransaction(em -> {
            if (em.contains(entity)) {
                em.merge(entity);
            } else {
                em.persist(entity);
            }
            return null;
        });
    }

    public void delete(T entity) {
        runInTransaction(em -> {
            em.remove(em.contains(entity) ? entity : em.merge(entity));
            return null;
        });
    }

    public Optional<T> findById(ID id) {
        return executeRead(em -> Optional.ofNullable(em.find(entityClass, id)));
    }

    public List<T> findAll() {
        return executeRead(em ->
            em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass)
                .getResultList()
        );
    }





}
