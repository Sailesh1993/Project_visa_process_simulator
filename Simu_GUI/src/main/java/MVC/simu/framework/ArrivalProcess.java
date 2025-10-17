package MVC.simu.framework;

import MVC.simu.model.EventType;
import eduni.distributions.*;

/**
 * Represents the arrival process in a discrete-event simulation.
 * <p>
 * The {@code ArrivalProcess} is responsible for generating new arrival events
 * based on a given probability distribution (for example, exponential or normal).
 * Each generated event is added to the global {@link EventList} to be processed
 * later by the simulation engine.
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Samples inter-arrival times from a {@link ContinuousGenerator}</li>
 *   <li>Schedules new {@link Event}s of a specific {@link EventType}</li>
 *   <li>Uses the shared {@link Clock} to determine the current simulation time</li>
 * </ul>
 *
 * <p>
 * Typically used by the simulation model {@code MyEngine} to create
 * incoming customers, applications, or entities that enter the system.
 */
public class ArrivalProcess {

    /** Random number generator used to sample inter-arrival times. */
	private ContinuousGenerator generator;

    /** Reference to the global event list. */
	private EventList eventList;

    /** The type of event to generate (e.g., {@code EventType.ARRIVAL}). */
	private EventType type;

    /**
     * Constructs a new {@code ArrivalProcess} with the given generator, event list, and event type.
     *
     * @param g the continuous random generator providing inter-arrival times
     * @param tl the global event list to which new events are added
     * @param type the event type associated with each generated arrival
     */
	public ArrivalProcess(ContinuousGenerator g, EventList tl, EventType type) {
		this.generator = g;
		this.eventList = tl;
		this.type = type;
	}

    /**
     * Generates the next arrival event and schedules it in the event list.
     * <p>
     * The new event's time is calculated as the current simulation time
     * plus a random sample from the inter-arrival time distribution.
     */
	public void generateNext() {
		Event t = new Event(type, Clock.getInstance().getTime() + generator.sample());
		eventList.add(t);
	}
}
