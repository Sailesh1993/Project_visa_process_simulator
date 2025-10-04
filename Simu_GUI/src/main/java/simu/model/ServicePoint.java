package simu.model;

import eduni.distributions.ContinuousGenerator;
import simu.framework.Clock;
import simu.framework.Event;
import simu.framework.EventList;

import java.util.LinkedList;

import controller.IControllerMtoV;

// TODO:
// Service Point functionalities & calculations (+ variables needed) and reporting to be implemented
public class ServicePoint {
    private LinkedList<ApplicationAsCustomer> queue = new LinkedList<ApplicationAsCustomer>(); // Data Structure used
    private ContinuousGenerator generator;
    private EventList eventList;
    private EventType eventTypeScheduled;
    private IControllerMtoV controller;

    private boolean reserved = false;

    // Measurement variables
    private int totalDepartures = 0;
    private double totalWaitingTime = 0.0;
    private int maxQueueLength = 0;
    private double busyTime = 0.0; // Total time SP was busy.
    private double lastServiceStart = 0.0;              // for utilization tracking
    private int numEmployees = 1;

    public ServicePoint(ContinuousGenerator generator, EventList eventList, EventType type, IControllerMtoV controller){
        this.eventList = eventList;
        this.generator = generator;
        this.eventTypeScheduled = type;
        this.controller = controller;
    }

    public String getServicePointName() {
        return eventTypeScheduled.getDisplayName();
    }

    public synchronized void addQueue(ApplicationAsCustomer application) {
        application.setTimeEnteredQueue(Clock.getInstance().getTime());
        queue.add(application);
        maxQueueLength = Math.max(maxQueueLength, queue.size());

        checkBottleneck();
        updateControllerQueueStatus();
        controller.visualiseCustomer();
    }

    public synchronized ApplicationAsCustomer removeQueue() {
        if (queue.isEmpty()) return null;

        ApplicationAsCustomer application = queue.poll();
        reserved = false;
        totalDepartures++;

        double now = Clock.getInstance().getTime();

        if (lastServiceStart > 0) busyTime += now - lastServiceStart;
        lastServiceStart = 0;
        updateControllerQueueStatus();
        return application;
    }

    public void beginService() {  		// Begins a new service, customer is on the queue during the service
        if (queue.isEmpty()) return;

        ApplicationAsCustomer application = queue.peek();
        reserved = true;

        //calculate waiting time for this application
        double waitingTime = Clock.getInstance().getTime() - application.getTimeEnteredQueue();
        totalWaitingTime += waitingTime;
        application.setTimeInWaitingRoom(waitingTime);

        maxQueueLength = Math.max(maxQueueLength, queue.size());        //Track max queue length

        // Get service time sample and clamp so it's never negative or zero
        double serviceTime = Math.max(1e-6, generator.sample());
        lastServiceStart = Clock.getInstance().getTime();              //Track when service starts for utilization tracking

        // Schedule service completion event
        eventList.add(new Event(eventTypeScheduled, Clock.getInstance().getTime() + serviceTime));

        // Safe employee adjustment based on queue size
        int desiredEmployees = Math.min(Math.max(1, queue.size()), 10);                     // 1 to 10 employees
        adjustEmployees(desiredEmployees);
    }

    public boolean isReserved(){return reserved;}

    public boolean isOnQueue(){return !queue.isEmpty();}

    public int getQueueSize() {return queue.size();}

    // Metrics getters
    public int getTotalDepartures() {return totalDepartures;}

    public double getAverageWaitingTime(){
        return totalDepartures > 0 ? totalWaitingTime / totalDepartures : 0.0;
    }

    //Track queue length
    public int getMaxQueueLength(){return maxQueueLength;}

    //Method to track utilization (percentage of busy time)
    public double getUtilization(double simulationTime){
        return simulationTime > 0 ? (busyTime / simulationTime) * 100 : 0.0;
    }

    public int getNumEmployees() {return numEmployees;}

    public void adjustEmployees(int newEmployeeCount) {
        this.numEmployees = Math.max(1, newEmployeeCount);
    }

    public void checkBottleneck() {
        if (queue.size() > 15) {
            adjustEmployees(numEmployees++);
        }
    }

    private void updateControllerQueueStatus() {
        int spId = eventTypeScheduled.getServicePointIndex();
        controller.updateQueueStatus(spId, queue.size());
    }
}
