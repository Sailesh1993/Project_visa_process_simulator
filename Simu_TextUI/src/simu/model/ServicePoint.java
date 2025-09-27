
package simu.model;

import eduni.distributions.ContinuousGenerator;
import simu.framework.*;
import java.util.LinkedList;

/**
 * Service Point implements the functionalities, calculations and reporting.
 *
 * TODO: This must be modified to actual implementation. Things to be added:
 *     - functionalities of the service point
 *     - measurement variables added
 *     - getters to obtain measurement values
 *
 * Service point has a queue where customers(application in our concept) are waiting to be serviced.
 * Service point simulated the servicing time using the given random number generator which
 * generated the given event (customer serviced) for that time.
 *
 * Service point collects measurement parameters.
 */
public class ServicePoint {

    private LinkedList<ApplicationAsCustomer> queue = new LinkedList<>(); // Data Structure used
    private ContinuousGenerator generator;
    private EventList eventList;
    private EventType eventTypeScheduled;
    //QueueStrategy strategy; // option: ordering of the customer(Application)
    private boolean reserved  = false;

    // Measurement variables
    private int totalDepartures = 0;
    private double totalWaitingTime = 0.0;
    private int maxQueueLength = 0;
    private double busyTime = 0.0; // Total time SP was busy.
    private double lastServiceStart = 0.0;              // for utilization tracking
    private int numEmployees = 1;

    /**
     * Create the service point with a waiting queue.
     *
     * @param generator Random number generator for service time simulation
     * @param eventList Simulator event list, needed for the insertion of service ready event
     * @param type Event type for the service end event
     */
    public ServicePoint(ContinuousGenerator generator, EventList eventList, EventType type){
        this.eventList = eventList;
        this.generator = generator;
        this.eventTypeScheduled = type;
    }

    public String getServicePointName() {
        switch (eventTypeScheduled) {
            case END_APPLICATION_ENTRY: return "[1] Application Entry & Appointment Booking";
            case END_DOC_SUBMISSION: return "[2] Document Submission & Interview";
            case END_BIOMETRICS: return "[3] Biometrics Collection";
            case MISSING_DOCS_RESOLVED: return "[4] Missing Documents Resolution";
            case END_DOC_CHECK: return "[5] Document Verification & Background Check";
            case END_DECISION: return "[6] Decision Room";
            default: return "Unknown Service Point";
        }
    }

    /**
     * Add a customer to the service point queue.
     *
     * @param a Customer to be queued
     */
    public void addQueue(ApplicationAsCustomer a) {	// The first customer of the queue is always in service
        a.setTimeEnteredQueue(Clock.getInstance().getClock());
        queue.add(a);
        maxQueueLength = Math.max(maxQueueLength, queue.size());
        checkBottleneck();
    }

    /**
     * Remove customer from the waiting queue.
     * Here we calculate also the appropriate measurement values.
     *
     * @return Customer retrieved from the waiting queue
     */
    public ApplicationAsCustomer removeQueue() {		// Remove serviced customer
        if (queue.isEmpty()) return null;

        ApplicationAsCustomer application = queue.poll();
        reserved = false;
        totalDepartures++;

        double now = Clock.getInstance().getClock();

        // Accumulate busy time only for the period the service was actually active
        if (lastServiceStart > 0) {
            double serviceDuration = now - lastServiceStart;
            if (serviceDuration > 0) {
                busyTime += serviceDuration;
            }
            lastServiceStart = 0;                   // reset for next service
        }

        Trace.out(Trace.Level.INFO, "Service Point " + "\"" + getServicePointName() + "\"" +
                " --> Completed service for Application #" + application.getId() +
                " | Total departures: " + totalDepartures);

        return application;
    }

    /**
     * Begins a new service, customer is on the queue during the service
     * Inserts a new event to the event list when the service should be ready.
     */
    public void beginService() {		// Begins a new service, customer is on the queue during the service
        if (queue.isEmpty()) return;

        ApplicationAsCustomer application = queue.peek();
        reserved = true;

        //calculate waiting time for this application
        double waitingTime = Clock.getInstance().getClock() - application.getTimeEnteredQueue();
        totalWaitingTime += waitingTime;
        application.setTimeInWaitingRoom(waitingTime);

        maxQueueLength = Math.max(maxQueueLength, queue.size());        //Track max queue length

        // Get service time sample and clamp so it's never negative or zero
        double serviceTime = generator.sample();
        serviceTime = Math.max(1e-6, serviceTime);

        lastServiceStart = Clock.getInstance().getClock();              //Track when service starts for utilization tracking

        // Schedule service completion event
        eventList.add(new Event(eventTypeScheduled, Clock.getInstance().getClock()+serviceTime));
        Trace.out(Trace.Level.INFO,"At Service Point " + "\"" + getServicePointName() + "\":");
        Trace.out(Trace.Level.INFO, "-->Started service for Application #" + application.getId() + " | Waiting time: " + Trace.formatTime(waitingTime)  + " minutes" + " | Service time: " + Trace.formatTime(serviceTime) + " minutes" );

        // Safe employee adjustment based on queue size
        int desiredEmployees = Math.min(Math.max(1, queue.size()), 10);                     // 1 to 10 employees
        if (numEmployees != desiredEmployees) {
            adjustEmployees(desiredEmployees);
            Trace.out(Trace.Level.INFO, "*Number of employees adjusted due to queue demand: " + queue.size());
        }
    }

    /**
     * Check whether the service point is busy
     *
     * @return logical value indicating service state
     */
    public boolean isReserved(){
        return reserved;
    }

    /**
     * Check whether there is customers on the waiting queue
     *
     * @return logical value indicating queue status
     */
    public boolean isOnQueue(){
        return !queue.isEmpty();
    }

    // Metrics getters
    public int getTotalDepartures() {
        return totalDepartures;
    }

    public double getAverageWaitingTime(){
        return totalDepartures > 0 ? totalWaitingTime / totalDepartures : 0.0;
    }

    //Track queue length
    public int getMaxQueueLength(){
        return maxQueueLength;
    }

    //Method to track utilization (percentage of busy time)
    public double getUtilization(double simulationTime){
        return simulationTime > 0 ? (busyTime / simulationTime) * 100 : 0.0;
    }

    public int getNumEmployees() {
        return numEmployees;
    }

    public void adjustEmployees(int newEmployeeCount) {
        this.numEmployees = Math.max(1, newEmployeeCount);
        Trace.out(Trace.Level.INFO, "ServicePoint " + getServicePointName() + " adjusted employees to " + this.numEmployees);
    }

    public void checkBottleneck() {
        if (queue.size() > 15) {
            Trace.out(Trace.Level.WAR, "Bottleneck detected at Service Point " + getServicePointName() + " (Queue length: " + queue.size() + ").");
            adjustEmployees(numEmployees + 1);
        }
    }
}
