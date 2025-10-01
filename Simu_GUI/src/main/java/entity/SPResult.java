package entity;

import jakarta.persistence.*;

@Entity
@Table(name="servicepoint_result")
public class SPResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="ServicePoint_Name")
    private String servicePointName;

    @Column(name="Departures")
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

    public Long getId() {return id;}

    public double getAvgWaitingTime() {return avgWaitingTime;}

    public int getMaxQueueLength() {return maxQueueLength;}

    public double getUtilization() {return utilization;}

    public int getNumEmployees() {return numEmployees;}

    public boolean isBottleneck() {return bottleneck;}

    public void setAvgWaitingTime(double avgWaitingTime) {this.avgWaitingTime = avgWaitingTime;}

    public void setMaxQueueLength(int maxQueueLength) {this.maxQueueLength = maxQueueLength;}

    public void setUtilization(double utilization) {this.utilization = utilization;}

    public void setNumEmployees(int numEmployees) {this.numEmployees = numEmployees;}

    public void setBottleneck(boolean bottleneck) {this.bottleneck = bottleneck;}

    public void setSimulationRun(SimulationRun run) {this.simulationRun = run;}


}
