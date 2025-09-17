package simu.model;

import simu.framework.*;

/**
 * Customer in a simulator
 *
 * TODO: This is to be implemented according to the requirements of the simulation model (data!)
 */
public class Customer {
	private double arrivalTime;
	private double removalTime;
	private int id;
	private static int i = 1;
	private static long sum = 0;

	/**
	 * Create a unique customer
	 */
	public Customer() {
	    id = i++;
	    
		arrivalTime = Clock.getInstance().getClock();
		Trace.out(Trace.Level.INFO, "New customer #" + id + " arrived at  " + arrivalTime);
	}

	/**
	 * Give the time when customer has been removed (from the system to be simulated)
	 * @return Customer removal time
	 */
	public double getRemovalTime() {
		return removalTime;
	}

	/**
	 * Mark the time when the customer has been removed (from the system to be simulated)
	 * @param removalTime Customer removal time
	 */
	public void setRemovalTime(double removalTime) {
		this.removalTime = removalTime;
	}

	/**
	 * Give the time when the customer arrived to the system to be simulated
	 * @return Customer arrival time
	 */
	public double getArrivalTime() {
		return arrivalTime;
	}

	/**
	 * Mark the time when the customer arrived to the system to be simulated
	 * @param arrivalTime Customer arrival time
	 */
	public void setArrivalTime(double arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	/**
	 * Get the (unique) customer id
	 * @return Customer id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Report the measured variables of the customer. In this case to the diagnostic output.
	 */
	public void reportResults() {
		Trace.out(Trace.Level.INFO, "\nCustomer " + id + " ready! ");
		Trace.out(Trace.Level.INFO, "Customer "   + id + " arrived: " + arrivalTime);
		Trace.out(Trace.Level.INFO,"Customer "    + id + " removed: " + removalTime);
		Trace.out(Trace.Level.INFO,"Customer "    + id + " stayed: "  + (removalTime - arrivalTime));

		sum += (removalTime - arrivalTime);
		double mean = sum/id;
		System.out.println("Current mean of the customer service times " + mean);
	}
}
