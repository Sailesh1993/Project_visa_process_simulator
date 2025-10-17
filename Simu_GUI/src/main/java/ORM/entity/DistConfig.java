package ORM.entity;

import jakarta.persistence.*;

/**
 * Entity class representing a distribution configuration for simulation runs.
 * This class is mapped to the "distribution_config" table in the database.
 */
@Entity
@Table(name = "distribution_config")
public class DistConfig {

    /** The unique identifier for the distribution configuration. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The name of the service point associated with this distribution configuration. */
    @Column(name="ServicePoint_Name")
    private String servicePointName;

    /** The type of distribution associated with this configuration. */
    @Column(name="Distribution_Type")
    private String distributionType;

    /** The first parameter associated with the distribution configuration. */
    @Column(name = "Parameter1")
    private double param1;

    /** The second parameter associated with the distribution configuration (nullable). */
    @Column(name="Parameter2")
    private Double param2;

    /**
     * The simulation run that this distribution configuration is associated with.
     * A simulation run can have many distribution configurations.
     */
    @ManyToOne
    @JoinColumn(name = "run_id", nullable = false)
    private SimulationRun simulationRun;

    /**
     * Constructs a new DistConfig instance with the specified service point name,
     * distribution type, and parameters.
     *
     * @param servicePointName the name of the service point
     * @param distributionType the type of the distribution
     * @param param1 the first parameter
     * @param param2 the second parameter (nullable)
     */
    public DistConfig(String servicePointName, String distributionType, double param1, Double param2) {
        this.servicePointName = servicePointName;
        this.distributionType = distributionType;
        this.param1 = param1;
        this.param2 = param2;
    }

    /** Default constructor for JPA */
    public DistConfig() {}

    /**
     * Returns the unique identifier of the distribution configuration.
     *
     * @return the ID of the distribution configuration
     */
    public Long getId() {return id;}

    /**
     * Returns the name of the service point.
     *
     * @return the name of the service point
     */
    public String getServicePointName() {return servicePointName;}

    /**
     * Returns the distribution type associated with this configuration.
     *
     * @return the type of the distribution
     */
    public String getDistributionType() {return distributionType;}

    /**
     * Returns the first parameter associated with the distribution configuration.
     *
     * @return the first parameter value
     */
    public double getParam1() {return param1;}

    /**
     * Returns the second parameter associated with the distribution configuration.
     *
     * @return the second parameter value (nullable)
     */
    public Double getParam2() {return param2;}

    /**
     * Sets the name of the service point.
     *
     * @param spName the new service point name
     */
    public void setServicePointName(String spName) {this.servicePointName = spName;}

    /**
     * Sets the distribution type for this configuration.
     *
     * @param distributionType the new distribution type
     */
    public void setDistributionType(String distributionType) {this.distributionType = distributionType;}

    /**
     * Sets the first parameter for this distribution configuration.
     *
     * @param param1 the new first parameter value
     */
    public void setParam1(double param1) {this.param1 = param1;}

    /**
     * Sets the second parameter for this distribution configuration.
     *
     * @param param2 the new second parameter value (nullable)
     */
    public void setParam2(Double param2) {this.param2 = param2;}

    /**
     * Sets the simulation run associated with this distribution configuration.
     *
     * @param run the simulation run to associate
     */
    public void setSimulationRun(SimulationRun run) {this.simulationRun = run;}
}