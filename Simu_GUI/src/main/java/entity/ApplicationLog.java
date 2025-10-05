package entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="application_log")
public class ApplicationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="Application_Id")
    private int appId;

    @Column(name="Arrival_Time")
    private double arrivalTime;

    @Column(name="Removal_Time")
    private double removalTime;

    private boolean approved;

    private double waitingTime;

    @ManyToOne
    @JoinColumn(name = "run_id", nullable = false)
    private SimulationRun simulationRun;

    @Column(name="message", columnDefinition = "TEXT")
    private String message;

    @Column(name="timestamp")
    private LocalDateTime timestamp;  // ADD THIS FIELD

    public ApplicationLog() {}

    public ApplicationLog(int appId, double arrivalTime, double removalTime, boolean approved, double waitingTime) {
        this();
        this.appId = appId;
        this.arrivalTime = arrivalTime;
        this.removalTime = removalTime;
        this.approved = approved;
        this.waitingTime = waitingTime;
    }

    public Long id() {return id;}

    public int getAppId() {return appId;}

    public double getArrivalTime() {return arrivalTime;}

    public double getRemovalTime() {return removalTime;}

    public boolean isApproved() {return approved;}

    public double getWaitingTime() {return waitingTime;}

    public void setAppId(int appId) {this.appId = appId;}

    public void setArrivalTime(double arrivalTime) {this.arrivalTime = arrivalTime;}

    public void setRemovalTime(double removalTime) {this.removalTime = removalTime;}

    public void setApproved(boolean approved) {this.approved = approved;}

    public void setWaitingTime(double waitingTime) {this.waitingTime = waitingTime;}

    public void setSimulationRun(SimulationRun run) {this.simulationRun = run;}

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}