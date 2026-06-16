package clientGUI;

import java.io.IOException;

import clientController.ClientController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Manages switching between client screens.
 */
public final class ClientScreenManager {

    private static Stage primaryStage;
    private static ClientController clientController;

    private ClientScreenManager() {
    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void setClientController(ClientController controller) {
        clientController = controller;
    }

    public static ClientController getClientController() {
        return clientController;
    }

    public static void showWelcomePage() {
        loadPage("/clientGUI/WelcomePage.fxml", "Welcome");
    }

    public static void showOrderTableDisplayPage() {
        loadPage("/clientGUI/OrderTableDisplayPage.fxml", "Orders");
    }

    public static void showOrderUpdatePage() {
        loadPage("/clientGUI/OrderUpdatePage.fxml", "Update Order");
    }

    public static void showParkSelectionPage() {
        loadPage("/clientGUI/ParkSelectionPage.fxml", "Park Selection");
    }

    public static void showReportsPage() {
        loadPage("/clientGUI/ReportsPage.fxml", "Reports");
    }

    private static void loadPage(String fxmlPath, String title) {
        try {
            if (primaryStage == null) {
                System.out.println("Primary stage was not set.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(ClientScreenManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            connectController(loader.getController());

            Scene scene = new Scene(root);
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void connectController(Object controller) {
        if (controller == null || clientController == null) {
            return;
        }

        if (controller instanceof ParkSelectionPageController parkController) {
            parkController.setServerRequestHandler(
                    message -> clientController.sendMessageToServer(message)
            );

            clientController.addParkObserver(parkController);
        }

        if (controller instanceof ReportsPageController reportsController) {
            reportsController.setClientController(clientController);

            clientController.addReportObserver(reportsController);
            clientController.addParkObserver(reportsController);

            clientController.requestActiveParks();
        }
    }
}