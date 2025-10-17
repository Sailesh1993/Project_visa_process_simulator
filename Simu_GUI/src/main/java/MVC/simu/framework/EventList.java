package MVC.simu.framework;

import java.util.PriorityQueue;

/**
 * Maintains a prioritized list of future simulation events.
 * <p>
 * The {@code EventList} stores {@link Event} objects in chronological order,
 * ensuring that the next event to be processed always has the smallest time value.
 * It acts as the central scheduling mechanism for the simulation engine.
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Stores events in a time-ordered queue</li>
 *   <li>Provides access to the next event time</li>
 *   <li>Supports adding, removing, and checking for remaining events</li>
 * </ul>
 *
 * <p>
 * Typically used by {@link Engine} and other components to schedule and retrieve events
 * during simulation execution.
 */
public class EventList {

    /** Priority queue that stores events sorted by their scheduled time. */
    private PriorityQueue<Event> lista = new PriorityQueue<Event>();

    /** Constructs an empty {@code EventList}. */
    public EventList() {}

    /**
     * Removes and returns the next event (the one with the smallest time).
     *
     * @return the next scheduled {@link Event}
     * @throws java.util.NoSuchElementException if the list is empty
     */
    public Event remove(){
        return lista.remove();
    }

    /**
     * Adds a new event to the event list.
     *
     * @param t the {@link Event} to be added
     */
    public void add(Event t){
        lista.add(t);
    }

    /**
     * Returns the time of the next scheduled event without removing it.
     *
     * @return the time of the next event
     */
    public double getNextTime(){
        return lista.peek().getTime();
    }

    /**
     * Checks whether the event list is empty.
     *
     * @return {@code true} if no events remain, {@code false} otherwise
     */
    public boolean isEmpty() {
        return lista.isEmpty();
    }
}