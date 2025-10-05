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

    private List<ApplicationLog> applicationLogs = new ArrayList<>();

    //counters
    private int totalApplications = 0;
    private int approvedCount = 0;
    private int rejectedCount = 0;
    private double totalSystemTime = 0.0;

    private int exitedApprovedCount = 0;
    private int exitedRejectedCount = 0;

    public MyEngine(IControllerMtoV controller, DistributionConfig[] configs, Long seed){ // NEW
<<<<<<< HEAD
        super(controller);// NEW(Pass controller to Engine)
=======
		super(controller);// NEW(Pass controller to Engine)
>>>>>>> origin/feature-myEngine
        Clock.getInstance().reset();// for new simulation
        this.controller = controller;
        this.userConfigs = configs;
        servicePoints = new ServicePoint[6];
        randomGenerator = (seed != null) ? new Random(seed) : new Random(System.currentTimeMillis());           //To make runs repeatable(Same results everytime)

        servicePoints[0] = new ServicePoint(configs[0].buildGenerator(), eventList, EventType.END_APPLICATION_ENTRY, controller); // SP1
        servicePoints[1] = new ServicePoint(configs[1].buildGenerator(), eventList, EventType.END_DOC_SUBMISSION, controller);    // SP2
        servicePoints[2] = new ServicePoint(configs[2].buildGenerator(), eventList, EventType.END_BIOMETRICS, controller);        // SP3: Biometrics
        servicePoints[3] = new ServicePoint(configs[3].buildGenerator(), eventList, EventType.MISSING_DOCS_RESOLVED, controller);   // SP4: Missing Docs
        servicePoints[4] = new ServicePoint(configs[4].buildGenerator(), eventList, EventType.END_DOC_CHECK, controller);         // SP5
        servicePoints[5] = new ServicePoint(configs[5].buildGenerator(), eventList, EventType.END_DECISION, controller);          // SP6

        // Initialize Arrival Process
        arrivalProcess = new ArrivalProcess(configs[6].buildGenerator(), eventList, EventType.ARRIVAL);
    }

    @Override
    protected void initialization() {
        ApplicationAsCustomer.resetIdCounter();
        arrivalProcess.generateNext();
    }

    @Override
    protected void runEvent(Event t) {  // B phase events
        ApplicationAsCustomer application;

        switch ((EventType)t.getType()){
            case ARRIVAL:
                boolean isNew = randomGenerator.nextDouble() < 0.65;      //65% chance of being a new application
                boolean docsComplete = randomGenerator.nextDouble() < 0.8;        //80% chance of having all documents complete
                servicePoints[0].addQueue(new ApplicationAsCustomer(isNew, docsComplete));          //Add to Application Entry & Appointment Booking
                controller.updateQueueStatus(0, servicePoints[0].getQueueSize());       //Queue update

                controller.visualiseCustomer();         //Notifying controller to visualize customer arrival
                arrivalProcess.generateNext();          //Schedule next arrival
                break;

            case END_APPLICATION_ENTRY:             // Application Entry & Appointment Booking done, move to Document Submission & Interview
                application = servicePoints[0].removeQueue();
                application.setCurrentStage(EventType.END_DOC_SUBMISSION);
                controller.getVisualisation().moveCustomer(0, 1, false);
                servicePoints[1].addQueue(application);
                controller.updateQueueStatus(0, servicePoints[0].getQueueSize());
                break;

            case END_DOC_SUBMISSION:                                // Document Submission & Interview done, move to conditional Service points
                application = servicePoints[1].removeQueue();
                if (application.requiresBiometrics()) {
                    controller.getVisualisation().moveCustomer(1, 2, false);
                    servicePoints[2].addQueue(application);         // Move to Biometrics Collection
                } else if (!application.isDocsComplete()) {
                    controller.getVisualisation().moveCustomer(1, 3, false);
                    servicePoints[3].addQueue(application);         // Move to Missing Documents Resolution
                } else {
                    controller.getVisualisation().moveCustomer(1, 4, false);
                    servicePoints[4].addQueue(application);         // Move to Document Verification & Background Check
                }
                controller.updateQueueStatus(1, servicePoints[1].getQueueSize());           //Update UI
                break;

            case END_BIOMETRICS:
                application = servicePoints[2].removeQueue();
                controller.getVisualisation().moveCustomer(2, 4, false);

                if (application.requiresBiometrics()) {
                    double timeInBiometrics = Clock.getInstance().getTime() - application.getTimeEnteredQueue();
                    application.setTimeInBiometrics(timeInBiometrics);
                }
                servicePoints[4].addQueue(application);             // move to Document Verification & Background Check
                controller.updateQueueStatus(2, servicePoints[2].getQueueSize());           //Update UI
                break;

            case MISSING_DOCS_RESOLVED:
                application = servicePoints[3].removeQueue();
                controller.getVisualisation().moveCustomer(3, 4, false);  // Add animation
                servicePoints[4].addQueue(application);             // move from [4] Missing Documents Resolution to [5] Document Verification & Background Check
                controller.updateQueueStatus(3, servicePoints[3].getQueueSize());           //Update UI
                break;

            case END_DOC_CHECK:
                application = servicePoints[4].removeQueue();
                controller.getVisualisation().moveCustomer(4, 5, false);  // Add animation
                servicePoints[5].addQueue(application); // move to [6] Decision Room
                controller.updateQueueStatus(4, servicePoints[4].getQueueSize());           //Update UI
                break;

            case END_DECISION:
                application = servicePoints[5].removeQueue();
                application.setRemovalTime(Clock.getInstance().getTime());

                boolean approved = randomGenerator.nextDouble() < 0.7;
                application.setApproved(approved);

                // ADD THIS BLOCK HERE - Create application log
                ApplicationLog log = new ApplicationLog();
                StringBuilder msg = new StringBuilder();
                msg.append("Application ID: ").append(application.getId())
                        .append(" | Arrival: ").append(application.getArrivalTime())
                        .append(" | Removal: ").append(application.getRemovalTime())
                        .append(" | TimeInSystem: ").append(String.format("%.2f", application.getRemovalTime() - application.getArrivalTime()))
                        .append(" | Approved: ").append(approved);
                log.setMessage(msg.toString());
                log.setTimestamp(LocalDateTime.now());
                applicationLogs.add(log);
                // END OF ADDED BLOCK

                // Animate customer leaving with approval status
                controller.getVisualisation().moveCustomer(5, -1, approved);

                totalApplications++;
                if (approved) approvedCount++;
                else rejectedCount++;

                totalSystemTime += application.getRemovalTime() - application.getArrivalTime();

<<<<<<< HEAD
                // NEW: Update UI with current statistics
=======
                // Update UI with current statistics
>>>>>>> origin/feature-myEngine
                double avgTime = totalApplications > 0 ? totalSystemTime / totalApplications : 0;
                controller.updateStatistics(totalApplications, approvedCount, rejectedCount, avgTime, Clock.getInstance().getTime());

                // Schedule exit
                EventType exitEvent = approved ? EventType.EXIT_APPROVED : EventType.EXIT_REJECTED;
                eventList.add(new Event(exitEvent, Clock.getInstance().getTime()));

<<<<<<< HEAD
                // Create and collect an ApplicationLog record for persistence
                ApplicationLog log = new ApplicationLog();
                StringBuilder msg = new StringBuilder();
                msg.append("Application ID: ").append(application.getId())
                        .append(" | Arrival: ").append(application.getArrivalTime())
                        .append(" | Removal: ").append(application.getRemovalTime())
                        .append(" | TimeInSystem: ").append(String.format("%.2f", application.getRemovalTime() - application.getArrivalTime()))
                        .append(" | Approved: ").append(approved);
                log.setMessage(msg.toString());
                log.setTimestamp(LocalDateTime.now());
                applicationLogs.add(log);
=======


>>>>>>> origin/feature-myEngine

                application.reportResults();

                // Handle reapplication for rejected applications
                if (!approved) {
                    application.markReapplication();
                    if (application.canReapply()) {
                        servicePoints[0].addQueue(application);
                    } else {
                        eventList.add(new Event(EventType.EXIT_REJECTED, Clock.getInstance().getTime()));
                    }
                }

                controller.updateQueueStatus(5, servicePoints[5].getQueueSize());
                break;

            case EXIT_APPROVED:
                exitedApprovedCount++;
                break;

            case EXIT_REJECTED:
                exitedRejectedCount++;
                break;
        }
    }
    @Override
    protected void tryCEvents() {
        for (ServicePoint servicePoint: servicePoints){
            if (!servicePoint.isReserved() && servicePoint.isOnQueue()){
                servicePoint.beginService();
            }
            servicePoint.checkBottleneck();
        }
    }

<<<<<<< HEAD
    @Override
=======
	@Override
>>>>>>> origin/feature-myEngine
    protected void results() {
        // NEW GUI
        double avgTimeInSystem = totalApplications > 0 ? totalSystemTime / totalApplications : 0;

        //SimulationRun
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

        // Create ServicePointResults with bottleneck info
        for (ServicePoint sp : servicePoints) {
            boolean isBottleneck = (sp == bottleneck);
            SPResult spr = new SPResult(
                    sp.getServicePointName(),
                    sp.getTotalDepartures(),
                    sp.getAverageWaitingTime(),
                    sp.getMaxQueueLength(),
                    sp.getUtilization(Clock.getInstance().getTime()),
                    sp.getNumEmployees(),
                    isBottleneck                                    // mark bottleneck
            );
            spResults.add(spr);
        }

        // Create DistributionConfigs
        List<DistConfig> configs = new ArrayList<>();
        for (int i = 0; i < servicePoints.length; i++) {
            DistributionConfig ucfg = userConfigs[i]; // user-provided simulation config
            DistConfig dc = new DistConfig();

            dc.setServicePointName(servicePoints[i].getServicePointName());

            dc.setDistributionType(ucfg.getType());
            dc.setParam1(ucfg.getParam1());
            // param2 might be absent for negexp; support null
            Double p2 = null;
            try {
                p2 = ucfg.getParam2();
            } catch (Exception ignored) {
            }
            dc.setParam2(p2);

            dc.setSimulationRun(run);
            configs.add(dc);
        }

// Arrival process config at index 6)
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

<<<<<<< HEAD
        List<ApplicationLog> logs = new ArrayList<>();
        for (ApplicationAsCustomer app : ApplicationAsCustomer.getAllApplications()) {
            ApplicationLog log = new ApplicationLog();
            log.setMessage("Application " + app.getId() + " completed. Approved: " + app.isApproved());
            log.setTimestamp(LocalDateTime.now());
            log.setSimulationRun(run);
            logs.add(log);
        }

        // Ask controller/UI layer to save (controller will persist using DAO on its own EM thread)
        if (controller instanceof controller.SimulationController) {
            // cast is safe if your controller implements that class
            ((controller.SimulationController) controller).saveSimulationRun(run, configs, spResults, logs);
        } else {
            // fallback - persist directly (keeps backward compatibility)
            SimulationRunDao dao = new SimulationRunDao();
            dao.persist(run, configs, spResults, logs);
        }
=======

        // Persist to database
        SimulationRunDao dao = new SimulationRunDao();
        dao.persist(run, configs, spResults, applicationLogs);
>>>>>>> origin/feature-myEngine

        // Prepare the result string
        StringBuilder resultStr = new StringBuilder();
        resultStr.append("\n*---------------------------------------------------------------------------------*");
        resultStr.append(String.format("\nSimulation ended at %.2f", Clock.getInstance().getTime()));
        resultStr.append("\n****** Simulation Results ******");
        resultStr.append(String.format("\n  -> Total applications processed: %d applications.", totalApplications));
        resultStr.append(String.format("\n  -> Approved applications: %d applications", approvedCount));
        resultStr.append(String.format("\n  -> Approved applications exits: %d", exitedApprovedCount));
        resultStr.append(String.format("\n  -> Rejected applications: %d applications", rejectedCount));
        resultStr.append(String.format("\n  -> Rejected applications exits: %d", exitedRejectedCount));
        resultStr.append(String.format("\n  -> Average time in system: %.2f minutes.\n", avgTimeInSystem));

        //Service Point performances
        resultStr.append("\n****** Service Point Performances ******");
        for (int i = 0; i < servicePoints.length; i++) {
            ServicePoint servicePoint = servicePoints[i];
            boolean isBottleneck = (servicePoint == bottleneck);

            double utilization = servicePoint.getUtilization(Clock.getInstance().getTime());
            String utilizationStr = String.format("%.2f%%", utilization);

            resultStr.append(String.format("\nService Point %d \"%s\" Metrics:", i + 1, servicePoint.getServicePointName(), isBottleneck ? " <-- BOTTLENECK" : ""));
            resultStr.append(String.format("\n  -> Total departures: %d applications.", servicePoint.getTotalDepartures()));
            resultStr.append(String.format("\n  -> Average waiting time: %s minutes", servicePoint.getAverageWaitingTime()));
            resultStr.append(String.format("\n  -> Max queue length: %d applications", servicePoint.getMaxQueueLength()));
            resultStr.append(String.format("\n  -> Utilization: %s%s", utilization, isBottleneck ? "<-- HIGHEST" : ""));
            resultStr.append(String.format("\n  -> Number of employees: %d", servicePoint.getNumEmployees()));
            resultStr.append("\n");
        }

        // Bottleneck summary line
        if (bottleneck != null) {
            resultStr.append("\n****** Bottleneck Summary ******");
            resultStr.append(String.format("\nBottleneck Service Point: \"%s\"", bottleneck.getServicePointName()));
            resultStr.append(String.format("\n  -> Utilization: %.2f%%", bottleneck.getUtilization(Clock.getInstance().getTime())));
            resultStr.append(String.format("\n  -> Max queue length: %d", bottleneck.getMaxQueueLength()));
            resultStr.append(String.format("\n  -> Average waiting time: %.2f minutes", bottleneck.getAverageWaitingTime()));
        }

        // Send the results to the GUI for display
        controller.displayResults(resultStr.toString());

        // for controller simulation result panel
        controller.showEndTime(Clock.getInstance().getTime());
    }
}
