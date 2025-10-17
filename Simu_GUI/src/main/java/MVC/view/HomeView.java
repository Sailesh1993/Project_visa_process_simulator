package MVC.view;

import ORM.datasource.MariaDbJpaConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * HomeView represents the main entry point and home screen of the Visa Application
 * Processing Simulator application.
 *
 * <p>This class extends {@link Application} and serves as the primary JavaFX application
 * class that launches the simulator. It is responsible for:</p>
 * <ul>
 *     <li>Loading and displaying the welcome/home screen where users configure simulations.</li>
 *     <li>Managing the application window lifecycle, including initialization and shutdown.</li>
 *     <li>Handling proper cleanup of database connections on application exit.</li>
 *     <li>Providing a navigation method for returning to the home screen from other views.</li>
 * </ul>
 *
 * <p>The home screen allows users to configure simulation parameters, select distribution
 * types for service points, view recent simulation runs, and launch new simulations.</p>
 *
 * See {@link Application}
 */
public class HomeView extends Application {

    /**
     * Starts the JavaFX application and displays the home screen.
     *
     * <p>Initializes the primary stage with the home view, sets up a close request
     * handler to ensure proper database shutdown when the application is closed, and
     * displays the window in maximized mode. If loading fails, the error is printed
     * to the console.</p>
     *
     * @param primaryStage the primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            show(primaryStage);
            primaryStage.setOnCloseRequest(event -> {
                MariaDbJpaConnection.shutdown();
                System.exit(0);
            });
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load the application: " + e.getMessage());
        }
    }

    /**
     * Loads and displays the home view in the specified stage.
     *
     * <p>This static method loads the {@code welcome.fxml} file, creates a scene with
     * specified dimensions, and configures the stage. It can be called both during
     * the initial application startup and when navigating back to the home screen
     * from other views.</p>
     *
     * @param stage the stage in which to display the home view
     * @throws Exception if the FXML file cannot be loaded or the scene creation fails
     */
    public static void show(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(HomeView.class.getResource("/fxml/welcome.fxml"));
        Scene scene = new Scene(loader.load(), 1550, 900);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setTitle("Visa Application Processing Simulator");
    }
}
