package simu.framework;

import java.util.PriorityQueue;

/**
 * Eventlist holds events according to time of the event.
 * Event with the smallest time will be retrieved first.
 */
public class EventList {
	private PriorityQueue<Event> eventlist;
	
	public EventList() {
		eventlist = new PriorityQueue<>();
	}

	/**
	 * Retrieve the next event from the list
	 *
	 * @return The next event, null is the event list is empty
	 */
	public Event remove() {
		Trace.out(Trace.Level.INFO,"Removing from the event list " + eventlist.peek().getType() + " " + eventlist.peek().getTime());
		return eventlist.remove();
	}

	/**
	 * Add a new event to the list
	 *
	 * @param t Event to be inserted to the list
	 */
	public void add(Event t) {
		Trace.out(Trace.Level.INFO,"Adding to the event list " + t.getType() + " " + t.getTime());
		eventlist.add(t);
	}

	/**
	 * Check the time of the next event. This does not retrieve the event from the list.
	 *
	 * @return Time of the event
	 */
	public double getNextEventTime(){
		return eventlist.peek().getTime();
	}
}
