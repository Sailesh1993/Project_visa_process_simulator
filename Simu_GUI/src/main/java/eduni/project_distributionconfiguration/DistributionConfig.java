package eduni.project_distributionconfiguration;

import eduni.distributions.ContinuousGenerator;
import eduni.distributions.Gamma;
import eduni.distributions.Negexp;
import eduni.distributions.Normal;

/**
 * Represents the configuration of a probability distribution used in the simulation.
 * <p>
 * Each {@code DistributionConfig} defines a specific statistical distribution
 * (e.g., Normal, Exponential, or Gamma) with its parameters.
 * These configurations are used to generate random samples for
 * service times or interarrival times in the simulation.
 * </p>
 *
 * <p>
 * The {@link #buildGenerator()} method creates a concrete
 * {@link ContinuousGenerator} instance from the {@code eduni.distributions} library
 * based on this configuration.
 * </p>
 *
 * <p>
 * Example usage (for demonstration only):
 * In this project, {@code DistributionConfig} objects are typically created
 * based on user inputs from the GUI, which are then passed to {@code MyEngine}.
 * </p>
 *
 * <pre>{@code
 * //Example of manual creation
 * DistributionConfig config = new DistributionConfig("Normal", 10.0, 2.0, false);
 * ContinuousGenerator generator = config.buildGenerator();
 * double sample = generator.sample();
 * }</pre>
 */
public class DistributionConfig {
    private final String type;
    private final double param1;
    private final Double param2;
    private final boolean forArrival;

    /**
     * Constructs a single-parameter distribution configuration (e.g., Exponential).
     *
     * @param type       the name of the distribution (e.g., "Negexp")
     * @param param1     the first parameter (mean, shape, or rate)
     * @param forArrival whether this distribution is used for arrival processes
     */
    public DistributionConfig(String type, double param1, boolean forArrival) {
        this.type = type;
        this.param1 = param1;
        this.param2 = null;
        this.forArrival = forArrival;
    }

    /**
     * Constructs a two-parameter distribution configuration (e.g., Normal, Gamma).
     *
     * @param type       the name of the distribution (e.g., "Normal" or "Gamma")
     * @param param1     the first parameter (mean or shape)
     * @param param2     the second parameter (standard deviation or scale)
     * @param forArrival whether this distribution is used for arrival processes
     */
    public DistributionConfig(String type, double param1, double param2, boolean forArrival) {
        this.type = type;
        this.param1 = param1;
        this.param2 = param2;
        this.forArrival = forArrival;
    }

    /**
     * Returns the distribution type.
     *
     * @return the name of the distribution (e.g., "Normal", "Negexp", "Gamma")
     */
    public String getType() {
        return this.type;
    }

    /**
     * Returns the first distribution parameter.
     *
     * @return the first parameter value (e.g., mean or shape)
     */
    public double getParam1() {
        return this.param1;
    }

    /**
     * Returns the second distribution parameter, if applicable.
     *
     * @return the second parameter value, or {@code null} if not used
     */
    public double getParam2() {
        return this.param2;
    }

    /**
     * Builds and returns a {@link ContinuousGenerator} instance based on the distribution type.
     * <p>
     * Supported types:
     * <ul>
     *   <li>{@code "Normal"} – requires mean and standard deviation</li>
     *   <li>{@code "Negexp"} – requires mean</li>
     *   <li>{@code "Gamma"} – requires shape and scale</li>
     * </ul>
     *
     * @return a new {@link ContinuousGenerator} configured according to this distribution
     * @throws IllegalArgumentException if parameters are invalid or distribution type is unknown
     */
    public ContinuousGenerator buildGenerator() {
        switch (type) {
            case "Normal":
                if (param2 == null || param2 <= 0) throw new IllegalArgumentException("Normal distribution requires mean, stddev > 0");
                return new Normal(param1, param2);
            case "Negexp":
                if (param1 <= 0) throw new IllegalArgumentException("Negexp distribution requires mean > 0");
                return new Negexp(param1, 1);               //second arg unused in Negexp, so is set to dummy 1

            case "Gamma":
                if (param2 == null || param1 <= 0 || param2 <= 0) throw new IllegalArgumentException("Gamma distribution require shape, scale > 0");
                return new Gamma(param1, param2);
            default:
                throw new IllegalArgumentException("Unknown distribution: " + type);
        }
    }

    /**
     * Returns a string representation of this distribution configuration.
     *
     * @return a human-readable summary of the distribution parameters
     */
    @Override
    public String toString() {
        if (param2 != null) {
            return String.format("DistributionConfig[type=%s, param1=%.2f, param2=%.2f, forArrival=%s]", type, param1, param2, forArrival);
        } else {
            return String.format("DistributionConfig[type=%s, param1=%.2f, forArrival=%s]", type, param1, forArrival);
        }
    }
}
