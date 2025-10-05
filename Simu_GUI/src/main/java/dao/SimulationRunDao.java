package dao;

import datasource.MariaDbJpaConnection;
import entity.ApplicationLog;
import entity.DistConfig;
import entity.SimulationRun;
import entity.SPResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class SimulationRunDao {

    /**
     * Persist a SimulationRun and its related entities (distConfigs, spResults, logs) atomically.
     */
    public void persist(SimulationRun run,
                        List<DistConfig> configs,
                        List<SPResult> spResults,
                        List<ApplicationLog> logs) {

        EntityManager em = MariaDbJpaConnection.createEntityManager();

        try {
            em.getTransaction().begin();

            em.persist(run);                    //persist parent first

            //persist distribution configs
            if (configs != null) {
                for (DistConfig config : configs) {
                    config.setSimulationRun(run);           //Link to parent
                    em.persist(config);
                }
            }

            //persist service point results
            if (spResults != null) {
                for (SPResult spr : spResults) {
                    spr.setSimulationRun(run);
                    em.persist(spr);
                }
            }

            //persist application logs
            if  (logs != null) {
                for (ApplicationLog log : logs) {
                    log.setSimulationRun(run);
                    em.persist(log);
                }
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Failed to persist SimulationRun and children", e);
        } finally {
            em.close();
        }
    }

    public SimulationRun find(Long id) {
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            // Use JOIN FETCH to eagerly load lists (assumes mappings in SimulationRun exist)
            // Adjust property names if your SimulationRun fields have different names
            TypedQuery<SimulationRun> q = em.createQuery(
                    "SELECT r FROM SimulationRun r " +
                            "LEFT JOIN FETCH r.distributionConfigs dc " +
                            "LEFT JOIN FETCH r.servicePointResults spr " +
                            "LEFT JOIN FETCH r.applicationLogs al " +
                            "WHERE r.id = :id", SimulationRun.class);
            q.setParameter("id", id);
            List<SimulationRun> results = q.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    public List<SimulationRun> findAll() {
        EntityManager em = datasource.MariaDbJpaConnection.createEntityManager();
        try {
            TypedQuery<SimulationRun> q = em.createQuery(
                    "SELECT r FROM SimulationRun r ORDER BY r.timestamp DESC", SimulationRun.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public List<SimulationRun> findByName(String name) {
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            TypedQuery<SimulationRun> q = em.createQuery(
                    "SELECT r FROM SimulationRun r WHERE r.runName LIKE :name ORDER BY r.timestamp DESC",
                    SimulationRun.class
            );
            q.setParameter("name", "%" + name + "%"); // partial match
            return q.getResultList();
        } finally {
            em.close();
        }
    }


    public void deleteById(Long id) {
        EntityManager em = MariaDbJpaConnection.createEntityManager();

        try {
            em.getTransaction().begin();
            SimulationRun run = em.find(SimulationRun.class, id);
            if (run != null) {
                em.remove(run);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Failed to delete run #" + id, e);
        } finally {
            em.close();
        }
    }

}