package MVC.view;

import MVC.controller.ResultsController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Loads and displays the results view for the Visa Application Processing Simulator.
 * Responsible for initializing the FXML layout, setting up the scene, and returning
 * the associated controller for further interaction.
 *
 * <p>Usage:
 * <pre>
 *     ResultsController controller = ResultView.show(stage);
 * </pre>
 * </p>
 */
public class ResultView {

    /**
     * Loads the results view FXML, sets up the scene on the given stage,
     * and returns the associated ResultsController.
     *
     * @param stage The JavaFX stage to display the results view on
     * @return The ResultsController associated with the loaded FXML
     * @throws Exception if the FXML or scene fails to load
     */
    public static ResultsController show(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(ResultView.class.getResource("/fxml/results.fxml"));
        Scene scene = new Scene(loader.load(),1550, 900);
        stage.setScene(scene);
        stage.setMaximized(true);

        return loader.getController();
    }
}