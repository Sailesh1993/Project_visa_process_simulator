package MVC.simu.framework;

import MVC.controller.IControllerMtoV;
import MVC.simu.model.ServicePoint;

/**
 * Abstract base class for the simulation engine.
 * <p>
 * The {@code Engine} class controls the overall execution of a discrete-event
 * simulation. It manages the event list, simulation clock, timing loop, and
 * coordination between {@link ServicePoint}s and the controller ({@link IControllerMtoV}).
 * <p>
 * Subclasses such as {@link MVC.simu.model.MyEngine} define the domain-specific
 * behavior of events, initialization, and results processing.
 *
 * <h3>Core responsibilities:</h3>
 * <ul>
 *   <li>Maintains the simulation clock and event list</li>
 *   <li>Executes events in chronological order</li>
 *   <li>Manages service points and their queues</li>
 *   <li>Provides pause, resume, and stop functionality for GUI control</li>
 *   <li>Delegates specific event handling to subclass-defined methods</li>
 * </ul>
 */
public abstract class Engine extends Thread implements IEngine {

    /** The total simulation time (stopping condition). */
	private double simulationTime = 0;

    /** Delay (in milliseconds) between simulation events for GUI visualization. */
	private long delay = 0;

    /** Shared simulation clock instance. */
	private Clock clock;

    /** Flag indicating whether the simulation is paused. */
    private volatile boolean paused = false;

    /** Flag indicating whether the simulation is stopped. */
    private volatile boolean stopped = false;

    /** The global event List used to store and process simulation events. */
	protected EventList eventList;

    /** Array of service points representing different processing stages in the system. */
	protected ServicePoint[] servicePoints;

    /** Controller interface for updating the GUI and communicating with the view layer. */
	protected IControllerMtoV controller; // NEW

    /**
     * Constructs a new {@code Engine} instance with the given controller.
     * Initializes the simulation clock and event list.
     * <p>
     * The service points themselves are created by subclasses such as {@link MVC.simu.model.MyEngine}.
     *
     * @param controller the controller interface used for GUI updates and view synchronization
     */
	public Engine(IControllerMtoV controller) {
		this.controller = controller;
		clock = Clock.getInstance();
		eventList = new EventList();
	}

    /**
     * Sets the total simulation time.
     *
     * @param time the time at which the simulation should stop
     */
	@Override
	public void setSimulationTime(double time) {
		simulationTime = time;
	}

    /**
     * Sets the delay (in milliseconds) between simulation events.
     * Used to slow down the simulation for visualization in the GUI.
     *
     * @param time the delay duration in milliseconds
     */
	@Override // NEW
	public void setDelay(long time) {
		this.delay = time;
	}

    /**
     * Returns the delay between events in milliseconds.
     *
     * @return the current delay value
     */
	@Override // NEW
	public long getDelay() {
		return delay;
	}

    /**
     * Main simulation loop.
     * <p>
     * Initializes the simulation, then repeatedly processes events
     * until the simulation time limit is reached or no events remain.
     * Supports pausing, resuming, and stopping via GUI controls.
     */
    @Override
    public void run() {
        initialization();

        while (simulate() && !stopped) {

            // Check if paused
            synchronized(this) {
                while (paused && !stopped) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        stopped = true;
                        break;
                    }
                }
            }

            if (stopped) break;

            delay();
            clock.setTime(currentTime());
            runBEvents();
            tryCEvents();
        }

        results();
    }

    /**
     * Executes all events scheduled for the current simulation time.
     */
	private void runBEvents() {
		while (eventList.getNextTime() == clock.getTime()){
			runEvent(eventList.remove());
		}
	}

    /**
     * * Checks each {@link ServicePoint} for waiting customers and starts service if idle.
     * <p>
     * Subclasses may override this method to implement custom service-start logic.
     */
	protected void tryCEvents() {
		for (ServicePoint p: servicePoints){
			if (p.isReserved() && p.isOnQueue()){
				p.beginService();
			}
		}
	}

    /**
     * Returns the next event time from the event list.
     *
     * @return the next scheduled event time
     */
	private double currentTime(){
		return eventList.getNextTime();
	}

    /**
     * Determines whether the simulation should continue running.
     *
     * @return {@code true} if there are remaining events and time left, {@code false} otherwise
     */
    private boolean simulate() {
        Trace.out(Trace.Level.INFO, "Time is: " + clock.getTime());

        // Force stop if we've reached simulation time
        if (clock.getTime() >= simulationTime) {
            return false;
        }

        // Also stop if event list is empty (nothing left to do)
        // Also stop if event list is empty (nothing left to do)
        if (eventList.isEmpty()) {
            Trace.out(Trace.Level.INFO, "Event list empty at time " + clock.getTime());
            return false;
        }

        return true;
    }

    /**
     * Delays execution for the configured {@code delay} time to control simulation speed.
     */
	private void delay() { // NEW
		Trace.out(Trace.Level.INFO, "Delay " + delay);
		try {
			sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

    // ======================
    // Control Methods (for GUI)
    // ======================

    /**
     * Pauses the simulation loop.
     * <p>
     * The engine remains in a waiting state until {@link #resume()} is called.
     */
    public void pause() {
        paused = true;
    }

    /**
     * Resumes the simulation loop after being paused.
     */
    public void resume() {
        paused = false;
        synchronized(this) {
            notifyAll();
        }
    }

    /**
     * Stops the simulation permanently.
     * Once stopped, it cannot be resumed.
     */
    public void stopSimulation() {
        stopped = true;
    }

    /**
     * Returns whether the simulation has been stopped.
     *
     * @return {@code true} if stopped, {@code false} otherwise
     */
    public boolean isStopped() {
        return stopped;
    }

    // ======================
    // Abstract Methods
    // ======================

    /**
     * Defines the initialization logic for the simulation.
     * <p>
     * Must be implemented by subclasses (e.g., create arrival processes,
     * initialize queues, generate first events, etc.).
     */
	protected abstract void initialization();

    /**
     * Defines how a specific event should be processed.
     * <p>
     * Each subclass implements this method to handle domain-specific event behavior.
     *
     * @param t the event to be executed
     */
	protected abstract void runEvent(Event t);

    /**
     * Defines how the simulation results should be processed after the run completes.
     * <p>
     * Typically includes aggregating statistics, detecting bottlenecks,
     * and persisting results to the database or GUI.
     */
	protected abstract void results();
}