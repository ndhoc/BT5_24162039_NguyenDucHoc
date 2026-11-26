package com.nguyenduchoc.bt5.repository;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

/** Cấu hình JPA dùng chung: một EntityManagerFactory cho cả ứng dụng web. */
public final class JpaConfig {
    private static final String UNIT = "jpa-hibernate-mysql";
    private static volatile EntityManagerFactory factory;

    private JpaConfig() { }

    public static EntityManagerFactory getEntityManagerFactory() {
        EntityManagerFactory current = factory;
        if (current == null || !current.isOpen()) {
            synchronized (JpaConfig.class) {
                current = factory;
                if (current == null || !current.isOpen()) {
                    try {
                        Map<String, String> settings = new HashMap<>();
                        copyEnv(settings, "DB_URL", "jakarta.persistence.jdbc.url");
                        copyEnv(settings, "DB_USERNAME", "jakarta.persistence.jdbc.user");
                        copyEnv(settings, "DB_PASSWORD", "jakarta.persistence.jdbc.password");
                        current = Persistence.createEntityManagerFactory(UNIT, settings);
                    } catch (RuntimeException e) {
                        throw new DataAccessException(e);
                    }
                    factory = current;
                }
            }
        }
        return current;
    }

    public static EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static <T> T read(Function<EntityManager, T> work) {
        try (EntityManager em = createEntityManager()) {
            return work.apply(em);
        } catch (DataAccessException | IllegalArgumentException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new DataAccessException(e);
        }
    }

    public static void write(Consumer<EntityManager> work) {
        writeResult(em -> {
            work.accept(em);
            return null;
        });
    }

    public static <T> T writeResult(Function<EntityManager, T> work) {
        EntityManager em = createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T result = work.apply(em);
            tx.commit();
            return result;
        } catch (DataAccessException | IllegalArgumentException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw new DataAccessException(e);
        } finally {
            em.close();
        }
    }

    public static void shutdown() {
        EntityManagerFactory current = factory;
        if (current != null && current.isOpen()) current.close();
    }

    private static void copyEnv(Map<String, String> settings, String env, String key) {
        String value = System.getenv(env);
        if (value != null && !value.isBlank()) settings.put(key, value);
    }
}
