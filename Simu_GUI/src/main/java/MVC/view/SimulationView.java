package MVC.view;

import MVC.controller.SimulationController;
import eduni.project_distributionconfiguration.DistributionConfig;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SimulationView {

    public static void show(Stage stage, double simTime, long delay, Long seed,
                            DistributionConfig[] configs) throws Exception {
        FXMLLoader loader = new FXMLLoader(SimulationView.class.getResource("/fxml/simulation.fxml"));
        Scene scene = new Scene(loader.load());

        // Get MVC.controller and initialize it
        SimulationController controller = loader.getController();
        controller.initialize(simTime, delay, seed, configs);

        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setTitle("Simulation Running");
    }
}