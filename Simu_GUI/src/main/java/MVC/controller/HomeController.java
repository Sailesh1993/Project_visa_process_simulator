package MVC.controller;

import ORM.dao.SimulationRunDao;
import eduni.project_distributionconfiguration.DistributionConfig;
import ORM.entity.SimulationRun;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import MVC.view.ResultView;
import MVC.view.SimulationView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * HomeController manages the main simulation configuration screen where users
 * can configure and launch new simulations or review previous simulation runs.
 *
 * <p>This controller is responsible for:
 * <ul>
 *     <li>Configuring basic simulation parameters (time, delay, seed)</li>
 *     <li>Setting up the distribution type and parameters for the arrival process and six service points</li>
 *     <li>Updating the UI dynamically based on selected distribution types</li>
 *     <li>Validating user inputs before starting a simulation</li>
 *     <li>Displaying and managing recent simulation runs retrieved from the database</li>
 *     <li>Navigating between simulation setup and result views</li>
 * </ul>
 *
 * The controller supports three distribution types: Normal, Negexp (Negative Exponential),
 * and Gamma, each requiring different parameter configurations. The UI dynamically
 * shows or hides fields based on the selected distribution type.</p>
 */
public class HomeController {

    /** Text field for specifying the total simulation time. */
    @FXML private TextField simulationTimeField;

    /** Text field for specifying the animation delay between simulation steps.*/
    @FXML private TextField delayField;

    /** ComboBox for selecting the distribution type for the arrival process.*/
    @FXML private ComboBox<String> arrival_distType;

    /** Text field for the first parameter of the arrival distribution.*/
    @FXML private TextField arrival_param1;

    /** Text field for the second parameter of the arrival distribution.*/
    @FXML private TextField arrival_param2;

    /** Label for the second parameter field of the arrival distribution.*/
    @FXML private Label arrival_param2Label;

    /** ComboBox for selecting the distribution type for service point 1.*/
    @FXML private ComboBox<String> sp1_distType;

    /** Text field for the first parameter of service point 1 distribution.*/
    @FXML private TextField sp1_param1;

    /** Text field for the second parameter of service point 1 distribution.*/
    @FXML private TextField sp1_param2;

    /** Label for the second parameter field of service point 1 distribution.*/
    @FXML private Label sp1_param2Label;

    /** ComboBox for selecting the distribution type for service point 2.*/
    @FXML private ComboBox<String> sp2_distType;

    /** Text field for the first parameter of service point 2 distribution.*/
    @FXML private TextField sp2_param1;

    /** Text field for the second parameter of service point 2 distribution.*/
    @FXML private TextField sp2_param2;

    /** Label for the second parameter field of service point 2 distribution.*/
    @FXML private Label sp2_param2Label;

    /** ComboBox for selecting the distribution type for service point 3.*/
    @FXML private ComboBox<String> sp3_distType;

    /** Text field for the first parameter of service point 3 distribution.*/
    @FXML private TextField sp3_param1;

    /** Text field for the second parameter of service point 3 distribution.*/
    @FXML private TextField sp3_param2;

    /** Label for the second parameter field of service point 3 distribution.*/
    @FXML private Label sp3_param2Label;

    /** ComboBox for selecting the distribution type for service point 4.*/
    @FXML private ComboBox<String> sp4_distType;

    /** Text field for the first parameter of service point 4 distribution.*/
    @FXML private TextField sp4_param1;

    /** Text field for the second parameter of service point 4 distribution.*/
    @FXML private TextField sp4_param2;

    /** Label for the second parameter field of service point 4 distribution.*/
    @FXML private Label sp4_param2Label;

    /** ComboBox for selecting the distribution type for service point 5.*/
    @FXML private ComboBox<String> sp5_distType;

    /** Text field for the first parameter of service point 5 distribution.*/
    @FXML private TextField sp5_param1;

    /** Text field for the second parameter of service point 5 distribution.*/
    @FXML private TextField sp5_param2;

    /** Label for the second parameter field of service point 5 distribution.*/
    @FXML private Label sp5_param2Label;

    /** ComboBox for selecting the distribution type for service point 6.*/
    @FXML private ComboBox<String> sp6_distType;

    /** Text field for the first parameter of service point 6 distribution.*/
    @FXML private TextField sp6_param1;

    /** Text field for the second parameter of service point 6 distribution.*/
    @FXML private TextField sp6_param2;

    /** Label for the second parameter field of service point 6 distribution.*/
    @FXML private Label sp6_param2Label;

    /** TableView displaying recent simulation runs from the database.*/
    @FXML private TableView<SimulationRun> recentRunsTable;

    /** TableColumn displaying the unique identifier of each simulation run.*/
    @FXML private TableColumn<SimulationRun, Long> runIdColumn;

    /** TableColumn displaying the timestamp when each simulation was executed.*/
    @FXML private TableColumn<SimulationRun, LocalDateTime> timestampColumn;

    /** TableColumn displaying the total number of applications processed in each run.*/
    @FXML private TableColumn<SimulationRun, Integer> totalAppsColumn;

    /** TableColumn displaying the number of approved applications in each run.*/
    @FXML private TableColumn<SimulationRun, Integer> approvedColumn;

    /** TableColumn displaying the number of rejected applications in each run.*/
    @FXML private TableColumn<SimulationRun, Integer> rejectedColumn;

    /** TableColumn displaying the average system time for applications in each run.*/
    @FXML private TableColumn<SimulationRun, Double> avgTimeColumn;

    /** Button to start a new simulation with the configured parameters. */
    @FXML private Button startButton;

    /** Data Access Object for performing database operations on simulation runs.*/
    private SimulationRunDao dao = new SimulationRunDao();

    /**
     * Initializes the controller after the FXML elements are loaded.
     * <p>
     * Sets up distribution type combo boxes with available options, configures listeners
     * for dynamic UI updates, initializes default parameter field visibility, loads recent
     * simulation runs from the database, and configures table columns for proper data display.
     * </p>
     */
    @FXML
    private void initialize() {
        setupDistributionComboBoxes();
        setupDistributionListeners();
        updateParameterFields("Normal", arrival_param2, arrival_param2Label);
        loadRecentRuns();
        setupTableColumns();
    }

    /**
     * Configures all distribution type combo boxes with available distribution options.
     *
     * <p>Sets up combo boxes for the arrival process and all six service points with
     * the available distribution types: Normal, Negexp, and Gamma. Also sets default
     * values for each combo box.</p>
     */
    private void setupDistributionComboBoxes() {
        ObservableList<String> distTypes = FXCollections.observableArrayList("Normal", "Negexp", "Gamma");

        arrival_distType.setItems(distTypes);
        arrival_distType.setValue("Normal");

        sp1_distType.setItems(distTypes);
        sp1_distType.setValue("Negexp");

        sp2_distType.setItems(distTypes);
        sp2_distType.setValue("Normal");

        sp3_distType.setItems(distTypes);
        sp3_distType.setValue("Gamma");

        sp4_distType.setItems(distTypes);
        sp4_distType.setValue("Negexp");

        sp5_distType.setItems(distTypes);
        sp5_distType.setValue("Gamma");

        sp6_distType.setItems(distTypes);
        sp6_distType.setValue("Normal");
    }

    /**
     * Sets up value change listeners for all distribution type combo boxes.
     *
     * <p>When a user selects a different distribution type, the corresponding
     * parameter fields are dynamically shown or hidden based on the requirements
     * of the selected distribution.</p>
     */
    private void setupDistributionListeners() {
        arrival_distType.valueProperty().addListener((obs, old, newVal) ->
                updateParameterFields(newVal, arrival_param2, arrival_param2Label));

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

    /**
     * Updates the visibility and labels of parameter fields based on the selected distribution type.
     *
     * <p>Normal distribution requires two parameters (mean and standard deviation),
     * Gamma distribution requires two parameters (shape and scale),
     * Negexp distribution requires only one parameter (mean), so the second parameter field is hidden.</p>
     *
     * @param distType the selected distribution type
     * @param param2Field the second parameter text field to show or hide
     * @param param2Label the label for the second parameter field
     */
    private void updateParameterFields(String distType, TextField param2Field, Label param2Label) {
        switch (distType) {
            case "Normal":
                param2Field.setVisible(true);
                param2Label.setVisible(true);
                param2Label.setText("Std Dev:");
                break;
            case "Gamma":
                param2Field.setVisible(true);
                param2Label.setVisible(true);
                param2Label.setText("Scale:");
                break;
            case "Negexp":
                param2Field.setVisible(false);
                param2Label.setVisible(false);
                break;
        }
    }

    /**
     * Configures all table columns with appropriate cell value factories and formatters.
     *
     * <p>Sets up property bindings for each column and applies custom cell factories
     * for timestamp formatting and decimal number formatting.</p>
     */
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

    /**
     * Loads recent simulation runs from the database and populates the table view.
     *
     * <p>Retrieves all simulation runs using the DAO and displays them in the table.
     * Shows an error dialog if the database operation fails.</p>
     */
    private void loadRecentRuns() {
        try {
            List<SimulationRun> runs = dao.findAll();
            recentRunsTable.setItems(FXCollections.observableArrayList(runs));
        } catch (Exception e) {
            showError("Failed to load recent runs", e.getMessage());
        }
    }

    /**
     * Handles the start button click event to launch a new simulation.
     *
     * <p>Validates all user inputs, builds distribution configurations from the
     * form data, and navigates to the simulation execution view. Displays
     * appropriate error messages if validation fails or an exception occurs.</p>
     */
    @FXML
    private void handleStart() {
        try {
            if (!validateInputs()) {
                return;
            }

            double simTime = Double.parseDouble(simulationTimeField.getText());
            long delay = Long.parseLong(delayField.getText());
            Long seed = null;

            DistributionConfig[] configs = buildConfigurations();

            navigateToSimulation(simTime, delay, seed, configs);

        } catch (NumberFormatException e) {
            showError("Invalid Input", "Please enter valid numbers for all fields.");
        } catch (Exception e) {
            showError("Error", "Failed to start simulation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Validates all user inputs before starting a simulation.
     *
     * <p>Checks that all required fields are filled, simulation time is positive,
     * delay is non-negative, and all distribution parameters are valid numbers
     * greater than zero. Returns false and displays an error dialog if any
     * validation fails.</p>
     *
     * @return true if all inputs are valid, false otherwise
     */
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

    /**
     * Validates a single distribution configuration.
     *
     * <p>Ensures that the distribution type is selected, parameter 1 is provided
     * and positive, and parameter 2 is provided and positive if required by
     * the distribution type (Normal and Gamma require two parameters, Negexp requires one).</p>
     *
     * @param name the display name of the configuration being validated
     * @param type the distribution type
     * @param param1Str the first parameter value as a string
     * @param param2Str the second parameter value as a string
     * @throws IllegalArgumentException if validation fails
     */
    private void validateDistConfig(String name, String type, String param1Str, String param2Str) {
        if (type == null || param1Str.isEmpty()) {
            throw new IllegalArgumentException(name + ": Missing distribution type or parameter 1");
        }

        double param1 = Double.parseDouble(param1Str);
        if (param1 <= 0) {
            throw new IllegalArgumentException(name + ": Parameter 1 must be greater than 0");
        }

        if ((type.equals("Normal") || type.equals("Gamma")) && param2Str.isEmpty()) {
            throw new IllegalArgumentException(name + ": " + type + " distribution requires parameter 2");
        }

        if (!param2Str.isEmpty()) {
            double param2 = Double.parseDouble(param2Str);
            if (param2 <= 0) {
                throw new IllegalArgumentException(name + ": Parameter 2 must be greater than 0");
            }
        }
    }

    /**
     * Builds an array of distribution configurations from the form inputs.
     *
     * <p>Creates seven DistributionConfig objects: six for the service points
     * (indices 0-5) and one for the arrival process (index 6). Each configuration
     * is built based on the selected distribution type and parameter values.</p>
     *
     * @return an array of seven DistributionConfig objects
     */
    private DistributionConfig[] buildConfigurations() {
        DistributionConfig[] configs = new DistributionConfig[7];

        configs[0] = buildDistConfig(sp1_distType.getValue(), sp1_param1.getText(), sp1_param2.getText(), false);
        configs[1] = buildDistConfig(sp2_distType.getValue(), sp2_param1.getText(), sp2_param2.getText(), false);
        configs[2] = buildDistConfig(sp3_distType.getValue(), sp3_param1.getText(), sp3_param2.getText(), false);
        configs[3] = buildDistConfig(sp4_distType.getValue(), sp4_param1.getText(), sp4_param2.getText(), false);
        configs[4] = buildDistConfig(sp5_distType.getValue(), sp5_param1.getText(), sp5_param2.getText(), false);
        configs[5] = buildDistConfig(sp6_distType.getValue(), sp6_param1.getText(), sp6_param2.getText(), false);

        configs[6] = buildDistConfig(arrival_distType.getValue(), arrival_param1.getText(), arrival_param2.getText(), true);

        return configs;
    }

    /**
     * Builds a single DistributionConfig object from the provided parameters.
     *
     * <p>Creates a DistributionConfig with one parameter for Negexp distributions
     * or two parameters for Normal and Gamma distributions.</p>
     *
     * @param type the distribution type
     * @param param1Str the first parameter value as a string
     * @param param2Str the second parameter value as a string
     * @param forArrival true if this configuration is for the arrival process, false for service points
     * @return a configured DistributionConfig object
     */
    private DistributionConfig buildDistConfig(String type, String param1Str, String param2Str, boolean forArrival) {
        double param1 = Double.parseDouble(param1Str);

        if (type.equals("Negexp")) {
            return new DistributionConfig(type, param1, forArrival);
        } else {
            double param2 = Double.parseDouble(param2Str);
            return new DistributionConfig(type, param1, param2, forArrival);
        }
    }

    /**
     * Navigates to the simulation execution view with the specified parameters.
     *
     * @param simTime the total simulation time
     * @param delay the animation delay between simulation steps
     * @param seed the random seed for reproducibility, or null for random seed
     * @param configs array of distribution configurations for service points and arrival process
     */
    private void navigateToSimulation(double simTime, long delay, Long seed, DistributionConfig[] configs) {
        try {
            Stage stage = (Stage) startButton.getScene().getWindow();
            SimulationView.show(stage, simTime, delay, seed, configs);
        } catch (Exception e) {
            showError("Navigation Error!", "Failed to load simulation page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the load button click event to view details of a selected simulation run.
     *
     * <p>Retrieves the selected simulation run from the table and navigates to the
     * results view. Displays a warning if no run is selected.</p>
     */
    @FXML
    private void handleLoadRun() {
        SimulationRun selectedRun = recentRunsTable.getSelectionModel().getSelectedItem();
        if (selectedRun == null) {
            showWarning("No Selection!", "Please select a simulation-run to MVC.view.");
            return;
        }

        navigateToResults(selectedRun.getId());
    }

    /**
     * Handles the delete button click event to remove a selected simulation run.
     *
     * <p>Prompts the user for confirmation before deleting the selected run from
     * the database. Refreshes the table and displays a success message if deletion
     * succeeds. Displays a warning if no run is selected or an error if deletion fails.</p>
     */
    @FXML
    private void handleDeleteRun() {
        SimulationRun selectedRun = recentRunsTable.getSelectionModel().getSelectedItem();
        if (selectedRun == null) {
            showWarning("No Selection!", "Please select a simulation-run to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion!");
        confirm.setHeaderText("Delete Simulation-Run #" + selectedRun.getId() + "?");
        confirm.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                dao.deleteById(selectedRun.getId());
                loadRecentRuns();
                showInfo("Deleted.", "Simulation run deleted successfully.");
            } catch (Exception e) {
                showError("Delete Failed!", "Failed to delete simulation run: " + e.getMessage());
            }
        }
    }

    /**
     * Navigates to the results view for a specific simulation run.
     *
     * @param runId the unique identifier of the simulation run to display
     */
    private void navigateToResults(Long runId) {
        try {
            Stage stage = (Stage) startButton.getScene().getWindow();

            ResultsController resultsController = ResultView.show(stage);
            resultsController.loadSimulationRun(runId);

            stage.setTitle("***Simulation Results***");

        } catch (Exception e) {
            showError("Navigation Error!", "Failed to load results page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Displays an error dialog with the specified title and message.
     *
     * @param title the dialog title
     * @param message the error message to display
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays a warning dialog with the specified title and message.
     *
     * @param title the dialog title
     * @param message the warning message to display
     */
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays an information dialog with the specified title and message.
     *
     * @param title the dialog title
     * @param message the information message to display
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}