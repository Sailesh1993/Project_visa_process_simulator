package entity;

import jakarta.persistence.*;

@Entity
@Table(name = "servicepoint_result")
public class SPResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ServicePoint_Name")
    private String servicePointName;

    @Column(name = "Departures")
    private int departures;

    private double avgWaitingTime;
    private int maxQueueLength;
    private double utilization;
    private int numEmployees;

    private boolean bottleneck;

    @ManyToOne
    @JoinColumn(name = "run_id", nullable = false)
    private SimulationRun simulationRun;

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

    public SPResult() {}

    @PrePersist
    @PreUpdate
    private void roundValues() {
        avgWaitingTime = Math.round(avgWaitingTime * 100.0) / 100.0;
        utilization = Math.round(utilization * 100.0) / 100.0;
    }

    public Long getId() {
        return id;
    }

    public double getAvgWaitingTime() {
        return avgWaitingTime;
    }

    public void setAvgWaitingTime(double avgWaitingTime) {
        this.avgWaitingTime = avgWaitingTime;
    }

    public int getMaxQueueLength() {
        return maxQueueLength;
    }

    public void setMaxQueueLength(int maxQueueLength) {
        this.maxQueueLength = maxQueueLength;
    }

    public double getUtilization() {
        return utilization;
    }

    public void setUtilization(double utilization) {
        this.utilization = utilization;
    }

    public int getNumEmployees() {
        return numEmployees;
    }

    public void setNumEmployees(int numEmployees) {
        this.numEmployees = numEmployees;
    }

    public boolean isBottleneck() {
        return bottleneck;
    }

    public void setBottleneck(boolean bottleneck) {
        this.bottleneck = bottleneck;
    }

    public String getServicePointName() {
        return servicePointName;
    }

    public void setServicePointName(String servicePointName) {
        this.servicePointName = servicePointName;
    }

    public int getDepartures() {
        return departures;
    }

    public void setDepartures(int departures) {
        this.departures = departures;

    }

    public void setSimulationRun(SimulationRun run) {
        this.simulationRun = run;
    }

}
