package ORM.datasource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Utility class for managing JPA connections to a MariaDB database.
 * <p>
 * Provides a centralized way to create {@link EntityManager} instances and
 * manage the {@link EntityManagerFactory} lifecycle.
 * </p>
 * <p>
 * This class is a singleton utility: all methods are static, and the constructor
 * is private to prevent instantiation.
 * </p>
 * <p>
 * Usage example:
 * <pre>
 * EntityManager em = MariaDbJpaConnection.createEntityManager();
 * try {
 *     // perform database operations
 * } finally {
 *     em.close();
 * }
 * MariaDbJpaConnection.shutdown(); // call at application termination
 * </pre>
 * </p>
 */
public class MariaDbJpaConnection {

    private static EntityManagerFactory emf = null;
    private static EntityManager em = null;

    /** Private constructor to prevent instantiation. */
    private MariaDbJpaConnection() {}

    /**
     * Ensures that the {@link EntityManagerFactory} is initialized.
     * <p>
     * This method is synchronized to be thread-safe and guarantees that
     * only a single factory instance exists during the application's lifetime.
     * </p>
     */
    private static synchronized void ensureFactory() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("CompanyMariaDbUnit");
        }
    }

    /**
     * Creates a new {@link EntityManager} instance.
     * <p>
     * The caller is responsible for closing the EntityManager after use
     * to avoid resource leaks.
     * </p>
     *
     * @return a new {@link EntityManager} connected to the MariaDB database
     */
    public static EntityManager createEntityManager() {
        ensureFactory();
        return emf.createEntityManager();
    }

    /**
     * Performs a clean shutdown of the {@link EntityManagerFactory} and
     * any shared {@link EntityManager}.
     * <p>
     * This method should be called once at application termination to release
     * all resources used by JPA.
     * </p>
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
