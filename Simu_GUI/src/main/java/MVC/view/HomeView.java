package MVC.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HomeView extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            show(primaryStage);
            primaryStage.setOnCloseRequest(event -> {
                Object_Relational_Mapping_ORM.datasource.MariaDbJpaConnection.shutdown();
                System.exit(0);
            });
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load the application: " + e.getMessage());
        }
    }

    // Single method for loading - used by both start() and navigation
    public static void show(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(HomeView.class.getResource("/fxml/welcome.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setTitle("Visa Application Processing Simulator");
    }
}