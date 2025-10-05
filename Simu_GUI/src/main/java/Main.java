import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the welcome page FXML
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/welcome.fxml"));
            Scene scene = new Scene(loader.load());

            // Setup stage
            primaryStage.setTitle("Visa Application Processing Simulator");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.setOnCloseRequest(event -> {
                // Cleanup resources
                datasource.MariaDbJpaConnection.shutdown();
                System.exit(0);
            });

            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load the application: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}