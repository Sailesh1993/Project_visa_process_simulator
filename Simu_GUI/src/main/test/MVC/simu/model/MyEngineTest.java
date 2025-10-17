package MVC.simu.model;

import MVC.controller.IControllerMtoV;
import MVC.view.IVisualisation;
import MVC.simu.framework.Event;
import MVC.simu.framework.Trace;
import eduni.project_distributionconfiguration.DistributionConfig;
import javafx.application.Platform;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Class MyEngine Tests")
class MyEngineTest {

    @BeforeAll
    static void initToolkit() {
        // Initialize JavaFX Toolkit once for all tests
        Platform.startup(() -> {}); // no-op lambda
    }

    // Dummy controller to avoid GUI/JavaFX calls
    static class DummyController implements IControllerMtoV {

        @Override
        public void updateQueueStatus(int id, int size) {}

        @Override
        public void displayResults(String s) {}

        @Override
        public void showEndTime(double t) {}

        @Override
        public void visualiseCustomer() {}

        // Correct return type for the interface
        @Override
        public IVisualisation getVisualisation() {
            return null;  // just return null for tests
        }

        @Override
        public void updateStatistics(int totalApps, int approved, int rejected, double avgTime, double currentTime) {}
    }

    private MyEngine engine;

    @BeforeEach
    void setup() {
        // Initialize Trace to avoid NullPointerException
        Trace.setTraceLevel(Trace.Level.INFO);

        // Create dummy distributions for the engine
        DistributionConfig[] cfgs = new DistributionConfig[7];
        for (int i = 0; i < 7; i++) {
            cfgs[i] = new DistributionConfig("Negexp", 1.0, true); // supported distribution
        }

        engine = new MyEngine(new DummyController(), cfgs, 42L);
    }

    @Test
    void testInitializationAndResults() {
        assertDoesNotThrow(() -> engine.initialization());
        assertDoesNotThrow(() -> engine.results());
    }

    @Test
    void testRunEventHandlesArrival() {
        Event e = new Event(EventType.ARRIVAL, 0.0);
        assertDoesNotThrow(() -> engine.runEvent(e));
    }

    @Test
    void testTryCEventsRunsCleanly() {
        assertDoesNotThrow(() -> engine.tryCEvents());
    }
}
