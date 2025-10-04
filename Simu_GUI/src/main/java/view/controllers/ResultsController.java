package view.controllers;

import dao.SimulationRunDao;
import entity.SPResult;
import entity.SimulationRun;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ResultsController {

    @FXML private ListView<String> runsListView;
    @FXML private Label runHeaderLabel;
    @FXML private Label runTimestampLabel;
    @FXML private Label totalAppsResultLabel;
    @FXML private Label avgSystemTimeResultLabel;
    @FXML private Label approvedResultLabel;
    @FXML private Label rejectedResultLabel;
    @FXML private TableView<SPResult> servicePointTable;
    @FXML private TableColumn<SPResult, String> spNameColumn;
    @FXML private TableColumn<SPResult, Integer> departuresColumn;
    @FXML private TableColumn<SPResult, Double> avgWaitColumn;
    @FXML private TableColumn<SPResult, Integer> maxQueueColumn;
    @FXML private TableColumn<SPResult, Double> utilizationColumn;
    @FXML private TableColumn<SPResult, Integer> employeesColumn;
    @FXML private TableColumn<SPResult, String> bottleneckColumn;
    @FXML private VBox bottleneckPanel;
    @FXML private Label bottleneckNameLabel;
    @FXML private Label bottleneckUtilLabel;
    @FXML private Label bottleneckQueueLabel;
    @FXML private Label bottleneckWaitLabel;
    @FXML private Label recommendationLabel;
    @FXML private TextArea detailedResultsTextArea;

    private SimulationRunDao dao = new SimulationRunDao();
    private SimulationRun currentRun;
    private List<SimulationRun> allRuns;

    @FXML
    private void initialize() {
        setupTableColumns();
        loadAllRuns();

        // Auto-load latest run if available
        if (!allRuns.isEmpty()) {
            runsListView.getSelectionModel().selectFirst();
            loadSimulationRun(allRuns.get(0).getId());
        }
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

    private void loadAllRuns() {
        try {
            allRuns = dao.findAll();
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

    public void loadLatestRun() {
        loadAllRuns();
        if (!allRuns.isEmpty()) {
            runsListView.getSelectionModel().selectFirst();
            loadSimulationRun(allRuns.get(0).getId());
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
        detailedResultsTextArea.setText(sb.toString());
    }

    @FXML
    private void handleLoadRun() {
        int index = runsListView.getSelectionModel().getSelectedIndex();
        if (index < 0) {
            showWarning("No Selection", "Please select a simulation run.");
            return;
        }
        loadSimulationRun(allRuns.get(index).getId());
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
    private void handleNewSimulation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/welcome.fxml"));
            Scene scene = new Scene(loader.load(), 1600, 900);
            Stage stage = (Stage) servicePointTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Visa Application Simulator");
        } catch (Exception e) {
            showError("Navigation Error", e.getMessage());
        }
    }

    @FXML
    private void handleExportPDF() {
        showInfo("Not Implemented", "PDF export coming in future version.");
    }

    @FXML
    private void handleExportCSV() {
        showInfo("Not Implemented", "CSV export coming in future version.");
    }

    @FXML
    private void handleCompare() {
        showInfo("Not Implemented", "Compare feature coming in future version.");
    }

    @FXML
    private void handleViewAll() {
        loadAllRuns();
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