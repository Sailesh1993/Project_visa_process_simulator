package MVC.view;

import MVC.controller.ResultsController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ResultView {

    public static ResultsController show(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(ResultView.class.getResource("/fxml/results.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.setMaximized(true);

        return loader.getController();
    }
}