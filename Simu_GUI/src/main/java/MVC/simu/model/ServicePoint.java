package MVC.simu.model;

import eduni.distributions.ContinuousGenerator;
import MVC.simu.framework.Clock;
import MVC.simu.framework.Event;
import MVC.simu.framework.EventList;
import MVC.controller.IControllerMtoV;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ServicePoint {

    private LinkedList<ApplicationAsCustomer> queue = new LinkedList<>();
    private LinkedList<ApplicationAsCustomer> inService = new LinkedList<>(); // holds customers currently being served
    private Map<ApplicationAsCustomer, Double> serviceStartTimes = new HashMap<>(); // service start per customer

    private ContinuousGenerator generator;
    private EventList eventList;
    private EventType eventTypeScheduled;
    private final IControllerMtoV controller;

    // Measurement variables
    private int totalDepartures = 0;
    private double totalWaitingTime = 0.0;
    private int maxQueueLength = 0;
    private double busyTime = 0.0;

    // Multi-server tracking
    private int numEmployees = 5;
    private int busyServers = 0; // active employees

    public ServicePoint(ContinuousGenerator generator, EventList eventList, EventType type, IControllerMtoV controller) {
        this.eventList = eventList;
        this.generator = generator;
        this.eventTypeScheduled = type;
        this.controller = controller;
    }

    public EventType getEventTypeScheduled() {
        return eventTypeScheduled;
    }

    public void setEventTypeScheduled(EventType eventTypeScheduled) {
        this.eventTypeScheduled = eventTypeScheduled;
    }

    public String getServicePointName() {
        if (eventTypeScheduled == null) {
            return "Unknown Service Point";
        }
        String name = eventTypeScheduled.getServicePointName();
        return (name != null) ? name : "Unknown Service Point";
    }

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
     * Returns the application that completed service (pulled from inService list).
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

    public boolean isReserved() {
        // True if all employees are busy
        return busyServers >= numEmployees;
    }

    public boolean isOnQueue() {
        return !queue.isEmpty();
    }

    public int getQueueSize() {
        return queue.size();
    }

    public synchronized List<ApplicationAsCustomer> getQueueSnapshot() {
        return Collections.unmodifiableList(new LinkedList<>(queue));
    }

    // Metrics
    public int getTotalDepartures() {
        return totalDepartures;
    }

    public double getAverageWaitingTime() {
        return totalDepartures > 0 ? totalWaitingTime / totalDepartures : 0.0;
    }

    public int getMaxQueueLength() {
        return maxQueueLength;
    }

    public double getUtilization(double simulationTime) {
        return simulationTime > 0 ? (busyTime / simulationTime) * 100 : 0.0;
    }

    public int getNumEmployees() {
        return numEmployees;
    }

    public void adjustEmployees(int newEmployeeCount) {
        this.numEmployees = Math.max(1, newEmployeeCount);
    }

    public void checkBottleneck() {
        if (queue.size() > 15) {
            adjustEmployees(numEmployees + 1);
        }
    }

    private void updateControllerQueueStatus() {
        int spId = eventTypeScheduled.getServicePointIndex();
        controller.updateQueueStatus(spId, queue.size());
    }
}
