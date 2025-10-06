package simu.framework;

import controller.IControllerMtoV;
import simu.model.ServicePoint;

public abstract class Engine extends Thread implements IEngine {  // NEW DEFINITIONS
	private double simulationTime = 0;	// time when the simulation will be stopped
	private long delay = 0;
	private Clock clock;
    private volatile boolean paused = false;
    private volatile boolean stopped = false;// in order to simplify the code (clock.getClock() instead Clock.getInstance().getClock())
	
	protected EventList eventList;
	protected ServicePoint[] servicePoints;
	protected IControllerMtoV controller; // NEW

	public Engine(IControllerMtoV controller) {	// NEW
		this.controller = controller;  			// NEW
		clock = Clock.getInstance();
		eventList = new EventList();
		/* Service Points are created in simu.model-package's class who is inheriting the Engine class */
	}

	@Override
	public void setSimulationTime(double time) {
		simulationTime = time;
	}
	
	@Override // NEW
	public void setDelay(long time) {
		this.delay = time;
	}
	
	@Override // NEW
	public long getDelay() {
		return delay;
	}

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

	
	private void runBEvents() {
		while (eventList.getNextTime() == clock.getTime()){
			runEvent(eventList.remove());
		}
	}

	protected void tryCEvents() {    // define protected, if you want to overwrite
		for (ServicePoint p: servicePoints){
			if (!p.isReserved() && p.isOnQueue()){
				p.beginService();
			}
		}
	}

	private double currentTime(){
		return eventList.getNextTime();
	}

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

	private void delay() { // NEW
		Trace.out(Trace.Level.INFO, "Delay " + delay);
		try {
			sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
    //For buttons in simulation page
    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
        synchronized(this) {
            notifyAll();
        }
    }
    public void stopSimulation() {
        stopped = true;
    }

    public boolean isStopped() {
        return stopped;
    }

	protected abstract void initialization(); 	// Defined in simu.model-package's class who is inheriting the Engine class
	protected abstract void runEvent(Event t);	// Defined in simu.model-package's class who is inheriting the Engine class
	protected abstract void results(); 			// Defined in simu.model-package's class who is inheriting the Engine class
}