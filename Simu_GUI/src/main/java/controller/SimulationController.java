package controller;

import distributionconfiguration.DistributionConfig;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import view.ISimulatorUI;
import view.IVisualisation;
import view.Animation;
public class SimulationController implements ISimulatorUI {

    // Top controls
    @FXML private Label simulationStatusLabel;
    @FXML private CheckMenuItem showDetailsMenuItem;

    // Statistics labels
    @FXML private Label timeElapsedLabel;
    @FXML private Label totalAppsLabel;
    @FXML private Label speedLabel;
    @FXML private Label approvedLabel;
    @FXML private Label rejectedLabel;
    @FXML private Label avgTimeLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressPercentLabel;

    // Visualization
    @FXML private Canvas visualizationCanvas;

    // Queue status container
    @FXML private VBox queueStatusContainer;

    // Control buttons
    @FXML private Button pauseButton;
    @FXML private Button speedUpButton;
    @FXML private Button slowDownButton;
    @FXML private Button stopButton;


    // Backend
    private IControllerVtoM controller;
    private IVisualisation visualisation;

    // Simulation parameters
    private double simulationTime;
    private long delay;
    private Long seed;
    private DistributionConfig[] configs;

    // Initial state
    private int currentTotalApps = 0;
    private int currentApproved = 0;
    private int currentRejected = 0;
    private double currentTime = 0.0;
    private double currentSpeed = 1.0;
    private boolean userStopped = false;

    // Queue status UI elements
    private ProgressBar[] queueBars = new ProgressBar[6];
    private Label[] queueLabels = new Label[6];

    private volatile boolean simulationRunning = true;
    private volatile boolean simulationComplete = false;

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

    private void setupVisualization() {
        visualisation = new Animation(1400, 550);

        // Get the actual canvas from Visualisation2
        Canvas visCanvas = (Canvas) visualisation;

        // Replace the FXML canvas with the Visualisation2 canvas
        StackPane parent = (StackPane) visualizationCanvas.getParent();
        parent.getChildren().remove(visualizationCanvas);
        parent.getChildren().add(visCanvas);
    }

    private void startSimulation() {
        // to check simulation
        simulationRunning = true;
        simulationComplete = false;

        // Initialize trace level to prevent null pointer
        simu.framework.Trace.setTraceLevel(simu.framework.Trace.Level.INFO);

        // Create Controller with this UI
        controller = new Controller(this, configs, seed);

        // Start simulation on background thread
        new Thread(() -> {
            controller.startSimulation();
        }).start();
    }

    // ISimulatorUI Implementation
    @Override
    public double getTime() {
        return simulationTime;
    }

    @Override
    public long getDelay() {
        return delay;
    }

    @Override
    public void setEndingTime(double time) {
        if (userStopped) return;  // Don't navigate if user manually stopped

        Platform.runLater(() -> {
            simulationStatusLabel.setText("Completed ✓");
            simulationStatusLabel.setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
            simulationRunning = false;
            simulationComplete = true;


        });
    }

    @Override
    public IVisualisation getVisualisation() {
        return visualisation;
    }

    @Override
    public void displayResults(String resultsText) {
        // Results will be shown on results page, we just update final stats here
        Platform.runLater(() -> {
            // Parse results text for final statistics (optional enhancement)
            System.out.println("Simulation Complete!");
            System.out.println(resultsText);
        });
    }

    // Update methods for real-time statistics
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

    @Override
    public void updateQueueStatus(int servicePointId, int queueSize) {
        if (servicePointId < 0 || servicePointId >= 6) return;

        Platform.runLater(() -> {
            queueLabels[servicePointId].setText("Queue: " + queueSize);

            // Normalize queue size to progress bar (assume max queue of 20 for display)
            double progress = Math.min(queueSize / 20.0, 1.0);
            queueBars[servicePointId].setProgress(progress);

            // Color code based on queue size
            if (queueSize > 15) {
                queueBars[servicePointId].setStyle("-fx-accent: #E74C3C;"); // Red for bottleneck
                queueLabels[servicePointId].setStyle("-fx-font-size: 12px; -fx-text-fill: #E74C3C; -fx-font-weight: bold;");
            } else if (queueSize > 10) {
                queueBars[servicePointId].setStyle("-fx-accent: #F39C12;"); // Orange for warning
                queueLabels[servicePointId].setStyle("-fx-font-size: 12px; -fx-text-fill: #F39C12;");
            } else {
                queueBars[servicePointId].setStyle("-fx-accent: #3498DB;"); // Blue for normal
                queueLabels[servicePointId].setStyle("-fx-font-size: 12px; -fx-text-fill: #2C3E50;");
            }
        });
    }

    // Button Handlers
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

                        // Show alert and return home
                        Alert stopped = new Alert(Alert.AlertType.INFORMATION);
                        stopped.setTitle("Simulation Stopped");
                        stopped.setContentText("Simulation has been stopped successfully.");
                        stopped.showAndWait();

                        // Return to home
                        navigateToWelcome();
                    });
                }
            }
        });
    }


    @FXML
    private void handleSpeedUp() {
        if (controller != null) {
            controller.increaseSpeed();
            currentSpeed *= 1.1;
            Platform.runLater(() -> speedLabel.setText(String.format("%.1fx", currentSpeed)));
        }
    }

    @FXML
    private void handleSlowDown() {
        if (controller != null) {
            controller.decreaseSpeed();
            currentSpeed *= 0.9;
            Platform.runLater(() -> speedLabel.setText(String.format("%.1fx", currentSpeed)));
        }
    }


    // This is the menu handler that checks if simulation is running
    @FXML
    private void handleReturnToHome() {
        if (simulationRunning && !simulationComplete) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Simulation Running");
            alert.setHeaderText("Cannot leave page");
            alert.setContentText("Simulation is currently running. Please stop or wait for completion before navigating away.");
            alert.showAndWait();
            return;
        }
        navigateToWelcome();
    }

    // This is the internal navigation method (called after stop confirmation)
    private void navigateToWelcome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/welcome.fxml"));
            Scene scene = new Scene(loader.load(), 1600, 900);
            Stage stage = (Stage) stopButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setTitle("Visa Application Simulator");
        } catch (Exception e) {
            showError("Navigation Error", "Failed to return to welcome page: " + e.getMessage());
        }
    }

    public void handleGoToResult() {
        if (simulationRunning && !simulationComplete){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Simulation Running");
            alert.setHeaderText("Cannot leave page");
            alert.setContentText("Simulation is currently running. Please stop or wait for completion before navigating away.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/results.fxml"));
            Scene scene = new Scene(loader.load(), 1600, 900);
            Stage stage = (Stage) stopButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
        } catch (Exception e) {
            showError("Navigation Error", "Failed to navigate: " + e.getMessage());
        }
    }


    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showInfo(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

}