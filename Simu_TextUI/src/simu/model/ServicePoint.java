
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
    private boolean reserved = false;

    // Measurement variables
    private int totalServed = 0;
    private double totalWaitingTime = 0.0;
    private int maxQueueLength = 0;
    private double busyTime = 0.0; // Total time SP was busy.
    private double lastServiceStart = 0.0;              // for utilization tracking

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
    /**
     * Add a customer to the service point queue.
     *
     * @param a Customer to be queued
     */
    public void addQueue(ApplicationAsCustomer a) {	// The first customer of the queue is always in service
        queue.add(a);
        maxQueueLength = Math.max(maxQueueLength, queue.size());
    }

    /**
     * Remove customer from the waiting queue.
     * Here we calculate also the appropriate measurement values.
     *
     * @return Customer retrieved from the waiting queue
     */
    public ApplicationAsCustomer removeQueue() {		// Remove serviced customer
        reserved = false;
        totalServed++;
        ApplicationAsCustomer a = queue.poll();
        busyTime += (Clock.getInstance().getClock() - lastServiceStart);
        return a;
    }

    /**
     * Begins a new service, customer is on the queue during the service
     * Inserts a new event to the event list when the service should be ready.
     */
    public void beginService() {		// Begins a new service, customer is on the queue during the service
        if (queue.isEmpty()) return;
        ApplicationAsCustomer a = queue.peek();
        reserved = true;
        // Calculate waiting time for this application
        double waitingTime = Clock.getInstance().getClock() - a.getArrivalTime();
        totalWaitingTime += waitingTime;
        lastServiceStart = Clock.getInstance().getClock();
        // Schedule service completion event
        double serviceTime = generator.sample();
        eventList.add(new Event(eventTypeScheduled, Clock.getInstance().getClock()+serviceTime));
        Trace.out(Trace.Level.INFO,"Service Point(" + eventTypeScheduled + ")");
        Trace.out(Trace.Level.INFO, "-->Started service for Application #" + a.getId() + " | Waiting time: " + Trace.formatTime(waitingTime)  + " minutes" + " | Service time: " + Trace.formatTime(serviceTime) + " minutes" );
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
    public int getTotalServed() {
        return totalServed;
    }


    public double getAverageWaitingTime(){
        return totalServed > 0 ? totalWaitingTime / totalServed : 0.0;
    }

    //
    public int getMaxQueueLength(){
        return maxQueueLength;
    }

    //how much time individual customer spent time in service point?
    public double getUtilization(double simulationTime){
        return simulationTime > 0 ? busyTime / simulationTime: 0.0;
    }
}
