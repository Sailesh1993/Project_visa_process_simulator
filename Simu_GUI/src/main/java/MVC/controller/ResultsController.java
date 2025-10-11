package MVC.controller;

import Object_Relational_Mapping_ORM.dao.SimulationRunDao;
import Object_Relational_Mapping_ORM.entity.DistConfig;
import Object_Relational_Mapping_ORM.entity.SPResult;
import Object_Relational_Mapping_ORM.entity.SimulationRun;
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

public class ResultsController {

    @FXML
    private ListView<String> runsListView;
    @FXML
    private Label runHeaderLabel;
    @FXML
    private Label runTimestampLabel;
    @FXML
    private Label totalAppsResultLabel;
    @FXML
    private Label avgSystemTimeResultLabel;
    @FXML
    private Label approvedResultLabel;
    @FXML
    private Label rejectedResultLabel;
    @FXML
    private TableView<SPResult> servicePointTable;
    @FXML
    private TableColumn<SPResult, String> spNameColumn;
    @FXML
    private TableColumn<SPResult, Integer> departuresColumn;
    @FXML
    private TableColumn<SPResult, Double> avgWaitColumn;
    @FXML
    private TableColumn<SPResult, Integer> maxQueueColumn;
    @FXML
    private TableColumn<SPResult, Double> utilizationColumn;
    @FXML
    private TableColumn<SPResult, Integer> employeesColumn;
    @FXML
    private TableColumn<SPResult, String> bottleneckColumn;
    @FXML
    private VBox bottleneckPanel;
    @FXML
    private Label bottleneckNameLabel;
    @FXML
    private Label bottleneckUtilLabel;
    @FXML
    private Label bottleneckQueueLabel;
    @FXML
    private Label bottleneckWaitLabel;

    // Combo box switching logic could be added here for different chart types
    @FXML private ComboBox<String> analysisTypeCombo;
    @FXML private BarChart<String, Number> analysisBarChart;

    //New: chart axes label
    @FXML private CategoryAxis analysisSPAxis;
    @FXML private NumberAxis analysisYAxis;

    // Distribution chart
    @FXML private BarChart<String, Number> waitByDistributionChart;
    @FXML private CategoryAxis waitByDistSPAxis;
    @FXML private NumberAxis waitByDistYAxis;

    @FXML private ComboBox<String> distributionTypeCombo;
    @FXML private BarChart<String, Number> compareByDistChart;
    @FXML private CategoryAxis compareByDistXAxis;
    @FXML private NumberAxis compareByDistYAxis;

    // New Scatter chart
    @FXML private ScatterChart<Number, Number> employeeImpactChart;
    @FXML private NumberAxis employeeXAxis;
    @FXML private NumberAxis utilizationYAxis;

    @FXML private ComboBox<String> compareMetricCombo;


    private SimulationRunDao dao = new SimulationRunDao();
    private SimulationRun currentRun;
    private List<SimulationRun> allRuns;

    @FXML
    private void initialize() {
        setupTableColumns();

        // Combo box options for analysis type
        compareMetricCombo.getItems().setAll("Avg Waiting Time", "Utilization", "Max Queue");
        compareMetricCombo.setOnAction(e -> updateWaitByDistributionChart(currentRun));
        compareMetricCombo.getSelectionModel().selectFirst();
        //distributionTypeCombo.setOnAction(e -> updateWaitByDistributionChart());

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
            //loadSimulationRun(allRuns.get(0).getId());
            handleLoadRun(); // Use existing handler to ensure all logic is applied
        }
    }

    // New  helper method to gather all distribution types across all runs
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

    // New method to gather all distribution types across all runs
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

    // New method to gather all distribution types across all runs

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

    private void updateAnalysisBarChart() {
        if (currentRun == null) {
            analysisBarChart.getData().clear();
            employeeImpactChart.setVisible(false);
            System.out.println("No current run selected.");
            return;
        }
        String selected = analysisTypeCombo.getValue();
        System.out.println("Selected analysis: " + selected);

        analysisBarChart.setVisible(!"Employee Impact".equals(selected));
        employeeImpactChart.setVisible("Employee Impact".equals(selected));

        if ("Employee Impact".equals(selected)) {
            employeeImpactChart.setVisible(true);
            analysisBarChart.setVisible(false);
            updateEmployeeImpactChart();
            System.out.println("Showing Employee Impact chart.");
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

    // New method to update the employee impact scatter chart
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

    // New method to load all runs with eager fetching of distributions
    private void loadAllRuns() {
        try {
            // Use the eager loading method from your DAO
            allRuns = dao.findAllWithAssociations();
            allRuns.sort(Comparator.comparing(SimulationRun::getTimestamp).reversed());
            runsListView.setItems(FXCollections.observableArrayList(
                    allRuns.stream()
                            .map(run -> String.format("Run #%d - %s", run.getId(),
                                    run.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))))
                            .toList()));
        } catch (Exception e) {
            showError("Load Error", "Failed to load simulation runs: " + e.getMessage());
        }
    }

    public void loadSimulationRun(Long runId) {
        try {
            currentRun = dao.find(runId);
            if (currentRun == null) {
                showError("Not Found", "Simulation run not found.");
                return;
            }
            displayRunResults();
        } catch (Exception e) {
            showError("Load Error", "Failed to load: " + e.getMessage());
        }
    }

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
                    dao.deleteById(allRuns.get(index).getId());
                    loadAllRuns();
                    showInfo("Deleted", "Simulation run deleted successfully.");
                } catch (Exception e) {
                    showError("Delete Failed", e.getMessage());
                }
            }
        });
    }

    @FXML
    private void navigateToHome() {
        try {
            Stage stage = (Stage) servicePointTable.getScene().getWindow();
            HomeView.show(stage);
        } catch (Exception e) {
            showError("Navigation Error", e.getMessage());
        }
    }

    @FXML
    private void handleAbout() {
        showInfo("About", "Visa Application Processing Simulator v1.0\nDeveloped by Group 7");
    }

    private void showError(String title, String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }

    private void showWarning(String title, String message) {
        new Alert(Alert.AlertType.WARNING, message, ButtonType.OK).showAndWait();
    }

    private void showInfo(String title, String message) {
        new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).showAndWait();
    }
}