package simu.framework;

import eduni.distributions.ContinuousGenerator;

/**
 * ArrivalProcess produces the time when next customer arrivals. This is based on the
 * current time and random number
 */
public class ArrivalProcess {
	private ContinuousGenerator generator;
	private EventList eventList;
	private IEventType type;

	/**
	 * Create the service point with a waiting queue.
	 *
	 * @param g Random number generator for customer arrival time simulation
	 * @param tl Simulator event list, needed for the insertion of customer arrival event
	 * @param type Event type for the customer arrival event
	 */
	public ArrivalProcess(ContinuousGenerator g, EventList tl, IEventType type) {
		this.generator = g;
		this.eventList = tl;
		this.type = type;
	}

	/**
	 * Create a new customer (Generate customer arrival event and put it to the event list)
	 */
	public void generateNextEvent() {
		Event t = new Event(type, Clock.getInstance().getClock() + generator.sample());
		eventList.add(t);
	}
}
