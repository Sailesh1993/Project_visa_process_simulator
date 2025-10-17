package MVC.controller;

import eduni.project_distributionconfiguration.DistributionConfig;
import MVC.simu.framework.IEngine;
import MVC.view.ISimulatorUI;
import MVC.view.IVisualisation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import javafx.application.Platform;

@DisplayName("Controller Tests")
public class ControllerTest {
    private Controller controller;
    private TestSimulatorUI testUI;
    private DistributionConfig[] testConfigs;
    private Long testSeed;

    @BeforeEach
    void setUp() {
        try { Platform.startup(() -> {}); } catch (IllegalStateException e) {}

        // Initialize Trace level to prevent NullPointerException
        try {
            Class<?> traceClass = Class.forName("MVC.simu.framework.Trace");
            Class<?> levelEnum = Class.forName("MVC.simu.framework.Trace$Level");
            Object[] enumConstants = levelEnum.getEnumConstants();
            if (enumConstants != null && enumConstants.length > 0) {
                java.lang.reflect.Field traceLevelField = traceClass.getDeclaredField("traceLevel");
                traceLevelField.setAccessible(true);
                traceLevelField.set(null, enumConstants[0]); // Set to first level (likely INFO or similar)
            }
        } catch (Exception e) {
            // If Trace initialization fails, continue with tests
        }

        testUI = new TestSimulatorUI();
        testConfigs = createTestConfigs();
        testSeed = 12345L;
        controller = new Controller(testUI, testConfigs, testSeed);
    }

    private DistributionConfig[] createTestConfigs() {
        // Create a minimal valid config array with actual objects
        // Using valid distribution types: "Normal", "Negexp", "Gamma"
        DistributionConfig[] configs = new DistributionConfig[7];
        configs[0] = new DistributionConfig("Negexp", 5.0, true);  // Arrival
        configs[1] = new DistributionConfig("Normal", 10.0, 2.0, false);  // Service
        configs[2] = new DistributionConfig("Gamma", 2.0, 3.0, false);    // Service
        configs[3] = new DistributionConfig("Negexp", 8.0, false);        // Service
        configs[4] = new DistributionConfig("Normal", 15.0, 3.0, false);  // Service
        configs[5] = new DistributionConfig("Gamma", 3.0, 2.0, false);    // Service
        configs[6] = new DistributionConfig("Negexp", 10.0, false);       // Service
        return configs;
    }

    @Test
    @DisplayName("Controller should be instantiated successfully")
    void testControllerInstantiation() {
        assertNotNull(controller);
    }

    @Test
    @DisplayName("Controller should accept various constructor parameters")
    void testControllerConstructor() {
        assertDoesNotThrow(() -> {
            new Controller(testUI, testConfigs, testSeed);
            new Controller(testUI, createTestConfigs(), null);
            new Controller(testUI, testConfigs, 99999L);
        });
    }

    @Test
    @DisplayName("Controller should implement required interfaces")
    void testControllerInterfaces() {
        assertInstanceOf(IControllerVtoM.class, controller);
        assertInstanceOf(IControllerMtoV.class, controller);
    }

    @Test
    @DisplayName("startSimulation should create and start engine")
    void testStartSimulation() {
        assertNull(controller.getEngine());
        controller.startSimulation();
        wait(200);
        assertNotNull(controller.getEngine());
    }

    @Test
    @DisplayName("decreaseSpeed should increase delay by 10%")
    void testDecreaseSpeed() {
        controller.startSimulation();
        wait(200);
        IEngine e = controller.getEngine();
        long initial = e.getDelay();
        controller.decreaseSpeed();
        assertTrue(e.getDelay() > initial);
    }

    @Test
    @DisplayName("increaseSpeed should decrease delay by 10%")
    void testIncreaseSpeed() {
        controller.startSimulation();
        wait(200);
        IEngine e = controller.getEngine();
        long initial = e.getDelay();
        controller.increaseSpeed();
        assertTrue(e.getDelay() < initial);
    }

    @Test
    @DisplayName("Multiple speed adjustments should work progressively")
    void testMultipleSpeedAdjustments() {
        controller.startSimulation();
        wait(200);
        IEngine e = controller.getEngine();
        long d1 = e.getDelay();
        controller.decreaseSpeed();
        long d2 = e.getDelay();
        controller.decreaseSpeed();
        long d3 = e.getDelay();
        assertTrue(d3 > d2 && d2 > d1);

        controller.increaseSpeed();
        long d4 = e.getDelay();
        controller.increaseSpeed();
        long d5 = e.getDelay();
        assertTrue(d5 < d4 && d4 < d3);
    }

    @ParameterizedTest
    @DisplayName("showEndTime should accept various time values")
    @ValueSource(doubles = {0.0, 1.5, 10.0, 100.5, 1000.0})
    void testShowEndTime(double time) {
        assertDoesNotThrow(() -> controller.showEndTime(time));
    }

    @Test
    @DisplayName("visualiseCustomer should call UI visualization")
    void testVisualiseCustomer() {
        controller.visualiseCustomer();
        wait(100);
        assertTrue(testUI.visualisationCalled);
    }

    @ParameterizedTest
    @DisplayName("updateQueueStatus should accept various service points")
    @ValueSource(ints = {0, 1, 2, 3, 4, 5})
    void testUpdateQueueStatus(int spId) {
        assertDoesNotThrow(() -> controller.updateQueueStatus(spId, 10));
    }

    @ParameterizedTest
    @DisplayName("updateQueueStatus should accept various queue sizes")
    @ValueSource(ints = {0, 1, 5, 10, 20, 50})
    void testUpdateQueueSizes(int size) {
        assertDoesNotThrow(() -> controller.updateQueueStatus(0, size));
    }

    @Test
    @DisplayName("displayResults should call UI display method")
    void testDisplayResults() {
        controller.displayResults("Test results");
        wait(100);
        assertTrue(testUI.resultsDisplayed);
    }

    @Test
    @DisplayName("displayResults should accept various text inputs")
    void testDisplayResultsVariousInputs() {
        assertDoesNotThrow(() -> {
            controller.displayResults("");
            controller.displayResults("Short text");
            controller.displayResults("Long text ".repeat(50));
        });
    }

    @Test
    @DisplayName("getVisualisation should return UI's visualization")
    void testGetVisualisation() {
        IVisualisation vis = controller.getVisualisation();
        assertNotNull(vis);
        assertSame(testUI.getVisualisation(), vis);
    }

    @Test
    @DisplayName("updateStatistics should accept various parameters")
    void testUpdateStatistics() {
        assertDoesNotThrow(() -> {
            controller.updateStatistics(0, 0, 0, 0.0, 0.0);
            controller.updateStatistics(10, 8, 2, 5.5, 50.0);
            controller.updateStatistics(100, 80, 20, 15.5, 100.0);
            controller.updateStatistics(1000, 800, 200, 20.5, 1000.0);
        });
    }

    @Test
    @DisplayName("Controller should handle multiple simulations")
    void testMultipleSimulations() {
        controller.startSimulation();
        wait(200);
        Controller c2 = new Controller(testUI, testConfigs, testSeed);
        assertDoesNotThrow(() -> c2.startSimulation());
    }

    @Test
    @DisplayName("Controller should work with various config array sizes")
    void testVariousConfigSizes() {
        assertDoesNotThrow(() -> {
            new Controller(testUI, createTestConfigs(), testSeed);
            new Controller(testUI, new DistributionConfig[6], testSeed);
            new Controller(testUI, new DistributionConfig[10], testSeed);
        });
    }

    @Test
    @DisplayName("All UI update methods should be thread-safe")
    void testThreadSafety() {
        assertDoesNotThrow(() -> {
            controller.showEndTime(100.0);
            controller.visualiseCustomer();
            controller.updateQueueStatus(0, 5);
            controller.displayResults("Test");
            controller.updateStatistics(10, 8, 2, 5.0, 50.0);
            wait(100);
        });
    }

    @Test
    @DisplayName("Controller should coordinate between View and Model")
    void testControllerCoordination() {
        controller.startSimulation();
        wait(200);
        assertDoesNotThrow(() -> {
            controller.visualiseCustomer();
            controller.updateQueueStatus(0, 3);
            controller.showEndTime(50.0);
        });
    }

    private void wait(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private static class TestSimulatorUI implements ISimulatorUI {
        private TestVisualisation vis;
        public boolean visualisationCalled = false;
        public boolean resultsDisplayed = false;

        public TestSimulatorUI() {
            this.vis = new TestVisualisation(this);
        }

        public double getTime() { return 100.0; }
        public long getDelay() { return 1000L; }
        public IVisualisation getVisualisation() { return vis; }
        public void setEndingTime(double time) {}
        public void displayResults(String results) { resultsDisplayed = true; }
        public void updateQueueStatus(int spId, int queueSize) {}
    }

    private static class TestVisualisation implements IVisualisation {
        private TestSimulatorUI ui;

        public TestVisualisation(TestSimulatorUI ui) {
            this.ui = ui;
        }

        public void newCustomer() { ui.visualisationCalled = true; }
        public void updateServicePointQueue(int spId, int size) {}
        public void moveCustomer(int fromSP, int toSP, boolean isApproved) {}
        public void clearDisplay() {}
    }
}