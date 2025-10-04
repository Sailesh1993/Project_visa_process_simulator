package dao;

import entity.*;
import jakarta.persistence.EntityManager;

import java.util.List;

public class SimulationRunDao {

    public void persist(SimulationRun run,
                        List<DistConfig> configs,
                        List<SPResult> spResults,
                        List<ApplicationLog> logs) {

        EntityManager em = datasource.MariaDbJpaConnection.getInstance();

        try {
            em.getTransaction().begin();

            em.persist(run);                    //persist parent first

            //persist children manually
            for (DistConfig config : configs) {
                config.setSimulationRun(run);
                em.persist(config);
            }

            for (SPResult spr : spResults) {
                spr.setSimulationRun(run);
                em.persist(spr);
            }

            for (ApplicationLog log : logs) {
                log.setSimulationRun(run);
                em.persist(log);
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        }
    }

    public SimulationRun find(Long id) {
        EntityManager em = datasource.MariaDbJpaConnection.getInstance();
        return em.find(SimulationRun.class, id);
    }

    public List<SimulationRun> findAll() {
        EntityManager em = datasource.MariaDbJpaConnection.getInstance();
        return em.createQuery("SELECT r FROM SimulationRun r", SimulationRun.class).getResultList();
    }

    public void deleteById(Long id) {
        EntityManager em = datasource.MariaDbJpaConnection.getInstance();

        try {
            em.getTransaction().begin();
            SimulationRun run = em.find(SimulationRun.class, id);
            if (run != null) {
                em.remove(run);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
        }
    }

}
