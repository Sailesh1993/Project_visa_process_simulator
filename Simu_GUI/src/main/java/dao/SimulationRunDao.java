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
     * Step 1: Persist only the SimulationRun to generate its ID.
     */
    public void persistSimulationRun(SimulationRun run) {
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(run);      // persist parent only
            em.getTransaction().commit(); // after commit, run.getId() is available
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Failed to persist SimulationRun", e);
        } finally {
            em.close();
        }
    }

    /**
     * Step 2: Persist all child entities linked to an existing SimulationRun.
     * The run must already have an ID.
     */
    public void persistChildren(SimulationRun run,
                                List<DistConfig> configs,
                                List<SPResult> spResults,
                                List<ApplicationLog> logs) {

        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            em.getTransaction().begin();

            // Merge in case run is detached
            run = em.merge(run);

            // Persist distribution configs
            if (configs != null) {
                for (DistConfig config : configs) {
                    config.setSimulationRun(run);
                    em.persist(config);
                }
            }

            // Persist service point results
            if (spResults != null) {
                for (SPResult spr : spResults) {
                    spr.setSimulationRun(run);
                    em.persist(spr);
                }
            }

            // Persist application logs
            if (logs != null) {
                for (ApplicationLog log : logs) {
                    log.setSimulationRun(run);
                    em.persist(log);
                }
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Failed to persist child entities for SimulationRun", e);
        } finally {
            em.close();
        }
    }

    // --- Existing methods (find, findAll, findByName, deleteById) remain unchanged ---

    public SimulationRun find(Long id) {
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
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
        EntityManager em = MariaDbJpaConnection.createEntityManager();
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
            if (run != null) em.remove(run);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Failed to delete run #" + id, e);
        } finally {
            em.close();
        }
    }
}
