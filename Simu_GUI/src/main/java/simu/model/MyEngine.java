package simu.model;

import controller.IControllerMtoV;
import dao.SimulationRunDao;
import distributionconfiguration.DistributionConfig;
import entity.*;
import simu.framework.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MyEngine extends Engine {
    private ArrivalProcess arrivalProcess;
    private ServicePoint[] servicePoints;
    private Random randomGenerator;
    private IControllerMtoV controller;
    private DistributionConfig[] userConfigs;

    // Counters
    private int totalApplications = 0;
    private int approvedCount = 0;
    private int rejectedCount = 0;
    private double totalSystemTime = 0.0;

    private int exitedApprovedCount = 0;
    private int exitedRejectedCount = 0;

    public MyEngine(IControllerMtoV controller, DistributionConfig[] configs, Long seed) {
        super(controller);
        Clock.getInstance().reset();
        this.controller = controller;
        this.userConfigs = configs;

        servicePoints = new ServicePoint[6];
        randomGenerator = (seed != null) ? new Random(seed) : new Random(System.currentTimeMillis());

        // Initialize service points
        servicePoints[0] = new ServicePoint(configs[0].buildGenerator(), eventList, EventType.END_APPLICATION_ENTRY, controller);
        servicePoints[1] = new ServicePoint(configs[1].buildGenerator(), eventList, EventType.END_DOC_SUBMISSION, controller);
        servicePoints[2] = new ServicePoint(configs[2].buildGenerator(), eventList, EventType.END_BIOMETRICS, controller);
        servicePoints[3] = new ServicePoint(configs[3].buildGenerator(), eventList, EventType.MISSING_DOCS_RESOLVED, controller);
        servicePoints[4] = new ServicePoint(configs[4].buildGenerator(), eventList, EventType.END_DOC_CHECK, controller);
        servicePoints[5] = new ServicePoint(configs[5].buildGenerator(), eventList, EventType.END_DECISION, controller);

        // Arrival process
        arrivalProcess = new ArrivalProcess(configs[6].buildGenerator(), eventList, EventType.ARRIVAL);
    }

    @Override
    protected void initialization() {
       /* if (ApplicationAsCustomer.getAllApplications().isEmpty()) {
            ApplicationAsCustomer.resetIdCounter();
        }*/
        arrivalProcess.generateNext();
    }


    @Override
    protected void runEvent(Event t) {
        ApplicationAsCustomer application;
        switch ((EventType) t.getType()) {
            case ARRIVAL -> {
                boolean isNew = randomGenerator.nextDouble() < 0.65;
                boolean docsComplete = randomGenerator.nextDouble() < 0.8;
                ApplicationAsCustomer app = new ApplicationAsCustomer(isNew, docsComplete);
                servicePoints[0].addQueue(app);
                controller.updateQueueStatus(0, servicePoints[0].getQueueSize());
                controller.visualiseCustomer();
                arrivalProcess.generateNext();
            }
            case END_APPLICATION_ENTRY -> {
                application = servicePoints[0].removeQueue();
                if (application != null) {
                    application.setCurrentStage(EventType.END_DOC_SUBMISSION);
                    controller.getVisualisation().moveCustomer(0, 1, false);
                    servicePoints[1].addQueue(application);
                }
                controller.updateQueueStatus(0, servicePoints[0].getQueueSize());
            }
            case END_DOC_SUBMISSION -> {
                application = servicePoints[1].removeQueue();
                if (application != null) {
                    if (application.requiresBiometrics()) {
                        controller.getVisualisation().moveCustomer(1, 2, false);
                        servicePoints[2].addQueue(application);
                    } else if (!application.isDocsComplete()) {
                        controller.getVisualisation().moveCustomer(1, 3, false);
                        servicePoints[3].addQueue(application);
                    } else {
                        controller.getVisualisation().moveCustomer(1, 4, false);
                        servicePoints[4].addQueue(application);
                    }
                }
                controller.updateQueueStatus(1, servicePoints[1].getQueueSize());
            }
            case END_BIOMETRICS -> {
                application = servicePoints[2].removeQueue();
                if (application != null) {
                    controller.getVisualisation().moveCustomer(2, 4, false);
                    double timeInBiometrics = Clock.getInstance().getTime() - application.getTimeEnteredQueue();
                    application.setTimeInBiometrics(timeInBiometrics);
                    servicePoints[4].addQueue(application);
                }
                controller.updateQueueStatus(2, servicePoints[2].getQueueSize());
            }
            case MISSING_DOCS_RESOLVED -> {
                application = servicePoints[3].removeQueue();
                if (application != null) {
                    controller.getVisualisation().moveCustomer(3, 4, false);
                    servicePoints[4].addQueue(application);
                }
                controller.updateQueueStatus(3, servicePoints[3].getQueueSize());
            }
            case END_DOC_CHECK -> {
                application = servicePoints[4].removeQueue();
                if (application != null) {
                    controller.getVisualisation().moveCustomer(4, 5, false);
                    servicePoints[5].addQueue(application);
                }
                controller.updateQueueStatus(4, servicePoints[4].getQueueSize());
            }
            case END_DECISION -> {
                application = servicePoints[5].removeQueue();
                if (application == null) break;

                application.setRemovalTime(Clock.getInstance().getTime());
                boolean approved = randomGenerator.nextDouble() < 0.7;
                application.setApproved(approved);
                controller.getVisualisation().moveCustomer(5, -1, approved);

                // Increment counters
                totalApplications++;
                if (approved) approvedCount++;
                else rejectedCount++;

                totalSystemTime += application.getRemovalTime() - application.getArrivalTime();

                double avgTime = totalApplications > 0 ? totalSystemTime / totalApplications : 0;
                controller.updateStatistics(totalApplications, approvedCount, rejectedCount, avgTime, Clock.getInstance().getTime());

                application.reportResults();

                // Handle reapplication
                if (!approved) {
                    application.markReapplication();
                    if (application.canReapply()) servicePoints[0].addQueue(application);
                    else eventList.add(new Event(EventType.EXIT_REJECTED, Clock.getInstance().getTime()));
                } else {
                    eventList.add(new Event(EventType.EXIT_APPROVED, Clock.getInstance().getTime()));
                }
                controller.updateQueueStatus(5, servicePoints[5].getQueueSize());
            }
            case EXIT_APPROVED -> exitedApprovedCount++;
            case EXIT_REJECTED -> exitedRejectedCount++;
        }
    }

    @Override
    protected void tryCEvents() {
        for (ServicePoint sp : servicePoints) {
            if (!sp.isReserved() && sp.isOnQueue()) sp.beginService();
            sp.checkBottleneck();
        }
    }

    @Override
    protected void results() {
        // --- 1. Calculate average system time ---
        double avgTimeInSystem = totalApplications > 0 ? totalSystemTime / totalApplications : 0;

        // --- 2. Create SimulationRun ---
        SimulationRun run = new SimulationRun();
        run.setTimestamp(LocalDateTime.now());
        run.setTotalApplications(totalApplications);
        run.setApprovedCount(approvedCount);
        run.setRejectedCount(rejectedCount);
        run.setAvgSystemTime(avgTimeInSystem);
        run.setConfigSaved(true);

        // --- 3. Find bottleneck service point ---
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

        // --- 4. Create ServicePointResults ---
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

        // --- 5. Create DistributionConfigs ---
        List<DistConfig> configs = new ArrayList<>();
        for (int i = 0; i < servicePoints.length; i++) {
            DistributionConfig ucfg = userConfigs[i];
            DistConfig dc = new DistConfig();
            dc.setServicePointName(servicePoints[i].getServicePointName());
            dc.setDistributionType(ucfg.getType());
            dc.setParam1(ucfg.getParam1());
            try {
                dc.setParam2(ucfg.getParam2());
            } catch (Exception ignored) {
                dc.setParam2(null);
            }
            dc.setSimulationRun(run);
            configs.add(dc);
        }

        // Arrival process config at index 6
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

        // --- 6. Create ApplicationLogs from all customers across service points ---
        // 4️⃣ Prepare ApplicationLog entities (new part)
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

        // --- 7. Persist everything atomically ---
        SimulationRunDao dao = new SimulationRunDao();
        dao.persistChildren(run, configs, spResults, logs);

        // --- 8. Build simulation results string ---
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

        // --- 9. Service Point performances ---
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

        // --- 10. Bottleneck summary ---
        if (bottleneck != null) {
            resultStr.append("\n****** Bottleneck Summary ******");
            resultStr.append(String.format("\nBottleneck Service Point: \"%s\"", bottleneck.getServicePointName()));
            resultStr.append(String.format("\n  -> Utilization: %.2f%%", bottleneck.getUtilization(Clock.getInstance().getTime())));
            resultStr.append(String.format("\n  -> Max queue length: %d", bottleneck.getMaxQueueLength()));
            resultStr.append(String.format("\n  -> Average waiting time: %.2f minutes", bottleneck.getAverageWaitingTime()));
            resultStr.append(String.format("Total applications found in ApplicationAsCustomer.getAllApplications(): "
                    + ApplicationAsCustomer.getAllApplications().size()));

        }

        // --- 11. Send results to GUI ---
        controller.displayResults(resultStr.toString());
        controller.showEndTime(Clock.getInstance().getTime());

        ApplicationAsCustomer.resetIdCounter();
    }
}
