package MVC.view;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

import javafx.application.Platform;

@DisplayName("Class SimulatorUI Tests")
public class SimulatorUITest {

    private SimulatorUI simulatorUI;
    private static final int TEST_WIDTH = 1400;
    private static final int TEST_HEIGHT = 500;

    @BeforeEach
    void setUp() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Platform already started
        }

        simulatorUI = new SimulatorUI(TEST_WIDTH, TEST_HEIGHT);
    }

    @Test
    @DisplayName("SimulatorUI should initialize with correct dimensions")
    void testInitialization() {
        assertEquals(TEST_WIDTH, simulatorUI.getWidth(),
                "Canvas width should match constructor parameter");
        assertEquals(TEST_HEIGHT, simulatorUI.getHeight(),
                "Canvas height should match constructor parameter");
    }

    @Test
    @DisplayName("SimulatorUI should be created without errors")
    void testConstructorExecutesSuccessfully() {
        assertNotNull(simulatorUI,
                "SimulatorUI should be created successfully");
    }

    @Test
    @DisplayName("newCustomer should execute without throwing exception")
    void testNewCustomerExecutes() {
        assertDoesNotThrow(() -> simulatorUI.newCustomer(),
                "newCustomer should not throw any exception");
    }

    @Test
    @DisplayName("Multiple calls to newCustomer should execute successfully")
    void testMultipleNewCustomerCalls() {
        assertDoesNotThrow(() -> {
                    for (int i = 0; i < 10; i++) {
                        simulatorUI.newCustomer();
                    }
                },
                "Multiple newCustomer calls should execute without exception");
    }

    @ParameterizedTest
    @DisplayName("moveCustomer should execute with various parameters")
    @CsvSource({
            "0, 1, true",
            "1, 2, false",
            "2, 3, true",
            "3, 4, false",
            "4, 5, true",
            "5, -1, false",
            "-1, 0, true"
    })
    void testMoveCustomerExecutesWithValidParameters(int fromSP, int toSP, boolean isApproved) {
        assertDoesNotThrow(() -> simulatorUI.moveCustomer(fromSP, toSP, isApproved),
                "moveCustomer should execute with parameters: fromSP=" + fromSP +
                        ", toSP=" + toSP + ", isApproved=" + isApproved);
    }

    @Test
    @DisplayName("moveCustomer should work after adding a customer")
    void testMoveCustomerAfterNewCustomer() {
        simulatorUI.newCustomer();
        waitForPlatformThread();

        assertDoesNotThrow(() -> simulatorUI.moveCustomer(0, 1, true),
                "moveCustomer should work after creating a customer");
    }

    @Test
    @DisplayName("moveCustomer should handle exit scenario (toSP = -1)")
    void testMoveCustomerExit() {
        simulatorUI.newCustomer();
        waitForPlatformThread();

        assertDoesNotThrow(() -> simulatorUI.moveCustomer(0, -1, true),
                "moveCustomer should handle exiting the system");
    }

    @Test
    @DisplayName("moveCustomer should handle sequential movements")
    void testMoveCustomerSequential() {
        simulatorUI.newCustomer();
        waitForPlatformThread();

        assertDoesNotThrow(() -> {
                    simulatorUI.moveCustomer(0, 1, true);
                    simulatorUI.moveCustomer(1, 2, false);
                    simulatorUI.moveCustomer(2, 3, true);
                },
                "moveCustomer should handle sequential movements");
    }

    @Test
    @DisplayName("moveCustomer should create customer if needed")
    void testMoveCustomerCreatesCustomerIfNeeded() {
        assertDoesNotThrow(() -> simulatorUI.moveCustomer(2, 3, true),
                "moveCustomer should create a customer if none exists at source");
    }

    @ParameterizedTest
    @DisplayName("moveCustomer should work from any service point")
    @ValueSource(ints = {0, 1, 2, 3, 4, 5})
    void testMoveCustomerFromAllServicePoints(int fromSP) {
        assertDoesNotThrow(() -> simulatorUI.moveCustomer(fromSP, 0, true),
                "moveCustomer should work from service point " + fromSP);
    }

    @ParameterizedTest
    @DisplayName("moveCustomer should work to any service point")
    @ValueSource(ints = {-1, 0, 1, 2, 3, 4, 5})
    void testMoveCustomerToAllServicePoints(int toSP) {
        simulatorUI.newCustomer();
        waitForPlatformThread();

        assertDoesNotThrow(() -> simulatorUI.moveCustomer(0, toSP, true),
                "moveCustomer should work to service point " + toSP);
    }

    @Test
    @DisplayName("clearDisplay should execute without throwing exception")
    void testClearDisplayExecutes() {
        simulatorUI.newCustomer();
        simulatorUI.newCustomer();
        waitForPlatformThread();

        assertDoesNotThrow(() -> simulatorUI.clearDisplay(),
                "clearDisplay should not throw any exception");
    }

    @Test
    @DisplayName("clearDisplay should work on empty system")
    void testClearDisplayOnEmptySystem() {
        assertDoesNotThrow(() -> simulatorUI.clearDisplay(),
                "clearDisplay should work on empty system");
    }

    @Test
    @DisplayName("clearDisplay should work multiple times")
    void testClearDisplayMultipleTimes() {
        assertDoesNotThrow(() -> {
                    simulatorUI.clearDisplay();
                    simulatorUI.newCustomer();
                    simulatorUI.clearDisplay();
                    simulatorUI.newCustomer();
                    simulatorUI.clearDisplay();
                },
                "clearDisplay should be callable multiple times");
    }

    @Test
    @DisplayName("updateServicePointQueue should execute without exception")
    void testUpdateServicePointQueueExecutes() {
        assertDoesNotThrow(() -> simulatorUI.updateServicePointQueue(0, 5),
                "updateServicePointQueue should not throw any exception");
    }

    @ParameterizedTest
    @DisplayName("updateServicePointQueue should work for all service points")
    @CsvSource({
            "0, 0",
            "1, 5",
            "2, 10",
            "3, 15",
            "4, 20",
            "5, 25"
    })
    void testUpdateServicePointQueueForAllServicePoints(int spId, int size) {
        assertDoesNotThrow(() -> simulatorUI.updateServicePointQueue(spId, size),
                "updateServicePointQueue should work for service point " + spId +
                        " with queue size " + size);
    }

    @Test
    @DisplayName("Workflow simulation: Entry -> Processing -> Exit")
    void testSimulationWorkflow() {
        assertDoesNotThrow(() -> {
                    // Customer enters system
                    simulatorUI.newCustomer();
                    waitForPlatformThread();

                    // Customer moves through service points
                    simulatorUI.moveCustomer(0, 1, true);
                    waitForPlatformThread();

                    simulatorUI.moveCustomer(1, 2, false);
                    waitForPlatformThread();

                    simulatorUI.moveCustomer(2, 3, true);
                    waitForPlatformThread();

                    // Customer exits system
                    simulatorUI.moveCustomer(3, -1, true);
                    waitForPlatformThread();
                },
                "Full workflow from entry to exit should execute without errors");
    }

    @Test
    @DisplayName("Complex scenario: Multiple customers in system")
    void testMultipleCustomersScenario() {
        assertDoesNotThrow(() -> {
                    // Add multiple customers
                    for (int i = 0; i < 5; i++) {
                        simulatorUI.newCustomer();
                    }
                    waitForPlatformThread();

                    // Move customers through system
                    simulatorUI.moveCustomer(0, 1, true);
                    simulatorUI.moveCustomer(0, 2, false);
                    simulatorUI.moveCustomer(0, 3, true);

                    // Clear and start fresh
                    simulatorUI.clearDisplay();

                    // Verify system is ready for new simulation
                    simulatorUI.newCustomer();
                },
                "System should handle multiple customers without errors");
    }

    @Test
    @DisplayName("Queue update multiple times should work")
    void testMultipleQueueUpdates() {
        assertDoesNotThrow(() -> {
                    for (int i = 0; i < 6; i++) {
                        for (int queueSize = 0; queueSize <= 20; queueSize += 5) {
                            simulatorUI.updateServicePointQueue(i, queueSize);
                        }
                    }
                },
                "Multiple queue updates should execute without errors");
    }

    @Test
    @DisplayName("Mixed operations should execute without interference")
    void testMixedOperations() {
        assertDoesNotThrow(() -> {
                    simulatorUI.newCustomer();
                    simulatorUI.updateServicePointQueue(0, 3);
                    simulatorUI.newCustomer();
                    simulatorUI.moveCustomer(0, 1, true);
                    simulatorUI.updateServicePointQueue(1, 2);
                    simulatorUI.newCustomer();
                    simulatorUI.moveCustomer(1, 2, false);
                    simulatorUI.clearDisplay();
                },
                "Mixed operations should execute without errors");
    }

    @Test
    @DisplayName("Canvas rendering should handle rapid customer creation")
    void testRapidCustomerCreation() {
        assertDoesNotThrow(() -> {
                    for (int i = 0; i < 50; i++) {
                        simulatorUI.newCustomer();
                    }
                    waitForPlatformThread();
                },
                "Rapid customer creation should not cause errors");
    }

    @Test
    @DisplayName("System should recover after clearDisplay")
    void testSystemRecoveryAfterClear() {
        assertDoesNotThrow(() -> {
                    simulatorUI.newCustomer();
                    simulatorUI.moveCustomer(0, 1, true);
                    simulatorUI.clearDisplay();

                    // System should be ready for new simulation
                    simulatorUI.newCustomer();
                    simulatorUI.moveCustomer(0, 2, false);
                },
                "System should recover and work after clearDisplay");
    }

    // Helper method to wait for JavaFX Platform thread execution
    private void waitForPlatformThread() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}