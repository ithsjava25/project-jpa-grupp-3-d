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

    protected <R> R runInTransaction(Function<EntityManager, R> dbOperation) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            R result = dbOperation.apply(em);
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
        try (EntityManager em = emf.createEntityManager()) {
            return action.apply(em);
        }
    }

    public T create(T entity) {
        return runInTransaction(em -> {
            em.persist(entity);
            return entity;
        });
    }

    public T update(T entity) {
        return runInTransaction(em -> {
            return em.merge(entity);
        });
    }

    public void delete(T entity) {
        runInTransaction(em -> {
            if (em.contains(entity)) {
                em.remove(entity);
            } else {
                // Entity is detached, fetch it by ID within the transaction
                Object id = em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
                if (id == null) {
                    throw new IllegalArgumentException("Cannot delete entity without an ID");
                }
                T managedEntity = em.find(entityClass, id);
                if (managedEntity == null) {
                    throw new IllegalArgumentException(entityClass.getSimpleName() + " not found with id: " + id);
                }
                em.remove(managedEntity);
            }
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
