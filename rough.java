package simu.controller;

import controller.FieldController;
import eduni.distributions.Normal;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import simu.framework.ProcessManager;
import simu.framework.Trace;
import simu.model.DataStore;
import simu.model.DelayProcess;
import simu.model.Exceptions.CannotLoadFileException;
import simu.model.Exceptions.NoFileSetException;
import simu.model.Exceptions.ZeroValueException;
import simu.model.MyEngine;
import simu.model.SimulationSettings;
import simu.view.ResultView;
import simu.view.StepperView;
import simu.view.VisualizeView;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

public class SimulatorSetupViewController {

    private int currentDelay = 500;
    private boolean hasRun = false;
    private double inspectionFailRate;
    private StepperViewController stepperViewController;
    private ProcessManager pm;
    private final PauseTransition holdTimer = new PauseTransition(Duration.millis(800));
    private Thread delayUpdateThread;
    private final DecimalFormat df = new DecimalFormat("#.##");
    private final int DELAY_MIN = 100;
    private final int DELAY_MAX = 2000;

    // Engine
    private MyEngine m;

    // Window closing properties
    private Stage stepperStage;
    private Stage visualizeStage;
    private Stage setupStage;

    @FXML private TextField arrivalMean;
    @FXML private TextField arrivalVariance;
    @FXML private TextField totalTime;
    @FXML private TextField meanService;
    @FXML private TextField serviceVariance;
    @FXML private Slider inspectionFailRateSlider;
    @FXML private Button runProgram;
    @FXML private Button delayDecreaseButton;
    @FXML private Button delayIncreaseButton;
    @FXML private Label delayLabel;
    @FXML private Label inspectionFailRateLabel;

    @FXML
    void onRunProgram(ActionEvent event) throws IOException {
        if (!hasRun) {
            runProgram.setText("Running");
            runProgram.setDisable(true);
            hasRun = true;

            setupStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            System.out.println("Car Garage Simulation starting...");

            VisualizeView visualizeView = new VisualizeView();
            visualizeStage = visualizeView.init();
            VisualizeController visualizeController = visualizeView.getController();

            StepperView stepperView = new StepperView();
            stepperStage = stepperView.init();
            stepperStage.setTitle("Simulation Control");

            setupWindowCloseHandlers();

            stepperViewController = stepperView.getController();
            try {
                startSimulation(visualizeController);
            } catch (ZeroValueException e) {
                runProgram.setText("Run");
                runProgram.setDisable(false);
                hasRun = false;
                Alert alert = new Alert(Alert.AlertType.ERROR, "Fields cannot be empty or zero");
                stepperStage.close();
                visualizeStage.close();
                alert.showAndWait();
                return;
            }
            Trace.out(Trace.Level.INFO, "Simulation started - Run button now disabled");
            System.out.println("Simulation windows opened successfully!");
        } else {
            Trace.out(Trace.Level.INFO, "Simulation already running - ignoring additional clicks");
        }
    }

    // CLEAN window close handlers
    private void setupWindowCloseHandlers() {
        if (stepperStage != null) {
            stepperStage.setOnCloseRequest(event -> stopSimulationOnly());
        }

        if (visualizeStage != null) {
            visualizeStage.setOnCloseRequest(event -> stopSimulationOnly());
        }
    }

    // CLEAN stop simulation only
    private void stopSimulationOnly() {
        System.out.println("Stop only the simulation, not the entire application");
        if (pm.containProcess(m)) {
            m.requestStop();
        }

        Platform.runLater(() -> {
            try {
                if (stepperStage != null && stepperStage.isShowing()) {
                    stepperStage.close();
                }
                if (visualizeStage != null && visualizeStage.isShowing()) {
                    visualizeStage.close();
                }
            } catch (Exception e) {
                // Ignore errors
            }

            runProgram.setText("Run");
            runProgram.setDisable(false);
            hasRun = false;
        });
    }

    private void stopSimulationAndCloseAll() {
//        if (stepperViewController != null) {
//            m.requestStop();
//            stepperViewController.stopSimulation();
//        }

//        if (pm != null) {
//            pm = null;
//        }

        //            try {
//                if (stepperStage != null && stepperStage.isShowing()) {
//                    stepperStage.close();
//                }
//                if (visualizeStage != null && visualizeStage.isShowing()) {
//                    visualizeStage.close();
//                }
//                if (setupStage != null && setupStage.isShowing()) {
//                    setupStage.close();
//                }
//            } catch (Exception e) {
//                // Ignore errors
//            }
        Platform.exit();
        System.exit(0);
    }

    void onInspectionFailRateSlide() {
        inspectionFailRate = inspectionFailRateSlider.getValue();
        inspectionFailRateLabel.setText(String.format("%.2f%%", inspectionFailRate));
    }

    @FXML
    void onSetDefault(ActionEvent event) {
        if (hasRun) {
            stopSimulationAndCloseAll();
        } else {
            System.out.println("Settings reset to default values");
        }
    }

    @FXML
    void onDecreaseDelayPressed(MouseEvent event) {
        decreaseDelay();
        holdTimer.playFromStart();
        holdTimer.setOnFinished(e -> {
            delayUpdateThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        decreaseDelay();
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            });
            delayUpdateThread.start();
        });
    }

    @FXML
    void onDecreaseDelayReleased(MouseEvent event) {
        if (delayUpdateThread != null) {
            delayUpdateThread.interrupt();
        }
        holdTimer.stop();
    }

    @FXML
    void onIncreaseDelayPressed(MouseEvent event) {
        increaseDelay();
        holdTimer.playFromStart();
        holdTimer.setOnFinished(e -> {
            delayUpdateThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        increaseDelay();
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            });
            delayUpdateThread.start();
        });
    }

    @FXML
    void onIncreaseDelayReleased(MouseEvent event) {
        if (delayUpdateThread != null) {
            delayUpdateThread.interrupt();
        }
        holdTimer.stop();
    }

    @FXML
    void onShowResults(ActionEvent event) {
        ResultView.getInstance();
    }

    public void startSimulation(VisualizeController visualizeController) throws ZeroValueException {
        pm = new ProcessManager();
        Trace.setTraceLevel(Trace.Level.INFO);

        Normal arrivalDistribution = new Normal(formatField(arrivalMean), formatField(arrivalVariance));
        m = new MyEngine(visualizeController, arrivalDistribution);
        FieldController fieldController = new FieldController(m);

        fieldController.setServiceRequired(formatField(meanService), formatField(serviceVariance));
        fieldController.setInspectionFailRate(inspectionFailRate / 100);

        m.setSimulationTime(formatField(totalTime));
        m.setName("Main Simulation");
        pm.addProcess(m);
        stepperViewController.init(pm, currentDelay);

        Trace.out(Trace.Level.INFO, "Simulation engine and delay process added to ProcessManager");
    }

    public void init() {
        updateDelayLabel();
        df.setGroupingUsed(false);
        onInspectionFailRateSlide();
        inspectionFailRateSlider.valueProperty().addListener((o, oldVal, newVal) -> onInspectionFailRateSlide());

        // Text formatters
        arrivalMean.setTextFormatter(new TextFormatter<>(change -> {
            String nextText = change.getControlNewText();
            if (nextText.matches("\\d*([.,]\\d{0,2})?") && nextText.replaceAll("[.,]", "").length() <= 10) {
                return change;
            }
            return null;
        }));
        arrivalVariance.setTextFormatter(new TextFormatter<>(change -> {
            String nextText = change.getControlNewText();
            if (nextText.matches("\\d*([.,]\\d{0,2})?") && nextText.replaceAll("[.,]", "").length() <= 10) {
                return change;
            }
            return null;
        }));
        totalTime.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                int asInt = newText.isBlank() ? 0 : Integer.parseInt(newText);
                if (asInt < 1000000) {
                    return change;
                }
            }
            return null;
        }));
        meanService.setTextFormatter(new TextFormatter<>(change -> {
            String nextText = change.getControlNewText();
            if (nextText.matches("\\d*([.,]\\d{0,2})?") && nextText.replaceAll("[.,]", "").length() <= 10) {
                return change;
            }
            return null;
        }));
        serviceVariance.setTextFormatter(new TextFormatter<>(change -> {
            String nextText = change.getControlNewText();
            if (nextText.matches("\\d*([.,]\\d{0,2})?") && nextText.replaceAll("[.,]", "").length() <= 10) {
                return change;
            }
            return null;
        }));
    }

    private void updateDelayLabel() {
        Platform.runLater(() -> delayLabel.setText(String.format("%dms", currentDelay)));
    }

    private void increaseDelay() {
        currentDelay = Math.min(currentDelay + 100, DELAY_MAX);
        if (currentDelay == DELAY_MAX) {
            delayIncreaseButton.setDisable(true);
        }
        delayDecreaseButton.setDisable(false);
        updateDelayLabel();
    }

    private void decreaseDelay() {
        currentDelay = Math.max(currentDelay - 100, DELAY_MIN);
        if (currentDelay == DELAY_MIN) {
            delayDecreaseButton.setDisable(true);
        }
        delayIncreaseButton.setDisable(false);
        updateDelayLabel();
    }

    private double formatField(TextField textField) throws ZeroValueException {
        String valText = textField.getText();
        if (!valText.isBlank()) {
            double doubleVal = Double.parseDouble(valText.replaceAll(",", "."));
            if (doubleVal <= 0) {
                Platform.runLater(() -> textField.getStyleClass().add("invalid"));
                throw new ZeroValueException();
            }
            textField.getStyleClass().remove("invalid");
            Platform.runLater(() -> textField.setText(df.format(doubleVal)));
            return doubleVal;
        }
        Trace.out(Trace.Level.INFO, "Field '" + (textField.getId() != null ? textField.getId() : "unknown") + "' is blank, throwing ZeroValueException");
        Platform.runLater(() -> textField.getStyleClass().add("invalid"));
        throw new ZeroValueException();
    }

    public void setFromSimulationSettings(SimulationSettings settings) {
        arrivalMean.setText(settings.getArrivalTimeMean());
        arrivalVariance.setText(settings.getArrivalTimeVariance());
        totalTime.setText(settings.getTotalSimulationTime());
        meanService.setText(settings.getServiceRequiredMean());
        serviceVariance.setText(settings.getServiceRequiredVariance());
        inspectionFailRate = settings.getInspectionFailRate();
        inspectionFailRateSlider.setValue(inspectionFailRate);
        onInspectionFailRateSlide();
    }

    // when user click load Object from file
    public void handleOpen(Stage viewStage) {
        if (hasRun) {
            stopSimulationAndCloseAll();
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Simulation File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Simulation Files", "*.simuc"));
        File selectedFile = fileChooser.showOpenDialog(viewStage);
        if (selectedFile == null) {
            return;
        }
        try {
            DataStore.loadFromFile(selectedFile);
        } catch (CannotLoadFileException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to load file. Please ensure it's a valid simulation file.");
            alert.showAndWait();
            return;
        }
        setFromSimulationSettings(DataStore.getInstance().getSimulationSettings());
//        disableAllFields();
    }

    // when user click saveAs Object to file
    public void handleSaveAs(Stage viewStage) {
        if (hasRun) {
            stopSimulationAndCloseAll();
        }
        DataStore.getInstance().setSimulationSettings(new SimulationSettings(
                arrivalMean.getText(),
                arrivalVariance.getText(),
                totalTime.getText(),
                meanService.getText(),
                serviceVariance.getText(),
                inspectionFailRate
        ));
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Simulation File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Simulation Files", "*.simuc"));
        File selectedFile = fileChooser.showSaveDialog(viewStage);
        if (selectedFile != null) {
            DataStore.getInstance().saveToFileAs(selectedFile);
//            disableAllFields();
        }
    }

    // when user click save Object to file
    public void handleSave(Stage viewStage) {
        if (hasRun) {
            stopSimulationAndCloseAll();
        }
        DataStore.getInstance().setSimulationSettings(new SimulationSettings(
                arrivalMean.getText(),
                arrivalVariance.getText(),
                totalTime.getText(),
                meanService.getText(),
                serviceVariance.getText(),
                inspectionFailRate
        ));
        try {
            DataStore.getInstance().saveToFile();
//            disableAllFields();
        } catch (NoFileSetException e) {
            handleSaveAs(viewStage);
        }
    }

    public void handleNew() {
        if (hasRun) {
            stopSimulationAndCloseAll();
        }
        DataStore.clearInstance();
        ResultView.clearTableView();
        clearAndEnableAllFields();
    }

    private void disableAllFields() {
        arrivalMean.setDisable(true);
        arrivalVariance.setDisable(true);
        totalTime.setDisable(true);
        meanService.setDisable(true);
        serviceVariance.setDisable(true);
        inspectionFailRateSlider.setDisable(true);
    }

    private void clearAndEnableAllFields() {
        arrivalMean.clear();
        arrivalVariance.clear();
        totalTime.clear();
        meanService.clear();
        serviceVariance.clear();
        inspectionFailRateSlider.setValue(0);
        inspectionFailRateSlider.setDisable(false);
        arrivalMean.setDisable(false);
        arrivalVariance.setDisable(false);
        totalTime.setDisable(false);
        meanService.setDisable(false);
        serviceVariance.setDisable(false);
    }
}