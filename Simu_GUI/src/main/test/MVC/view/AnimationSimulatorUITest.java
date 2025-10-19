package MVC.view;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

import javafx.application.Platform;

@DisplayName("Class AnimationSimulatorUI Tests")
public class AnimationSimulatorUITest {

    private AnimationSimulatorUI animationSimulatorUI;
    private static final int TEST_WIDTH = 1400;
    private static final int TEST_HEIGHT = 500;

    @BeforeEach
    void setUp() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Platform already started
        }

        animationSimulatorUI = new AnimationSimulatorUI(TEST_WIDTH, TEST_HEIGHT);
    }

    @Test
    @DisplayName("AnimationSimulatorUI should initialize with correct dimensions")
    void testInitialization() {
        assertEquals(TEST_WIDTH, animationSimulatorUI.getWidth(),
                "Canvas width should match constructor parameter");
        assertEquals(TEST_HEIGHT, animationSimulatorUI.getHeight(),
                "Canvas height should match constructor parameter");
    }

    @Test
    @DisplayName("AnimationSimulatorUI should be created without errors")
    void testConstructorExecutesSuccessfully() {
        assertNotNull(animationSimulatorUI,
                "AnimationSimulatorUI should be created successfully");
    }

    @Test
    @DisplayName("newCustomer should execute without throwing exception")
    void testNewCustomerExecutes() {
        assertDoesNotThrow(() -> animationSimulatorUI.newCustomer(),
                "newCustomer should not throw any exception");
    }

    @Test
    @DisplayName("Multiple calls to newCustomer should execute successfully")
    void testMultipleNewCustomerCalls() {
        assertDoesNotThrow(() -> {
                    for (int i = 0; i < 10; i++) {
                        animationSimulatorUI.newCustomer();
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
        assertDoesNotThrow(() -> animationSimulatorUI.moveCustomer(fromSP, toSP, isApproved),
                "moveCustomer should execute with parameters: fromSP=" + fromSP +
                        ", toSP=" + toSP + ", isApproved=" + isApproved);
    }

    @Test
    @DisplayName("moveCustomer should work after adding a customer")
    void testMoveCustomerAfterNewCustomer() {
        animationSimulatorUI.newCustomer();
        waitForPlatformThread();

        assertDoesNotThrow(() -> animationSimulatorUI.moveCustomer(0, 1, true),
                "moveCustomer should work after creating a customer");
    }

    @Test
    @DisplayName("moveCustomer should handle exit scenario (toSP = -1)")
    void testMoveCustomerExit() {
        animationSimulatorUI.newCustomer();
        waitForPlatformThread();

        assertDoesNotThrow(() -> animationSimulatorUI.moveCustomer(0, -1, true),
                "moveCustomer should handle exiting the system");
    }

    @Test
    @DisplayName("moveCustomer should handle sequential movements")
    void testMoveCustomerSequential() {
        animationSimulatorUI.newCustomer();
        waitForPlatformThread();

        assertDoesNotThrow(() -> {
                    animationSimulatorUI.moveCustomer(0, 1, true);
                    animationSimulatorUI.moveCustomer(1, 2, false);
                    animationSimulatorUI.moveCustomer(2, 3, true);
                },
                "moveCustomer should handle sequential movements");
    }

    @Test
    @DisplayName("moveCustomer should create customer if needed")
    void testMoveCustomerCreatesCustomerIfNeeded() {
        assertDoesNotThrow(() -> animationSimulatorUI.moveCustomer(2, 3, true),
                "moveCustomer should create a customer if none exists at source");
    }

    @ParameterizedTest
    @DisplayName("moveCustomer should work from any service point")
    @ValueSource(ints = {0, 1, 2, 3, 4, 5})
    void testMoveCustomerFromAllServicePoints(int fromSP) {
        assertDoesNotThrow(() -> animationSimulatorUI.moveCustomer(fromSP, 0, true),
                "moveCustomer should work from service point " + fromSP);
    }

    @ParameterizedTest
    @DisplayName("moveCustomer should work to any service point")
    @ValueSource(ints = {-1, 0, 1, 2, 3, 4, 5})
    void testMoveCustomerToAllServicePoints(int toSP) {
        animationSimulatorUI.newCustomer();
        waitForPlatformThread();

        assertDoesNotThrow(() -> animationSimulatorUI.moveCustomer(0, toSP, true),
                "moveCustomer should work to service point " + toSP);
    }

    @Test
    @DisplayName("clearDisplay should execute without throwing exception")
    void testClearDisplayExecutes() {
        animationSimulatorUI.newCustomer();
        animationSimulatorUI.newCustomer();
        waitForPlatformThread();

        assertDoesNotThrow(() -> animationSimulatorUI.clearDisplay(),
                "clearDisplay should not throw any exception");
    }

    @Test
    @DisplayName("clearDisplay should work on empty system")
    void testClearDisplayOnEmptySystem() {
        assertDoesNotThrow(() -> animationSimulatorUI.clearDisplay(),
                "clearDisplay should work on empty system");
    }

    @Test
    @DisplayName("clearDisplay should work multiple times")
    void testClearDisplayMultipleTimes() {
        assertDoesNotThrow(() -> {
                    animationSimulatorUI.clearDisplay();
                    animationSimulatorUI.newCustomer();
                    animationSimulatorUI.clearDisplay();
                    animationSimulatorUI.newCustomer();
                    animationSimulatorUI.clearDisplay();
                },
                "clearDisplay should be callable multiple times");
    }

    @Test
    @DisplayName("updateServicePointQueue should execute without exception")
    void testUpdateServicePointQueueExecutes() {
        assertDoesNotThrow(() -> animationSimulatorUI.updateServicePointQueue(0, 5),
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
        assertDoesNotThrow(() -> animationSimulatorUI.updateServicePointQueue(spId, size),
                "updateServicePointQueue should work for service point " + spId +
                        " with queue size " + size);
    }

    @Test
    @DisplayName("Workflow simulation: Entry -> Processing -> Exit")
    void testSimulationWorkflow() {
        assertDoesNotThrow(() -> {
                    // Customer enters system
                    animationSimulatorUI.newCustomer();
                    waitForPlatformThread();

                    // Customer moves through service points
                    animationSimulatorUI.moveCustomer(0, 1, true);
                    waitForPlatformThread();

                    animationSimulatorUI.moveCustomer(1, 2, false);
                    waitForPlatformThread();

                    animationSimulatorUI.moveCustomer(2, 3, true);
                    waitForPlatformThread();

                    // Customer exits system
                    animationSimulatorUI.moveCustomer(3, -1, true);
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
                        animationSimulatorUI.newCustomer();
                    }
                    waitForPlatformThread();

                    // Move customers through system
                    animationSimulatorUI.moveCustomer(0, 1, true);
                    animationSimulatorUI.moveCustomer(0, 2, false);
                    animationSimulatorUI.moveCustomer(0, 3, true);

                    // Clear and start fresh
                    animationSimulatorUI.clearDisplay();

                    // Verify system is ready for new simulation
                    animationSimulatorUI.newCustomer();
                },
                "System should handle multiple customers without errors");
    }

    @Test
    @DisplayName("Queue update multiple times should work")
    void testMultipleQueueUpdates() {
        assertDoesNotThrow(() -> {
                    for (int i = 0; i < 6; i++) {
                        for (int queueSize = 0; queueSize <= 20; queueSize += 5) {
                            animationSimulatorUI.updateServicePointQueue(i, queueSize);
                        }
                    }
                },
                "Multiple queue updates should execute without errors");
    }

    @Test
    @DisplayName("Mixed operations should execute without interference")
    void testMixedOperations() {
        assertDoesNotThrow(() -> {
                    animationSimulatorUI.newCustomer();
                    animationSimulatorUI.updateServicePointQueue(0, 3);
                    animationSimulatorUI.newCustomer();
                    animationSimulatorUI.moveCustomer(0, 1, true);
                    animationSimulatorUI.updateServicePointQueue(1, 2);
                    animationSimulatorUI.newCustomer();
                    animationSimulatorUI.moveCustomer(1, 2, false);
                    animationSimulatorUI.clearDisplay();
                },
                "Mixed operations should execute without errors");
    }

    @Test
    @DisplayName("Canvas rendering should handle rapid customer creation")
    void testRapidCustomerCreation() {
        assertDoesNotThrow(() -> {
                    for (int i = 0; i < 50; i++) {
                        animationSimulatorUI.newCustomer();
                    }
                    waitForPlatformThread();
                },
                "Rapid customer creation should not cause errors");
    }

    @Test
    @DisplayName("System should recover after clearDisplay")
    void testSystemRecoveryAfterClear() {
        assertDoesNotThrow(() -> {
                    animationSimulatorUI.newCustomer();
                    animationSimulatorUI.moveCustomer(0, 1, true);
                    animationSimulatorUI.clearDisplay();

                    // System should be ready for new simulation
                    animationSimulatorUI.newCustomer();
                    animationSimulatorUI.moveCustomer(0, 2, false);
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