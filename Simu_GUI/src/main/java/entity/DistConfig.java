package entity;

import jakarta.persistence.*;

@Entity
@Table(name = "distribution_config")
public class DistConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="ServicePoint_Name")
    private String servicePointName;

    @Column(name="Distribution_Type")
    private String distributionType;

    @Column(name = "Parameter1")
    private double param1;

    @Column(name="Parameter2")
    private Double param2;

    @ManyToOne
    @JoinColumn(name = "run_id", nullable = false)
    private SimulationRun simulationRun;

    public DistConfig(String servicePointName, String distributionType, double param1, Double param2) {
        this.servicePointName = servicePointName;
        this.distributionType = distributionType;
        this.param1 = param1;
        this.param2 = param2;
    }

    public DistConfig() {}

    public Long getId() {return id;}

    public String getServicePointName() {return servicePointName;}

    public String getDistributionType() {return distributionType;}

    public double getParam1() {return param1;}

    public Double getParam2() {return param2;}

    public void setServicePointName(String spName) {this.servicePointName = servicePointName;}

    public void setDistributionType(String distributionType) {this.distributionType = distributionType;}

    public void setParam1(double param1) {this.param1 = param1;}

    public void setParam2(Double param2) {this.param2 = param2;}

    public void setSimulationRun(SimulationRun run) {this.simulationRun = run;}
}
