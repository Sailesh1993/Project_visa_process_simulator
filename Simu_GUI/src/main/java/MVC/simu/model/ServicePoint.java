package MVC.simu.model;

import eduni.distributions.ContinuousGenerator;
import MVC.simu.framework.Clock;
import MVC.simu.framework.Event;
import MVC.simu.framework.EventList;
import MVC.controller.IControllerMtoV;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Represents a service point in the simulation, managing a queue of applications,
 * service processing, and performance metrics. Supports multiserver (employee) logic,
 * tracks waiting and busy times, and interacts with the controller for visualization.
 */
public class ServicePoint {

    /** Queue of applications waiting for service. */
    private LinkedList<ApplicationAsCustomer> queue = new LinkedList<>();

    /** List of applications currently being served. */
    private LinkedList<ApplicationAsCustomer> inService = new LinkedList<>();

    /** Maps each application to its service start time for busy time calculation. */
    private Map<ApplicationAsCustomer, Double> serviceStartTimes = new HashMap<>();

    /** Generator for service times (random distribution). */
    private ContinuousGenerator generator;

    /** Event list for scheduling service completion events. */
    private EventList eventList;

    /** Event type scheduled for this service point. */
    private EventType eventTypeScheduled;

    /** Controller for updating the view and visualization. */
    private final IControllerMtoV controller;

    // Measurement variables
    /** Total number of applications that have departed (completed service). */
    private int totalDepartures = 0;

    /** Cumulative waiting time for all applications. */
    private double totalWaitingTime = 0.0;

    /** Maximum observed queue length. */
    private int maxQueueLength = 0;

    /** Total time employees have been busy serving applications. */
    private double busyTime = 0.0;

    // Multi-server tracking
    /** Number of employees (servers) at this service point. */
    private int numEmployees = 5;

    /** Number of employees currently busy. */
    private int busyServers = 0;

    /**
     * Constructs a ServicePoint with the given generator, event list, event type, and controller.
     *
     * @param generator Service time generator
     * @param eventList Event list for scheduling
     * @param type Event type for this service point
     * @param controller Controller for visualization and updates
     */
    public ServicePoint(ContinuousGenerator generator, EventList eventList, EventType type, IControllerMtoV controller) {
        this.eventList = eventList;
        this.generator = generator;
        this.eventTypeScheduled = type;
        this.controller = controller;
    }

    /**
     * Gets the event type scheduled for this service point.
     *
     * @return EventType scheduled
     */
    public EventType getEventTypeScheduled() {
        return eventTypeScheduled;
    }

    /**
     * Sets the event type scheduled for this service point.
     *
     * @param eventTypeScheduled EventType to set
     */
    public void setEventTypeScheduled(EventType eventTypeScheduled) {
        this.eventTypeScheduled = eventTypeScheduled;
    }

    /**
     * Gets the name of this service point.
     *
     * @return Service point name, or "Unknown Service Point" if not set
     */
    public String getServicePointName() {
        if (eventTypeScheduled == null) {
            return "Unknown Service Point";
        }
        String name = eventTypeScheduled.getServicePointName();
        return (name != null) ? name : "Unknown Service Point";
    }

    /**
     * Adds an application to the queue and attempts to start service if employees are available.
     * Updates metrics and visualization.
     *
     * @param application Application to add to the queue
     */
    public synchronized void addQueue(ApplicationAsCustomer application) {
        application.setTimeEnteredQueue(Clock.getInstance().getTime());
        queue.add(application);
        maxQueueLength = Math.max(maxQueueLength, queue.size());

        checkBottleneck();
        updateControllerQueueStatus();
        controller.visualiseCustomer();

        // Try to start service immediately if there are free employees
        beginService();
    }

    /**
     * Called by the engine when a service completion event for this SP occurs.
     * Removes the application from the in-service list, updates metrics,
     * and attempts to serve the next customer.
     *
     * @return The application that completed service, or null if none
     */
    public synchronized ApplicationAsCustomer removeQueue() {
        if (inService.isEmpty()) return null;

        ApplicationAsCustomer app = inService.poll();
        totalDepartures++;

        double now = Clock.getInstance().getTime();

        // accumulate busy time using the recorded service start time for this app
        Double start = serviceStartTimes.remove(app);
        if (start != null) {
            busyTime += now - start;
        }

        busyServers = Math.max(0, busyServers - 1);

        updateControllerQueueStatus();

        // Allow next waiting customer(s) to start service if possible
        beginService();

        return app;
    }

    /**
     * Starts service for as many waiting customers as there are free employees.
     * Moves customers from waiting queue -> inService and schedules completion events.
     */
    public synchronized void beginService() {
        double now = Clock.getInstance().getTime();

        while (busyServers < numEmployees && !queue.isEmpty()) {
            ApplicationAsCustomer app = queue.poll(); // remove from waiting queue
            if (app == null) break;

            busyServers++;

            // Waiting time tracking
            double waitingTime = now - app.getTimeEnteredQueue();
            totalWaitingTime += waitingTime;
            app.setTimeInWaitingRoom(waitingTime);

            maxQueueLength = Math.max(maxQueueLength, queue.size());

            // Service time (guard against zero)
            double serviceTime = Math.max(1e-6, generator.sample());

            // Track service start for utilization/busy-time calculation and put into in-service list
            serviceStartTimes.put(app, now);
            inService.add(app);

            // Schedule service completion event for this service point (no direct app reference in Event)
            eventList.add(new Event(eventTypeScheduled, now + serviceTime));
        }

        updateControllerQueueStatus();
    }

    /**
     * Checks if all employees are busy.
     *
     * @return true if all employees are busy, false otherwise
     */
    public boolean isReserved() {
        return busyServers >= numEmployees;
    }

    /**
     * Checks if there are applications waiting in the queue.
     *
     * @return true if queue is not empty, false otherwise
     */
    public boolean isOnQueue() {
        return !queue.isEmpty();
    }

    /**
     * Gets the current size of the queue.
     *
     * @return Number of applications in the queue
     */
    public int getQueueSize() {
        return queue.size();
    }

    /**
     * Gets the total number of applications that have departed.
     *
     * @return Total departures
     */
    public int getTotalDepartures() {
        return totalDepartures;
    }

    /**
     * Gets the average waiting time for applications.
     *
     * @return Average waiting time, or 0.0 if no departures
     */
    public double getAverageWaitingTime() {
        return totalDepartures > 0 ? totalWaitingTime / totalDepartures : 0.0;
    }

    /**
     * Gets the maximum observed queue length.
     *
     * @return Maximum queue length
     */
    public int getMaxQueueLength() {
        return maxQueueLength;
    }

    /**
     * Calculates the utilization percentage of employees over the given simulation time.
     *
     * @param simulationTime Total simulation time
     * @return Utilization percentage (0-100)
     */
    public double getUtilization(double simulationTime) {
        return simulationTime > 0 ? (busyTime / simulationTime) * 100 : 0.0;
    }

    /**
     * Gets the number of employees (servers) at this service point.
     *
     * @return Number of employees
     */
    public int getNumEmployees() {
        return numEmployees;
    }

    /**
     * Sets the number of employees (servers) for this service point.
     * Ensures the number is at least 1.
     *
     * @param newEmployeeCount Desired number of employees
     */

    public void adjustEmployees(int newEmployeeCount) {
        this.numEmployees = Math.max(1, newEmployeeCount);
    }

    /**
     * Checks for bottleneck conditions and increases employees if queue exceeds threshold.
     */
    public void checkBottleneck() {
        if (queue.size() > 15) {
            adjustEmployees(numEmployees + 1);
        }
    }

    /**
     * Updates the controller with the current queue status for visualization.
     */
    private void updateControllerQueueStatus() {
        int spId = eventTypeScheduled.getServicePointIndex();
        controller.updateQueueStatus(spId, queue.size());
    }
}
