package datasource;

import jakarta.persistence.*;

public class MariaDbJpaConnection {

    private static EntityManagerFactory emf = null;
    private static EntityManager em = null;

    public static EntityManager getInstance() {

        if (em==null) {
            if (emf==null) {
                emf = Persistence.createEntityManagerFactory("CompanyMariaDbUnit");
            }
            em = emf.createEntityManager();
        }
        return em;
    }

    public static void shutdown() {
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