package Object_Relational_Mapping_ORM.datasource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class MariaDbJpaConnection {

    private static EntityManagerFactory emf = null;
    private static EntityManager em = null;

    private MariaDbJpaConnection() {}

    /**
     * Ensure that the EntityManagerFactory is initialized.
     */
    private static synchronized void ensureFactory() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("CompanyMariaDbUnit");
        }
    }

    /**
     * Returns a new EntityManager. Caller must close it after use.
     */
    public static EntityManager createEntityManager() {
        ensureFactory();
        return emf.createEntityManager();
    }

    /**
     * Returns a shared EntityManager instance (lazy initialized).
     */
    public static synchronized EntityManager getInstance() {
        ensureFactory();
        if (em == null || !em.isOpen()) {
            em = emf.createEntityManager();
        }
        return em;
    }

    /**
     * Clean shutdown of the factory and shared EntityManager.
     */
    public static synchronized void shutdown() {
        if (em != null && em.isOpen()) {
            em.close();
            em = null;
        }
        if (emf != null && emf.isOpen()) {
            emf.close();
            emf = null;
        }
    }
}
