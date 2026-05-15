package client;

import java.util.Scanner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main JavaFX application class for launching the client application.
 * Loads the welcome page and displays it as the first screen.
 */
@SuppressWarnings("deprecation")
public class ClientUI extends Application {

    
    /**
     * Starts the JavaFX application.
     * Loads the welcome page FXML file, creates the scene,
     * and displays the main application window.
     *
     * @param stage the primary stage of the JavaFX application
     * @throws Exception if the FXML file cannot be loaded
     */
    @Override
    public void start(Stage stage) throws Exception {

        // Load the FXML file of the welcome page
        FXMLLoader loader = new FXMLLoader(getClass().getResource(ConstantsUI.welcomePage));
        Parent root = loader.load();

        // Show UI
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Welcome Page");
        stage.show();
    }

    /**
     * Default constructor required by JavaFX.
     */
    public ClientUI() {
        // For JavaFX
    }

    /**
     * Main method that launches the client application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}