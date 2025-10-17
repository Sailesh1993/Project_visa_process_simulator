package MVC.controller;

import MVC.simu.framework.IEngine;

/**
 * IControllerVtoM defines the interface for communication from the View to the Model
 * through the Controller in the MVC architecture.
 *
 * <p>This interface specifies methods that the user interface (View) can call
 * to control the simulation engine (Model). It provides operations for starting
 * the simulation, adjusting the simulation speed, and accessing the engine
 * for additional control operations such as pausing and stopping.</p>
 *
 * <p>The interface supports the following features:</p>
 * <ul>
 *     <li>Starting new simulation runs.</li>
 *     <li>Increasing simulation speed by reducing delay between steps.</li>
 *     <li>Decreasing simulation speed by increasing delay between steps.</li>
 *     <li>Retrieving the engine instance for pause and stop operations.</li>
 * </ul>
 *
 * <p>See {@link IControllerMtoV}</p>
 * <p>See {@link IEngine}</p>
 */
public interface IControllerVtoM {

    /**
     * Starts a new simulation run.
     *
     * <p>This method creates a new engine instance, configures it with the
     * simulation parameters from the UI, and starts the simulation thread.</p>
     */
    void startSimulation();

    /**
     * Increases the simulation speed by decreasing the delay between simulation steps.
     *
     * <p>This method makes the simulation run faster by reducing the time
     * delay between each simulation step.</p>
     */
    void increaseSpeed();

    /**
     * Decreases the simulation speed by increasing the delay between simulation steps.
     *
     * <p>This method makes the simulation run slower by increasing the time
     * delay between each simulation step.</p>
     */
    void decreaseSpeed();

    /**
     * Retrieves the current simulation engine instance.
     *
     * <p>This method provides access to the engine for control operations
     * such as pausing and stopping the simulation.</p>
     *
     * @return the current engine instance
     */
    IEngine getEngine();
}
