package simu.model;

import simu.framework.Clock;
import simu.framework.Trace;

// TODO:
// ApplicationAsCustomer to be implemented according to the requirements of the simulation model (data!)
public class ApplicationAsCustomer {
	private double arrivalTime;
	private double removalTime;
	private int id;
	private static int i = 1;
	private static long sum = 0;
	
	public ApplicationAsCustomer() {
	    id = i++;
	    
		arrivalTime = Clock.getInstance().getTime();
		Trace.out(Trace.Level.INFO, "New customer #" + id + " arrived at  " + arrivalTime);
	}

	public double getRemovalTime() {
		return removalTime;
	}

	public void setRemovalTime(double removalTime) {
		this.removalTime = removalTime;
	}

	public double getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(double arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	
	public void reportResults() {
		Trace.out(Trace.Level.INFO, "\nApplicationAsCustomer " + id + " ready! ");
		Trace.out(Trace.Level.INFO, "ApplicationAsCustomer "   + id + " arrived: " + arrivalTime);
		Trace.out(Trace.Level.INFO,"ApplicationAsCustomer "    + id + " removed: " + removalTime);
		Trace.out(Trace.Level.INFO,"ApplicationAsCustomer "    + id + " stayed: "  + (removalTime - arrivalTime));

		sum += (removalTime - arrivalTime);
		double mean = sum/id;
		System.out.println("Current mean of the customer service times " + mean);
	}

}
