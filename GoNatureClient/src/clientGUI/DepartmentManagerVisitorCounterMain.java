package clientGUI;

import java.io.IOException;
import java.net.URL;

import clientController.ClientController;
import common.CommonConstants;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Temporary main for testing the department manager visitor counter view page.
 */
public class DepartmentManagerVisitorCounterMain extends Application {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = CommonConstants.DEFAULT_PORT;

    /*
     * According to your employee table:
     * Avi Sofer - employee_id 4 - department_manager - park_id NULL
     */
    private static final int DEPARTMENT_MANAGER_EMPLOYEE_ID = 4;

    private static final String TEST_CLIENT_ID = "department-manager-visitor-counter-test";

    private ClientController clientController;

    @Override
    public void start(Stage stage) {
        try {
            setTestSession();

            clientController = new ClientController(
                    SERVER_HOST,
                    SERVER_PORT,
                    TEST_CLIENT_ID
            );

            URL fxmlUrl = getClass().getResource(
                    "/clientGUI/ParkVisitorCounterViewPage.fxml"
            );

            if (fxmlUrl == null) {
                throw new IOException("FXML file was not found: ParkVisitorCounterViewPage.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            ParkVisitorCounterViewPageController controller =
                    loader.getController();

            controller.setClientController(clientController);
            clientController.addParkVisitorCounterObserver(controller);

            clientController.requestParkVisitorCounters(DEPARTMENT_MANAGER_EMPLOYEE_ID);

            Scene scene = new Scene(root, 900, 700);

            stage.setTitle("Department Manager - Visitor Counters");
            stage.setScene(scene);
            stage.setMinWidth(650);
            stage.setMinHeight(500);
            stage.setOnCloseRequest(event -> closeClientSafely());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();

            showErrorAndExit(
                    "Connection / FXML Error",
                    "Failed to open department manager visitor counter page",
                    e.getMessage()
            );

        } catch (Exception e) {
            e.printStackTrace();

            showErrorAndExit(
                    "Error",
                    "Failed to open department manager visitor counter page",
                    e.getMessage()
            );
        }
    }

    private void setTestSession() {
        System.setProperty("visitorCounterTestMode", "true");
        System.setProperty("visitorCounterTestRole", "department_manager");
        System.setProperty("visitorCounterTestEmployeeId", String.valueOf(DEPARTMENT_MANAGER_EMPLOYEE_ID));
        System.setProperty("visitorCounterTestParkId", "-1");
    }

    private void closeClientSafely() {
        try {
            if (clientController != null) {
                clientController.disconnectFromServer();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showErrorAndExit(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();

        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}