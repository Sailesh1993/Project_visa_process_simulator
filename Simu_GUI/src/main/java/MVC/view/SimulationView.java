package MVC.view;

import MVC.controller.SimulationController;
import eduni.project_distributionconfiguration.DistributionConfig;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * SimulationView manages the display of the simulation execution screen where
 * users monitor and control a running simulation.
 *
 * <p>This class is responsible for:</p>
 * <ul>
 *     <li>Loading and displaying the simulation layout from the {@code simulation.fxml} file.</li>
 *     <li>Initializing the {@link SimulationController} with the required simulation parameters.</li>
 *     <li>Configuring the stage and scene for the simulation display, including window settings.</li>
 * </ul>
 *
 * <p>The simulation view provides real-time visualization of customer flow through
 * service points, displays live statistics, and offers controls for pausing,
 * resuming, stopping, and adjusting the simulation speed.</p>
 *
 * <p>This view is typically accessed from the {@link HomeView} after the user
 * has configured and started a new simulation.</p>
 *
 * See {@link SimulationController}
 * See {@link HomeView}
 */
public class SimulationView {

    /**
     * Loads and displays the simulation view in the specified stage with the given parameters.
     *
     * <p>This method creates a new scene by loading the {@code simulation.fxml} file, retrieves
     * the {@link SimulationController} from the FXML loader, and initializes the controller with
     * the provided simulation parameters. The stage is then configured to display the simulation
     * in maximized mode.</p>
     *
     * @param stage the stage in which to display the simulation view
     * @param simTime the total simulation time in minutes
     * @param delay the animation delay between simulation steps, in milliseconds
     * @param seed the random seed for reproducibility, or {@code null} for a random seed
     * @param configs an array of distribution configurations for service points and the arrival process
     * @throws Exception if the FXML file cannot be loaded or if the controller initialization fails
     */
    public static void show(Stage stage, double simTime, long delay, Long seed,
                            DistributionConfig[] configs) throws Exception {
        FXMLLoader loader = new FXMLLoader(SimulationView.class.getResource("/fxml/simulation.fxml"));
        Scene scene = new Scene(loader.load(), 1550, 900);

        // Retrieve the controller and initialize with parameters
        SimulationController controller = loader.getController();
        controller.initialize(simTime, delay, seed, configs);

        // Set up the stage with the scene and additional settings
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setTitle("Simulation Running");
    }
}
