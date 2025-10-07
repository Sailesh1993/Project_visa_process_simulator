package Object_Relational_Mapping_ORM.dao;

import Object_Relational_Mapping_ORM.datasource.MariaDbJpaConnection;
import Object_Relational_Mapping_ORM.entity.ApplicationLog;
import Object_Relational_Mapping_ORM.entity.DistConfig;
import Object_Relational_Mapping_ORM.entity.SimulationRun;
import Object_Relational_Mapping_ORM.entity.SPResult;
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

    public List<SimulationRun> findAll() {
        EntityManager em = Object_Relational_Mapping_ORM.datasource.MariaDbJpaConnection.createEntityManager();
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
            if (run == null) {
                System.out.println("No SimulationRun found with ID " + id);
                em.getTransaction().commit();
                return;
            }

            // Explicitly clear child relationships (optional, helps Hibernate clean up)
            if (run.getServicePointResults() != null) run.getServicePointResults().clear();
            if (run.getDistConfiguration() != null) run.getDistConfiguration().clear();
            if (run.getApplicationLogs() != null) run.getApplicationLogs().clear();

            em.remove(run);
            em.getTransaction().commit();

            System.out.println("Deleted SimulationRun ID: " + id);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Failed to delete SimulationRun #" + id, e);
        } finally {
            em.close();
        }
    }
}