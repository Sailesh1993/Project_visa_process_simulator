package MVC.simu.model;

import MVC.controller.IControllerMtoV;
import ORM.dao.SimulationRunDao;
import eduni.project_distributionconfiguration.DistributionConfig;
import ORM.entity.*;
import MVC.simu.framework.*;
import javafx.application.Platform;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The {@code MyEngine} class defines the main simulation logic
 * for the visa application processing system.
 * <p>
 * It manages the flow of {@link ApplicationAsCustomer} objects through
 * various {@link ServicePoint}s according to the events defined in
 * {@link EventType}. It tracks statistics such as total processed
 * applications, approvals, rejections, average time in system,
 * and detects the system bottleneck.
 * <p>
 * At the end of the simulation, {@code MyEngine} persists all
 * results using {@link SimulationRunDao} and updates the GUI through
 * the {@link IControllerMtoV} controller.
 */

public class MyEngine extends Engine {

    /** Manages the generation of new applications entering the system. */
    private ArrivalProcess arrivalProcess;

    /** Array of all service points in the system, indexed 0 to 5. */
    private ServicePoint[] servicePoints;

    /** Random number generator used for stochastic decisions (e.g., approval, docs completeness). */
    private Random randomGenerator;

    /** Reference to the controller for updating the GUI and passing data. */
    private IControllerMtoV controller;

    /** Array of user-defined distribution configurations for each service point and arrival process. */
    private DistributionConfig[] userConfigs;

    /** Probability that a new application is a first-time applicant. */
    private static final double NEW_APPLICATION_PROBABILITY = 0.65;

    /** Probability that an application has complete documents on arrival. */
    private static final double DOCS_COMPLETE_PROBABILITY = 0.8;

    // Simulation counters

    /** Total number of applications processed by the system. */
    private int totalApplications = 0;

    /** Number of applications approved by the system. */
    private int approvedCount = 0;

    /** Number of applications rejected by the system. */
    private int rejectedCount = 0;

    /** Cumulative time all applications spent in the system (for computing average). */
    private double totalSystemTime = 0.0;

    /** Number of approved applications that exited the system. */
    private int exitedApprovedCount = 0;

    /** Number of rejected applications that exited the system. */
    private int exitedRejectedCount = 0;

    /**
     * Constructs a new simulation engine instance.
     *
     * @param controller the controller used to update the GUI
     * @param configs    the distribution configurations for each service point and the arrival process
     * @param seed       optional random seed (if {@code null}, a system time–based seed is used)
     */
    public MyEngine(IControllerMtoV controller, DistributionConfig[] configs, Long seed) {
        super(controller);
        Clock.getInstance().reset();
        this.controller = controller;
        this.userConfigs = configs;

        servicePoints = new ServicePoint[6];
        randomGenerator = (seed != null) ? new Random(seed) : new Random(System.currentTimeMillis());

        // Initialize all service points according to configuration
        servicePoints[0] = new ServicePoint(configs[0].buildGenerator(), eventList, EventType.END_APPLICATION_ENTRY, controller);
        servicePoints[1] = new ServicePoint(configs[1].buildGenerator(), eventList, EventType.END_DOC_SUBMISSION, controller);
        servicePoints[2] = new ServicePoint(configs[2].buildGenerator(), eventList, EventType.END_BIOMETRICS, controller);
        servicePoints[3] = new ServicePoint(configs[3].buildGenerator(), eventList, EventType.MISSING_DOCS_RESOLVED, controller);
        servicePoints[4] = new ServicePoint(configs[4].buildGenerator(), eventList, EventType.END_DOC_CHECK, controller);
        servicePoints[5] = new ServicePoint(configs[5].buildGenerator(), eventList, EventType.END_DECISION, controller);

        // Initialize arrival process
        arrivalProcess = new ArrivalProcess(configs[6].buildGenerator(), eventList, EventType.ARRIVAL);
    }

    /**
     * Initializes the simulation by scheduling the first arrival event.
     */
    @Override
    protected void initialization() {
        arrivalProcess.generateNext();
    }

    /**
     * Handles all event types that occur during the simulation run.
     * This method defines how {@link ApplicationAsCustomer} objects
     * move between service points and how outcomes (approval/rejection)
     * are determined.
     *
     * @param t the event to process
     */
    @Override
    protected void runEvent(Event t) {
        ApplicationAsCustomer application;
        switch ((EventType) t.getType()) {
            case ARRIVAL -> {
                boolean isNew = randomGenerator.nextDouble() < NEW_APPLICATION_PROBABILITY;
                boolean docsComplete = randomGenerator.nextDouble() < DOCS_COMPLETE_PROBABILITY;

                ApplicationAsCustomer app = new ApplicationAsCustomer(isNew, docsComplete);
                servicePoints[0].addQueue(app);
                Platform.runLater(() -> {
                            controller.updateQueueStatus(0, servicePoints[0].getQueueSize());
                            controller.visualiseCustomer();
                        });
                arrivalProcess.generateNext();
            }
            case END_APPLICATION_ENTRY -> {
                application = servicePoints[0].removeQueue();
                if (application != null) {
                    application.setCurrentStage(EventType.END_DOC_SUBMISSION);
                    servicePoints[1].addQueue(application);
                    Platform.runLater(() -> {
                        controller.getVisualisation().moveCustomer(0, 1, false);
                        controller.updateQueueStatus(0, servicePoints[0].getQueueSize());
                    });
                } else {
                    Platform.runLater(() -> controller.updateQueueStatus(0, servicePoints[0].getQueueSize()));
                }
            }

            case END_DOC_SUBMISSION -> {
                application = servicePoints[1].removeQueue();
                if (application != null) {
                    if (application.requiresBiometrics()) {
                        servicePoints[2].addQueue(application);
                        Platform.runLater(() -> {controller.getVisualisation().moveCustomer(1, 2, false);});
                    } else if (!application.isDocsComplete()) {
                        servicePoints[3].addQueue(application);
                        Platform.runLater(() -> {controller.getVisualisation().moveCustomer(1, 3, false);});
                    } else {
                        servicePoints[4].addQueue(application);
                        Platform.runLater(() -> {controller.getVisualisation().moveCustomer(1, 4, false); });
                    }
                }
                controller.updateQueueStatus(1, servicePoints[1].getQueueSize());
            }
            case END_BIOMETRICS -> {
                application = servicePoints[2].removeQueue();
                if (application != null) {
                    double timeInBiometrics = Clock.getInstance().getTime() - application.getTimeEnteredQueue();
                    application.setTimeInBiometrics(timeInBiometrics);
                    servicePoints[4].addQueue(application);
                    Platform.runLater(() -> {controller.getVisualisation().moveCustomer(2, 4, false);});
                }
                Platform.runLater(() -> {controller.updateQueueStatus(2, servicePoints[2].getQueueSize());});
            }
            case MISSING_DOCS_RESOLVED -> {
                application = servicePoints[3].removeQueue();
                if (application != null) {
                    servicePoints[4].addQueue(application);
                    Platform.runLater(() -> {controller.getVisualisation().moveCustomer(3, 4, false);});
                }
                Platform.runLater(() -> {controller.updateQueueStatus(3, servicePoints[3].getQueueSize());});
            }
            case END_DOC_CHECK -> {
                application = servicePoints[4].removeQueue();
                if (application != null) {
                    servicePoints[5].addQueue(application);
                    Platform.runLater(() -> {controller.getVisualisation().moveCustomer(4, 5, false);});
                }
                Platform.runLater(() -> {controller.updateQueueStatus(4, servicePoints[4].getQueueSize());});
            }
            case END_DECISION -> {
                application = servicePoints[5].removeQueue();
                if (application == null) break;

                application.setRemovalTime(Clock.getInstance().getTime());
                boolean approved = randomGenerator.nextDouble() < 0.7;
                application.setApproved(approved);
                Platform.runLater(() -> {controller.getVisualisation().moveCustomer(5, -1, approved);});

                // Increment counters
                totalApplications++;
                if (approved) approvedCount++;
                else rejectedCount++;

                totalSystemTime += application.getRemovalTime() - application.getArrivalTime();

                double avgTime = totalApplications > 0 ? totalSystemTime / totalApplications : 0;
                Platform.runLater(() -> {controller.updateStatistics(totalApplications, approvedCount, rejectedCount, avgTime, Clock.getInstance().getTime());});

                application.reportResults();

                // Handle reapplication
                if (!approved) {
                    application.markReapplication();
                    if (application.canReapply()) servicePoints[0].addQueue(application);
                    else eventList.add(new Event(EventType.EXIT_REJECTED, Clock.getInstance().getTime()));
                } else {
                    eventList.add(new Event(EventType.EXIT_APPROVED, Clock.getInstance().getTime()));
                }
                Platform.runLater(() -> {controller.updateQueueStatus(5, servicePoints[5].getQueueSize());});
            }
            case EXIT_APPROVED -> exitedApprovedCount++;
            case EXIT_REJECTED -> exitedRejectedCount++;
        }
    }

    /**
     * Checks each {@link ServicePoint} for conditions that allow
     * service to begin or a bottleneck to be detected.
     * This is called repeatedly during the simulation loop.
     */
    @Override
    protected void tryCEvents() {
        for (ServicePoint sp : servicePoints) {
            if (sp.isReserved() && sp.isOnQueue()) sp.beginService();
            sp.checkBottleneck();
        }
    }

    /**
     * Collects and persists the final simulation results at the end of each simulation run.
     * <p>
     * This includes:
     * <ul>
     *   <li>Computing overall statistics (approval rate, average time, exits)</li>
     *   <li>Detecting the bottleneck service point</li>
     *   <li>Persisting all entities ({@link SimulationRun}, {@link SPResult}, {@link DistConfig}, {@link ApplicationLog}) into the database</li>
     *   <li>Building and displaying a summary report in the GUI</li>
     * </ul>
     */
    @Override
    protected void results() {
        // Calculate average system time
        double avgTimeInSystem = totalApplications > 0 ? totalSystemTime / totalApplications : 0;

        // Create a SimulationRun entity representing this simulation run
        // Sets local timestamp and aggregates key statistics (total applications, approvals, rejections, average system time)
        // Each SimulationRun is persisted in the database with a primary key (runId) and displayed in the GUI
        SimulationRun run = new SimulationRun();
        run.setTimestamp(LocalDateTime.now());
        run.setTotalApplications(totalApplications);
        run.setApprovedCount(approvedCount);
        run.setRejectedCount(rejectedCount);
        run.setAvgSystemTime(avgTimeInSystem);
        run.setConfigSaved(true);

        // Find bottleneck service point
        ServicePoint bottleneck = null;
        double maxUtilization = 0.0;
        List<SPResult> spResults = new ArrayList<>();
        for (ServicePoint sp : servicePoints) {
            double utilization = sp.getUtilization(Clock.getInstance().getTime());
            if (utilization > maxUtilization) {
                maxUtilization = utilization;
                bottleneck = sp;
            }
        }

        // Prepare ServicePoint results
        for (ServicePoint sp : servicePoints) {
            boolean isBottleneck = (sp == bottleneck);
            SPResult spr = new SPResult(
                    sp.getServicePointName(),
                    sp.getTotalDepartures(),
                    sp.getAverageWaitingTime(),
                    sp.getMaxQueueLength(),
                    sp.getUtilization(Clock.getInstance().getTime()),
                    sp.getNumEmployees(),
                    isBottleneck
            );
            spr.setSimulationRun(run);
            spResults.add(spr);
        }

        // Prepare DistributionConfigs entity
        List<DistConfig> configs = new ArrayList<>();
        for (int i = 0; i < servicePoints.length; i++) {
            DistributionConfig userDistConfig = userConfigs[i];
            DistConfig dc = new DistConfig();
            dc.setServicePointName(servicePoints[i].getServicePointName());
            dc.setDistributionType(userDistConfig.getType());
            dc.setParam1(userDistConfig.getParam1());
            try {
                dc.setParam2(userDistConfig.getParam2());
            } catch (Exception ignored) {
                dc.setParam2(null);
            }
            dc.setSimulationRun(run);
            configs.add(dc);
        }

        // Prepare arrival process configuration
        DistributionConfig arrivalCfg = userConfigs[6];
        DistConfig arrivalDc = new DistConfig();
        arrivalDc.setServicePointName("Arrival Process");
        arrivalDc.setDistributionType(arrivalCfg.getType());
        arrivalDc.setParam1(arrivalCfg.getParam1());
        try {
            arrivalDc.setParam2(arrivalCfg.getParam2());
        } catch (Exception ignored) {
            arrivalDc.setParam2(null);
        }
        arrivalDc.setSimulationRun(run);
        configs.add(arrivalDc);

        // Prepare Application logs
        List<ApplicationLog> logs = new ArrayList<>();
        for (ApplicationAsCustomer app : ApplicationAsCustomer.getAllApplications()) {
            // only completed apps
            ApplicationLog log = new ApplicationLog(
                    app.getId(),
                    app.getArrivalTime(),
                    app.getRemovalTime(),
                    app.isApproved(),
                    app.getTimeInWaitingRoom()
            );
            log.setMessage("Application #" + app.getId() + " completed. Approved: " + app.isApproved());
            log.setTimestamp(LocalDateTime.now());
            log.setSimulationRun(run);
            logs.add(log);
        }

        // Persist all entities atomically
        SimulationRunDao dao = new SimulationRunDao();
        dao.persist(run, configs, spResults, logs);

        // Build simulation results string
        StringBuilder resultStr = new StringBuilder();
        resultStr.append("\n*---------------------------------------------------------------------------------*");
        resultStr.append(String.format("\nSimulation ended at %.2f", Clock.getInstance().getTime()));
        resultStr.append("\n****** Simulation Results ******");
        resultStr.append(String.format("\n  -> Total applications processed: %d applications.", totalApplications));
        resultStr.append(String.format("\n  -> Approved applications: %d applications", approvedCount));
        resultStr.append(String.format("\n  -> Approved application exits: %d", exitedApprovedCount));
        resultStr.append(String.format("\n  -> Rejected applications: %d applications", rejectedCount));
        resultStr.append(String.format("\n  -> Rejected application exits: %d", exitedRejectedCount));
        resultStr.append(String.format("\n  -> Average time in system: %.2f minutes.\n", avgTimeInSystem));

        // Service Point performances
        for (ServicePoint sp : servicePoints) {
            boolean isBottleneck = sp == bottleneck;
            resultStr.append(String.format("\nService Point \"%s\" Metrics%s:",
                    sp.getServicePointName(),
                    isBottleneck ? " <-- BOTTLENECK" : ""));
            resultStr.append(String.format("\n  -> Total departures: %d applications.", sp.getTotalDepartures()));
            resultStr.append(String.format("\n  -> Average waiting time: %.2f minutes", sp.getAverageWaitingTime()));
            resultStr.append(String.format("\n  -> Max queue length: %d applications", sp.getMaxQueueLength()));
            resultStr.append(String.format("\n  -> Utilization: %.2f%s", sp.getUtilization(Clock.getInstance().getTime()), isBottleneck ? " <-- HIGHEST" : ""));
            resultStr.append(String.format("\n  -> Number of employees: %d", sp.getNumEmployees()));
            resultStr.append("\n");
        }

        // Bottleneck summary
        if (bottleneck != null) {
            resultStr.append("\n****** Bottleneck Summary ******");
            resultStr.append(String.format("\nBottleneck Service Point: \"%s\"", bottleneck.getServicePointName()));
            resultStr.append(String.format("\n  -> Utilization: %.2f%%", bottleneck.getUtilization(Clock.getInstance().getTime())));
            resultStr.append(String.format("\n  -> Max queue length: %d", bottleneck.getMaxQueueLength()));
            resultStr.append(String.format("\n  -> Average waiting time: %.2f minutes", bottleneck.getAverageWaitingTime()));
            resultStr.append(String.format("\nfound in ApplicationAsCustomer.getAllApplications(): "
                    + ApplicationAsCustomer.getAllApplications().size()));

        }

        // Send results to GUI
        Platform.runLater(() -> {
        controller.displayResults(resultStr.toString());
        controller.showEndTime(Clock.getInstance().getTime());
    });
        ApplicationAsCustomer.resetIdCounter();
    }
}
