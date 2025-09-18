package simu.model;

import simu.framework.*;

/**
 * ApplicationAsCustomer in a simulator
 *
 * TODO: This is to be implemented according to the requirements of the simulation model (data!)
 */
public class ApplicationAsCustomer {
	private double arrivalTime;
	private double removalTime;
	private int id;
	private static int idCounter = 1;
	private static long totalSystemTime = 0;

    private boolean newApplication;
    private boolean docsComplete;
    private boolean approved;
    private EventType currentStage;

    private double timeInBiometrics;
    private double timeInWaitingRoom;

	//Create a unique customer

	public ApplicationAsCustomer(boolean newApplication, boolean docsComplete) {
	    this.id = idCounter++;
        this.newApplication = newApplication;
        this.docsComplete = docsComplete;
		this.arrivalTime = Clock.getInstance().getClock();
        this.currentStage = EventType.ARRIVAL;

		Trace.out(Trace.Level.INFO,
                "New application #" + id + " arrived at  " + arrivalTime +
                " | New application: " + newApplication +
                " | Documents complete: " + docsComplete);
	}

    public int getId() {
        return id;
    }       //Get unique customer id

    public double getArrivalTime() {
        return arrivalTime;
    }       //Get customer arrival time for simulation

	public double getRemovalTime() {
		return removalTime;
	}           // Get time when customer has been removed (from the system to be simulated)

	public void setRemovalTime(double removalTime) {
		this.removalTime = removalTime;
	}           //Mark the time when the customer has been removed (from the system to be simulated)

	public void setArrivalTime(double arrivalTime) {
		this.arrivalTime = arrivalTime;
	}           //Mark the time when the customer arrived to the system to be simulated

	public boolean isNewApplication() {
        return newApplication;
    }

    public boolean isDocsComplete() {
        return docsComplete;
    }

    public void setDocsComplete(boolean docsComplete) {
        this.docsComplete = docsComplete;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public EventType getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(EventType currentStage) {
        this.currentStage = currentStage;
    }

    public double getTimeInBiometrics() {
        return timeInBiometrics;
    }

    public void setTimeInBiometrics(double time) {
        this.timeInBiometrics = time;
    }

    public double getTimeInWaitingRoom() {
        return timeInWaitingRoom;
    }

    public void setTimeInWaitingRoom(double time) {
        this.timeInWaitingRoom = time;
    }

	//Report the measured variables of the customer. In this case to the diagnostic output.

	public void reportResults() {
		Trace.out(Trace.Level.INFO, "\nApplication " + id + " is processed! ");
		Trace.out(Trace.Level.INFO, "Application " + id + " arrived: " + arrivalTime);
		Trace.out(Trace.Level.INFO,"Application " + id + " removed: " + removalTime);
        Trace.out(Trace.Level.INFO,"Total time in system: " + id + " "  + (removalTime - arrivalTime));

        Trace.out(Trace.Level.INFO,"Is this new application? " + newApplication);
        Trace.out(Trace.Level.INFO,"Are the required documents complete? " + docsComplete);
        Trace.out(Trace.Level.INFO,"Visa decision: " + (approved ? "Approved" : "Denied"));

        totalSystemTime += (removalTime - arrivalTime);
		double mean = totalSystemTime/id;
		System.out.println("Current mean of the customer service times " + mean);
	}
}
