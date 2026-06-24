package clientGUI;

import java.io.IOException;

import clientController.ClientController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Temporary main class for testing the Entry Payment page directly.
 *
 * Before running this main:
 * 1. Run the server.
 * 2. Make sure the server listens on port 5555.
 * 3. Make sure the DB password was entered in the server.
 */
public class EntryPaymentPageMain extends Application {

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 5555;

    /*
     * This id is only used to create the ClientController.
     * The real customer id is entered inside the page.
     */
    private static final String DEFAULT_CLIENT_ID = "764937601";

    @Override
    public void start(Stage primaryStage) {
        try {
            /*
             * Allows opening the page directly without the regular employee login flow.
             * This is only for testing this specific page.
             */
            System.setProperty("entryPaymentTestMode", "true");

            ClientController clientController = new ClientController(
                    DEFAULT_HOST,
                    DEFAULT_PORT,
                    DEFAULT_CLIENT_ID
            );

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/clientGUI/EntryPaymentPage.fxml")
            );

            Parent root = loader.load();

            EntryPaymentPageController pageController = loader.getController();
            pageController.setClientController(clientController);

            Scene scene = new Scene(root, 900, 700);

            primaryStage.setTitle("Entry Payment Test");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(650);
            primaryStage.setMinHeight(500);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Connection Error");
            alert.setHeaderText("Failed to open Entry Payment page");
            alert.setContentText(
                    "Make sure the server is running on 127.0.0.1:5555"
            );
            alert.showAndWait();

            Platform.exit();

        } catch (Exception e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to open Entry Payment page");
            alert.setContentText(e.getMessage());
            alert.showAndWait();

            Platform.exit();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}