package MVC.simu.model;

import MVC.simu.framework.Clock;
import MVC.simu.framework.Trace;

import java.util.ArrayList;
import java.util.List;

// TODO:
// ApplicationAsCustomer to be implemented according to the requirements of the simulation model (data!)
public class ApplicationAsCustomer {
    private static final List<ApplicationAsCustomer> allApplications = new ArrayList<>();
    private static final int MAX_ATTEMPTS = 3;
    private static int idCounter = 1;
    private double arrivalTime;
    private double removalTime;
    private int id;
    private int reapplyAttempts = 1;
    private boolean newApplication;
    private boolean docsComplete;
    private boolean requiresBiometrics;
    private boolean approved;
    private EventType currentStage;

    private double timeInBiometrics;
    private double timeInWaitingRoom;
    private double timeEnteredQueue = -1.0;         //timestamp for waiting time measurement

    //Create a unique customer
    public ApplicationAsCustomer(boolean newApplication, boolean docsComplete) {
        id = idCounter++;
        this.newApplication = newApplication;
        this.docsComplete = docsComplete;
        this.requiresBiometrics = newApplication;       // Only new applications require biometrics
        arrivalTime = Clock.getInstance().getTime();
        this.currentStage = EventType.ARRIVAL;
        allApplications.add(this);

        Trace.out(Trace.Level.INFO, "New application #" + id + " arrived at " + Trace.formatTime(arrivalTime));
        Trace.out(Trace.Level.INFO,
                "Checking.... New application? " + newApplication +
                        " | Requires biometrics? " + requiresBiometrics +
                        " | Complete documents? " + docsComplete
        );
    }

    public int getId() {
        return id;
    }       //Get unique customer id

    public double getArrivalTime() {
        return arrivalTime;
    }       //Get customer arrival time for simulation

    public void setArrivalTime(double arrivalTime) {
        this.arrivalTime = arrivalTime;
    }           //Mark the time when the customer arrived to the system to be simulated

    public double getRemovalTime() {
        return removalTime;
    }         // Get time when customer has been removed (from the system to be simulated)


    public static List<ApplicationAsCustomer> getAllApplications() {return allApplications;}

    public void setRemovalTime(double removalTime) {
        this.removalTime = removalTime;
    }           //Mark the time when the customer has been removed (from the system to be simulated)

    public boolean isNewApplication() {
        return newApplication;
    }

    public boolean isDocsComplete() {
        return docsComplete;
    }

    public void setDocsComplete(boolean docsComplete) {
        this.docsComplete = docsComplete;
    }

    public boolean requiresBiometrics() {
        return requiresBiometrics;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public EventType getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(EventType currentStage) {
        this.currentStage = currentStage;
    }

    public double getTimeInBiometrics() {
        return timeInBiometrics;
    }

    public void setTimeInBiometrics(double time) {
        this.timeInBiometrics = time;
    }

    public double getTimeInWaitingRoom() {
        return timeInWaitingRoom;
    }

    public void setTimeInWaitingRoom(double time) {
        this.timeInWaitingRoom = time;
    }

    public double getTimeEnteredQueue() {
        return timeEnteredQueue;
    }

    public void setTimeEnteredQueue(double t) {
        this.timeEnteredQueue = t;
    }

    public void markReapplication() {
        this.newApplication = false;
        this.requiresBiometrics = false;             // Reapplications do not require biometrics
        this.currentStage = EventType.REAPPLICATION;
        reapplyAttempts++;
    }

    public boolean canReapply() {
        return reapplyAttempts < MAX_ATTEMPTS;
    }

    public static void resetIdCounter() {
        allApplications.clear();
        idCounter = 1;
    }


    //Report the measured variables of the customer. In this case to the diagnostic output.

    public void reportResults() {
        Trace.out(Trace.Level.INFO, "\nApplication #" + id + " is processed! ");
        Trace.out(Trace.Level.INFO, "Application #" + id + " arrived at " + Trace.formatTime(arrivalTime));
        Trace.out(Trace.Level.INFO, "Application #" + id + " removed at " + Trace.formatTime(removalTime));
        Trace.out(Trace.Level.INFO, "Total time in system: " + id + " " + Trace.formatTime((removalTime - arrivalTime)));
        Trace.out(Trace.Level.INFO, "Application waited " + Trace.formatTime(getTimeInWaitingRoom()) + " minutes in queue.");
        System.out.println("Creating app #" + id + " | total in memory now: " + allApplications.size());

        if (requiresBiometrics) {
            Trace.out(Trace.Level.INFO, "Time spent in biometrics: " + Trace.formatTime(getTimeInBiometrics()) + " minutes");
        }

        Trace.out(Trace.Level.INFO, "Is this new application? " + newApplication);
        Trace.out(Trace.Level.INFO, "Are the required documents complete? " + docsComplete);
        Trace.out(Trace.Level.INFO, "Is biometrics required? " + requiresBiometrics);
        Trace.out(Trace.Level.INFO, "Visa decision: " + (approved ? "Approved ✅" : "Denied ❌"));
    }
}