package clientGUI;

import java.io.IOException;

import clientCommon.ClientSession;
import clientController.ClientController;
import common.CommonConstants;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Direct test main for opening only the relevant park parameter page.
 * 
 * Run arguments:
 * park_manager
 * department_manager
 */
public class ClientParameterDirectTestMain extends Application {

    private static final String HOST = "127.0.0.1";

    private static final int DANA_ID = 1;
    private static final String DANA_ROLE = "park_manager";
    private static final int DANA_PARK_ID = 1;

    private static final int AVI_ID = 4;
    private static final String AVI_ROLE = "department_manager";
    private static final int AVI_PARK_ID = -1;

    private ClientController clientController;

    @Override
    public void start(Stage primaryStage) {
        ClientScreenManager.setPrimaryStage(primaryStage);

        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(620);

        primaryStage.setOnCloseRequest(event -> {
            disconnectCurrentClient();
            Platform.exit();
            System.exit(0);
        });

        String mode = getRequestedMode();

        try {
            if ("park_manager".equals(mode)) {
                openAsParkManager();
            } else if ("department_manager".equals(mode)) {
                openAsDepartmentManager();
            } else {
                System.out.println("Unknown mode. Use: park_manager or department_manager");
                Platform.exit();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    private String getRequestedMode() {
        if (getParameters().getRaw().isEmpty()) {
            return "park_manager";
        }

        return getParameters().getRaw().get(0);
    }

    private void openAsParkManager() throws IOException {
        clientController = new ClientController(
                HOST,
                CommonConstants.DEFAULT_PORT,
                String.valueOf(DANA_ID)
        );

        ClientSession.setLoggedEmployee(
                DANA_ID,
                DANA_ROLE,
                DANA_PARK_ID
        );

        ClientScreenManager.setClientController(clientController);
        ClientScreenManager.showParkParameterRequestPage();
    }

    private void openAsDepartmentManager() throws IOException {
        clientController = new ClientController(
                HOST,
                CommonConstants.DEFAULT_PORT,
                String.valueOf(AVI_ID)
        );

        ClientSession.setLoggedEmployee(
                AVI_ID,
                AVI_ROLE,
                AVI_PARK_ID
        );

        ClientScreenManager.setClientController(clientController);
        ClientScreenManager.showParkParameterApprovalPage();
    }

    private void disconnectCurrentClient() {
        if (clientController != null) {
            try {
                clientController.disconnectFromServer();
            } catch (Exception e) {
                e.printStackTrace();
            }

            clientController = null;
        }

        ClientSession.clear();
    }

    public static void main(String[] args) {
        launch(args);
    }
}