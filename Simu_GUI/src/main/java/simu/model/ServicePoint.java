package simu.model;

import eduni.distributions.ContinuousGenerator;
import simu.framework.Clock;
import simu.framework.Event;
import simu.framework.EventList;

import java.util.LinkedList;

// TODO:
// Service Point functionalities & calculations (+ variables needed) and reporting to be implemented
public class ServicePoint {
	private LinkedList<ApplicationAsCustomer> queue = new LinkedList<ApplicationAsCustomer>(); // Data Structure used
	private ContinuousGenerator generator;
	private EventList eventList;
	private EventType eventTypeScheduled;
	//Queuestrategy strategy; // option: ordering of the customer
	private boolean reserved = false;

	public ServicePoint(ContinuousGenerator generator, EventList eventList, EventType type){
		this.eventList = eventList;
		this.generator = generator;
		this.eventTypeScheduled = type;
				
	}

	public void addQueue(ApplicationAsCustomer application){   // First customer at the queue is always on the service
        queue.add(application);
	}

	public ApplicationAsCustomer removeQueue(){		// Remove serviced customer
		reserved = false;
		return queue.poll();
	}

	public void beginService() {  		// Begins a new service, customer is on the queue during the service
		reserved = true;
		double serviceTime = generator.sample();
		eventList.add(new Event(eventTypeScheduled, Clock.getInstance().getTime()+serviceTime));
	}

	public boolean isReserved(){
		return reserved;
	}

	public boolean isOnQueue(){
		return queue.size() != 0;
	}
}
