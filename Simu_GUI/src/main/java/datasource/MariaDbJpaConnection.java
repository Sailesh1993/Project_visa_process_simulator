package datasource;

import jakarta.persistence.*;

public class MariaDbJpaConnection {

    private static EntityManagerFactory emf = null;

    private MariaDbJpaConnection() {}

    private static synchronized void ensureFactory() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("CompanyMariaDbUnit");
        }
    }

    /**
     * Returns a new EntityManager. Caller must close the EntityManager when done.
     */
    public static EntityManager createEntityManager() {
        ensureFactory();
        return emf.createEntityManager();
    }

    /**
     * Clean shutdown of the factory (call at app exit).
     */
    public static synchronized void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            emf = null;
        }
    }
}
