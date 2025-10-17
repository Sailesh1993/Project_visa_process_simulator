package ORM.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a simulation run in the system. This class is mapped to the "simulation_run" table
 * in the database. It contains information such as the timestamp of the run, the number of applications,
 * the number of approved/rejected applications, and other related data.
 */
@Entity
@Table(name = "simulation_run")
public class SimulationRun {

    /** The unique identifier for the simulation run. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The timestamp indicating when the simulation run started or was created. */
    @Column(name = "Timestamp")
    private LocalDateTime timestamp;

    /** The total number of applications processed in this simulation run. */
    @Column(name = "Total_Applications")
    private int totalApplications;

    /** The number of approved applications in this simulation run. */
    @Column(name = "Approved_Count")
    private int approvedCount;

    /** The number of rejected applications in this simulation run. */
    @Column(name = "Rejected_Count")
    private int rejectedCount;

    /** The average system time for processing applications in this simulation run. */
    @Column(name = "Avg_System_Time")
    private double avgSystemTime;

    /** A flag indicating whether the configuration for this simulation run has been saved. */
    @Column(name = "Saved_config")
    private boolean configSaved;

    /**
     * A list of distribution configurations associated with this simulation run.
     */
    @OneToMany(mappedBy = "simulationRun", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DistConfig> distributionConfigs = new ArrayList<>();

    /**
     * A list of service point results associated with this simulation run.
     */
    @OneToMany(mappedBy = "simulationRun", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SPResult> servicePointResults = new ArrayList<>();

    /**
     * A list of application logs associated with this simulation run.
     */
    @OneToMany(mappedBy = "simulationRun", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApplicationLog> applicationLogs = new ArrayList<>();

    /**
     * Default constructor. Initializes the timestamp to the current date and time.
     */
    public SimulationRun() {this.timestamp = LocalDateTime.now();}

    /**
     * Rounds the average system time to two decimal places before persisting or updating.
     */
    @PrePersist
    @PreUpdate
    private void roundValues() {avgSystemTime = Math.round(avgSystemTime * 100.0) / 100.0;}

    /**
     * Returns the unique identifier for the simulation run.
     *
     * @return the ID of the simulation run
     */
    public Long getId() {return id;}

    /**
     * Returns the timestamp of the simulation run.
     *
     * @return the timestamp of the simulation run
     */
    public LocalDateTime getTimestamp() {return timestamp;}

    /**
     * Returns the total number of applications processed in this simulation run.
     *
     * @return the total number of applications
     */
    public int getTotalApplications() {return totalApplications;}

    /**
     * Returns the number of approved applications in this simulation run.
     *
     * @return the number of approved applications
     */
    public int getApprovedCount() {return approvedCount;}

    /**
     * Returns the number of rejected applications in this simulation run.
     *
     * @return the number of rejected applications
     */
    public int getRejectedCount() {return rejectedCount;}

    /**
     * Returns the average system time for processing applications in this simulation run.
     *
     * @return the average system time
     */
    public double getAvgSystemTime() {return avgSystemTime;}

    /**
     * Returns whether the configuration for this simulation run has been saved.
     *
     * @return true if the configuration has been saved, false otherwise
     */
    public boolean isConfigSaved() {return configSaved;}

    /**
     * Returns the list of distribution configurations associated with this simulation run.
     *
     * @return the list of distribution configurations
     */
    public List<DistConfig> getDistConfiguration() {return distributionConfigs;}

    /**
     * Returns the list of service point results associated with this simulation run.
     *
     * @return the list of service point results
     */
    public List<SPResult> getServicePointResults() {return servicePointResults;}

    /**
     * Returns the list of application logs associated with this simulation run.
     *
     * @return the list of application logs
     */
    public List<ApplicationLog> getApplicationLogs() {return applicationLogs;}

    /**
     * Sets the unique identifier for the simulation run.
     *
     * @param id the new ID of the simulation run
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Sets the timestamp for the simulation run.
     *
     * @param timestamp the new timestamp for the simulation run
     */
    public void setTimestamp(LocalDateTime timestamp) {this.timestamp = timestamp;}

    /**
     * Sets the total number of applications processed in this simulation run.
     *
     * @param totalApplications the new total number of applications
     */
    public void setTotalApplications(int totalApplications) {this.totalApplications = totalApplications;}

    /**
     * Sets the number of approved applications in this simulation run.
     *
     * @param approvedCount the new number of approved applications
     */
    public void setApprovedCount(int approvedCount) {this.approvedCount = approvedCount;}

    /**
     * Sets the number of rejected applications in this simulation run.
     *
     * @param rejectedCount the new number of rejected applications
     */
    public void setRejectedCount(int rejectedCount) {this.rejectedCount = rejectedCount;}

    /**
     * Sets the average system time for processing applications in this simulation run.
     *
     * @param avgSystemTime the new average system time
     */
    public void setAvgSystemTime(double avgSystemTime) {this.avgSystemTime = avgSystemTime;}

    /**
     * Sets whether the configuration for this simulation run has been saved.
     *
     * @param configSaved true if the configuration has been saved, false otherwise
     */
    public void setConfigSaved(boolean configSaved) {this.configSaved = configSaved;}

    /**
     * Sets the list of service point results.
     *
     * @param servicePointResults the list of service point results to be set
     */
    public void setServicePointResults(List<SPResult> servicePointResults) {this.servicePointResults = servicePointResults;}

    /**
     * Sets the list of distribution configurations.
     *
     * @param distributionConfigs the list of distribution configurations to be set
     */
    public void setDistConfiguration(List<DistConfig> distributionConfigs) {this.distributionConfigs = distributionConfigs;}
}
