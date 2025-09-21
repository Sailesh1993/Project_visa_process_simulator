package simu.model;

import eduni.distributions.ContinuousGenerator;
import eduni.distributions.Normal;
import simu.framework.*;
import eduni.distributions.Negexp;

import java.util.Random;

/**
 * Main simulator engine.
 *
 * TODO: This is the place where you implement your own simulator
 *
 * Demo simulation case:
 * Simulate three service points, customer goes through all three service points to get serviced
 * 		--> SP1 --> SP2 --> SP3 -->
 */
public class MyEngine extends Engine {
	private ArrivalProcess arrivalProcess;
	private ServicePoint[] servicePoints;
	public static final boolean TEXTDEMO = true;
	public static final boolean FIXEDARRIVALTIMES = false;
	public static final boolean FIXEDSERVICETIMES = false;

    private Random randomGenerator;

    //counters
    private int totalApplications = 0;
    private int approvedCount = 0;
    private int rejectedCount = 0;
    private double totalSystemTime = 0.0;

    private double[] totalWaitingTimePerSP;         //per-service point waiting times

    /**
	 * Service Points and random number generator with different distributions are created here.
	 * We use exponent distribution for customer arrival times and normal distribution for the
	 * service times.
	 */
	public MyEngine() {
		servicePoints = new ServicePoint[6];
        randomGenerator = new Random(System.currentTimeMillis());

        totalWaitingTimePerSP = new double[servicePoints.length];

		if (TEXTDEMO) {
			/* special setup for the example in text
			 * https://github.com/jacquesbergelius/PP-CourseMaterial/blob/master/1.1_Introduction_to_Simulation.md
			 */
			Random r = new Random();

			ContinuousGenerator arrivalTime = null;
			if (FIXEDARRIVALTIMES) {
				/* version where the arrival times are constant (and greater than service times) */

				// make a special "random number distribution" which produces constant value for the customer arrival times
				arrivalTime = new ContinuousGenerator() {
					@Override
					public double sample() {
						return 10;
					}

					@Override
					public void setSeed(long seed) {
					}

					@Override
					public long getSeed() {
						return 0;
					}

					@Override
					public void reseed() {
					}
				};
			} else
				// exponential distribution is used to model customer arrivals times, to get variability between programs runs, give a variable seed
				arrivalTime = new Negexp(10, Integer.toUnsignedLong(r.nextInt()));

			ContinuousGenerator serviceTime = null;
			if (FIXEDSERVICETIMES) {
				// make a special "random number distribution" which produces constant value for the service time in service points
				serviceTime = new ContinuousGenerator() {
					@Override
					public double sample() {
						return 9;
					}

					@Override
					public void setSeed(long seed) {
					}

					@Override
					public long getSeed() {
						return 0;
					}

					@Override
					public void reseed() {
					}
				};
			} else
				// normal distribution used to model service times
				serviceTime = new Normal(10, 6, Integer.toUnsignedLong(r.nextInt()));

            servicePoints[0] = new ServicePoint(serviceTime, eventList, EventType.END_APPLICATION_ENTRY); // SP1
            servicePoints[1] = new ServicePoint(serviceTime, eventList, EventType.END_DOC_SUBMISSION);    // SP2
            servicePoints[2] = new ServicePoint(serviceTime, eventList, EventType.END_BIOMETRICS);        // SP2a: Biometrics
            servicePoints[3] = new ServicePoint(serviceTime, eventList, EventType.MISSING_DOCS_RESOLVED);   // SP2b: Missing Docs Waiting
            servicePoints[4] = new ServicePoint(serviceTime, eventList, EventType.END_DOC_CHECK);         // SP3
            servicePoints[5] = new ServicePoint(serviceTime, eventList, EventType.END_DECISION);          // SP4

            arrivalProcess = new ArrivalProcess(arrivalTime, eventList, EventType.ARRIVAL);

		} else {
			/* more realistic simulation case with variable customer arrival times and service times */
			servicePoints[0] = new ServicePoint(new Normal(10, 6), eventList, EventType.END_APPLICATION_ENTRY);
			servicePoints[1] = new ServicePoint(new Normal(10, 10), eventList, EventType.END_DOC_SUBMISSION);
			servicePoints[2] = new ServicePoint(new Normal(5, 3), eventList, EventType.END_BIOMETRICS);
            servicePoints[3] = new ServicePoint(new Normal(5, 3), eventList, EventType.MISSING_DOCS_RESOLVED);
            servicePoints[4] = new ServicePoint(new Normal(5, 3), eventList, EventType.END_DOC_CHECK);
            servicePoints[5] = new ServicePoint(new Normal(5, 3), eventList, EventType.END_DECISION);

			arrivalProcess = new ArrivalProcess(new Negexp(15, 5), eventList, EventType.ARRIVAL);
		}
	}

	@Override
	protected void initialize() {	// First arrival in the system
		arrivalProcess.generateNextEvent();
	}

	@Override
	protected void runEvent(Event t) {  // B phase events
        ApplicationAsCustomer application;

        switch ((EventType) t.getType()) {
            case ARRIVAL:
                boolean isNew = randomGenerator.nextDouble() < 0.65;      //65% chance of being a new application
                boolean docsComplete = randomGenerator.nextDouble() < 0.8;        //80% chance of having all documents complete
                servicePoints[0].addQueue(new ApplicationAsCustomer(isNew, docsComplete));
                arrivalProcess.generateNextEvent();
                break;

            case END_APPLICATION_ENTRY:             // SP1 done, move to SP2
                application = servicePoints[0].removeQueue();
                servicePoints[1].addQueue(application);
                break;

            case END_DOC_SUBMISSION:                                // SP1 done, move to SP4 or SP3 or SP2
                application = servicePoints[1].removeQueue();
                if (application.requiresBiometrics()) {
                    servicePoints[2].addQueue(application);         // Move to SP2a: Biometrics
                } else if (!application.isDocsComplete()) {
                    servicePoints[3].addQueue(application);         // Move to SP2b: Missing Docs Waiting
                } else {
                    servicePoints[4].addQueue(application);         // Move to SP4: Document Check
                }
                break;

            case END_BIOMETRICS:
                application = servicePoints[2].removeQueue();
                servicePoints[4].addQueue(application);             // move to Doc Check
                break;

            case MISSING_DOCS_RESOLVED:
                application = servicePoints[3].removeQueue();
                servicePoints[4].addQueue(application);             // move to Doc Check
                break;

            case END_DOC_CHECK:
                application = servicePoints[4].removeQueue();
                servicePoints[5].addQueue(application); // move to Decision Room
                break;

            case END_DECISION:
                application = servicePoints[5].removeQueue();
                application.setRemovalTime(Clock.getInstance().getClock());

                boolean approved = randomGenerator.nextDouble() < 0.7; // 70% chance of approval
                application.setApproved(approved);

                totalApplications++;
                // Schedule exit events
                if (approved) {
                    eventList.add(new Event(EventType.EXIT_APPROVED, Clock.getInstance().getClock()));
                    approvedCount++;
                } else {
                    eventList.add(new Event(EventType.EXIT_REJECTED, Clock.getInstance().getClock()));
                    rejectedCount++;
                }

                totalSystemTime += application.getRemovalTime() + application.getArrivalTime();
                application.reportResults();
                break;

            case EXIT_APPROVED:
            case EXIT_REJECTED:
                // Customer leaves system, stats already collected
                break;
        }
    }

	@Override
	protected void tryCEvents() {
		for (ServicePoint p: servicePoints){
			if (!p.isReserved() && p.isOnQueue()){
				p.beginService();
			}
		}
	}

	@Override
	protected void results() {

        double avgTimeInSystem = totalApplications > 0 ? totalSystemTime / totalApplications : 0;
        System.out.println();
        System.out.println("*---------------------------------------------------------------------------------*");
        System.out.printf("Simulation ended at %.2f%n", Clock.getInstance().getClock());
        System.out.println("****** Simulation Results ******");
        System.out.println("Total applications processed: " + totalApplications);
        System.out.println("Approved applications: " + approvedCount);
        System.out.println("Rejected applications: " + rejectedCount);
        System.out.printf("Average time in system: %.2f%n", avgTimeInSystem);
	}
}
