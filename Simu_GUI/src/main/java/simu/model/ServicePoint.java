package simu.model;

import eduni.distributions.ContinuousGenerator;
import simu.framework.Clock;
import simu.framework.Event;
import simu.framework.EventList;
import controller.IControllerMtoV;

import java.util.LinkedList;

public class ServicePoint {
    private LinkedList<ApplicationAsCustomer> queue = new LinkedList<>();
    private ContinuousGenerator generator;
    private EventList eventList;
    private EventType eventTypeScheduled;
    private IControllerMtoV controller;

    // Measurement variables
    private int totalDepartures = 0;
    private double totalWaitingTime = 0.0;
    private int maxQueueLength = 0;
    private double busyTime = 0.0;
    private double lastServiceStart = 0.0;

    // Multi-server tracking
    private int numEmployees = 1;
    private int busyServers = 0; // active employees

    public ServicePoint(ContinuousGenerator generator, EventList eventList, EventType type, IControllerMtoV controller) {
        this.eventList = eventList;
        this.generator = generator;
        this.eventTypeScheduled = type;
        this.controller = controller;
    }

<<<<<<< HEAD
    public String getServicePointName() {
        return eventTypeScheduled.getDisplayName();
    }

=======
>>>>>>> origin/feature-myEngine
    public synchronized void addQueue(ApplicationAsCustomer application) {
        application.setTimeEnteredQueue(Clock.getInstance().getTime());
        queue.add(application);
        maxQueueLength = Math.max(maxQueueLength, queue.size());
<<<<<<< HEAD
        checkBottleneck();
        updateControllerQueueStatus();
        controller.visualiseCustomer();
    }

    public synchronized ApplicationAsCustomer removeQueue() {
        // MyEngine still calls this at END_* events
        busyServers = Math.max(0, busyServers - 1);
        totalDepartures++;

        double now = Clock.getInstance().getTime();
        if (lastServiceStart > 0) busyTime += now - lastServiceStart;
        lastServiceStart = 0;

        updateControllerQueueStatus();

        // Try to start another service automatically
        beginService();

        return null; // kept for API compatibility
    }

    public synchronized void beginService() {
        // Allow as many concurrent services as there are free employees
        while (busyServers < numEmployees && !queue.isEmpty()) {
            ApplicationAsCustomer app = queue.poll();
            if (app == null) break;

            busyServers++;

            // Waiting time tracking
            double waitingTime = Clock.getInstance().getTime() - app.getTimeEnteredQueue();
            totalWaitingTime += waitingTime;
            app.setTimeInWaitingRoom(waitingTime);

            maxQueueLength = Math.max(maxQueueLength, queue.size());

            // Service time
            double serviceTime = Math.max(1e-6, generator.sample());
            lastServiceStart = Clock.getInstance().getTime();

            // Schedule service completion
            eventList.add(new Event(eventTypeScheduled, Clock.getInstance().getTime() + serviceTime));
        }

        updateControllerQueueStatus();
=======

        int currentQueueSize = queue.size();
        int servicePointId = getServicePointId();

        checkBottleneck();

        // Call controller OUTSIDE synchronized block
        controller.updateQueueStatus(servicePointId, currentQueueSize);
        controller.visualiseCustomer();
    }

    public ApplicationAsCustomer removeQueue() {
        ApplicationAsCustomer application;
        int currentQueueSize;
        int servicePointId;

        synchronized(this) {
            if (queue.isEmpty()) return null;

            application = queue.poll();
            reserved = false;
            totalDepartures++;

            double now = Clock.getInstance().getTime();

            if (lastServiceStart > 0) {
                double serviceDuration = now - lastServiceStart;
                if (serviceDuration > 0) {
                    busyTime += serviceDuration;
                }
                lastServiceStart = 0;
            }

            currentQueueSize = queue.size();
            servicePointId = getServicePointId();
        }

        // Call controller OUTSIDE synchronized block
        controller.updateQueueStatus(servicePointId, currentQueueSize);
        return application;
>>>>>>> origin/feature-myEngine
    }

    public boolean isReserved() {
        // Tells Engine if at least one employee is busy
        return busyServers >= numEmployees;
    }

    public boolean isOnQueue() {
        return !queue.isEmpty();
    }

    public int getQueueSize() {
        return queue.size();
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
