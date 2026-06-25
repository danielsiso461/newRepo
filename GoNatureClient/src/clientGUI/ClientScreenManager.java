
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

    /**
     * the main stage of the client application
     */
    private static Stage primaryStage;

    /**
     * the client controller used by the screens
     */
    private static ClientController clientController;

    /**
     * Private constructor to prevent creating ClientScreenManager objects.
     */
    private ClientScreenManager() {
    }

    /**
     * Sets the primary stage of the application.
     *
     * @param stage the primary stage
     */
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Sets the client controller used by the screen manager.
     *
     * @param controller the client controller
     */
    public static void setClientController(ClientController controller) {
        clientController = controller;
    }

    /**
     * Returns the client controller used by the screen manager.
     *
     * @return the client controller
     */
    public static ClientController getClientController() {
        return clientController;
    }

    /**
     * Shows the welcome page.
     */
    public static void showWelcomePage() {
        loadPage("/clientGUI/WelcomePage.fxml", "Welcome");
    }

    /**
     * Shows the order table display page.
     */
    public static void showOrderTableDisplayPage() {
        loadPage("/clientGUI/OrderTableDisplayPage.fxml", "Orders");
    }

    /**
     * Shows the order update page.
     */
    public static void showOrderUpdatePage() {
        loadPage("/clientGUI/OrderUpdatePage.fxml", "Update Order");
    }

    /**
     * Shows the make order page.
     */
    public static void showMakeOrderPage() {
        loadPage("/clientGUI/MakeOrderPage.fxml", "Make Order");
        
    }

    /**
     * Shows the park selection page.
     */
    public static void showParkSelectionPage() {
        loadPage("/clientGUI/ParkSelectionPage.fxml", "Park Selection");
    }

    /**
     * Shows the reports page.
     */
    public static void showReportsPage() {
        loadPage("/clientGUI/ReportsPage.fxml", "Reports");
    }

    /**
     * Shows the entry payment page.
     *
     * The page is opened only if an employee is logged in.
     */
    public static void showEntryPaymentPage() {
        if (!ClientSession.isEmployeeLoggedIn()) {
            System.out.println("Access denied: only employees can open entry payment page.");
            return; 
        }

        loadPage("/clientGUI/EntryPaymentPage.fxml", "Entry Payment");
    }
    
    /**
     * Shows the park visitor counter view page.
     *
     * The page is opened only for park managers and department managers.
     */
    public static void showParkVisitorCounterViewPage() {
        String role = ClientSession.getEmployeeRole();

        if (!"park_manager".equals(role)
                && !"department_manager".equals(role)) {
            System.out.println("Access denied: only park managers and department managers can open visitor counter view page.");
            return;
        }

        loadPage("/clientGUI/ParkVisitorCounterViewPage.fxml", "Park Visitor Counter");
    }

    /**
     * Shows the park visitor counter update page.
     *
     * The page is opened only for park workers and park managers.
     */
    public static void showParkVisitorCounterUpdatePage() {
        String role = ClientSession.getEmployeeRole();

        if (!"park_worker".equals(role)
                && !"park_manager".equals(role)) {
            System.out.println("Access denied: only park workers and park managers can update visitor counter.");
            return;
        }

        loadPage("/clientGUI/ParkVisitorCounterUpdatePage.fxml", "Update Visitor Counter");
    }

    /**
     * Shows the park parameter request page.
     *
     * The page is opened only for park managers.
     */
    public static void showParkParameterRequestPage() {
        if (!"park_manager".equals(ClientSession.getEmployeeRole())) {
            System.out.println("Access denied: only park managers can open parameter request page.");
            return;
        }

        loadPage("/clientGUI/ParkParameterRequestPage.fxml", "Park Parameter Request");
    }

    /**
     * Shows the park parameter approval page.
     *
     * The page is opened only for department managers.
     */
    public static void showParkParameterApprovalPage() {
        if (!"department_manager".equals(ClientSession.getEmployeeRole())) {
            System.out.println("Access denied: only department managers can open parameter approval page.");
            return;
        }

        loadPage("/clientGUI/ParkParameterApprovalPage.fxml", "Park Parameter Approval");
    }

    /**
     * Loads an FXML page and displays it on the primary stage.
     *
     * @param fxmlPath the path of the FXML file
     * @param title the title of the window
     */
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
             * The FXML layouts are responsible for resizing naturally.
             */
            primaryStage.setMinWidth(650.0);
            primaryStage.setMinHeight(500.0);

            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Connects the loaded page controller to the client controller.
     *
     * @param controller the loaded page controller
     */
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
        
        if (controller instanceof ParkVisitorCounterViewPageController counterViewController) {
            counterViewController.setClientController(clientController);

            clientController.addParkVisitorCounterObserver(counterViewController);

            if (ClientSession.isEmployeeLoggedIn()) {
                clientController.requestParkVisitorCounters(
                        ClientSession.getEmployeeId()
                );
            }

            return;
        }

        if (controller instanceof ParkVisitorCounterUpdatePageController counterUpdateController) {
            counterUpdateController.setClientController(clientController);

            clientController.addParkVisitorCounterObserver(counterUpdateController);

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

        if (controller instanceof EntryPaymentPageController entryPaymentController) {
            entryPaymentController.setClientController(clientController);

            return;
        }
    }
}
