package ORM.entity;

import jakarta.persistence.*;

/**
 * Represents the result data for a service point in the simulation.
 * This class is mapped to the "servicepoint_result" table in the database.
 */
@Entity
@Table(name = "servicepoint_result")
public class SPResult {

    /** The unique identifier for the service point result entry. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The name of the service point associated with this result. */
    @Column(name = "ServicePoint_Name")
    private String servicePointName;

    /** The number of departures processed at this service point during the simulation. */
    @Column(name = "Departures")
    private int departures;

    /** The average waiting time of the customers at this service point. */
    private double avgWaitingTime;

    /** The maximum queue length observed at this service point. */
    private int maxQueueLength;

    /** The utilization of the service point, calculated as the ratio of time the service point is in use. */
    private double utilization;

    /** The number of employees assigned to the service point. */
    private int numEmployees;

    /** A flag indicating whether this service point is a bottleneck in the system. */
    private boolean bottleneck;

    /**
     * The simulation run that this service point result is associated with.
     * A simulation run can have many service point results.
     */
    @ManyToOne
    @JoinColumn(name = "run_id", nullable = false)
    private SimulationRun simulationRun;

    /**
     * Constructs a new SPResult with the specified service point name, number of departures,
     * average waiting time, maximum queue length, utilization, number of employees, and bottleneck status.
     *
     * @param servicePointName the name of the service point
     * @param departures the number of departures processed at the service point
     * @param avgWaitingTime the average waiting time of customers at this service point
     * @param maxQueueLength the maximum queue length observed at this service point
     * @param utilization the utilization of the service point
     * @param numEmployees the number of employees assigned to this service point
     * @param bottleneck true if the service point is a bottleneck, false otherwise
     */
    public SPResult(String servicePointName, int departures, double avgWaitingTime,
                    int maxQueueLength, double utilization, int numEmployees, boolean bottleneck) {
        this.servicePointName = servicePointName;
        this.departures = departures;
        this.avgWaitingTime = avgWaitingTime;
        this.maxQueueLength = maxQueueLength;
        this.utilization = utilization;
        this.numEmployees = numEmployees;
        this.bottleneck = bottleneck;
    }

    /** Default constructor for JPA. */
    public SPResult() {}

    /** Rounds the average waiting time and utilization to two decimal places before persisting or updating. */
    @PrePersist
    @PreUpdate
    private void roundValues() {
        avgWaitingTime = Math.round(avgWaitingTime * 100.0) / 100.0;
        utilization = Math.round(utilization * 100.0) / 100.0;
    }

    /**
     * Returns the unique identifier for this service point result entry.
     *
     * @return the ID of the service point result
     */
    public Long getId() {return id;}

    /**
     * Returns the average waiting time at the service point.
     *
     * @return the average waiting time
     */
    public double getAvgWaitingTime() {return avgWaitingTime;}

    /**
     * Sets the average waiting time at the service point.
     *
     * @param avgWaitingTime the new average waiting time
     */
    public void setAvgWaitingTime(double avgWaitingTime) {this.avgWaitingTime = avgWaitingTime;}

    /**
     * Returns the maximum queue length observed at the service point.
     *
     * @return the maximum queue length
     */
    public int getMaxQueueLength() {return maxQueueLength;}

    /**
     * Sets the maximum queue length observed at the service point.
     *
     * @param maxQueueLength the new maximum queue length
     */
    public void setMaxQueueLength(int maxQueueLength) {this.maxQueueLength = maxQueueLength;}

    /**
     * Returns the utilization of the service point.
     *
     * @return the utilization of the service point
     */
    public double getUtilization() {return utilization;}

    /**
     * Sets the utilization of the service point.
     *
     * @param utilization the new utilization value
     */
    public void setUtilization(double utilization) {this.utilization = utilization;}

    /**
     * Returns the number of employees assigned to the service point.
     *
     * @return the number of employees
     */
    public int getNumEmployees() {return numEmployees;}

    /**
     * Sets the number of employees assigned to the service point.
     *
     * @param numEmployees the new number of employees
     */
    public void setNumEmployees(int numEmployees) {this.numEmployees = numEmployees;}

    /**
     * Returns whether this service point is a bottleneck in the system.
     *
     * @return true if the service point is a bottleneck, false otherwise
     */
    public boolean isBottleneck() {return bottleneck;}

    /**
     * Sets the status of the bottleneck in the service point.
     *
     * @param bottleneck true if a bottleneck is found, false otherwise
     */
    public void setBottleneck(boolean bottleneck) {this.bottleneck = bottleneck;}

    /**
     * Returns the name of the service point.
     *
     * @return the service point name
     */
    public String getServicePointName() {return servicePointName;}

    /**
     * Sets the name of the service point.
     *
     * @param servicePointName the name of the service point to be set
     */
    public void setServicePointName(String servicePointName) {
        this.servicePointName = servicePointName;
    }

    /**
     * Returns the number of departures at the service point.
     *
     * @return the number of departures
     */
    public int getDepartures() {return departures;}

    /**
     * Sets the number of departures at the service point.
     *
     * @param departures the new number of departures
     */
    public void setDepartures(int departures) {this.departures = departures;}

    /**
     * Sets the simulation run associated with this service point result.
     *
     * @param run the simulation run to associate with this result
     */
    public void setSimulationRun(SimulationRun run) {this.simulationRun = run;}
}
