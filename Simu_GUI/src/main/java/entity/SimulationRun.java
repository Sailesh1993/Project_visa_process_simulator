package entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "simulation_run")
public class SimulationRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Timestamp")
    private LocalDateTime timestamp;

    @Column(name = "Total_Applications")
    private int totalApplications;

    @Column(name = "Approved_Count")
    private int approvedCount;

    @Column(name = "Rejected_Count")
    private int rejectedCount;

    @Column(name = "Avg_System_Time")
    private double avgSystemTime;

    @Column(name = "Saved_config")
    private boolean configSaved;

    @OneToMany(mappedBy = "simulationRun")
    private List<DistConfig> distributionConfigs = new ArrayList<>();

    @OneToMany(mappedBy = "simulationRun")
    private List<SPResult> servicePointResults = new ArrayList<>();

    @OneToMany(mappedBy = "simulationRun")
    private List<ApplicationLog> applicationLogs = new ArrayList<>();

    public SimulationRun() {}

    //getters
    public Long getId() {return id;}

    public LocalDateTime getTimestamp() {return timestamp;}

    public int getTotalApplications() {return totalApplications;}

    public int getApprovedCount() {return approvedCount;}

    public int getRejectedCount() {return rejectedCount;}

    public double getAvgSystemTime() {return avgSystemTime;}

    public boolean isConfigSaved() {return configSaved;}

    public List<DistConfig> getDistConfiguration() {return distributionConfigs;}

    public List<SPResult> getServicePointResults() {return servicePointResults;}

    public List<ApplicationLog> getApplicationLogs() {return applicationLogs;}

    //setters
    public void setTimestamp(LocalDateTime timestamp) {this.timestamp = timestamp;}

    public void setTotalApplications(int totalApplications) {this.totalApplications = totalApplications;}

    public void setApprovedCount(int approvedCount) {this.approvedCount = approvedCount;}

    public void setRejectedCount(int rejectedCount) {this.rejectedCount = rejectedCount;}

    public void setAvgSystemTime(double avgSystemTime) {this.avgSystemTime = avgSystemTime;}

    public void setConfigSaved(boolean configSaved) {this.configSaved = configSaved;}

    public void setDistConfiguration(List<DistConfig> distributionConfigs) {
        this.distributionConfigs = distributionConfigs;
    }

    public void setServicePointResults(List<SPResult> servicePointResults) {
        this.servicePointResults = servicePointResults;
    }

    public void setApplicationLogs(List<ApplicationLog> applicationLogs) {
        this.applicationLogs = applicationLogs;
    }
}
