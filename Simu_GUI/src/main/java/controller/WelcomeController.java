package controller;

import dao.SimulationRunDao;
import distributionconfiguration.DistributionConfig;
import entity.SimulationRun;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class WelcomeController {

    // Basic Parameters
    @FXML private TextField simulationTimeField;
    @FXML private TextField delayField;
    //@FXML private TextField seedField;

    // Arrival Process Controls
    @FXML private ComboBox<String> arrival_distType;
    @FXML private TextField arrival_param1;
    @FXML private TextField arrival_param2;
    @FXML private Label arrival_param2Label;

    // SP1 Controls
    @FXML private ComboBox<String> sp1_distType;
    @FXML private TextField sp1_param1;
    @FXML private TextField sp1_param2;
    @FXML private Label sp1_param2Label;

    // SP2 Controls
    @FXML private ComboBox<String> sp2_distType;
    @FXML private TextField sp2_param1;
    @FXML private TextField sp2_param2;
    @FXML private Label sp2_param2Label;

    // SP3 Controls
    @FXML private ComboBox<String> sp3_distType;
    @FXML private TextField sp3_param1;
    @FXML private TextField sp3_param2;
    @FXML private Label sp3_param2Label;

    // SP4 Controls
    @FXML private ComboBox<String> sp4_distType;
    @FXML private TextField sp4_param1;
    @FXML private TextField sp4_param2;
    @FXML private Label sp4_param2Label;

    // SP5 Controls
    @FXML private ComboBox<String> sp5_distType;
    @FXML private TextField sp5_param1;
    @FXML private TextField sp5_param2;
    @FXML private Label sp5_param2Label;

    // SP6 Controls
    @FXML private ComboBox<String> sp6_distType;
    @FXML private TextField sp6_param1;
    @FXML private TextField sp6_param2;
    @FXML private Label sp6_param2Label;

    // Recent Runs Table
    @FXML private TableView<SimulationRun> recentRunsTable;
    @FXML private TableColumn<SimulationRun, Long> runIdColumn;
    @FXML private TableColumn<SimulationRun, LocalDateTime> timestampColumn;
    @FXML private TableColumn<SimulationRun, Integer> totalAppsColumn;
    @FXML private TableColumn<SimulationRun, Integer> approvedColumn;
    @FXML private TableColumn<SimulationRun, Integer> rejectedColumn;
    @FXML private TableColumn<SimulationRun, Double> avgTimeColumn;

    // Buttons
    @FXML private Button startButton;
//    @FXML private Button loadRunButton;
//    @FXML private Button deleteRunButton;
//    @FXML private Accordion distributionAccordion;

    private SimulationRunDao dao = new SimulationRunDao();

    @FXML
    private void initialize() {
        setupDistributionComboBoxes();
        setupDistributionListeners();
        updateParameterFields("Normal", arrival_param2, arrival_param2Label); // set default
        loadRecentRuns();
        setupTableColumns();
    }

    private void setupDistributionComboBoxes() {
        ObservableList<String> distTypes = FXCollections.observableArrayList("Normal", "Negexp", "Gamma");

        arrival_distType.setItems(distTypes);
        arrival_distType.setValue("Normal"); // Set default value

        sp1_distType.setItems(distTypes);
        sp1_distType.setValue("Negexp");

        sp2_distType.setItems(distTypes);
        sp2_distType.setValue("Negexp");

        sp3_distType.setItems(distTypes);
        sp3_distType.setValue("Normal");

        sp4_distType.setItems(distTypes);
        sp4_distType.setValue("Negexp");

        sp5_distType.setItems(distTypes);
        sp5_distType.setValue("Gamma");

        sp6_distType.setItems(distTypes);
        sp6_distType.setValue("Gamma");
    }

    private void setupDistributionListeners() {
        // Arrival
        arrival_distType.valueProperty().addListener((obs, old, newVal) ->
                updateParameterFields(newVal, arrival_param2, arrival_param2Label));

        // SP1-SP6
        sp1_distType.valueProperty().addListener((obs, old, newVal) ->
                updateParameterFields(newVal, sp1_param2, sp1_param2Label));
        sp2_distType.valueProperty().addListener((obs, old, newVal) ->
                updateParameterFields(newVal, sp2_param2, sp2_param2Label));
        sp3_distType.valueProperty().addListener((obs, old, newVal) ->
                updateParameterFields(newVal, sp3_param2, sp3_param2Label));
        sp4_distType.valueProperty().addListener((obs, old, newVal) ->
                updateParameterFields(newVal, sp4_param2, sp4_param2Label));
        sp5_distType.valueProperty().addListener((obs, old, newVal) ->
                updateParameterFields(newVal, sp5_param2, sp5_param2Label));
        sp6_distType.valueProperty().addListener((obs, old, newVal) ->
                updateParameterFields(newVal, sp6_param2, sp6_param2Label));
    }

    private void updateParameterFields(String distType, TextField param2Field, Label param2Label) {
        switch (distType) {
            case "Normal":
                param2Field.setVisible(true);
                param2Label.setVisible(true);
                param2Label.setText("Std Dev (σ):");
                break;
            case "Gamma":
                param2Field.setVisible(true);
                param2Label.setVisible(true);
                param2Label.setText("Scale (θ):");
                break;
            case "Negexp":
                param2Field.setVisible(false);
                param2Label.setVisible(false);
                break;
        }
    }

    private void setupTableColumns() {
        runIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        timestampColumn.setCellFactory(col -> new TableCell<SimulationRun, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                }
            }
        });

        totalAppsColumn.setCellValueFactory(new PropertyValueFactory<>("totalApplications"));
        approvedColumn.setCellValueFactory(new PropertyValueFactory<>("approvedCount"));
        rejectedColumn.setCellValueFactory(new PropertyValueFactory<>("rejectedCount"));
        avgTimeColumn.setCellValueFactory(new PropertyValueFactory<>("avgSystemTime"));
        avgTimeColumn.setCellFactory(col -> new TableCell<SimulationRun, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });
    }

    private void loadRecentRuns() {
        try {
            List<SimulationRun> runs = dao.findAll();
            recentRunsTable.setItems(FXCollections.observableArrayList(runs));
        } catch (Exception e) {
            showError("Failed to load recent runs", e.getMessage());
        }
    }

    @FXML
    private void handleStart() {
        try {
            // Validate inputs
            if (!validateInputs()) {
                return;
            }

            // Get basic parameters
            double simTime = Double.parseDouble(simulationTimeField.getText());
            long delay = Long.parseLong(delayField.getText());
            Long seed = null;  // Remove seed field - always use random seed

            // Build distribution configurations
            DistributionConfig[] configs = buildConfigurations();

            // Navigate to simulation page
            navigateToSimulation(simTime, delay, seed, configs);

        } catch (NumberFormatException e) {
            showError("Invalid Input", "Please enter valid numbers for all fields.");
        } catch (Exception e) {
            showError("Error", "Failed to start simulation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateInputs() {
        if (simulationTimeField.getText().isEmpty() || delayField.getText().isEmpty()) {
            showError("Missing Input", "Please fill in simulation time and delay.");
            return false;
        }

        try {
            double simTime = Double.parseDouble(simulationTimeField.getText());
            if (simTime <= 0) {
                showError("Invalid Input", "Simulation time must be positive.");
                return false;
            }

            long delay = Long.parseLong(delayField.getText());
            if (delay < 0) {
                showError("Invalid Input", "Delay cannot be negative.");
                return false;
            }

            // Validate all distribution parameters
            validateDistConfig("Arrival Process", arrival_distType.getValue(), arrival_param1.getText(), arrival_param2.getText());
            validateDistConfig("SP1", sp1_distType.getValue(), sp1_param1.getText(), sp1_param2.getText());
            validateDistConfig("SP2", sp2_distType.getValue(), sp2_param1.getText(), sp2_param2.getText());
            validateDistConfig("SP3", sp3_distType.getValue(), sp3_param1.getText(), sp3_param2.getText());
            validateDistConfig("SP4", sp4_distType.getValue(), sp4_param1.getText(), sp4_param2.getText());
            validateDistConfig("SP5", sp5_distType.getValue(), sp5_param1.getText(), sp5_param2.getText());
            validateDistConfig("SP6", sp6_distType.getValue(), sp6_param1.getText(), sp6_param2.getText());

        } catch (NumberFormatException e) {
            showError("Invalid Input", "All parameters must be valid numbers.");
            return false;
        } catch (IllegalArgumentException e) {
            showError("Invalid Distribution", e.getMessage());
            return false;
        }

        return true;
    }

    private void validateDistConfig(String name, String type, String param1Str, String param2Str) {
        if (type == null || param1Str.isEmpty()) {
            throw new IllegalArgumentException(name + ": Missing distribution type or parameter 1");
        }

        double param1 = Double.parseDouble(param1Str);
        if (param1 <= 0) {
            throw new IllegalArgumentException(name + ": Parameter 1 must be positive");
        }

        if ((type.equals("Normal") || type.equals("Gamma")) && param2Str.isEmpty()) {
            throw new IllegalArgumentException(name + ": " + type + " distribution requires parameter 2");
        }

        if (!param2Str.isEmpty()) {
            double param2 = Double.parseDouble(param2Str);
            if (param2 <= 0) {
                throw new IllegalArgumentException(name + ": Parameter 2 must be positive");
            }
        }
    }

    private DistributionConfig[] buildConfigurations() {
        DistributionConfig[] configs = new DistributionConfig[7];

        // SP1-SP6 (service points)
        configs[0] = buildDistConfig(sp1_distType.getValue(), sp1_param1.getText(), sp1_param2.getText(), false);
        configs[1] = buildDistConfig(sp2_distType.getValue(), sp2_param1.getText(), sp2_param2.getText(), false);
        configs[2] = buildDistConfig(sp3_distType.getValue(), sp3_param1.getText(), sp3_param2.getText(), false);
        configs[3] = buildDistConfig(sp4_distType.getValue(), sp4_param1.getText(), sp4_param2.getText(), false);
        configs[4] = buildDistConfig(sp5_distType.getValue(), sp5_param1.getText(), sp5_param2.getText(), false);
        configs[5] = buildDistConfig(sp6_distType.getValue(), sp6_param1.getText(), sp6_param2.getText(), false);

        // Arrival process (index 6)
        configs[6] = buildDistConfig(arrival_distType.getValue(), arrival_param1.getText(), arrival_param2.getText(), true);

        return configs;
    }

    private DistributionConfig buildDistConfig(String type, String param1Str, String param2Str, boolean forArrival) {
        double param1 = Double.parseDouble(param1Str);

        if (type.equals("Negexp")) {
            return new DistributionConfig(type, param1, forArrival);
        } else {
            double param2 = Double.parseDouble(param2Str);
            return new DistributionConfig(type, param1, param2, forArrival);
        }
    }

    private void navigateToSimulation(double simTime, long delay, Long seed, DistributionConfig[] configs) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/simulation.fxml"));
            Scene scene = new Scene(loader.load());

            SimulationController simController = loader.getController();
            simController.initialize(simTime, delay, seed, configs);

            Stage stage = (Stage) startButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setTitle("Simulation Running");

        } catch (Exception e) {
            showError("Navigation Error", "Failed to load simulation page: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    private void handleLoadRun() {
        SimulationRun selectedRun = recentRunsTable.getSelectionModel().getSelectedItem();
        if (selectedRun == null) {
            showWarning("No Selection", "Please select a simulation run to view.");
            return;
        }

        navigateToResults(selectedRun.getId());
    }

    @FXML
    private void handleDeleteRun() {
        SimulationRun selectedRun = recentRunsTable.getSelectionModel().getSelectedItem();
        if (selectedRun == null) {
            showWarning("No Selection", "Please select a simulation run to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Simulation Run #" + selectedRun.getId() + "?");
        confirm.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                dao.deleteById(selectedRun.getId());
                loadRecentRuns();
                showInfo("Deleted", "Simulation run deleted successfully.");
            } catch (Exception e) {
                showError("Delete Failed", "Failed to delete simulation run: " + e.getMessage());
            }
        }
    }

    private void navigateToResults(Long runId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/results.fxml"));
            Scene scene = new Scene(loader.load(), 1600, 900);

            ResultsController resultsController = loader.getController();
            resultsController.loadSimulationRun(runId);

            Stage stage = (Stage) startButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setTitle("Simulation Results");

        } catch (Exception e) {
            showError("Navigation Error", "Failed to load results page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}