package MVC.controller;

import eduni.project_distributionconfiguration.DistributionConfig;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import MVC.view.*;

/**
 * SimulationController manages the simulation execution screen, providing
 * real-time monitoring and control of the running simulation.
 *
 * <p>This controller implements {@link ISimulatorUI} and serves as the bridge between
 * the simulation engine and the user interface during simulation execution. It handles:</p>
 * <ul>
 *     <li>Real-time visualization of customer flow through service points.</li>
 *     <li>Live statistics display including application counts, approval rates, and timing.</li>
 *     <li>Queue status monitoring with color-coded progress indicators.</li>
 *     <li>Simulation control operations (pause, resume, stop, speed adjustment).</li>
 *     <li>Progress tracking with percentage completion display.</li>
 *     <li>Navigation management to prevent premature page exits.</li>
 * </ul>
 *
 * <p>The controller ensures thread-safe UI updates by routing all model-generated
 * updates through the JavaFX Application Thread using {@code Platform.runLater}. It maintains
 * simulation state to control user interactions and manages the lifecycle of the
 * visualization canvas.</p>
 *
 * <p>See {@link ISimulatorUI}, {@link IControllerVtoM}, and {@link IVisualisation} for related interfaces.</p>
 */
public class SimulationController implements ISimulatorUI {

    /** Label displaying the current simulation status (Running, Paused, Stopped, Completed).*/
    @FXML private Label simulationStatusLabel;

    /** Menu item for toggling detail display visibility.*/
    @FXML private CheckMenuItem showDetailsMenuItem;

    /** Label showing elapsed time and total simulation time.*/
    @FXML private Label timeElapsedLabel;

    /** Label displaying the total number of applications processed.*/
    @FXML private Label totalAppsLabel;

    /** Label showing the current simulation speed multiplier.*/
    @FXML private Label speedLabel;

    /** Label displaying approved application count and percentage.*/
    @FXML private Label approvedLabel;

    /** Label displaying rejected application count and percentage.*/
    @FXML private Label rejectedLabel;

    /** Label showing the average processing time per application.*/
    @FXML private Label avgTimeLabel;

    /** Progress bar indicating simulation completion percentage.*/
    @FXML private ProgressBar progressBar;

    /** Label displaying the numerical percentage of simulation completion.*/
    @FXML private Label progressPercentLabel;

    /** Canvas element for rendering the simulation visualization.*/
    @FXML private Canvas visualizationCanvas;

    /** Container for queue status indicators and progress bars.*/
    @FXML private VBox queueStatusContainer;

    /** Button for pausing and resuming the simulation.*/
    @FXML private Button pauseButton;

    /** Button for increasing simulation speed.*/
    @FXML private Button speedUpButton;

    /** Button for decreasing simulation speed.*/
    @FXML private Button slowDownButton;

    /** Button for stopping the simulation permanently.*/
    @FXML private Button stopButton;

    /** Controller interface for sending commands to the simulation model.*/
    private IControllerVtoM controller;

    /** Visualization component for rendering customer flow and service points.*/
    private IVisualisation visualisation;

    /** Total simulation time in minutes.*/
    private double simulationTime;

    /** Delay between simulation steps in milliseconds.*/
    private long delay;

    /** Random seed for reproducible simulation runs, or null for random seed.*/
    private Long seed;

    /** Array of distribution configurations for service points and arrival process.*/
    private DistributionConfig[] configs;

    /** Current total number of applications processed.*/
    private int currentTotalApps = 0;

    /** Current number of approved applications.*/
    private int currentApproved = 0;

    /** Current number of rejected applications.*/
    private int currentRejected = 0;

    /** Current simulation time in minutes.*/
    private double currentTime = 0.0;

    /** Current simulation speed multiplier.*/
    private double currentSpeed = 1.0;

    /** Flag indicating whether the user manually stopped the simulation.*/
    private boolean userStopped = false;

    /** Array of progress bars for displaying queue status at each service point.*/
    private ProgressBar[] queueBars = new ProgressBar[6];

    /** Array of labels for displaying queue counts at each service point.*/
    private Label[] queueLabels = new Label[6];

    /** Flag indicating whether the simulation is currently running.*/
    private volatile boolean simulationRunning = true;

    /** Flag indicating whether the simulation has completed naturally.*/
    private volatile boolean simulationComplete = false;

    /**
     * Initializes the simulation controller with the specified parameters.
     *
     * <p>Sets up the visualization canvas, configures simulation parameters,
     * and starts the simulation on the JavaFX Application Thread.</p>
     *
     * @param simTime total simulation time in minutes
     * @param delay animation delay between simulation steps in milliseconds
     * @param seed random seed for reproducibility, or null for random seed
     * @param configs array of distribution configurations for service points and arrival process
     */
    public void initialize(double simTime, long delay, Long seed, DistributionConfig[] configs) {
        this.simulationTime = simTime;
        this.delay = delay;
        this.seed = seed;
        this.configs = configs;

        Platform.runLater(() -> {
            setupVisualization();
            startSimulation();
        });
    }

    /**
     * Sets up the visualization canvas by creating a SimulatorUI instance
     * and replacing the FXML canvas placeholder.
     *
     * <p>The visualization canvas displays animated customer movement and
     * service point status in real-time.</p>
     */
    private void setupVisualization() {
        visualisation = new AnimationSimulatorUI(1400, 550);

        Canvas visCanvas = (Canvas) visualisation;

        StackPane parent = (StackPane) visualizationCanvas.getParent();
        parent.getChildren().remove(visualizationCanvas);
        parent.getChildren().add(visCanvas);
    }

    /**
     * Starts the simulation by initializing trace logging, creating the controller,
     * and launching the simulation on a background thread.
     *
     * <p>Sets the simulation running flags and ensures the simulation engine
     * does not block the UI thread.</p>
     */
    private void startSimulation() {
        simulationRunning = true;
        simulationComplete = false;

        MVC.simu.framework.Trace.setTraceLevel(MVC.simu.framework.Trace.Level.INFO);

        controller = new Controller(this, configs, seed);

        new Thread(() -> {
            controller.startSimulation();
        }).start();
    }

    /**
     * Retrieves the total simulation time.
     *
     * @return the simulation time in minutes
     */
    @Override
    public double getTime() {
        return simulationTime;
    }

    /**
     * Retrieves the animation delay between simulation steps.
     *
     * @return the delay in milliseconds
     */
    @Override
    public long getDelay() {
        return delay;
    }

    /**
     * Updates the UI when the simulation reaches its end time.
     *
     * <p>Sets the status label to show completion and marks the simulation
     * as no longer running. Does not trigger navigation if the user
     * manually stopped the simulation.</p>
     *
     * @param time the final simulation time
     */
    @Override
    public void setEndingTime(double time) {
        if (userStopped) return;

        Platform.runLater(() -> {
            simulationStatusLabel.setText("Completed ✓");
            simulationStatusLabel.setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
            simulationRunning = false;
            simulationComplete = true;
        });
    }

    /**
     * Retrieves the visualization component.
     *
     * @return the visualization component
     */
    @Override
    public IVisualisation getVisualisation() {
        return visualisation;
    }

    /**
     * Displays final simulation results.
     *
     * <p>This implementation logs the results to the console. The detailed
     * results are displayed in the separate results view after navigation.</p>
     *
     * @param resultsText the formatted results text
     */
    @Override
    public void displayResults(String resultsText) {
        Platform.runLater(() -> {
            System.out.println("Simulation Complete!");
            System.out.println(resultsText);
        });
    }

    /**
     * Updates the real-time statistics display with current simulation metrics.
     *
     * <p>This method is called periodically during simulation execution to refresh
     * the UI with current application counts, approval percentages, average
     * processing time, elapsed time, and progress bar status.</p>
     *
     * @param totalApps total number of applications processed so far
     * @param approved number of approved applications
     * @param rejected number of rejected applications
     * @param avgTime average processing time per application in minutes
     * @param currentTime current simulation time in minutes
     */
    public void updateStatistics(int totalApps, int approved, int rejected, double avgTime, double currentTime) {
        Platform.runLater(() -> {
            this.currentTotalApps = totalApps;
            this.currentApproved = approved;
            this.currentRejected = rejected;
            this.currentTime = currentTime;

            totalAppsLabel.setText(String.valueOf(totalApps));

            double approvedPct = totalApps > 0 ? (approved * 100.0 / totalApps) : 0;
            approvedLabel.setText(String.format("%d (%.1f%%)", approved, approvedPct));

            double rejectedPct = totalApps > 0 ? (rejected * 100.0 / totalApps) : 0;
            rejectedLabel.setText(String.format("%d (%.1f%%)", rejected, rejectedPct));

            avgTimeLabel.setText(String.format("%.2f min", avgTime));

            timeElapsedLabel.setText(String.format("%.2f / %.0f min", currentTime, simulationTime));

            double progress = currentTime / simulationTime;
            progressBar.setProgress(progress);
            progressPercentLabel.setText(String.format("%.1f%%", progress * 100));
        });
    }

    /**
     * Updates the queue status display for a specific service point.
     *
     * <p>Updates both the progress bar and label for the specified service point,
     * applying color coding based on queue size: blue for normal (0-10),
     * orange for warning (11-15), and red for bottleneck (16+).</p>
     *
     * @param servicePointId the identifier of the service point (0-5)
     * @param queueSize the current number of customers in the queue
     */
    @Override
    public void updateQueueStatus(int servicePointId, int queueSize) {
        if (servicePointId < 0 || servicePointId >= 6) return;

        Platform.runLater(() -> {
            queueLabels[servicePointId].setText("Queue: " + queueSize);

            double progress = Math.min(queueSize / 20.0, 1.0);
            queueBars[servicePointId].setProgress(progress);

            if (queueSize > 15) {
                queueBars[servicePointId].setStyle("-fx-accent: #E74C3C;");
                queueLabels[servicePointId].setStyle("-fx-font-size: 12px; -fx-text-fill: #E74C3C; -fx-font-weight: bold;");
            } else if (queueSize > 10) {
                queueBars[servicePointId].setStyle("-fx-accent: #F39C12;");
                queueLabels[servicePointId].setStyle("-fx-font-size: 12px; -fx-text-fill: #F39C12;");
            } else {
                queueBars[servicePointId].setStyle("-fx-accent: #3498DB;");
                queueLabels[servicePointId].setStyle("-fx-font-size: 12px; -fx-text-fill: #2C3E50;");
            }
        });
    }

    /**
     * Handles the pause/resume button click event.
     *
     * <p>Toggles the simulation between paused and running states, updating
     * the button text and styling accordingly. Updates the status label
     * to reflect the current state.</p>
     */
    @FXML
    private void handlePause() {
        if (controller == null || controller.getEngine() == null) return;

        if (pauseButton.getText().equals("Pause")) {
            controller.getEngine().pause();
            pauseButton.setText("Resume");
            pauseButton.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
            simulationStatusLabel.setText("Paused");
        } else {
            controller.getEngine().resume();
            pauseButton.setText("Pause");
            pauseButton.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
            simulationStatusLabel.setText("Running...");
        }
    }

    /**
     * Handles the stop button click event.
     *
     * <p>Prompts the user for confirmation before permanently stopping the simulation.
     * If confirmed, stops the engine, disables control buttons, displays a
     * confirmation message, and navigates back to the home screen.</p>
     */
    @FXML
    private void handleStop() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Stop Simulation");
        confirm.setHeaderText("Are you sure you want to stop the simulation?");
        confirm.setContentText("The simulation will be terminated.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (controller != null && controller.getEngine() != null) {
                    controller.getEngine().stopSimulation();
                    userStopped = true;

                    Platform.runLater(() -> {
                        simulationStatusLabel.setText("Stopped");
                        pauseButton.setDisable(true);
                        speedUpButton.setDisable(true);
                        slowDownButton.setDisable(true);
                        stopButton.setDisable(true);

                        Alert stopped = new Alert(Alert.AlertType.INFORMATION);
                        stopped.setTitle("Simulation Stopped");
                        stopped.setContentText("Simulation has been stopped successfully.");
                        stopped.showAndWait();

                        navigateToHome();
                    });
                }
            }
        });
    }

    /**
     * Handles the speed-up button click event.
     *
     * <p>Increases the simulation speed by decreasing the delay between steps.
     * Updates the speed multiplier display to reflect the new speed.</p>
     */
    @FXML
    private void handleSpeedUp() {
        if (controller != null) {
            controller.increaseSpeed();
            currentSpeed *= 0.9;
            Platform.runLater(() -> speedLabel.setText(String.format("%.1fx", currentSpeed)));
        }
    }

    /**
     * Handles the slow-down button click event.
     *
     * <p>Decreases the simulation speed by increasing the delay between steps.
     * Updates the speed multiplier display to reflect the new speed.</p>
     */
    @FXML
    private void handleSlowDown() {
        if (controller != null) {
            controller.decreaseSpeed();
            currentSpeed *= 1.10;
            Platform.runLater(() -> speedLabel.setText(String.format("%.1fx", -currentSpeed)));
        }
    }

    /**
     * Navigates to the home screen.
     *
     * <p>Loads the HomeView and displays it in the current stage.
     * Shows an error dialog if navigation fails.</p>
     */
    private void navigateToHome() {
        try {
            Stage stage = (Stage) stopButton.getScene().getWindow();
            HomeView.show(stage);
        } catch (Exception e) {
            showError("Navigation Error", "Failed to return to welcome page: " + e.getMessage());
        }
    }

    /**
     * Handles menu-triggered navigation to the main home screen.
     *
     * <p>Prevents navigation if the simulation is currently running and not yet
     * complete, displaying a warning dialog to inform the user they must
     * stop or wait for completion before leaving.</p>
     */
    @FXML
    private void navigateToMain() {
        if (simulationRunning && !simulationComplete) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Simulation Running");
            alert.setHeaderText("Cannot leave page");
            alert.setContentText("Simulation is currently running. Please stop or wait for completion before navigating away.");
            alert.showAndWait();
            return;
        }
        navigateToHome();
    }

    /**
     * Navigates to the results view screen.
     *
     * <p>Prevents navigation if the simulation is currently running and not yet
     * complete, displaying a warning dialog. Otherwise, loads the ResultView
     * and displays it in the current stage.</p>
     */
    public void navigateToResult() {
        if (simulationRunning && !simulationComplete){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Simulation Running");
            alert.setHeaderText("Cannot leave page");
            alert.setContentText("Simulation is currently running. Please stop or wait for completion before navigating away.");
            alert.showAndWait();
            return;
        }

        try {
            Stage stage = (Stage) stopButton.getScene().getWindow();
            ResultView.show(stage);
        } catch (Exception e) {
            showError("Navigation Error", "Failed to navigate: " + e.getMessage());
        }
    }

    /**
     * Displays an error dialog with the specified title and message.
     *
     * <p>Ensures the dialog is shown on the JavaFX Application Thread.</p>
     *
     * @param title the dialog title
     * @param message the error message to display
     */
    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}