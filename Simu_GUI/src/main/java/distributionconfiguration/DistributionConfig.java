package distributionconfiguration;

import eduni.distributions.ContinuousGenerator;
import eduni.distributions.Gamma;
import eduni.distributions.Negexp;
import eduni.distributions.Normal;

public class DistributionConfig {
    private final String type;
    private final double param1;
    private final Double param2;
    private final boolean forArrival;               //NEW: flag to clarify if this config is for interarrival times

    public DistributionConfig(String type, double param1, boolean forArrival) {
        this.type = type;
        this.param1 = param1;
        this.param2 = null;
        this.forArrival = forArrival;
    }

    public DistributionConfig(String type, double param1, double param2, boolean forArrival) {
        this.type = type;
        this.param1 = param1;
        this.param2 = param2;
        this.forArrival = forArrival;

    }
    public String getType() {
        return this.type;
    }

    public double getParam1() {
        return this.param1;
    }

    public double getParam2() {
        return this.param2;
    }

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

    public boolean isForArrival(){
        return forArrival;
    }

    @Override
    public String toString() {
        if (param2 != null) {
            return String.format("DistributionConfig[type=%s, param1=%.2f, param2=%.2f, forArrival=%s]", type, param1, param2, forArrival);
        } else {
            return String.format("DistributionConfig[type=%s, param1=%.2f, forArrival=%s]", type, param1, forArrival);
        }
    }
}
