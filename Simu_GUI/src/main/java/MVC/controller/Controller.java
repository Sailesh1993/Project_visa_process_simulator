package MVC.controller;

import eduni.project_distributionconfiguration.DistributionConfig;
import javafx.application.Platform;
import MVC.simu.framework.IEngine;
import MVC.simu.model.MyEngine;
import MVC.view.ISimulatorUI;
import MVC.view.IVisualisation;

/**
 * Controller serves as the intermediary between the simulation model and the user interface,
 * implementing the Controller component of the MVC (Model-View-Controller) architecture.
 *
 * <p>This class manages the simulation engine lifecycle, controls the speed of the simulation,
 * and ensures thread-safe communication between the simulation engine thread and the JavaFX UI thread.
 * It implements both {@link IControllerVtoM} for view-to-model communication and
 * {@link IControllerMtoV} for model-to-view communication.</p>
 *
 * Key responsibilities include:
 * <ul>
 *     <li>Starting and managing simulation engine instances</li>
 *     <li>Controlling simulation speed through delay adjustments</li>
 *     <li>Routing simulation results and updates to the UI thread</li>
 *     <li>Managing visualization updates for customer movement and queue status</li>
 *     <li>Coordinating statistics display updates</li>
 * </ul>
 *
 * See {@link IControllerVtoM}
 * See {@link IControllerMtoV}
 * See {@link IEngine}
 * See {@link ISimulatorUI}
 */
public class Controller implements IControllerVtoM, IControllerMtoV {
    /** The simulation engine instance managing the current simulation execution. */
    private IEngine engine;

    /** The user interface component for displaying simulation state and results. */
    private ISimulatorUI ui;

    /** Array of distribution configurations defining the behavior of service points. */
    private DistributionConfig[] configs;

    /** Random seed value for reproducible simulation runs. */
    private Long seed;

    /**
     * Constructs a Controller with the specified UI, distribution configurations, and random seed.
     *
     * @param ui the user interface component
     * @param configs array of distribution configurations for service points
     * @param seed random seed for simulation reproducibility
     */
    public Controller(ISimulatorUI ui, DistributionConfig[] configs, Long seed) {
        this.ui = ui;
        this.configs = configs;
        this.seed = seed;
    }

    /**
     * Starts a new simulation by creating a fresh engine instance, configuring it with
     * simulation parameters from the UI, clearing the visualization display, and starting
     * the engine thread.
     *
     * <p>A new engine thread is created for every simulation run to ensure clean state.</p>
     */
    @Override
    public void startSimulation() {
        engine = new MyEngine(this, configs, seed); // new Engine thread is created for every simulation
        engine.setSimulationTime(ui.getTime());
        engine.setDelay(ui.getDelay());
        ui.getVisualisation().clearDisplay();
        ((Thread) engine).start();
    }

    /**
     * Decreases the simulation speed by increasing the delay between simulation steps.
     * The delay is multiplied by 1.10, making the simulation run approximately 10% slower.
     */
    @Override
    public void decreaseSpeed() {engine.setDelay((long)(engine.getDelay() * 1.10));}

    /**
     * Increases the simulation speed by decreasing the delay between simulation steps.
     * The delay is multiplied by 0.9, making the simulation run approximately 10% faster.
     */
    @Override
    public void increaseSpeed() {engine.setDelay((long)(engine.getDelay() * 0.9));}

    /**
     * Displays the simulation end time in the UI.
     *
     * <p>This method is called from the engine thread and uses Platform.runLater to ensure
     * the UI update occurs on the JavaFX Application Thread.</p>
     *
     * @param time the final simulation time to display
     */
    @Override
    public void showEndTime(double time) {Platform.runLater(() -> ui.setEndingTime(time));}

    /**
     * Triggers visualization of a new customer entering the system.
     *
     * <p>This method is called from the engine thread and uses Platform.runLater to ensure
     * the visualization update occurs on the JavaFX Application Thread.</p>
     */
    @Override
    public void visualiseCustomer() {Platform.runLater(() -> ui.getVisualisation().newCustomer());}

    /**
     * Updates the queue status display for a specific service point.
     *
     * <p>This method is called from the engine thread and uses Platform.runLater to ensure
     * the UI update occurs on the JavaFX Application Thread.</p>
     *
     * @param servicePointId the identifier of the service point
     * @param queueSize the current number of customers in the queue
     */
    @Override
    public void updateQueueStatus(int servicePointId, int queueSize) {
        Platform.runLater(() -> ui.getVisualisation().updateServicePointQueue(servicePointId, queueSize));
    }

    /**
     * Displays simulation results text in the UI.
     *
     * <p>This method is called from the engine thread and uses Platform.runLater to ensure
     * the UI update occurs on the JavaFX Application Thread.</p>
     *
     * @param resultsText the formatted results text to display
     */
    @Override
    public void displayResults(String resultsText) {Platform.runLater(() -> ui.displayResults(resultsText));}

    /**
     * Retrieves the visualization component for direct access to visualization operations.
     *
     * <p>This method provides access to the component responsible for rendering the simulation's visual display.</p>
     *
     * @return the visualization component
     */
    @Override
    public IVisualisation getVisualisation() {return ui.getVisualisation();}

    /**
     * Updates the real-time statistics display in the UI with current simulation metrics.
     *
     * <p>This method is called from the engine thread and uses Platform.runLater to ensure
     * the UI update occurs on the JavaFX Application Thread. The statistics are only updated
     * if the UI implements {@link SimulationController}.</p>
     *
     * @param totalApps total number of applications processed
     * @param approved number of approved applications
     * @param rejected number of rejected applications
     * @param avgTime average processing time per application
     * @param currentTime current simulation time
     */
    @Override
    public void updateStatistics(int totalApps, int approved, int rejected, double avgTime, double currentTime) {
        Platform.runLater(() -> {
            if (ui instanceof SimulationController) {
                ((SimulationController) ui).updateStatistics(totalApps, approved, rejected, avgTime, currentTime);
            }
        });
    }

    /**
     * Retrieves the current simulation engine instance.
     *
     * <p>This method provides access to the engine for control operations such as pausing and stopping the simulation.</p>
     *
     * @return the current engine instance
     */
    @Override
    public IEngine getEngine() {
        return engine;
    }
}
