package ORM.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a log entry for an application within the simulation.
 * This class is mapped to the "application_log" table in the database.
 */
@Entity
@Table(name="application_log")
public class ApplicationLog {

    /** The unique identifier for the application log entry. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The unique ID for the application that this log entry pertains to. */
    @Column(name="Application_Id")
    private int appId;

    /**
     * The arrival time of the application in the system.
     * This time is recorded when the application enters the simulation.
     */
    @Column(name="Arrival_Time")
    private double arrivalTime;

    /**
     * The removal time of the application from the system.
     * This time is recorded when the application exits the simulation.
     */
    @Column(name="Removal_Time")
    private double removalTime;

    /** A message associated with the application log entry, providing context or additional details. */
    @Column(name = "Message", length = 255)
    private String message;

    /** The timestamp of when this log entry was created. */
    @Column(name = "Timestamp")
    private LocalDateTime timestamp;

    /** A flag indicating whether the application was approved or not. */
    private boolean approved;

    /** The waiting time for the application, calculated as the time the application waited in the system. */
    private double waitingTime;

    /**
     * The simulation run that this application log is associated with.
     * A simulation run can have many application logs.
     */
    @ManyToOne
    @JoinColumn(name = "run_id", nullable = false)
    private SimulationRun simulationRun;

    /**
     * Constructs a new ApplicationLog with the specified application ID, arrival time, removal time,
     * approval status, and waiting time.
     *
     * @param appId the ID of the application
     * @param arrivalTime the arrival time of the application in the system
     * @param removalTime the removal time of the application from the system
     * @param approved the approval status of the application
     * @param waitingTime the waiting time for the application
     */
    public ApplicationLog(int appId, double arrivalTime, double removalTime, boolean approved, double waitingTime) {
        this.appId = appId;
        this.arrivalTime = arrivalTime;
        this.removalTime = removalTime;
        this.approved = approved;
        this.waitingTime = waitingTime;
    }

    /** Default constructor for JPA. */
    public ApplicationLog() {}

    /**
     * Rounds the arrival time, removal time, and waiting time to two decimal places before persisting or updating.
     */
    @PrePersist
    @PreUpdate
    private void roundValues() {
        arrivalTime = Math.round(arrivalTime * 100.0) / 100.0;
        removalTime = Math.round(removalTime * 100.0) / 100.0;
        waitingTime = Math.round(waitingTime * 100.0) / 100.0;
    }

    /**
     * Returns the unique identifier for the application log entry.
     *
     * @return the ID of the application log entry
     */
    public Long getId() {return id;}

    /**
     * Returns the application ID associated with this log entry.
     *
     * @return the application ID
     */
    public int getAppId() {return appId;}

    /**
     * Returns the arrival time of the application in the system.
     *
     * @return the arrival time
     */
    public double getArrivalTime() {return arrivalTime;}

    /**
     * Returns the removal time of the application from the system.
     *
     * @return the removal time
     */
    public double getRemovalTime() {return removalTime;}

    /**
     * Returns the message associated with this log entry.
     *
     * @return the message
     */
    public String getMessage() { return message; }

    /**
     * Returns the timestamp of when this log entry was created.
     *
     * @return the timestamp
     */
    public LocalDateTime getTimestamp() { return timestamp; }

    /**
     * Returns the simulation run associated with this application log entry.
     *
     * @return the simulation run
     */
    public SimulationRun getSimulationRun() {
        return simulationRun;
    }

    /**
     * Returns whether the application was approved.
     *
     * @return true if the application was approved, false otherwise
     */
    public boolean isApproved() {return approved;}

    /**
     * Sets whether the application was approved.
     *
     * @param approved true if the application is approved, false otherwise
     */
    public void setApproved(boolean approved) {this.approved = approved;}

    /**
     * Sets the waiting time for the application in the system.
     *
     * @param waitingTime the new waiting time
     */
    public void setWaitingTime(double waitingTime) {this.waitingTime = waitingTime;}

    /**
     * Sets the simulation run associated with this application log entry.
     *
     * @param run the simulation run to associate
     */
    public void setSimulationRun(SimulationRun run) {this.simulationRun = run;}

    /**
     * Sets the message associated with this log entry.
     *
     * @param message the new message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Sets the timestamp for this application log entry.
     *
     * @param timestamp the new timestamp
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}