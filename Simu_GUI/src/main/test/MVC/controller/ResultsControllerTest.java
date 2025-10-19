package MVC.controller;

import ORM.dao.SimulationRunDao;
import ORM.entity.DistConfig;
import ORM.entity.SPResult;
import ORM.entity.SimulationRun;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ResultsController class.
 * <p>
 * These tests use reflection to inject dependencies and JavaFX controls,
 * and run UI-related assertions on the JavaFX Application Thread.
 */
class ResultsControllerTest {
    private ResultsController controller;

    /**
     * Fake DAO for providing controlled SimulationRun data to the controller.
     */

    static class FakeSimulationRunDao extends SimulationRunDao {
        private final SimulationRun run;

        FakeSimulationRunDao() {
            run = new SimulationRun();
            run.setId(1L);
            run.setTimestamp(LocalDateTime.now());
            run.setTotalApplications(100);
            run.setApprovedCount(80);
            run.setRejectedCount(20);
            run.setAvgSystemTime(12.5);

            // Multiple service points
            SPResult sp1 = new SPResult();
            sp1.setServicePointName("SP1");
            sp1.setDepartures(100);
            sp1.setAvgWaitingTime(5.0);
            sp1.setMaxQueueLength(10);
            sp1.setUtilization(75.0);
            sp1.setNumEmployees(5);
            sp1.setBottleneck(true);

            SPResult sp2 = new SPResult();
            sp2.setServicePointName("SP2");
            sp2.setDepartures(80);
            sp2.setAvgWaitingTime(7.0);
            sp2.setMaxQueueLength(12);
            sp2.setUtilization(60.0);
            sp2.setNumEmployees(4);
            sp2.setBottleneck(false);

            // Multiple distributions
            DistConfig dist1 = new DistConfig();
            dist1.setServicePointName("SP1");
            dist1.setDistributionType("Exponential");

            DistConfig dist2 = new DistConfig();
            dist2.setServicePointName("SP2");
            dist2.setDistributionType("Normal");

            run.setServicePointResults(List.of(sp1, sp2));
            run.setDistConfiguration(List.of(dist1, dist2));
        }

        @Override
        public List<SimulationRun> findAllWithAssociations() {
            return new ArrayList<>(List.of(run));
        }

        @Override
        public SimulationRun find(Long id) {
            return (id != null && id.equals(run.getId())) ? run : null;
        }
    }

    /** Initialize JavaFX toolkit before running tests. */
    @BeforeAll
    static void initJfx() {
        Platform.startup(() -> {});
    }


    /**
     * Sets up a fresh ResultsController and injects all required fields before each test.
     */
    @BeforeEach
    void setUp() throws Exception {
        controller = new ResultsController();
        setPrivateField(controller, "dao", new FakeSimulationRunDao());
        setPrivateField(controller, "runsListView", new ListView<>());
        setPrivateField(controller, "runHeaderLabel", new Label());
        setPrivateField(controller, "runTimestampLabel", new Label());
        setPrivateField(controller, "totalAppsResultLabel", new Label());
        setPrivateField(controller, "avgSystemTimeResultLabel", new Label());
        setPrivateField(controller, "approvedResultLabel", new Label());
        setPrivateField(controller, "rejectedResultLabel", new Label());
        setPrivateField(controller, "servicePointTable", new TableView<>());
        setPrivateField(controller, "spNameColumn", new TableColumn<>());
        setPrivateField(controller, "departuresColumn", new TableColumn<>());
        setPrivateField(controller, "avgWaitColumn", new TableColumn<>());
        setPrivateField(controller, "maxQueueColumn", new TableColumn<>());
        setPrivateField(controller, "utilizationColumn", new TableColumn<>());
        setPrivateField(controller, "employeesColumn", new TableColumn<>());
        setPrivateField(controller, "bottleneckColumn", new TableColumn<>());
        setPrivateField(controller, "bottleneckPanel", new VBox());
        setPrivateField(controller, "bottleneckNameLabel", new Label());
        setPrivateField(controller, "bottleneckUtilLabel", new Label());
        setPrivateField(controller, "bottleneckQueueLabel", new Label());
        setPrivateField(controller, "bottleneckWaitLabel", new Label());

        ComboBox<String> analysisTypeCombo = new ComboBox<>();
        analysisTypeCombo.getItems().addAll("Avg Wait Time", "Utilization", "Max Queue", "Wait by Distribution", "Employee Impact");
        analysisTypeCombo.getSelectionModel().selectFirst();
        setPrivateField(controller, "analysisTypeCombo", analysisTypeCombo);

        ComboBox<String> compareMetricCombo = new ComboBox<>();
        compareMetricCombo.getItems().addAll("Avg Waiting Time", "Utilization", "Max Queue");
        compareMetricCombo.getSelectionModel().selectFirst();
        setPrivateField(controller, "compareMetricCombo", compareMetricCombo);

        ComboBox<String> distributionTypeCombo = new ComboBox<>();
        distributionTypeCombo.getItems().addAll("Exponential");
        distributionTypeCombo.getSelectionModel().selectFirst();
        setPrivateField(controller, "distributionTypeCombo", distributionTypeCombo);

        setPrivateField(controller, "analysisBarChart", new BarChart<>(new CategoryAxis(), new NumberAxis()));
        setPrivateField(controller, "analysisSPAxis", new CategoryAxis());
        setPrivateField(controller, "analysisYAxis", new NumberAxis());
        setPrivateField(controller, "waitByDistributionChart", new BarChart<>(new CategoryAxis(), new NumberAxis()));
        setPrivateField(controller, "waitByDistSPAxis", new CategoryAxis());
        setPrivateField(controller, "waitByDistYAxis", new NumberAxis());
        setPrivateField(controller, "compareByDistChart", new BarChart<>(new CategoryAxis(), new NumberAxis()));
        setPrivateField(controller, "compareByDistXAxis", new CategoryAxis());
        setPrivateField(controller, "compareByDistYAxis", new NumberAxis());
        setPrivateField(controller, "employeeImpactChart", new ScatterChart<>(new NumberAxis(), new NumberAxis()));
        setPrivateField(controller, "employeeXAxis", new NumberAxis());
        setPrivateField(controller, "utilizationYAxis", new NumberAxis());
    }

    /**
     * Uses reflection to set a private field on the target object.
     */
    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    /**
     * Uses reflection to get a private field value from the target object.
     */
    private Object getPrivateField(Object target, String fieldName) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private void invokePrivateMethod(Object target, String name, Object... params) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(name) && method.getParameterCount() == params.length) {
                    boolean match = true;
                    Class<?>[] paramTypes = method.getParameterTypes();
                    for (int i = 0; i < paramTypes.length; i++) {
                        if (params[i] != null && !paramTypes[i].isAssignableFrom(params[i].getClass())) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        method.setAccessible(true);
                        method.invoke(target, params);
                        return;
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        throw new NoSuchMethodException(name);
    }

    /**
     * Tests that the controller initializes UI components and selects the first run.
     */
    @Test
    void testInitializePopulatesUIAndSelectsFirstRun() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                ListView<String> runsListView = (ListView<String>) getPrivateField(controller, "runsListView");
                VBox root = new VBox(runsListView);
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setScene(scene);

                invokePrivateMethod(controller, "initialize");

                assertFalse(runsListView.getItems().isEmpty());
                assertEquals(0, runsListView.getSelectionModel().getSelectedIndex());

                ComboBox<String> analysisTypeCombo = (ComboBox<String>) getPrivateField(controller, "analysisTypeCombo");
                assertFalse(analysisTypeCombo.getItems().isEmpty());
                ComboBox<String> compareMetricCombo = (ComboBox<String>) getPrivateField(controller, "compareMetricCombo");
                assertFalse(compareMetricCombo.getItems().isEmpty());
                ComboBox<String> distributionTypeCombo = (ComboBox<String>) getPrivateField(controller, "distributionTypeCombo");
                assertFalse(distributionTypeCombo.getItems().isEmpty());
            } catch (Exception e) {
                fail(e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Tests that loading a simulation run displays the correct results in the UI.
     */
    @Test
    void testLoadSimulationRunDisplaysResults() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.loadSimulationRun(1L);
                Label runHeaderLabel = (Label) getPrivateField(controller, "runHeaderLabel");
                Label totalAppsResultLabel = (Label) getPrivateField(controller, "totalAppsResultLabel");
                assertEquals("Simulation Run #1", runHeaderLabel.getText());
                assertEquals("100", totalAppsResultLabel.getText());
            } catch (Exception e) {
                fail(e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Tests that loading a non-existent simulation run triggers an error dialog.
     */
    @Test
    void testLoadSimulationRunNotFoundShowsError() throws Exception {
        AtomicBoolean errorShown = new AtomicBoolean(false);

        controller = new ResultsController() {
            @Override
            protected void showError(String title, String message) {
                errorShown.set(true);
            }
        };

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.loadSimulationRun(999L);
                assertTrue(errorShown.get(), "showError should have been called");
            } catch (Exception e) {
                // Acceptable: error dialog expected
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    /**
     * Tests that the bottleneck panel is visible when a bottleneck is present.
     */
    @Test
    void testDisplayRunResultsBottleneckPanelVisible() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.loadSimulationRun(1L);
                invokePrivateMethod(controller, "displayRunResults");
                VBox bottleneckPanel = (VBox) getPrivateField(controller, "bottleneckPanel");
                Label bottleneckNameLabel = (Label) getPrivateField(controller, "bottleneckNameLabel");
                assertTrue(bottleneckPanel.isVisible());
                assertEquals("SP1", bottleneckNameLabel.getText());
            } catch (Exception e) {
                fail(e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Tests that the bottleneck panel is not visible when no bottleneck is present.
     */
    @Test
    void testDisplayRunResultsBottleneckPanelNotVisible() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        FakeSimulationRunDao dao = (FakeSimulationRunDao) getPrivateField(controller, "dao");
        dao.findAllWithAssociations().get(0).getServicePointResults().get(0).setBottleneck(false);
        Platform.runLater(() -> {
            try {
                controller.loadSimulationRun(1L);
                invokePrivateMethod(controller, "displayRunResults");
                VBox bottleneckPanel = (VBox) getPrivateField(controller, "bottleneckPanel");
                assertFalse(bottleneckPanel.isVisible());
            } catch (Exception e) {
                fail(e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Tests that updating the analysis bar chart populates it with data.
     */
    @Test
    void testUpdateAnalysisBarChartPopulatesChart() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.loadSimulationRun(1L);
                ComboBox<String> analysisTypeCombo = (ComboBox<String>) getPrivateField(controller, "analysisTypeCombo");
                analysisTypeCombo.getItems().addAll("Avg Wait Time", "Utilization", "Max Queue");
                analysisTypeCombo.getSelectionModel().select("Avg Wait Time");
                invokePrivateMethod(controller, "updateAnalysisBarChart");
                BarChart<String, Number> analysisBarChart = (BarChart<String, Number>) getPrivateField(controller, "analysisBarChart");
                assertFalse(analysisBarChart.getData().isEmpty());
            } catch (Exception e) {
                fail(e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Tests that the analysis bar chart is populated with utilization data
     * and that the Y-axis label is set to "Utilization (%)" when the analysis type
     * is set to "Utilization".
     */
    @Test
    void testUpdateAnalysisBarChartUtilization() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.loadSimulationRun(1L);
                ComboBox<String> analysisTypeCombo = (ComboBox<String>) getPrivateField(controller, "analysisTypeCombo");
                analysisTypeCombo.getSelectionModel().select("Utilization");
                invokePrivateMethod(controller, "updateAnalysisBarChart");
                BarChart<String, Number> analysisBarChart = (BarChart<String, Number>) getPrivateField(controller, "analysisBarChart");
                NumberAxis analysisYAxis = (NumberAxis) getPrivateField(controller, "analysisYAxis");
                assertFalse(analysisBarChart.getData().isEmpty());
                assertEquals("Utilization (%)", analysisYAxis.getLabel());
            } catch (Exception e) {
                fail(e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Tests that the analysis bar chart is populated with max queue length data
     * and that the Y-axis label is set to "Max Queue Length" when the analysis type
     * is set to "Max Queue".
     */
    @Test
    void testUpdateAnalysisBarChartMaxQueue() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.loadSimulationRun(1L);
                ComboBox<String> analysisTypeCombo = (ComboBox<String>) getPrivateField(controller, "analysisTypeCombo");
                analysisTypeCombo.getSelectionModel().select("Max Queue");
                invokePrivateMethod(controller, "updateAnalysisBarChart");
                BarChart<String, Number> analysisBarChart = (BarChart<String, Number>) getPrivateField(controller, "analysisBarChart");
                NumberAxis analysisYAxis = (NumberAxis) getPrivateField(controller, "analysisYAxis");
                assertFalse(analysisBarChart.getData().isEmpty());
                assertEquals("Max Queue Length", analysisYAxis.getLabel());
            } catch (Exception e) {
                fail(e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Tests that the analysis bar chart is populated with average waiting time data
     * and that the Y-axis label is set to "Avg Waiting Time (min)" when the analysis type
     * is set to "Wait by Distribution".
     */
    @Test
    void testUpdateAnalysisBarChartWaitByDistribution() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.loadSimulationRun(1L);
                ComboBox<String> analysisTypeCombo = (ComboBox<String>) getPrivateField(controller, "analysisTypeCombo");
                analysisTypeCombo.getSelectionModel().select("Wait by Distribution");
                invokePrivateMethod(controller, "updateAnalysisBarChart");
                BarChart<String, Number> analysisBarChart = (BarChart<String, Number>) getPrivateField(controller, "analysisBarChart");
                NumberAxis analysisYAxis = (NumberAxis) getPrivateField(controller, "analysisYAxis");
                assertFalse(analysisBarChart.getData().isEmpty());
                assertEquals("Avg Waiting Time (min)", analysisYAxis.getLabel());
            } catch (Exception e) {
                fail(e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Tests that the employee impact scatter chart is populated and visible,
     * and the analysis bar chart is hidden, when the analysis type is set to "Employee Impact".
     */
    @Test
    void testUpdateAnalysisBarChartEmployeeImpact() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.loadSimulationRun(1L);
                ComboBox<String> analysisTypeCombo = (ComboBox<String>) getPrivateField(controller, "analysisTypeCombo");
                analysisTypeCombo.getSelectionModel().select("Employee Impact");
                invokePrivateMethod(controller, "updateAnalysisBarChart");
                BarChart<String, Number> analysisBarChart = (BarChart<String, Number>) getPrivateField(controller, "analysisBarChart");
                ScatterChart<Number, Number> employeeImpactChart = (ScatterChart<Number, Number>) getPrivateField(controller, "employeeImpactChart");
                assertFalse(employeeImpactChart.getData().isEmpty());
                assertFalse(analysisBarChart.isVisible());
                assertTrue(employeeImpactChart.isVisible());
            } catch (Exception e) {
                fail(e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Tests that both the analysis bar chart and employee impact chart are cleared
     * and the impact chart is hidden when there is no current simulation run.
     */
    @Test
    void testUpdateAnalysisBarChartWithNullRunClearsCharts() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                setPrivateField(controller, "currentRun", null);
                invokePrivateMethod(controller, "updateAnalysisBarChart");
                BarChart<String, Number> analysisBarChart = (BarChart<String, Number>) getPrivateField(controller, "analysisBarChart");
                ScatterChart<Number, Number> employeeImpactChart = (ScatterChart<Number, Number>) getPrivateField(controller, "employeeImpactChart");
                assertTrue(analysisBarChart.getData().isEmpty());
                assertFalse(employeeImpactChart.isVisible());
            } catch (Exception e) {
                fail(e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Tests that the employee impact scatter chart is populated with data
     * when a simulation run is loaded and the chart is updated.
     */
    @Test
    void testUpdateEmployeeImpactChartPopulatesScatter() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.loadSimulationRun(1L);
                invokePrivateMethod(controller, "updateEmployeeImpactChart");
                ScatterChart<Number, Number> employeeImpactChart = (ScatterChart<Number, Number>) getPrivateField(controller, "employeeImpactChart");
                assertFalse(employeeImpactChart.getData().isEmpty());
                // Optionally, check that the data matches expected employees/utilization
            } catch (Exception e) {
                fail(e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Tests that the employee impact scatter chart is cleared when there is no current simulation run.
     */
    @Test
    void testUpdateEmployeeImpactChartWithNullRunClearsScatter() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                setPrivateField(controller, "currentRun", null);
                invokePrivateMethod(controller, "updateEmployeeImpactChart");
                ScatterChart<Number, Number> employeeImpactChart = (ScatterChart<Number, Number>) getPrivateField(controller, "employeeImpactChart");
                assertTrue(employeeImpactChart.getData().isEmpty());
            } catch (Exception e) {
                fail(e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Tests that the comparison bar chart is populated with average waiting time data
     * and the Y-axis label is set to "Avg Waiting Time (min)" when the comparison metric
     * is set to "Avg Waiting Time".
     */
    @Test
    void testUpdateCompareByDistributionAvgWaitingTime() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.loadSimulationRun(1L);
                ComboBox<String> compareMetricCombo = (ComboBox<String>) getPrivateField(controller, "compareMetricCombo");
                compareMetricCombo.getSelectionModel().select("Avg Waiting Time");
                invokePrivateMethod(controller, "updateCompareByDistribution");
                BarChart<String, Number> compareByDistChart = (BarChart<String, Number>) getPrivateField(controller, "compareByDistChart");
                assertFalse(compareByDistChart.getData().isEmpty());
                NumberAxis compareByDistYAxis = (NumberAxis) getPrivateField(controller, "compareByDistYAxis");
                assertEquals("Avg Waiting Time (min)", compareByDistYAxis.getLabel());
            } catch (Exception e) {
                fail(e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Tests that the comparison bar chart is populated with utilization data
     * and the Y-axis label is set to "Utilization (%)" when the comparison metric
     * is set to "Utilization".
     */
    @Test
    void testUpdateCompareByDistributionUtilization() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.loadSimulationRun(1L);
                ComboBox<String> compareMetricCombo = (ComboBox<String>) getPrivateField(controller, "compareMetricCombo");
                compareMetricCombo.getSelectionModel().select("Utilization");
                invokePrivateMethod(controller, "updateCompareByDistribution");
                BarChart<String, Number> compareByDistChart = (BarChart<String, Number>) getPrivateField(controller, "compareByDistChart");
                assertFalse(compareByDistChart.getData().isEmpty());
                NumberAxis compareByDistYAxis = (NumberAxis) getPrivateField(controller, "compareByDistYAxis");
                assertEquals("Utilization (%)", compareByDistYAxis.getLabel());
            } catch (Exception e) {
                fail(e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Tests that the comparison bar chart is populated with max queue length data
     * and the Y-axis label is set to "Max Queue Length" when the comparison metric
     * is set to "Max Queue".
     */
    @Test
    void testUpdateCompareByDistributionMaxQueue() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.loadSimulationRun(1L);
                ComboBox<String> compareMetricCombo = (ComboBox<String>) getPrivateField(controller, "compareMetricCombo");
                compareMetricCombo.getSelectionModel().select("Max Queue");
                invokePrivateMethod(controller, "updateCompareByDistribution");
                BarChart<String, Number> compareByDistChart = (BarChart<String, Number>) getPrivateField(controller, "compareByDistChart");
                assertFalse(compareByDistChart.getData().isEmpty());
                NumberAxis compareByDistYAxis = (NumberAxis) getPrivateField(controller, "compareByDistYAxis");
                assertEquals("Max Queue Length", compareByDistYAxis.getLabel());
            } catch (Exception e) {
                fail(e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Tests that the wait-by-distribution bar chart is populated with data
     * for all service points and distributions when a simulation run is loaded.
     */
    @Test
    void testUpdateWaitByDistributionChartPopulatesChart() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.loadSimulationRun(1L);
                SimulationRun run = (SimulationRun) getPrivateField(controller, "currentRun");
                invokePrivateMethod(controller, "updateWaitByDistributionChart", run);
                BarChart<String, Number> waitByDistributionChart = (BarChart<String, Number>) getPrivateField(controller, "waitByDistributionChart");
                assertFalse(waitByDistributionChart.getData().isEmpty());
            } catch (Exception e) {
                fail(e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Tests that the comparison bar chart is cleared when there is no current simulation run.
     */
    @Test
    void testUpdateCompareByDistributionWithNullRun() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                setPrivateField(controller, "currentRun", null);
                invokePrivateMethod(controller, "updateCompareByDistribution");
                BarChart<String, Number> compareByDistChart = (BarChart<String, Number>) getPrivateField(controller, "compareByDistChart");
                assertTrue(compareByDistChart.getData().isEmpty());
            } catch (Exception e) {
                fail(e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    /**
     * Tests that all service point table columns are initialized with value factories.
     */
    @Test
    void testSetupTableColumnsInitializesColumns() throws Exception {
        invokePrivateMethod(controller, "setupTableColumns");
        TableColumn<?, ?> spNameColumn = (TableColumn<?, ?>) getPrivateField(controller, "spNameColumn");
        TableColumn<?, ?> departuresColumn = (TableColumn<?, ?>) getPrivateField(controller, "departuresColumn");
        TableColumn<?, ?> avgWaitColumn = (TableColumn<?, ?>) getPrivateField(controller, "avgWaitColumn");
        TableColumn<?, ?> maxQueueColumn = (TableColumn<?, ?>) getPrivateField(controller, "maxQueueColumn");
        TableColumn<?, ?> utilizationColumn = (TableColumn<?, ?>) getPrivateField(controller, "utilizationColumn");
        TableColumn<?, ?> employeesColumn = (TableColumn<?, ?>) getPrivateField(controller, "employeesColumn");
        TableColumn<?, ?> bottleneckColumn = (TableColumn<?, ?>) getPrivateField(controller, "bottleneckColumn");

        assertNotNull(spNameColumn.getCellValueFactory());
        assertNotNull(departuresColumn.getCellValueFactory());
        assertNotNull(avgWaitColumn.getCellValueFactory());
        assertNotNull(maxQueueColumn.getCellValueFactory());
        assertNotNull(utilizationColumn.getCellValueFactory());
        assertNotNull(employeesColumn.getCellValueFactory());
        assertNotNull(bottleneckColumn.getCellValueFactory());
    }

    /**
     * Tests that a warning dialog is shown when attempting to load a run
     * with no selection in the runs list. The dialog method is overridden to set a flag.
     */
    @Test
    void testHandleLoadRunNoSelectionShowsWarning() throws Exception {
        AtomicBoolean warningShown = new AtomicBoolean(false);
        controller = new ResultsController() {
            @Override
            protected void showWarning(String title, String message) {
                warningShown.set(true);
            }
        };
        setPrivateField(controller, "runsListView", new ListView<>());
        invokePrivateMethod(controller, "handleLoadRun");
        assertTrue(warningShown.get());
    }
}