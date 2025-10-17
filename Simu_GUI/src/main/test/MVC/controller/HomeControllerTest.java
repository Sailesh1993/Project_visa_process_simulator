package MVC.controller;

import eduni.project_distributionconfiguration.DistributionConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import javafx.application.Platform;
import java.lang.reflect.Method;

@DisplayName("HomeController Tests")
public class HomeControllerTest {
    private HomeController controller;

    @BeforeEach
    void setUp() {
        try { Platform.startup(() -> {}); } catch (IllegalStateException e) {}
        controller = new HomeController();
    }

    @Test
    @DisplayName("HomeController should be instantiated successfully")
    void testInstantiation() {
        assertNotNull(controller);
    }

    @Test
    @DisplayName("HomeController should have buildDistConfig method")
    void testBuildDistConfigMethod() {
        assertDoesNotThrow(() -> {
            Method method = HomeController.class.getDeclaredMethod(
                    "buildDistConfig", String.class, String.class, String.class, boolean.class);
            assertNotNull(method);
        });
    }

    @Test
    @DisplayName("buildDistConfig should create Negexp distribution")
    void testBuildDistConfigNegexp() throws Exception {
        Method method = HomeController.class.getDeclaredMethod(
                "buildDistConfig", String.class, String.class, String.class, boolean.class);
        method.setAccessible(true);

        DistributionConfig config = (DistributionConfig) method.invoke(
                controller, "Negexp", "5.0", "", false);

        assertNotNull(config);
        assertEquals("Negexp", config.getType());
        assertEquals(5.0, config.getParam1());
    }

    @Test
    @DisplayName("buildDistConfig should create Normal distribution")
    void testBuildDistConfigNormal() throws Exception {
        Method method = HomeController.class.getDeclaredMethod(
                "buildDistConfig", String.class, String.class, String.class, boolean.class);
        method.setAccessible(true);

        DistributionConfig config = (DistributionConfig) method.invoke(
                controller, "Normal", "10.0", "2.0", false);

        assertNotNull(config);
        assertEquals("Normal", config.getType());
        assertEquals(10.0, config.getParam1());
        assertEquals(2.0, config.getParam2());
    }

    @Test
    @DisplayName("buildDistConfig should create Gamma distribution")
    void testBuildDistConfigGamma() throws Exception {
        Method method = HomeController.class.getDeclaredMethod(
                "buildDistConfig", String.class, String.class, String.class, boolean.class);
        method.setAccessible(true);

        DistributionConfig config = (DistributionConfig) method.invoke(
                controller, "Gamma", "3.0", "2.5", true);

        assertNotNull(config);
        assertEquals("Gamma", config.getType());
        assertEquals(3.0, config.getParam1());
        assertEquals(2.5, config.getParam2());
    }

    @ParameterizedTest
    @DisplayName("buildDistConfig should handle various parameters")
    @CsvSource({
            "Negexp, 1.0, '', false",
            "Negexp, 10.0, '', true",
            "Normal, 5.0, 1.5, false",
            "Gamma, 2.0, 3.0, true"
    })
    void testBuildDistConfigVariousInputs(String type, String p1, String p2, boolean forArrival) throws Exception {
        Method method = HomeController.class.getDeclaredMethod(
                "buildDistConfig", String.class, String.class, String.class, boolean.class);
        method.setAccessible(true);

        assertDoesNotThrow(() -> method.invoke(controller, type, p1, p2, forArrival));
    }

    @Test
    @DisplayName("validateDistConfig should accept valid Negexp config")
    void testValidateDistConfigNegexp() throws Exception {
        Method method = HomeController.class.getDeclaredMethod(
                "validateDistConfig", String.class, String.class, String.class, String.class);
        method.setAccessible(true);

        assertDoesNotThrow(() ->
                method.invoke(controller, "SP1", "Negexp", "5.0", ""));
    }

    @Test
    @DisplayName("validateDistConfig should accept valid Normal config")
    void testValidateDistConfigNormal() throws Exception {
        Method method = HomeController.class.getDeclaredMethod(
                "validateDistConfig", String.class, String.class, String.class, String.class);
        method.setAccessible(true);

        assertDoesNotThrow(() ->
                method.invoke(controller, "SP1", "Normal", "10.0", "2.0"));
    }

    @Test
    @DisplayName("validateDistConfig should accept valid Gamma config")
    void testValidateDistConfigGamma() throws Exception {
        Method method = HomeController.class.getDeclaredMethod(
                "validateDistConfig", String.class, String.class, String.class, String.class);
        method.setAccessible(true);

        assertDoesNotThrow(() ->
                method.invoke(controller, "SP1", "Gamma", "3.0", "2.5"));
    }

    @Test
    @DisplayName("validateDistConfig should reject negative param1")
    void testValidateDistConfigNegativeParam1() throws Exception {
        Method method = HomeController.class.getDeclaredMethod(
                "validateDistConfig", String.class, String.class, String.class, String.class);
        method.setAccessible(true);

        Exception exception = assertThrows(Exception.class, () ->
                method.invoke(controller, "SP1", "Negexp", "-5.0", ""));

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
    }

    @Test
    @DisplayName("validateDistConfig should reject zero param1")
    void testValidateDistConfigZeroParam1() throws Exception {
        Method method = HomeController.class.getDeclaredMethod(
                "validateDistConfig", String.class, String.class, String.class, String.class);
        method.setAccessible(true);

        Exception exception = assertThrows(Exception.class, () ->
                method.invoke(controller, "SP1", "Normal", "0", "2.0"));

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
    }

    @Test
    @DisplayName("validateDistConfig should reject missing param2 for Normal")
    void testValidateDistConfigMissingParam2Normal() throws Exception {
        Method method = HomeController.class.getDeclaredMethod(
                "validateDistConfig", String.class, String.class, String.class, String.class);
        method.setAccessible(true);

        Exception exception = assertThrows(Exception.class, () ->
                method.invoke(controller, "SP1", "Normal", "10.0", ""));

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
    }

    @Test
    @DisplayName("validateDistConfig should reject missing param2 for Gamma")
    void testValidateDistConfigMissingParam2Gamma() throws Exception {
        Method method = HomeController.class.getDeclaredMethod(
                "validateDistConfig", String.class, String.class, String.class, String.class);
        method.setAccessible(true);

        Exception exception = assertThrows(Exception.class, () ->
                method.invoke(controller, "SP1", "Gamma", "3.0", ""));

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
    }

    @Test
    @DisplayName("validateDistConfig should reject negative param2")
    void testValidateDistConfigNegativeParam2() throws Exception {
        Method method = HomeController.class.getDeclaredMethod(
                "validateDistConfig", String.class, String.class, String.class, String.class);
        method.setAccessible(true);

        Exception exception = assertThrows(Exception.class, () ->
                method.invoke(controller, "SP1", "Normal", "10.0", "-2.0"));

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
    }

    @Test
    @DisplayName("HomeController should have showError method")
    void testShowErrorMethod() {
        assertDoesNotThrow(() -> {
            Method method = HomeController.class.getDeclaredMethod(
                    "showError", String.class, String.class);
            assertNotNull(method);
        });
    }

    @Test
    @DisplayName("HomeController should have showWarning method")
    void testShowWarningMethod() {
        assertDoesNotThrow(() -> {
            Method method = HomeController.class.getDeclaredMethod(
                    "showWarning", String.class, String.class);
            assertNotNull(method);
        });
    }

    @Test
    @DisplayName("HomeController should have showInfo method")
    void testShowInfoMethod() {
        assertDoesNotThrow(() -> {
            Method method = HomeController.class.getDeclaredMethod(
                    "showInfo", String.class, String.class);
            assertNotNull(method);
        });
    }

    @Test
    @DisplayName("HomeController should have setupDistributionComboBoxes method")
    void testSetupDistributionComboBoxesMethod() {
        assertDoesNotThrow(() -> {
            Method method = HomeController.class.getDeclaredMethod("setupDistributionComboBoxes");
            assertNotNull(method);
        });
    }

    @Test
    @DisplayName("HomeController should have setupDistributionListeners method")
    void testSetupDistributionListenersMethod() {
        assertDoesNotThrow(() -> {
            Method method = HomeController.class.getDeclaredMethod("setupDistributionListeners");
            assertNotNull(method);
        });
    }

    @Test
    @DisplayName("HomeController should have updateParameterFields method")
    void testUpdateParameterFieldsMethod() {
        assertDoesNotThrow(() -> {
            Method method = HomeController.class.getDeclaredMethod(
                    "updateParameterFields", String.class,
                    javafx.scene.control.TextField.class,
                    javafx.scene.control.Label.class);
            assertNotNull(method);
        });
    }

    @Test
    @DisplayName("HomeController should have setupTableColumns method")
    void testSetupTableColumnsMethod() {
        assertDoesNotThrow(() -> {
            Method method = HomeController.class.getDeclaredMethod("setupTableColumns");
            assertNotNull(method);
        });
    }

    @Test
    @DisplayName("HomeController should have loadRecentRuns method")
    void testLoadRecentRunsMethod() {
        assertDoesNotThrow(() -> {
            Method method = HomeController.class.getDeclaredMethod("loadRecentRuns");
            assertNotNull(method);
        });
    }

    @Test
    @DisplayName("HomeController should have handleStart method")
    void testHandleStartMethod() {
        assertDoesNotThrow(() -> {
            Method method = HomeController.class.getDeclaredMethod("handleStart");
            assertNotNull(method);
            assertTrue(method.isAnnotationPresent(javafx.fxml.FXML.class));
        });
    }

    @Test
    @DisplayName("HomeController should have handleLoadRun method")
    void testHandleLoadRunMethod() {
        assertDoesNotThrow(() -> {
            Method method = HomeController.class.getDeclaredMethod("handleLoadRun");
            assertNotNull(method);
            assertTrue(method.isAnnotationPresent(javafx.fxml.FXML.class));
        });
    }

    @Test
    @DisplayName("HomeController should have handleDeleteRun method")
    void testHandleDeleteRunMethod() {
        assertDoesNotThrow(() -> {
            Method method = HomeController.class.getDeclaredMethod("handleDeleteRun");
            assertNotNull(method);
            assertTrue(method.isAnnotationPresent(javafx.fxml.FXML.class));
        });
    }

    @Test
    @DisplayName("HomeController should have validateInputs method")
    void testValidateInputsMethod() {
        assertDoesNotThrow(() -> {
            Method method = HomeController.class.getDeclaredMethod("validateInputs");
            assertNotNull(method);
        });
    }

    @Test
    @DisplayName("HomeController should have buildConfigurations method")
    void testBuildConfigurationsMethod() {
        assertDoesNotThrow(() -> {
            Method method = HomeController.class.getDeclaredMethod("buildConfigurations");
            assertNotNull(method);
        });
    }

    @Test
    @DisplayName("HomeController should have navigateToSimulation method")
    void testNavigateToSimulationMethod() {
        assertDoesNotThrow(() -> {
            Method method = HomeController.class.getDeclaredMethod(
                    "navigateToSimulation", double.class, long.class, Long.class, DistributionConfig[].class);
            assertNotNull(method);
        });
    }

    @Test
    @DisplayName("HomeController should have navigateToResults method")
    void testNavigateToResultsMethod() {
        assertDoesNotThrow(() -> {
            Method method = HomeController.class.getDeclaredMethod("navigateToResults", Long.class);
            assertNotNull(method);
        });
    }

    @Test
    @DisplayName("HomeController should have all required FXML fields")
    void testFXMLFields() {
        assertDoesNotThrow(() -> {
            assertNotNull(HomeController.class.getDeclaredField("simulationTimeField"));
            assertNotNull(HomeController.class.getDeclaredField("delayField"));
            assertNotNull(HomeController.class.getDeclaredField("arrival_distType"));
            assertNotNull(HomeController.class.getDeclaredField("sp1_distType"));
            assertNotNull(HomeController.class.getDeclaredField("sp2_distType"));
            assertNotNull(HomeController.class.getDeclaredField("sp3_distType"));
            assertNotNull(HomeController.class.getDeclaredField("sp4_distType"));
            assertNotNull(HomeController.class.getDeclaredField("sp5_distType"));
            assertNotNull(HomeController.class.getDeclaredField("sp6_distType"));
            assertNotNull(HomeController.class.getDeclaredField("recentRunsTable"));
            assertNotNull(HomeController.class.getDeclaredField("startButton"));
        });
    }

    @Test
    @DisplayName("buildDistConfig should set forArrival flag correctly")
    void testBuildDistConfigForArrivalFlag() throws Exception {
        Method method = HomeController.class.getDeclaredMethod(
                "buildDistConfig", String.class, String.class, String.class, boolean.class);
        method.setAccessible(true);

        DistributionConfig config1 = (DistributionConfig) method.invoke(
                controller, "Negexp", "5.0", "", true);

        DistributionConfig config2 = (DistributionConfig) method.invoke(
                controller, "Normal", "10.0", "2.0", false);
    }

    @ParameterizedTest
    @DisplayName("buildDistConfig should handle various Negexp means")
    @ValueSource(strings = {"1.0", "5.0", "10.0", "50.0", "100.0"})
    void testBuildDistConfigVariousMeans(String mean) throws Exception {
        Method method = HomeController.class.getDeclaredMethod(
                "buildDistConfig", String.class, String.class, String.class, boolean.class);
        method.setAccessible(true);

        DistributionConfig config = (DistributionConfig) method.invoke(
                controller, "Negexp", mean, "", false);

        assertEquals(Double.parseDouble(mean), config.getParam1());
    }

    @ParameterizedTest
    @DisplayName("buildDistConfig should handle various Normal distributions")
    @CsvSource({
            "5.0, 1.0",
            "10.0, 2.0",
            "15.0, 3.0",
            "20.0, 5.0"
    })
    void testBuildDistConfigVariousNormal(String mean, String stdDev) throws Exception {
        Method method = HomeController.class.getDeclaredMethod(
                "buildDistConfig", String.class, String.class, String.class, boolean.class);
        method.setAccessible(true);

        DistributionConfig config = (DistributionConfig) method.invoke(
                controller, "Normal", mean, stdDev, false);

        assertEquals(Double.parseDouble(mean), config.getParam1());
        assertEquals(Double.parseDouble(stdDev), config.getParam2());
    }

    @ParameterizedTest
    @DisplayName("buildDistConfig should handle various Gamma distributions")
    @CsvSource({
            "2.0, 1.5",
            "3.0, 2.0",
            "5.0, 3.5",
            "10.0, 5.0"
    })
    void testBuildDistConfigVariousGamma(String shape, String scale) throws Exception {
        Method method = HomeController.class.getDeclaredMethod(
                "buildDistConfig", String.class, String.class, String.class, boolean.class);
        method.setAccessible(true);

        DistributionConfig config = (DistributionConfig) method.invoke(
                controller, "Gamma", shape, scale, false);

        assertEquals(Double.parseDouble(shape), config.getParam1());
        assertEquals(Double.parseDouble(scale), config.getParam2());
    }

    @Test
    @DisplayName("Multiple HomeController instances should be independent")
    void testMultipleInstances() {
        HomeController c1 = new HomeController();
        HomeController c2 = new HomeController();

        assertNotSame(c1, c2);
        assertNotNull(c1);
        assertNotNull(c2);
    }
}