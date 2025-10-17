package MVC.simu.framework;

/**
 * Defines the core control interface for the simulation engine.
 * <p>
 * Implementations of this interface manage simulation timing,
 * speed control, and execution flow (pause, resume, stop).
 * It is primarily used by the controller to control the simulation lifecycle.
 * </p>
 */
public interface IEngine {

    /**
     * Sets the total simulation duration.
     *
     * @param time the simulation time limit, after which the engine will stop
     */
    void setSimulationTime(double time);

    /**
     * Sets the delay between simulation steps (used to slow down visualization).
     *
     * @param time the delay in milliseconds
     */
    void setDelay(long time);

    /**
     * Returns the current delay between simulation steps.
     *
     * @return the delay in milliseconds
     */
    long getDelay();

    /**
     * Pauses the simulation without stopping progress permanently.
     * The engine thread will wait until {@link #resume()} is called.
     */
    void pause();

    /**
     * Resumes a previously paused simulation.
     */
    void resume();

    /**
     * Stops the simulation completely and terminates execution.
     */
    void stopSimulation();
}
