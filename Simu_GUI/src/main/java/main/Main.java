package main;

import javafx.application.Application;
import MVC.view.HomeView;

/**
 * Entry point of the simulation application.
 * <p>Launches the JavaFX GUI by starting the {@link HomeView} class.</p>
 */
public class Main {

    /**
     * Launches the JavaFX application.
     *
     * @param args the command-line arguments passed to the application
     */
    public static void main(String[] args) {
        Application.launch(HomeView.class);
    }
}