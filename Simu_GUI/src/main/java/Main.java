import javafx.application.Application;
import MVC.view.HomeView;

/**
 * Entry point of the simulation application.
 * <p>Launches the JavaFX GUI by starting the {@link HomeView} class.</p>
 */
public class Main {

    /**
     * Main method that launches the JavaFX application.
     */
    public static void main(String[] args) {
        Application.launch(HomeView.class);
    }
}