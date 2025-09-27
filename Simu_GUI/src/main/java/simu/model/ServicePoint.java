package simu.model;

import eduni.distributions.ContinuousGenerator;
import simu.framework.Clock;
import simu.framework.Event;
import simu.framework.EventList;

import java.util.LinkedList;

import controller.IControllerMtoV;
import simu.framework.Trace;

// TODO:
// Service Point functionalities & calculations (+ variables needed) and reporting to be implemented
public class ServicePoint {
	private LinkedList<ApplicationAsCustomer> queue = new LinkedList<ApplicationAsCustomer>(); // Data Structure used
	private ContinuousGenerator generator;
	private EventList eventList;
	private EventType eventTypeScheduled;

	private boolean reserved = false;

    // Measurement variables
    private int totalDepartures = 0;
    private double totalWaitingTime = 0.0;
    private int maxQueueLength = 0;
    private double busyTime = 0.0; // Total time SP was busy.
    private double lastServiceStart = 0.0;              // for utilization tracking
    private int numEmployees = 1;

    private IControllerMtoV controller;

	public ServicePoint(ContinuousGenerator generator, EventList eventList, EventType type, IControllerMtoV controller){
		this.eventList = eventList;
		this.generator = generator;
		this.eventTypeScheduled = type;
        this.controller = controller;
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

	public synchronized void addQueue(ApplicationAsCustomer application){   // First customer at the queue is always on the service
        application.setTimeEnteredQueue(Clock.getInstance().getTime());
        queue.add(application);
        maxQueueLength = Math.max(maxQueueLength, queue.size());

        controller.updateQueueStatus(getServicePointId(), queue.size());            //Update uI with current queue size

        controller.visualiseCustomer();                                             //Update visualization
        checkBottleneck();
    }

	public ApplicationAsCustomer removeQueue(){		// Remove serviced customer
        if (queue.isEmpty()) return null;

        ApplicationAsCustomer application = queue.poll();
        reserved = false;
        totalDepartures++;

        double now = Clock.getInstance().getTime();

        // Accumulate busy time only for the period the service was actually active
        if (lastServiceStart > 0) {
            double serviceDuration = now - lastServiceStart;
            if (serviceDuration > 0) {
                busyTime += serviceDuration;
            }
            lastServiceStart = 0;                   // reset for next service
        }

        controller.updateQueueStatus(getServicePointId(), queue.size());            //Update uI with current queue size
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
        double serviceTime = generator.sample();
        serviceTime = Math.max(1e-6, serviceTime);

        lastServiceStart = Clock.getInstance().getTime();              //Track when service starts for utilization tracking

        // Schedule service completion event
        eventList.add(new Event(eventTypeScheduled, Clock.getInstance().getTime()+serviceTime));

        // Safe employee adjustment based on queue size
        int desiredEmployees = Math.min(Math.max(1, queue.size()), 10);                     // 1 to 10 employees
        if (numEmployees != desiredEmployees) {
            adjustEmployees(desiredEmployees);
        }
	}

	public boolean isReserved(){
		return reserved;
	}

	public boolean isOnQueue(){
		return !queue.isEmpty();
	}

    public int getQueueSize() {return queue.size();}

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
    }

    public void checkBottleneck() {
        if (queue.size() > 15) {
            adjustEmployees(numEmployees + 1);
        }
    }

    // Utility method to determine the service point ID
    private int getServicePointId() {
        switch (eventTypeScheduled) {
            case END_APPLICATION_ENTRY: return 0; // SP1
            case END_DOC_SUBMISSION: return 1;    // SP2
            case END_BIOMETRICS: return 2;        // SP3
            case MISSING_DOCS_RESOLVED: return 3;  // SP4
            case END_DOC_CHECK: return 4;         // SP5
            case END_DECISION: return 5;          // SP6
            default: return -1;
        }
    }
}
