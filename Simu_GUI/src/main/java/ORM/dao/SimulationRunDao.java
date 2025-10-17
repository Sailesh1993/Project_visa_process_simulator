package ORM.dao;

import ORM.datasource.MariaDbJpaConnection;
import ORM.entity.ApplicationLog;
import ORM.entity.DistConfig;
import ORM.entity.SimulationRun;
import ORM.entity.SPResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Data Access Object for {@link SimulationRun} and its related entities.
 * <p>
 * Provides methods to persist, find, and delete simulation runs along with
 * their associated distribution configurations, service point results, and application logs.
 * </p>
 */
public class SimulationRunDao {

    /**
     * Persist a {@link SimulationRun} and its associated entities atomically.
     * <p>
     * Links {@link DistConfig}, {@link SPResult}, and {@link ApplicationLog} entities
     * to the parent simulation run before persisting.
     *
     * @param run       the simulation run to persist
     * @param configs   distribution configurations related to the run
     * @param spResults service point results related to the run
     * @param logs      application logs related to the run
     * @throws RuntimeException if persistence fails
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

    /**
     * Find a {@link SimulationRun} by its ID.
     * <p>
     * Ensures that all related collections are loaded.
     *
     * @param id the primary key of the simulation run
     * @return the SimulationRun, or {@code null} if not found
     */
    public SimulationRun find(Long id) {
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            SimulationRun run = em.find(SimulationRun.class, id);

            if (run != null) {
                run.getDistConfiguration().size();
                run.getServicePointResults().size();
                run.getApplicationLogs().size();
            }

            return run;
        } finally {
            em.close();
        }
    }

    /**
     * Retrieve all {@link SimulationRun} entities, ordered by timestamp descending.
     *
     * @return list of all simulation runs
     */
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

    /**
     * Retrieve all {@link SimulationRun} entities along with their associated
     * {@link DistConfig} entities.
     *
     * @return list of simulation runs with distributions fetched
     */
    public List<SimulationRun> findAllWithAssociations() {
        EntityManager em = MariaDbJpaConnection.createEntityManager();
        try {
            TypedQuery<SimulationRun> q = em.createQuery(
                    "SELECT DISTINCT r FROM SimulationRun r " +
                            "LEFT JOIN FETCH r.distributionConfigs " +
                            "ORDER BY r.timestamp DESC",
                    SimulationRun.class
            );
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Delete a {@link SimulationRun} by its ID.
     *
     * @param id the primary key of the simulation run to delete
     * @throws RuntimeException if deletion fails
     */
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