package MVC.simu.model;

import MVC.simu.framework.Clock;
import MVC.simu.framework.Trace;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer application in the simulation.
 * Tracks arrival/removal times, document completeness, biometrics, approval status, and processing stages.
 * <p>The application's progress is tracked using {@link EventType}, which defines stages such as:
 * ARRIVAL, REAPPLICATION, BIOMETRICS, and other processing steps.</p>
 */
public class ApplicationAsCustomer {

    /** List of all applications in the system. */
    private static final List<ApplicationAsCustomer> allApplications = new ArrayList<>();

    /** Counter for generating unique application IDs. */
    private static int idCounter = 1;

    /** Time when the application arrived. */
    private double arrivalTime;

    /** Time when the application was removed. */
    private double removalTime;

    /** Unique ID of the application. */
    private int id;

    /** Number of reapplication attempts made. */
    private int reapplyAttempts = 1;

    /** Maximum number of reapplication attempts. */
    private static final int MAX_ATTEMPTS = 3;

    /** True if this is a new application. */
    private boolean newApplication;

    /** True if required documents are complete. */
    private boolean docsComplete;

    /** True if biometrics are required. */
    private boolean requiresBiometrics;

    /** True if the application is approved. */
    private boolean approved;

    /** Current stage of the application process. See {@link EventType}. */
    private EventType currentStage;

    /** Time spent in biometrics stage. */
    private double timeInBiometrics;

    /** Time spent in the waiting room. */
    private double timeInWaitingRoom;

    /** Timestamp when entering a queue. */
    private double timeEnteredQueue = -1.0;

    /**
     * Creates a new application with specified type and document completeness.
     *
     * @param newApplication true if this is a new application
     * @param docsComplete   true if documents are complete
     */
    public ApplicationAsCustomer(boolean newApplication, boolean docsComplete) {
        id = idCounter++;
        this.newApplication = newApplication;
        this.docsComplete = docsComplete;
        this.requiresBiometrics = newApplication;
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

    /**
     * Returns the unique ID of this application.
     *
     * @return the application ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the arrival time of the application.
     *
     * @return the time the application entered the system
     */
    public double getArrivalTime() {
        return arrivalTime;
    }

    /**
     * Sets the arrival time of the application.
     *
     * @param arrivalTime the time the application entered the system
     */
    public void setArrivalTime(double arrivalTime) {this.arrivalTime = arrivalTime;}

    /**
     * Returns the removal time of the application.
     *
     * @return the time the application left the system
     */
    public double getRemovalTime() {return removalTime;}

    /**
     * Sets the removal time of the application.
     *
     * @param removalTime the time the application left the system
     */
    public void setRemovalTime(double removalTime) {this.removalTime = removalTime;}

    /**
     * Returns the list of all applications in the system.
     *
     * @return list of all ApplicationAsCustomer instances
     */
    public static List<ApplicationAsCustomer> getAllApplications() {return allApplications;}

    /**
     * Returns whether this is a new application.
     *
     * @return true if the application is new; false if it's a reapplication
     */
    public boolean isNewApplication() {return newApplication;}

    /**
     * Returns whether the required documents are complete.
     *
     * @return true if documents are complete; false otherwise
     */
    public boolean isDocsComplete() {return docsComplete;}

    /**
     * Returns whether biometrics are required for this application.
     *
     * @return true if biometrics are required; false otherwise
     */
    public boolean requiresBiometrics() {return requiresBiometrics;}

    /**
     * Returns whether the application was approved.
     *
     * @return true if approved; false if denied
     */
    public boolean isApproved() {return approved;}

    /**
     * Sets the approval status of the application.
     *
     * @param approved true if approved; false if denied
     */
    public void setApproved(boolean approved) {this.approved = approved;}

    /**
     * Returns the current stage of the application.
     *
     * @return the application's current processing stage
     */
    public EventType getCurrentStage() {return currentStage;}

    /** Sets the current stage of the application. See {@link EventType} */
    public void setCurrentStage(EventType currentStage) {this.currentStage = currentStage;}

    /**
     * Returns the total time the application spent in the biometrics stage.
     *
     * @return time in biometrics, in simulation units
     */
    public double getTimeInBiometrics() {return timeInBiometrics;}

    /**
     * Sets the time spent in the biometrics stage.
     *
     * @param time the duration spent in biometrics
     */
    public void setTimeInBiometrics(double time) {this.timeInBiometrics = time;}

    /**
     * Returns the total time the application spent in the waiting room.
     *
     * @return time in the waiting room, in simulation units
     */
    public double getTimeInWaitingRoom() {return timeInWaitingRoom;}

    /**
     * Sets the time spent in the waiting room.
     *
     * @param time the duration spent waiting
     */
    public void setTimeInWaitingRoom(double time) {this.timeInWaitingRoom = time;}

    /**
     * Returns the time the application entered a queue.
     *
     * @return timestamp of queue entry, or -1.0 if not yet entered
     */
    public double getTimeEnteredQueue() {return timeEnteredQueue;}

    /**
     * Sets the time the application entered a queue.
     *
     * @param t the time the application entered the queue
     */
    public void setTimeEnteredQueue(double t) {this.timeEnteredQueue = t;}

    /**
     * Marks this application as a reapplication.
     * Updates relevant flags (biometrics not required) and increments attempt count.
     */
    public void markReapplication() {
        this.newApplication = false;
        this.requiresBiometrics = false;             // Reapplications do not require biometrics
        this.currentStage = EventType.REAPPLICATION;
        reapplyAttempts++;
    }

    /**
     * Checks if the application can reapply, based on maximum allowed attempts.
     *
     * @return true if the application can reapply, false otherwise
     */
    public boolean canReapply() {return reapplyAttempts < MAX_ATTEMPTS;}

    /**
     * Resets the application ID counter and clears all existing application records.
     * Used to restart the simulation cleanly.
     */
    public static void resetIdCounter() {
        allApplications.clear();
        idCounter = 1;
    }

    /** Prints a summary report of the application's processing details in console. */
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