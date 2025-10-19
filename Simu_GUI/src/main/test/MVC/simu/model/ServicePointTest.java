package MVC.simu.model;

import MVC.controller.IControllerMtoV;
import MVC.simu.framework.EventList;
import MVC.simu.framework.Trace;
import MVC.view.IVisualisation;
import eduni.distributions.ContinuousGenerator;
import eduni.distributions.Negexp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServicePointTest {
    private ServicePoint servicePoint;
    private EventList eventList;
    private EventType eventType;
    private IControllerMtoV controller;

    /**
     * Sets up a new {@link ServicePoint} instance and its dependencies before each test.
     * Initializes the trace level, event list, event type, and controller stub.
     */
    @BeforeEach
    void setUp() {

        // Initialize Trace.traceLevel to avoid NullPointerException
        Trace.setTraceLevel(Trace.Level.INFO);
        // Use a real generator
        ContinuousGenerator generator = new Negexp(5.0);

        // Use fakes/stubs for other dependencies
        eventList = new EventList();
        eventType = EventType.ARRIVAL; // Use a real enum value

        controller = new IControllerMtoV() {
            @Override
            public void showEndTime(double time) {}

            @Override
            public void visualiseCustomer() {}

            @Override
            public void updateQueueStatus(int servicePointId, int queueSize) {}

            @Override
            public void displayResults(String resultsText) {}

            @Override
            public void updateStatistics(int totalApps, int approved, int rejected, double avgTime, double currentTime) {}

            @Override
            public IVisualisation getVisualisation() {return null;}
        };

        servicePoint = new ServicePoint(generator, eventList, eventType, controller);
    }

    /**
     * Verifies that adding to the queue increases the queue size.
     */
    @Test
    void testAddQueueIncreasesQueueSize() {
        int numEmployees = servicePoint.getNumEmployees();
        for (int i = 0; i < numEmployees + 1; i++) {
            servicePoint.addQueue(new ApplicationAsCustomer(true, true));
        }
        assertTrue(servicePoint.getQueueSize() > 0);
    }

    /**
     * Verifies that removing from the queue decreases the queue size.
     */
    @Test
    void testRemoveQueueDecreasesQueueSize() {
        ApplicationAsCustomer app = new ApplicationAsCustomer(true, false);
        servicePoint.addQueue(app);
        servicePoint.removeQueue();
        assertEquals(0, servicePoint.getQueueSize());
    }

    /**
     * Checks that {@link ServicePoint#isReserved()} returns true when the queue is full.
     */
    @Test
    void testIsReservedReturnsTrueWhenFull() {
        for (int i = 0; i < servicePoint.getNumEmployees(); i++) {
            servicePoint.addQueue(new ApplicationAsCustomer(false, true));
        }
        assertTrue(servicePoint.isReserved());
    }

    /**
     * Checks that {@link ServicePoint#isOnQueue()} returns true when the queue is not empty.
     */
    @Test
    void testIsOnQueueReturnsTrueWhenNotEmpty() {
        int numEmployees = servicePoint.getNumEmployees();
        // Add more customers than employees
        for (int i = 0; i < numEmployees + 1; i++) {
            servicePoint.addQueue(new ApplicationAsCustomer(false, true));
        }
        assertTrue(servicePoint.isOnQueue());
    }

    @Test
    void testGetTotalDeparturesIncreasesOnRemove() {
        ApplicationAsCustomer app = new ApplicationAsCustomer(true, true);
        servicePoint.addQueue(app);
        servicePoint.removeQueue();
        assertEquals(1, servicePoint.getTotalDepartures());
    }

    /**
     * Ensures that the average waiting time is zero if there have been no departures.
     */
    @Test
    void testGetAverageWaitingTimeZeroIfNoDepartures() {
        assertEquals(0.0, servicePoint.getAverageWaitingTime());
    }

    /**
     * Checks that the maximum queue length is tracked correctly.
     */
    @Test
    void testGetMaxQueueLengthTracksMax() {
        int numEmployees = servicePoint.getNumEmployees();
        // Add more applications than employees
        for (int i = 0; i < numEmployees + 2; i++) {
            servicePoint.addQueue(new ApplicationAsCustomer(true, true));
        }
        // The queue should have reached size 2 at its maximum
        assertEquals(2, servicePoint.getMaxQueueLength());
    }

    /**
     * Ensures that utilization is zero if there has been no busy time.
     */
    @Test
    void testGetUtilizationReturnsZeroIfNoBusyTime() {
        assertEquals(0.0, servicePoint.getUtilization(100.0));
    }

    /**
     * Verifies that the default number of employees is returned.
     */
    @Test
    void testGetNumEmployeesReturnsDefault() {
        assertEquals(5, servicePoint.getNumEmployees());
    }

    /**
     * Checks that adjusting the number of employees updates the count.
     */
    @Test
    void testAdjustEmployeesChangesCount() {
        servicePoint.adjustEmployees(10);
        assertEquals(10, servicePoint.getNumEmployees());
    }

    /**
     * Ensures that the bottleneck check increases the number of employees when the queue is long.
     */
    @Test
    void testCheckBottleneckIncreasesEmployees() {
        int numEmployees = servicePoint.getNumEmployees();
        // Add enough applications so the queue size exceeds 15
        for (int i = 0; i < numEmployees + 16; i++) {
            servicePoint.addQueue(new ApplicationAsCustomer(true, true));
        }
        // The number of employees should have increased by 1
        assertEquals(numEmployees + 1, servicePoint.getNumEmployees());
    }

    /**
     * Verifies that the scheduled event type can be set and retrieved.
     */
    @Test
    void testSetAndGetEventTypeScheduled() {
        servicePoint.setEventTypeScheduled(EventType.ARRIVAL);
        assertEquals(EventType.ARRIVAL, servicePoint.getEventTypeScheduled());
    }

    /**
     * Ensures that the service point name is not null.
     */
    @Test
    void testGetServicePointNameReturnsName() {
        assertNotNull(servicePoint.getServicePointName());
    }
}