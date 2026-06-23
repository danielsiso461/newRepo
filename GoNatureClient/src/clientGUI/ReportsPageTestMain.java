package clientGUI;

import java.io.IOException;

import clientCommon.ClientSession;
import clientController.ClientController;
import common.CommonConstants;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Temporary main class for testing the reports page directly.
 */
public class ReportsPageTestMain extends Application {

    private ClientController clientController;
    private static final String HOST = "127.0.0.1";
    @Override
    public void start(Stage primaryStage) {

        int employeeId = 1;
        String employeeRole = "park_manager";
        int employeeParkId = 1;

        try {
            clientController = new ClientController(
            		HOST,
                    CommonConstants.DEFAULT_PORT,
                    String.valueOf(employeeId)
            );

            ClientSession.setLoggedEmployee(employeeId, employeeRole, employeeParkId);

            ClientScreenManager.setPrimaryStage(primaryStage);
            ClientScreenManager.setClientController(clientController);

            primaryStage.setMinWidth(850);
            primaryStage.setMinHeight(600);
            
            ClientScreenManager.showReportsPage();

        } catch (IOException e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        if (clientController != null) {
            clientController.disconnectFromServer();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}