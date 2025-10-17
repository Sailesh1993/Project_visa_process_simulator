package MVC.controller;

import eduni.project_distributionconfiguration.DistributionConfig;
import MVC.view.IVisualisation;
import MVC.view.ISimulatorUI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import javafx.application.Platform;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

@DisplayName("SimulationController Tests")
public class SimulationControllerTest {
    private SimulationController controller;
    private DistributionConfig[] testConfigs;
    private Long testSeed;

    @BeforeEach
    void setUp() {
        try { Platform.startup(() -> {}); } catch (IllegalStateException e) {}

        controller = new SimulationController();
        testConfigs = createTestConfigs();
        testSeed = 12345L;
    }

    private DistributionConfig[] createTestConfigs() {
        DistributionConfig[] configs = new DistributionConfig[7];
        configs[0] = new DistributionConfig("Negexp", 5.0, true);
        configs[1] = new DistributionConfig("Normal", 10.0, 2.0, false);
        configs[2] = new DistributionConfig("Gamma", 2.0, 3.0, false);
        configs[3] = new DistributionConfig("Negexp", 8.0, false);
        configs[4] = new DistributionConfig("Normal", 15.0, 3.0, false);
        configs[5] = new DistributionConfig("Gamma", 3.0, 2.0, false);
        configs[6] = new DistributionConfig("Negexp", 10.0, false);
        return configs;
    }

    @Test
    @DisplayName("SimulationController should be instantiated successfully")
    void testInstantiation() {
        assertNotNull(controller);
    }

    @Test
    @DisplayName("SimulationController should implement ISimulatorUI")
    void testImplementsISimulatorUI() {
        assertInstanceOf(ISimulatorUI.class, controller);
    }

    @Test
    @DisplayName("getTime should return simulation time")
    void testGetTime() throws Exception {
        Field field = SimulationController.class.getDeclaredField("simulationTime");
        field.setAccessible(true);
        field.set(controller, 100.0);

        assertEquals(100.0, controller.getTime());
    }

    @ParameterizedTest
    @DisplayName("getTime should return various simulation times")
    @ValueSource(doubles = {10.0, 50.0, 100.0, 500.0, 1000.0})
    void testGetTimeVariousValues(double time) throws Exception {
        Field field = SimulationController.class.getDeclaredField("simulationTime");
        field.setAccessible(true);
        field.set(controller, time);

        assertEquals(time, controller.getTime());
    }

    @Test
    @DisplayName("getDelay should return delay value")
    void testGetDelay() throws Exception {
        Field field = SimulationController.class.getDeclaredField("delay");
        field.setAccessible(true);
        field.set(controller, 1000L);

        assertEquals(1000L, controller.getDelay());
    }

    @ParameterizedTest
    @DisplayName("getDelay should return various delay values")
    @ValueSource(longs = {100L, 500L, 1000L, 2000L, 5000L})
    void testGetDelayVariousValues(long delay) throws Exception {
        Field field = SimulationController.class.getDeclaredField("delay");
        field.setAccessible(true);
        field.set(controller, delay);

        assertEquals(delay, controller.getDelay());
    }

    @Test
    @DisplayName("setEndingTime method should exist")
    void testSetEndingTimeMethodExists() {
        assertDoesNotThrow(() -> {
            Method method = SimulationController.class.getDeclaredMethod("setEndingTime", double.class);
            assertNotNull(method);
        });
    }

    @Test
    @DisplayName("displayResults method should exist")
    void testDisplayResultsMethodExists() {
        assertDoesNotThrow(() -> {
            Method method = SimulationController.class.getDeclaredMethod("displayResults", String.class);
            assertNotNull(method);
        });
    }

    @Test
    @DisplayName("updateStatistics method should exist and have correct signature")
    void testUpdateStatisticsMethodExists() {
        assertDoesNotThrow(() -> {
            Method method = SimulationController.class.getDeclaredMethod(
                    "updateStatistics", int.class, int.class, int.class, double.class, double.class);
            assertNotNull(method);
        });
    }

    @Test
    @DisplayName("updateQueueStatus method should exist and have correct signature")
    void testUpdateQueueStatusMethodExists() {
        assertDoesNotThrow(() -> {
            Method method = SimulationController.class.getDeclaredMethod(
                    "updateQueueStatus", int.class, int.class);
            assertNotNull(method);
        });
    }

    @Test
    @DisplayName("SimulationController should have handlePause method")
    void testHandlePauseMethod() {
        assertDoesNotThrow(() -> {
            Method method = SimulationController.class.getDeclaredMethod("handlePause");
            assertNotNull(method);
            assertTrue(method.isAnnotationPresent(javafx.fxml.FXML.class));
        });
    }

    @Test
    @DisplayName("SimulationController should have handleStop method")
    void testHandleStopMethod() {
        assertDoesNotThrow(() -> {
            Method method = SimulationController.class.getDeclaredMethod("handleStop");
            assertNotNull(method);
            assertTrue(method.isAnnotationPresent(javafx.fxml.FXML.class));
        });
    }

    @Test
    @DisplayName("SimulationController should have handleSpeedUp method")
    void testHandleSpeedUpMethod() {
        assertDoesNotThrow(() -> {
            Method method = SimulationController.class.getDeclaredMethod("handleSpeedUp");
            assertNotNull(method);
            assertTrue(method.isAnnotationPresent(javafx.fxml.FXML.class));
        });
    }

    @Test
    @DisplayName("SimulationController should have handleSlowDown method")
    void testHandleSlowDownMethod() {
        assertDoesNotThrow(() -> {
            Method method = SimulationController.class.getDeclaredMethod("handleSlowDown");
            assertNotNull(method);
            assertTrue(method.isAnnotationPresent(javafx.fxml.FXML.class));
        });
    }

    @Test
    @DisplayName("SimulationController should have navigateToMain method")
    void testNavigateToMainMethod() {
        assertDoesNotThrow(() -> {
            Method method = SimulationController.class.getDeclaredMethod("navigateToMain");
            assertNotNull(method);
            assertTrue(method.isAnnotationPresent(javafx.fxml.FXML.class));
        });
    }

    @Test
    @DisplayName("SimulationController should have initialize method")
    void testInitializeMethod() {
        assertDoesNotThrow(() -> {
            Method method = SimulationController.class.getDeclaredMethod(
                    "initialize", double.class, long.class, Long.class, DistributionConfig[].class);
            assertNotNull(method);
        });
    }

    @Test
    @DisplayName("SimulationController should have navigateToResult method")
    void testNavigateToResultMethod() {
        assertDoesNotThrow(() -> {
            Method method = SimulationController.class.getDeclaredMethod("navigateToResult");
            assertNotNull(method);
        });
    }

    @Test
    @DisplayName("SimulationController should have navigateToHome method")
    void testNavigateToHomeMethod() {
        assertDoesNotThrow(() -> {
            Method method = SimulationController.class.getDeclaredMethod("navigateToHome");
            assertNotNull(method);
        });
    }

    @Test
    @DisplayName("SimulationController should have showError method")
    void testShowErrorMethod() {
        assertDoesNotThrow(() -> {
            Method method = SimulationController.class.getDeclaredMethod(
                    "showError", String.class, String.class);
            assertNotNull(method);
        });
    }

    @Test
    @DisplayName("SimulationController should have setupVisualization method")
    void testSetupVisualizationMethod() {
        assertDoesNotThrow(() -> {
            Method method = SimulationController.class.getDeclaredMethod("setupVisualization");
            assertNotNull(method);
        });
    }

    @Test
    @DisplayName("SimulationController should have startSimulation method")
    void testStartSimulationMethod() {
        assertDoesNotThrow(() -> {
            Method method = SimulationController.class.getDeclaredMethod("startSimulation");
            assertNotNull(method);
        });
    }

    @Test
    @DisplayName("SimulationController should have all required FXML fields")
    void testFXMLFields() {
        assertDoesNotThrow(() -> {
            assertNotNull(SimulationController.class.getDeclaredField("simulationStatusLabel"));
            assertNotNull(SimulationController.class.getDeclaredField("timeElapsedLabel"));
            assertNotNull(SimulationController.class.getDeclaredField("totalAppsLabel"));
            assertNotNull(SimulationController.class.getDeclaredField("speedLabel"));
            assertNotNull(SimulationController.class.getDeclaredField("approvedLabel"));
            assertNotNull(SimulationController.class.getDeclaredField("rejectedLabel"));
            assertNotNull(SimulationController.class.getDeclaredField("avgTimeLabel"));
            assertNotNull(SimulationController.class.getDeclaredField("progressBar"));
            assertNotNull(SimulationController.class.getDeclaredField("visualizationCanvas"));
            assertNotNull(SimulationController.class.getDeclaredField("pauseButton"));
            assertNotNull(SimulationController.class.getDeclaredField("speedUpButton"));
            assertNotNull(SimulationController.class.getDeclaredField("slowDownButton"));
            assertNotNull(SimulationController.class.getDeclaredField("stopButton"));
        });
    }

    @Test
    @DisplayName("SimulationController should initialize with correct field types")
    void testFieldTypes() throws Exception {
        Field simulationTimeField = SimulationController.class.getDeclaredField("simulationTime");
        assertEquals(double.class, simulationTimeField.getType());

        Field delayField = SimulationController.class.getDeclaredField("delay");
        assertEquals(long.class, delayField.getType());

        Field seedField = SimulationController.class.getDeclaredField("seed");
        assertEquals(Long.class, seedField.getType());

        Field configsField = SimulationController.class.getDeclaredField("configs");
        assertEquals(DistributionConfig[].class, configsField.getType());
    }

    @Test
    @DisplayName("SimulationController should have volatile flags for thread safety")
    void testVolatileFlags() throws Exception {
        Field runningField = SimulationController.class.getDeclaredField("simulationRunning");
        assertTrue(java.lang.reflect.Modifier.isVolatile(runningField.getModifiers()));

        Field completeField = SimulationController.class.getDeclaredField("simulationComplete");
        assertTrue(java.lang.reflect.Modifier.isVolatile(completeField.getModifiers()));
    }

    @Test
    @DisplayName("SimulationController internal state fields should be initialized")
    void testInternalStateInitialization() throws Exception {
        Field totalField = SimulationController.class.getDeclaredField("currentTotalApps");
        totalField.setAccessible(true);
        assertEquals(0, totalField.getInt(controller));

        Field approvedField = SimulationController.class.getDeclaredField("currentApproved");
        approvedField.setAccessible(true);
        assertEquals(0, approvedField.getInt(controller));

        Field rejectedField = SimulationController.class.getDeclaredField("currentRejected");
        rejectedField.setAccessible(true);
        assertEquals(0, rejectedField.getInt(controller));
    }

    @Test
    @DisplayName("SimulationController should track user stop action")
    void testUserStoppedFlag() throws Exception {
        Field userStoppedField = SimulationController.class.getDeclaredField("userStopped");
        userStoppedField.setAccessible(true);

        assertFalse(userStoppedField.getBoolean(controller));
    }

    @Test
    @DisplayName("SimulationController should initialize speed to 1.0")
    void testInitialSpeed() throws Exception {
        Field speedField = SimulationController.class.getDeclaredField("currentSpeed");
        speedField.setAccessible(true);

        assertEquals(1.0, speedField.getDouble(controller));
    }

    @Test
    @DisplayName("Multiple SimulationController instances should be independent")
    void testMultipleInstances() {
        SimulationController c1 = new SimulationController();
        SimulationController c2 = new SimulationController();

        assertNotSame(c1, c2);
        assertNotNull(c1);
        assertNotNull(c2);
    }

    @Test
    @DisplayName("SimulationController should accept null seed")
    void testNullSeed() throws Exception {
        Field seedField = SimulationController.class.getDeclaredField("seed");
        seedField.setAccessible(true);
        seedField.set(controller, null);

        assertNull(seedField.get(controller));
    }



    @Test
    @DisplayName("getVisualisation should be callable after proper initialization")
    void testGetVisualisationMethodSignature() {
        assertDoesNotThrow(() -> {
            Method method = SimulationController.class.getDeclaredMethod("getVisualisation");
            assertNotNull(method);
            assertEquals(IVisualisation.class, method.getReturnType());
        });
    }

    @Test
    @DisplayName("SimulationController methods should have proper signatures for thread-safe updates")
    void testMethodSignaturesForThreadSafety() {
        assertDoesNotThrow(() -> {
            Method setEndingTime = SimulationController.class.getDeclaredMethod("setEndingTime", double.class);
            assertNotNull(setEndingTime);

            Method displayResults = SimulationController.class.getDeclaredMethod("displayResults", String.class);
            assertNotNull(displayResults);

            Method updateStats = SimulationController.class.getDeclaredMethod(
                    "updateStatistics", int.class, int.class, int.class, double.class, double.class);
            assertNotNull(updateStats);

            Method updateQueue = SimulationController.class.getDeclaredMethod(
                    "updateQueueStatus", int.class, int.class);
            assertNotNull(updateQueue);
        });
    }

    private void wait(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}