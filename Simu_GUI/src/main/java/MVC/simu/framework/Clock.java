package MVC.simu.framework;

/**
 * Singleton class representing the simulation clock.
 * <p>
 * Keeps track of the current simulation time and provides methods
 * to get, set, and reset the time. Only one instance of {@code Clock}
 * exists throughout the simulation.
 */
public class Clock {
	private double time;
	private static Clock instance;

    /**
     * Private constructor to enforce the singleton pattern.
     */
	private Clock(){
		time = 0;
	}

    /**
     * Returns the single instance of the {@code Clock} class.
     * Creates a new instance if it does not already exist.
     *
     * @return the singleton {@code Clock} instance
     */
	public static Clock getInstance(){
		if (instance == null){
			instance = new Clock();
		}
		return instance;
	}

    /**
     * Sets the current simulation time.
     *
     * @param time the new simulation time
     */
	public void setTime(double time){
		this.time = time;
	}

    /**
     * Returns the current simulation time.
     *
     * @return current simulation time
     */
	public double getTime(){
		return time;
	}

    /**
     * Resets the simulation time to zero.
     */
    public void reset() {
        this.time = 0.0;
    }
}
