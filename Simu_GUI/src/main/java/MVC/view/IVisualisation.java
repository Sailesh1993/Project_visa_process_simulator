package MVC.view;

/**
 * IVisualisation defines the interface for visualizing a queue simulation system.
 *
 * <p>This interface specifies the methods required to display and animate a customer
 * queue management system with multiple service points. Implementations of this
 * interface handle all visual updates including customer animations, queue status
 * displays, and system state representations.</p>
 *
 * <p>Typical implementations include Canvas-based graphics, JavaFX nodes, or other
 * graphical rendering systems that can display real-time animation of customers
 * moving through service points.</p>
 *
 * See {@link SimulatorUI} for the UI implementation that uses this interface.
 */
public interface IVisualisation {

    /**
     * Clears the display and resets all visualization state.
     *
     * <p>This method removes all drawn elements from the display, clears all customer
     * data, and resets counters or status indicators. It is typically called when
     * starting a new simulation or when the user requests a reset of the system.
     * After this method is called, the display should be in a clean, empty state
     * ready for a new simulation.</p>
     */
	void clearDisplay();

    /**
     * Adds a new customer to the visualization and animation system.
     *
     * <p>This method is called when a new customer enters the system at the initial
     * entry point (typically service point 0). The implementation should create
     * a visual representation of the customer (such as an animated bubble) and
     * begin animating it towards the first service point. The customer count
     * should be incremented.</p>
     */
	void newCustomer();

    /**
     * Updates the queue status display for a specific service point.
     *
     * <p>This method is called to update the visual representation of the queue
     * length at a particular service point. Implementations should update queue
     * length indicators, change color codes based on queue size thresholds, or
     * modify status displays accordingly.</p>
     *
     * @param servicePointId the identifier of the service point (typically 0-5)
     * @param queueSize the current number of customers waiting at this service point
     */
    void updateServicePointQueue(int servicePointId, int queueSize);

    /**
     * Animates a customer moving from one service point to another.
     *
     * <p>This method is called when a customer completes service at one point and
     * must move to the next service point or exit the system. The implementation
     * should animate the customer from their current location to the destination,
     * updating their visual appearance based on approval status. If no customer
     * exists at the source service point, the implementation may create a new
     * customer for visualization purposes.</p>
     *
     * @param fromSP the source service point identifier (0-5, or -1 if starting fresh)
     * @param toSP the destination service point identifier (0-5, or -1 if exiting the system)
     * @param isApproved true if the customer was approved at the source service point,
     *                   false otherwise. This may affect the customer's visual representation.
     */
    void moveCustomer(int fromSP, int toSP, boolean isApproved);
}

