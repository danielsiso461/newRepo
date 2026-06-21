package clientGUI;

import java.io.IOException;

import clientCommon.ClientSession;
import clientController.ClientController;
import javafx.application.Platform;
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

    public static void showMakeOrderPage() {
        loadPage("/clientGUI/MakeOrderPage.fxml", "Make Order");
    }

    public static void showParkSelectionPage() {
        loadPage("/clientGUI/ParkSelectionPage.fxml", "Park Selection");
    }

    public static void showReportsPage() {
        loadPage("/clientGUI/ReportsPage.fxml", "Reports");
    }

    public static void showParkParameterRequestPage() {
        if (!"park_manager".equals(ClientSession.getEmployeeRole())) {
            System.out.println("Access denied: only park managers can open parameter request page.");
            return;
        }

        loadPage("/clientGUI/ParkParameterRequestPage.fxml", "Park Parameter Request");
    }

    public static void showParkParameterApprovalPage() {
        if (!"department_manager".equals(ClientSession.getEmployeeRole())) {
            System.out.println("Access denied: only department managers can open parameter approval page.");
            return;
        }

        loadPage("/clientGUI/ParkParameterApprovalPage.fxml", "Park Parameter Approval");
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

            /*
             * Keep a reasonable minimum size.
             * The FXML layouts are now responsible for resizing naturally.
             */
            primaryStage.setMinWidth(650.0);
            primaryStage.setMinHeight(500.0);

            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void connectController(Object controller) {
        if (controller == null || clientController == null) {
            return;
        }

        if (controller instanceof OrderTableDisplayController orderTableController) {
            orderTableController.setClientController(clientController);

            clientController.requestOrders();

            Platform.runLater(orderTableController);

            return;
        }

        if (controller instanceof ParkSelectionPageController parkController) {
            parkController.setServerRequestHandler(
                    message -> clientController.sendMessageToServer(message)
            );

            clientController.addParkObserver(parkController);

            return;
        }

        if (controller instanceof ReportsPageController reportsController) {
            reportsController.setClientController(clientController);

            clientController.addReportObserver(reportsController);
            clientController.addParkObserver(reportsController);

            clientController.requestActiveParks();

            return;
        }

        if (controller instanceof ParkParameterRequestPageController requestController) {
            requestController.setClientController(clientController);

            clientController.addParkObserver(requestController);
            clientController.addParkParameterObserver(requestController);

            clientController.requestActiveParks();

            return;
        }

        if (controller instanceof ParkParameterApprovalPageController approvalController) {
            approvalController.setClientController(clientController);

            clientController.addParkParameterObserver(approvalController);

            if (ClientSession.isEmployeeLoggedIn()
                    && "department_manager".equals(ClientSession.getEmployeeRole())) {

                clientController.requestPendingParkParameterChangeRequests(
                        ClientSession.getEmployeeId()
                );
            }

            return;
        }
    }
}