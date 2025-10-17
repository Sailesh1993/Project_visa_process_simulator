package MVC.simu.framework;

/**
 * Represents a scheduled event within the simulation.
 * <p>
 * Each {@code Event} contains a specific {@link IEventType type} and a timestamp
 * indicating when it will occur in simulation time. Events are stored and ordered
 * within the {@link EventList}, which executes them chronologically.
 * </p>
 *
 * <p>Events are comparable by their scheduled time, enabling time-based ordering
 * in a priority queue.</p>
 */
public class Event implements Comparable<Event> {
	private IEventType type;
	private double time;

    /**
     * Constructs a new {@code Event} with the given type and scheduled time.
     *
     * @param type the {@link IEventType} representing what kind of event this is
     * @param time the simulation time at which the event will occur
     */
	public Event(IEventType type, double time) {
		this.type = type;
		this.time = time;
	}

    /**
     * Sets the event type.
     *
     * @param type the {@link IEventType} to assign to this event
     */
	public void setType(IEventType type) {
		this.type = type;
	}

    /**
     * Returns the type of this event.
     *
     * @return the {@link IEventType} of this event
     */
	public IEventType getType() {
		return type;
	}

    /**
     * Sets the scheduled simulation time of this event.
     *
     * @param time the simulation time when this event should occur
     */
	public void setTime(double time) {
		this.time = time;
	}

    /**
     * Returns the scheduled simulation time of this event.
     *
     * @return the time at which this event is set to occur
     */
	public double getTime() {
		return time;
	}

    /**
     * Compares this event to another based on their scheduled times.
     * <p>
     * Used for sorting events chronologically in the {@link java.util.PriorityQueue}.
     * </p>
     *
     * @param arg the other {@code Event} to compare to
     * @return a negative integer if this event occurs earlier, a positive integer
     *         if later, or zero if they occur at the same time
     */
	@Override
	public int compareTo(Event arg) {
		if (this.time < arg.time) return -1;
		else if (this.time > arg.time) return 1;
		return 0;
	}
}
