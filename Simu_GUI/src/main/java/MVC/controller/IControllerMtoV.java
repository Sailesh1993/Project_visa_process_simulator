package MVC.controller;

import MVC.view.IVisualisation;

/**
 * IControllerMtoV defines the interface for communication from the Model to the View
 * through the Controller in the MVC architecture.
 *
 * <p>This interface specifies methods that the simulation engine (Model) can call
 * to update the user interface (View) with simulation progress, results, and
 * real-time statistics. All methods in this interface are called from the
 * simulation engine thread and must ensure thread-safe UI updates.</p>
 *
 * <p>Thread-safety considerations: Since methods in this interface can be called
 * from the simulation engine's thread (which is separate from the UI thread),
 * it is important that all updates to the UI components are done in a thread-safe manner.
 * Typically, this involves using mechanisms like `Platform.runLater()` in JavaFX to ensure
 * that UI updates occur on the JavaFX Application Thread.</p>
 *
 * The interface supports the following features:
 * <ul>
 *     <li>Displaying the simulation completion time.</li>
 *     <li>Triggering customer visualization as they enter the system.</li>
 *     <li>Updating service point queue statuses in real time.</li>
 *     <li>Displaying final simulation results.</li>
 *     <li>Updating real-time statistics during simulation execution.</li>
 *     <li>Providing access to the visualization component for direct interactions.</li>
 * </ul>
 *
 * See {@link IControllerVtoM}
 * See {@link IVisualisation}
 */
public interface IControllerMtoV {

    /**
     * Displays the simulation end time in the user interface.
     *
     * <p>This method is invoked when the simulation completes, to show the
     * final simulation time in the View.</p>
     *
     * @param time the final simulation time (in arbitrary units, e.g., seconds or minutes)
     */
    void showEndTime(double time);

    /**
     * Triggers the visualization of a new customer entering the system.
     *
     * <p>This method is called by the model whenever a new customer is
     * created, to initiate its display in the visualization.</p>
     */
    void visualiseCustomer();

    /**
     * Updates the queue status display for a specific service point.
     *
     * <p>This method is invoked when the queue size for a service point
     * changes (e.g., a customer enters or leaves the queue).
     * It refreshes the queue size display in the View.</p>
     *
     * @param servicePointId the identifier of the service point whose queue is being updated
     * @param queueSize the current number of customers in the service point's queue
     */
    void updateQueueStatus(int servicePointId, int queueSize);

    /**
     * Displays the final simulation results in the user interface.
     *
     * <p>This method is invoked when the simulation completes, to display
     * the comprehensive results. The results may include:</p>
     * <ul>
     *     <li>Simulation run time</li>
     *     <li>Number of customers processed</li>
     *     <li>Approval and rejection rates</li>
     *     <li>Queue performance metrics</li>
     *     <li>Other relevant statistics</li>
     * </ul>
     *
     * @param resultsText a formatted string containing the results of the simulation
     */
    void displayResults(String resultsText);

    /**
     * Updates real-time statistics during the simulation execution.
     *
     * <p>This method is periodically called during the simulation to update
     * the display with real-time statistics, such as the following:</p>
     * <ul>
     *     <li><b>totalApps:</b> total number of applications processed so far during the simulation.</li>
     *     <li><b>approved:</b> number of applications that have been approved.</li>
     *     <li><b>rejected:</b> number of applications that have been rejected.</li>
     *     <li><b>avgTime:</b> average processing time per application (in the same units as simulation time).</li>
     *     <li><b>currentTime:</b> the current simulation time (in the same units as simulation time).</li>
     * </ul>
     */
    void updateStatistics(int totalApps, int approved, int rejected, double avgTime, double currentTime);

    /**
     * Retrieves the visualization component for direct access to visualization operations.
     *
     * <p>This method provides access to the visualization component, allowing
     * the model to interact directly with the view (e.g., for moving customers
     * between service points during the simulation).</p>
     *
     * @return the visualization component used for rendering simulation data
     */
    IVisualisation getVisualisation();
}
