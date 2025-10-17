package MVC.view;

/**
 * ISimulatorUI defines the interface between the Controller and the UI layer
 * in an MVC architecture for a queue simulation system.
 *
 * <p>This interface handles bidirectional communication:</p>
 * <ul>
 *     <li>Provides simulation parameters from the UI to the Controller</li>
 *     <li>Receives simulation results from the Controller to display</li>
 *     <li>Manages the visualization component and queue status updates</li>
 *</ul>
 * See {@link IVisualisation}
 */
public interface ISimulatorUI {

    /**
     * Retrieves the simulation time parameter from the UI.
     *
     * <p>This value represents the total duration for which the simulation
     * should run, typically measured in seconds or milliseconds depending
     * on the simulation's time unit.</p>
     *
     * @return the simulation time as a double value
     */
    double getTime();

    /**
     * Retrieves the delay parameter from the UI.
     *
     * <p>This value represents the time delay (typically in milliseconds)
     * between simulation steps or events, controlling the speed at which
     * the simulation progresses.</p>
     *
     * @return the delay value in milliseconds as a long
     */
    long getDelay();

    /**
     * Sets the ending time of the simulation in the UI.
     *
     * <p>This method is called by the Controller after the simulation completes
     * to display the final simulation time. This allows the UI to show when
     * the simulation ended.</p>
     *
     * @param time the ending time of the simulation
     */
    void setEndingTime(double time);

    /**
     * Retrieves the visualization component from the UI.
     *
     * <p>The Controller uses this method to access the IVisualisation interface,
     * allowing it to send drawing commands and animation updates to the UI
     * component. This decouples the visualization logic from the simulation engine.</p>
     *
     * @return the IVisualisation implementation used by this UI
     * @see IVisualisation
     */
    IVisualisation getVisualisation();

    /**
     * Displays simulation results as text in the UI.
     *
     * <p>This method is called by the Controller to show results, statistics,
     * or status messages to the user. The text is typically displayed in
     * a results panel or text area.</p>
     *
     * @param resultsText the results or status message to display
     */
    void displayResults(String resultsText);

    /**
     * Updates the queue status display for a specific service point.
     *
     * <p>This method is called by the Controller whenever the queue size at
     * a service point changes. The UI uses this information to update queue
     * length displays or status indicators.</p>
     *
     * @param servicePointId the identifier of the service point (typically 0-5)
     * @param queueSize the current number of customers waiting at this service point
     */
    void updateQueueStatus(int servicePointId, int queueSize);
}