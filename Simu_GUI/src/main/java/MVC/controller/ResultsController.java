package MVC.controller;

import ORM.dao.*;
import ORM.entity.DistConfig;
import ORM.entity.SPResult;
import ORM.entity.SimulationRun;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import MVC.view.HomeView;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for the results view in the Visa Application Processing Simulator.
 * Handles loading, displaying, and analyzing simulation run results, including
 * charts, tables, and bottleneck detection. Integrates with the DAO for data access
 * and updates the UI using JavaFX components.
 *
 * <p>Features:
 * <ul>
 *   <li>Loads simulation runs and their results from the database</li>
 *   <li>Displays run details, service point metrics, and bottleneck info</li>
 *   <li>Provides interactive charts for analysis (bar, scatter, distribution)</li>
 *   <li>Supports filtering and comparison by distribution type and metric</li>
 *   <li>Handles run selection, deletion, and navigation</li>
 * </ul>
 * </p>
 */
public class ResultsController {

    /** ListView for displaying simulation runs. */
    @FXML
    private ListView<String> runsListView;

    /** Label for the run header (run number). */
    @FXML
    private Label runHeaderLabel;

    /** Label for the run timestamp. */
    @FXML
    private Label runTimestampLabel;

    /** Label for total applications processed in the run. */
    @FXML
    private Label totalAppsResultLabel;

    /** Label for average system time in the run. */
    @FXML
    private Label avgSystemTimeResultLabel;

    /** Label for approved applications count. */
    @FXML
    private Label approvedResultLabel;

    /** Label for rejected applications count. */
    @FXML
    private Label rejectedResultLabel;

    /** TableView for displaying service point results. */
    @FXML
    private TableView<SPResult> servicePointTable;

    /** TableColumn for service point name. */
    @FXML
    private TableColumn<SPResult, String> spNameColumn;

    /** TableColumn for departures count. */
    @FXML
    private TableColumn<SPResult, Integer> departuresColumn;

    /** TableColumn for average waiting time. */
    @FXML
    private TableColumn<SPResult, Double> avgWaitColumn;

    /** TableColumn for maximum queue length. */
    @FXML
    private TableColumn<SPResult, Integer> maxQueueColumn;

    /** TableColumn for utilization percentage. */
    @FXML
    private TableColumn<SPResult, Double> utilizationColumn;

    /** TableColumn for number of employees. */
    @FXML
    private TableColumn<SPResult, Integer> employeesColumn;

    /** TableColumn for bottleneck indicator. */
    @FXML
    private TableColumn<SPResult, String> bottleneckColumn;

    /** Panel for displaying bottleneck details. */
    @FXML
    private VBox bottleneckPanel;

    /** Label for bottleneck service point name. */
    @FXML
    private Label bottleneckNameLabel;

    /** Label for bottleneck utilization. */
    @FXML
    private Label bottleneckUtilLabel;

    /** Label for bottleneck queue length. */
    @FXML
    private Label bottleneckQueueLabel;

    /** Label for bottleneck waiting time. */
    @FXML
    private Label bottleneckWaitLabel;

    /** ComboBox for selecting analysis type. */
    @FXML private ComboBox<String> analysisTypeCombo;

    /** BarChart for displaying analysis results. */
    @FXML private BarChart<String, Number> analysisBarChart;

    /** CategoryAxis for service point names in analysis chart. */
    @FXML private CategoryAxis analysisSPAxis;

    /** NumberAxis for metric values in analysis chart. */
    @FXML private NumberAxis analysisYAxis;

    /** BarChart for waiting time by distribution. */
    @FXML private BarChart<String, Number> waitByDistributionChart;

    /** CategoryAxis for service point names in distribution chart. */
    @FXML private CategoryAxis waitByDistSPAxis;

    /** NumberAxis for waiting time in distribution chart. */
    @FXML private NumberAxis waitByDistYAxis;

    /** ComboBox for selecting distribution type. */
    @FXML private ComboBox<String> distributionTypeCombo;

    /** BarChart for comparing metrics by distribution. */
    @FXML private BarChart<String, Number> compareByDistChart;

    /** CategoryAxis for service point names in comparison chart. */
    @FXML private CategoryAxis compareByDistXAxis;

    /** NumberAxis for metric values in comparison chart. */
    @FXML private NumberAxis compareByDistYAxis;

    /** ScatterChart for employee impact analysis. */
    @FXML private ScatterChart<Number, Number> employeeImpactChart;

    /** NumberAxis for number of employees in scatter chart. */
    @FXML private NumberAxis employeeXAxis;

    /** NumberAxis for utilization in scatter chart. */
    @FXML private NumberAxis utilizationYAxis;

    /** ComboBox for selecting metric to compare. */
    @FXML private ComboBox<String> compareMetricCombo;

    /** DAO for accessing simulation run data. */
    private SimulationRunDao dao;

    private SimulationRunDao getDao() {
        if (dao == null) {
            dao = new SimulationRunDao();
        }
        return dao;
    }
    /** Currently selected simulation run. */
    private SimulationRun currentRun;

    /** List of all loaded simulation runs. */
    private List<SimulationRun> allRuns;

    /**
     * Initializes the controller, sets up table columns, combo boxes, and loads runs.
     * Populates UI elements and sets up event handlers for analysis and navigation.
     */
    @FXML
    private void initialize() {
        setupTableColumns();

        // Combo box options for analysis type
        compareMetricCombo.getItems().setAll("Avg Waiting Time", "Utilization", "Max Queue");
        compareMetricCombo.setOnAction(e -> updateWaitByDistributionChart(currentRun));
        compareMetricCombo.getSelectionModel().selectFirst();

        analysisTypeCombo.getItems().addAll("Avg Wait Time", "Utilization", "Max Queue", "Wait by Distribution", "Employee Impact");
        analysisTypeCombo.setOnAction(e -> updateAnalysisBarChart());
        analysisTypeCombo.getSelectionModel().selectFirst();
        updateAnalysisBarChart();
        loadAllRuns();

        // Distribution type options for filtering and comparing
        Set<String> allDists = getAllDistributionTypesAcrossRuns();
        distributionTypeCombo.getItems().addAll(allDists);
        distributionTypeCombo.setOnAction(e -> updateCompareByDistribution());
        if (!distributionTypeCombo.getItems().isEmpty()) {
            distributionTypeCombo.getSelectionModel().selectFirst();
        }
        // Select latest run
        if (!allRuns.isEmpty()) {
            runsListView.getSelectionModel().selectFirst();
            handleLoadRun();
        }
    }

    /**
     * Gathers all unique distribution types across all loaded simulation runs.
     *
     * @return Set of distribution type names
     */
    private Set<String> getAllDistributionTypesAcrossRuns() {
        Set<String> allDists = new HashSet<>();
        for (SimulationRun run : allRuns) {
            if (run.getDistConfiguration() != null) {
                allDists.addAll(run.getDistConfiguration().stream()
                        .map(cfg -> cfg.getDistributionType().trim())
                        .collect(Collectors.toSet()));
            }
        }
        return allDists;
    }

    /**
     * Updates the comparison chart by distribution type and selected metric.
     * Sets axis labels and populates chart data for the current run.
     */
    private void updateCompareByDistribution() {
        compareByDistChart.getData().clear();
        String selectedDist = distributionTypeCombo.getValue();
        String selectedMetric = compareMetricCombo.getValue();
        if (selectedDist == null || selectedMetric == null || currentRun == null) return;

        // Dynamically update Y-axis label for selected metric
        if ("Max Queue".equals(selectedMetric)) {
            compareByDistYAxis.setLabel("Max Queue Length");
        } else if ("Utilization".equals(selectedMetric)) {
            compareByDistYAxis.setLabel("Utilization (%)");
        } else {
            compareByDistYAxis.setLabel("Avg Waiting Time (min)");
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Run #" + currentRun.getId());
        List<String> spNames = currentRun.getServicePointResults().stream()
                .map(sp -> sp.getServicePointName() == null ? "Unnamed" : sp.getServicePointName().trim())
                .distinct().toList();
        for (String spName : spNames) {
            Optional<SPResult> ospr = currentRun.getServicePointResults().stream()
                    .filter(sp -> sp.getServicePointName().trim().equals(spName)).findFirst();
            Optional<DistConfig> config = currentRun.getDistConfiguration().stream()
                    .filter(cfg -> cfg.getServicePointName().trim().equals(spName) &&
                            cfg.getDistributionType().trim().equals(selectedDist)).findFirst();

            double value = 0.0;
            if (ospr.isPresent() && config.isPresent()) {
                if ("Max Queue".equals(selectedMetric)) {
                    value = ospr.get().getMaxQueueLength();
                } else if ("Utilization".equals(selectedMetric)) {
                    value = ospr.get().getUtilization();
                } else {
                    value = ospr.get().getAvgWaitingTime();
                }
            }
            series.getData().add(new XYChart.Data<>(spName, value));
        }
        compareByDistChart.getData().add(series);

        // Explicitly set axis labels and categories:
        compareByDistXAxis.setLabel("Service Point");
        compareByDistXAxis.setCategories(FXCollections.observableArrayList(spNames));
        compareByDistXAxis.setTickLabelRotation(45);
        compareByDistChart.setLegendVisible(true);
    }

    /**
     * Updates the bar chart that shows average waiting times per service point,
     * grouped by distribution type, for the specified simulation run.
     *
     * @param run the simulation run to visualize
     */

    private void updateWaitByDistributionChart(SimulationRun run) {
        waitByDistributionChart.getData().clear();
        List<String> allSPNames = run.getServicePointResults().stream()
                .map(sp -> sp.getServicePointName() == null ? "Unnamed" : sp.getServicePointName().trim())
                .distinct().toList();
        Set<String> allDists = run.getDistConfiguration().stream()
                .map(cfg -> cfg.getDistributionType().trim()).collect(Collectors.toSet());
        for (String dist : allDists) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(dist);
            for (String spName : allSPNames) {
                Optional<SPResult> ospr = run.getServicePointResults().stream()
                        .filter(sp -> sp.getServicePointName().trim().equals(spName)).findFirst();
                Optional<DistConfig> config = run.getDistConfiguration().stream()
                        .filter(cfg -> cfg.getServicePointName().trim().equals(spName) &&
                                cfg.getDistributionType().trim().equals(dist)).findFirst();
                double value = (ospr.isPresent() && config.isPresent()) ? ospr.get().getAvgWaitingTime() : 0.0;
                series.getData().add(new XYChart.Data<>(spName, value));
            }
            waitByDistributionChart.getData().add(series);
        }
        waitByDistSPAxis.setCategories(FXCollections.observableArrayList(allSPNames));
        // Explicitly set axis labels and categories:
        waitByDistSPAxis.setLabel("Service Point");
        waitByDistSPAxis.setTickLabelRotation(45);
        waitByDistributionChart.setLegendVisible(true);
        waitByDistYAxis.setLabel("Avg Waiting Time (min)");
    }

    /**
     * Updates the analysis chart based on the selected metric and the currently loaded simulation run.
     * Displays either a bar chart or scatter chart depending on the analysis type.
     */
    private void updateAnalysisBarChart() {
        if (currentRun == null) {
            analysisBarChart.getData().clear();
            employeeImpactChart.setVisible(false);
            return;
        }
        String selected = analysisTypeCombo.getValue();

        analysisBarChart.setVisible(!"Employee Impact".equals(selected));
        employeeImpactChart.setVisible("Employee Impact".equals(selected));

        if ("Employee Impact".equals(selected)) {
            employeeImpactChart.setVisible(true);
            analysisBarChart.setVisible(false);
            updateEmployeeImpactChart();
            return;
        }

        analysisBarChart.setVisible(true);
        employeeImpactChart.setVisible(false);
        analysisBarChart.getData().clear();

        // Build complete list of service point names
        List<String> allSPNames = currentRun.getServicePointResults().stream()
                .map(sp -> {
                    String spName = sp.getServicePointName();
                    if (spName == null || spName.trim().isEmpty()) {
                        return "Unnamed";
                    }
                    return spName.trim();
                })
                .distinct()
                .toList();

        // --- Dynamic Y-axis label logic ---
        if ("Avg Wait Time".equals(selected)) {
            analysisYAxis.setLabel("Avg Waiting Time (min)");
            analysisYAxis.setAutoRanging(true);
        } else if ("Utilization".equals(selected)) {
            analysisYAxis.setLabel("Utilization (%)");
            analysisYAxis.setAutoRanging(false);
            analysisYAxis.setLowerBound(0);
            analysisYAxis.setUpperBound(100);
            analysisYAxis.setTickUnit(10);
        } else if ("Max Queue".equals(selected)) {
            analysisYAxis.setLabel("Max Queue Length");
            analysisYAxis.setAutoRanging(true);
        } else if ("Wait by Distribution".equals(selected)) {
            analysisYAxis.setLabel("Avg Waiting Time (min)");
        }

        switch (selected) {
            case "Avg Wait Time" -> {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Avg Wait (min)");
                for (String spName : allSPNames) {
                    Optional<SPResult> match = currentRun.getServicePointResults().stream()
                            .filter(sp -> {
                                String name = sp.getServicePointName();
                                if (name == null || name.trim().isEmpty()) name = "Unnamed";
                                return name.trim().equalsIgnoreCase(spName);
                            }).findFirst();
                    double value = match.map(SPResult::getAvgWaitingTime).orElse(0.0);
                    series.getData().add(new XYChart.Data<>(spName, value));
                }
                analysisBarChart.getData().add(series);
            }
            case "Utilization" -> {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Utilization (%)");
                for (String spName : allSPNames) {
                    Optional<SPResult> match = currentRun.getServicePointResults().stream()
                            .filter(sp -> {
                                String name = sp.getServicePointName();
                                if (name == null || name.trim().isEmpty()) name = "Unnamed";
                                return name.trim().equalsIgnoreCase(spName);
                            }).findFirst();
                    double value = match.map(SPResult::getUtilization).orElse(0.0);
                    series.getData().add(new XYChart.Data<>(spName, value));
                }
                analysisBarChart.getData().add(series);
            }
            case "Max Queue" -> {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Max Queue");
                for (String spName : allSPNames) {
                    Optional<SPResult> match = currentRun.getServicePointResults().stream()
                            .filter(sp -> {
                                String name = sp.getServicePointName();
                                if (name == null || name.trim().isEmpty()) name = "Unnamed";
                                return name.trim().equalsIgnoreCase(spName);
                            }).findFirst();
                    int value = match.map(SPResult::getMaxQueueLength).orElse(0);
                    series.getData().add(new XYChart.Data<>(spName, value));
                }
                analysisBarChart.getData().add(series);
            }
            case "Wait by Distribution" -> {
                analysisSPAxis.setLabel("Service Point");
                Set<String> allDists = currentRun.getDistConfiguration().stream()
                        .filter(cfg -> cfg.getServicePointName() != null)
                        .map(cfg -> cfg.getDistributionType().trim())
                        .collect(Collectors.toSet());

                Map<String, XYChart.Series<String, Number>> distSeries = new LinkedHashMap<>();
                for (String dist : allDists) {
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName(dist);
                    for (String spName : allSPNames) {
                        Optional<SPResult> ospr = currentRun.getServicePointResults().stream()
                                .filter(sp -> {
                                    String serviceName = (sp.getServicePointName() == null) ? "Unnamed" : sp.getServicePointName().trim();
                                    return serviceName.equalsIgnoreCase(spName.trim());
                                }).findFirst();

                        Optional<DistConfig> config = currentRun.getDistConfiguration().stream()
                                .filter(cfg -> {
                                    String cfgName = (cfg.getServicePointName() == null) ? "Unnamed" : cfg.getServicePointName().trim();
                                    return cfgName.equalsIgnoreCase(spName.trim())
                                            && cfg.getDistributionType().trim().equalsIgnoreCase(dist.trim());
                                }).findFirst();

                        double value = (ospr.isPresent() && config.isPresent())
                                ? ospr.get().getAvgWaitingTime()
                                : 0.0;
                        series.getData().add(new XYChart.Data<>(spName, value));
                    }
                    distSeries.put(dist, series);
                }
                analysisBarChart.getData().setAll(distSeries.values());
            }
        }

        // Explicitly set axis categories and update labels (for all chart cases!)
        analysisSPAxis.setCategories(FXCollections.observableArrayList(allSPNames));
        analysisSPAxis.setTickLabelsVisible(true);
        analysisSPAxis.setAutoRanging(true);
        analysisSPAxis.setTickLabelGap(1);
        analysisSPAxis.setTickLabelRotation(45);
    }

    /**
     * Updates the scatter chart showing the relationship between the number of employees
     * and utilization for each service point in the current run.
     */
    private void updateEmployeeImpactChart() {
        if (currentRun == null || currentRun.getServicePointResults() == null) {
            employeeImpactChart.getData().clear();
            return;
        }
        employeeImpactChart.getData().clear();

        List<SPResult> spResults = currentRun.getServicePointResults();

        int minEmployees = spResults.stream().mapToInt(SPResult::getNumEmployees).min().orElse(0);
        int maxEmployees = spResults.stream().mapToInt(SPResult::getNumEmployees).max().orElse(10);
        double minUtilization = spResults.stream().mapToDouble(SPResult::getUtilization).min().orElse(0);
        double maxUtilization = spResults.stream().mapToDouble(SPResult::getUtilization).max().orElse(100);

        for (SPResult sp : spResults) {
            XYChart.Series<Number, Number> spSeries = new XYChart.Series<>();
            spSeries.setName(sp.getServicePointName());
            XYChart.Data<Number, Number> data = new XYChart.Data<>(sp.getNumEmployees(), sp.getUtilization());
            spSeries.getData().add(data);
            employeeImpactChart.getData().add(spSeries);
            Tooltip.install(data.getNode(), new Tooltip(
                    sp.getServicePointName() + ": Employees=" + sp.getNumEmployees() +
                            ", Utilization=" + String.format("%.1f%%", sp.getUtilization())
            ));
        }

        employeeXAxis.setLabel("Number of Employees");
        utilizationYAxis.setLabel("Utilization (%)");
        employeeXAxis.setAutoRanging(false);
        employeeXAxis.setLowerBound(Math.max(0, minEmployees - 1)); // Add buffer
        employeeXAxis.setUpperBound(maxEmployees + 2); // Add buffer

        utilizationYAxis.setAutoRanging(false);
        utilizationYAxis.setLowerBound(Math.max(0, minUtilization - 5)); // Add buffer for clarity
        utilizationYAxis.setUpperBound(Math.min(100, maxUtilization + 10)); // Add buffer but not if max is low

        employeeXAxis.setTickUnit(1);
        utilizationYAxis.setTickUnit(10);
    }

    /**
     * Sets up table columns for the service point results table.
     * Configures cell factories for formatting and bottleneck indication.
     */
    private void setupTableColumns() {
        spNameColumn.setCellValueFactory(new PropertyValueFactory<>("servicePointName"));
        departuresColumn.setCellValueFactory(new PropertyValueFactory<>("departures"));
        avgWaitColumn.setCellValueFactory(new PropertyValueFactory<>("avgWaitingTime"));
        avgWaitColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f", item));
            }
        });
        maxQueueColumn.setCellValueFactory(new PropertyValueFactory<>("maxQueueLength"));
        utilizationColumn.setCellValueFactory(new PropertyValueFactory<>("utilization"));
        utilizationColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.1f%%", item));
                    setStyle(item > 85 ? "-fx-text-fill: #E74C3C; -fx-font-weight: bold;" :
                            item > 70 ? "-fx-text-fill: #F39C12;" : "-fx-text-fill: #27AE60;");
                }
            }
        });
        employeesColumn.setCellValueFactory(new PropertyValueFactory<>("numEmployees"));
        bottleneckColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().isBottleneck() ? "🔴 BOTTLENECK" : ""));
    }

    /**
     * Loads all simulation runs, sorts them by timestamp in descending order,
     * formats them for display, and populates the run list view.
     *
     * <p>Skips invalid runs (null or missing ID/timestamp). Shows an error alert if loading fails.</p>
     */
    private void loadAllRuns() {
        try {
            allRuns = getDao().findAllWithAssociations();
            allRuns.sort(Comparator.comparing(SimulationRun::getTimestamp).reversed());
            List<String> runStrings = allRuns.stream()
                    .map(run -> {
                        if (run == null || run.getId() == null || run.getTimestamp() == null) {
                            return null;
                        }
                        return String.format("Run #%d - %s", run.getId(),
                                run.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                    })
                    .filter(Objects::nonNull)
                    .toList();
            runsListView.setItems(FXCollections.observableArrayList(runStrings));
        } catch (Exception e) {
            e.printStackTrace(); // Add this for debugging
            showError("Load Error", "Failed to load simulation runs: " + e.getMessage());
        }
    }


    /**
     * Loads a specific simulation run by ID and displays its results.
     *
     * @param runId ID of the simulation run to load
     */
    public void loadSimulationRun(Long runId) {
        try {
            currentRun = getDao().find(runId);
            if (currentRun == null) {
                showError("Not Found", "Simulation run not found.");
                return;
            }
            displayRunResults();
        } catch (Exception e) {
            showError("Load Error", "Failed to load: " + e.getMessage());
        }
    }

    /**
     * Displays results for the currently loaded simulation run.
     * Updates labels, tables, bottleneck panel, and analysis charts.
     */
    private void displayRunResults() {
        runHeaderLabel.setText("Simulation Run #" + currentRun.getId());
        runTimestampLabel.setText(currentRun.getTimestamp().format(
                DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm:ss")));

        totalAppsResultLabel.setText(String.valueOf(currentRun.getTotalApplications()));
        avgSystemTimeResultLabel.setText(String.format("%.2f min", currentRun.getAvgSystemTime()));

        double total = currentRun.getTotalApplications();
        approvedResultLabel.setText(String.format("%d (%.1f%%)",
                currentRun.getApprovedCount(), total > 0 ? currentRun.getApprovedCount() * 100.0 / total : 0));
        rejectedResultLabel.setText(String.format("%d (%.1f%%)",
                currentRun.getRejectedCount(), total > 0 ? currentRun.getRejectedCount() * 100.0 / total : 0));

        servicePointTable.setItems(FXCollections.observableArrayList(currentRun.getServicePointResults()));

        Optional<SPResult> bottleneck = currentRun.getServicePointResults().stream()
                .filter(SPResult::isBottleneck).findFirst();

        if (bottleneck.isPresent()) {
            bottleneckPanel.setVisible(true);
            bottleneckNameLabel.setText(bottleneck.get().getServicePointName());
            bottleneckUtilLabel.setText(String.format("%.1f%%", bottleneck.get().getUtilization()));
            bottleneckQueueLabel.setText(String.valueOf(bottleneck.get().getMaxQueueLength()));
            bottleneckWaitLabel.setText(String.format("%.2f min", bottleneck.get().getAvgWaitingTime()));
        } else {
            bottleneckPanel.setVisible(false);
        }
        generateDetailedResults();
        updateWaitByDistributionChart(currentRun);
        updateCompareByDistribution();
        updateAnalysisBarChart();
    }

    /**
     * Updates the comparison bar chart based on the selected distribution type and metric.
     * Populates the chart with data from the currently selected simulation run.
     */

    private void generateDetailedResults() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SIMULATION RUN #%d\n", currentRun.getId()));
        sb.append(String.format("Timestamp: %s\n\n", currentRun.getTimestamp()));
        sb.append(String.format("Total Applications: %d\n", currentRun.getTotalApplications()));
        sb.append(String.format("Approved: %d\n", currentRun.getApprovedCount()));
        sb.append(String.format("Rejected: %d\n", currentRun.getRejectedCount()));
        sb.append(String.format("Avg System Time: %.2f min\n\n", currentRun.getAvgSystemTime()));

        for (SPResult sp : currentRun.getServicePointResults()) {
            sb.append(String.format("%s%s\n", sp.getServicePointName(),
                    sp.isBottleneck() ? " [BOTTLENECK]" : ""));
            sb.append(String.format("  Departures: %d\n", sp.getDepartures()));
            sb.append(String.format("  Avg Wait: %.2f min\n", sp.getAvgWaitingTime()));
            sb.append(String.format("  Max Queue: %d\n", sp.getMaxQueueLength()));
            sb.append(String.format("  Utilization: %.1f%%\n\n", sp.getUtilization()));
        }
    }

    /**
     * Handles selection of a simulation run from the list view.
     * Loads and displays the selected run.
     */
    @FXML
    private void handleLoadRun() {
        int index = runsListView.getSelectionModel().getSelectedIndex();
        if (index < 0) {
            showWarning("No Selection", "Please select a simulation run.");
            return;
        }
        Long selectedRunId = allRuns.get(index).getId();
        loadSimulationRun(selectedRunId);
        updateCompareByDistribution(); // Add this to immediately update after loading!
        runsListView.getSelectionModel().select(index);

    }

    /**
     * Handles deletion of the selected simulation run.
     * Prompts for confirmation and updates UI after deletion.
     */
    @FXML
    private void handleDeleteRun() {
        int index = runsListView.getSelectionModel().getSelectedIndex();
        if (index < 0) {
            showWarning("No Selection", "Please select a simulation run to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setContentText("Delete this simulation run permanently?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    getDao().deleteById(allRuns.get(index).getId());
                    loadAllRuns();
                    showInfo("Deleted", "Simulation run deleted successfully.");
                } catch (Exception e) {
                    showError("Delete Failed", e.getMessage());
                }
            }
        });
    }

    /**
     * Navigates back to the home view.
     */
    @FXML
    private void navigateToHome() {
        try {
            Stage stage = (Stage) servicePointTable.getScene().getWindow();
            HomeView.show(stage);
        } catch (Exception e) {
            showError("Navigation Error", e.getMessage());
        }
    }

    /**
     * Displays information about the application.
     */
    @FXML
    private void handleAbout() {
        showInfo("About", "Visa Application Processing Simulator v1.0\nDeveloped by Group 7");
    }

    /**
     * Shows an error alert dialog.
     *
     * @param title Dialog title
     * @param message Error message
     */
    protected void showError(String title, String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }

    /**
     * Shows a warning alert dialog.
     *
     * @param title Dialog title
     * @param message Warning message
     */
    protected void showWarning(String title, String message) {
        new Alert(Alert.AlertType.WARNING, message, ButtonType.OK).showAndWait();
    }

    /**
     * Shows an informational alert dialog.
     *
     * @param title Dialog title
     * @param message Information message
     */
    private void showInfo(String title, String message) {
        new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).showAndWait();
    }
}