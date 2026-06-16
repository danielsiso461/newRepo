package clientGUI;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import clientController.ClientController;
import common.CommonConstants;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Controls the welcome page.
 * 
 * The user enters an ID and a server address.
 * After a successful connection, the order table page is opened.
 */
public class WelcomePageController {

    private boolean idEntered = false;
    private String id;
    private String address;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Label commandLabel;

    @FXML
    private Button confirmButton;

    @FXML
    private TextField inputField;

    @FXML
    private Label messageLabel;

    @FXML
    void btnClick(ActionEvent event) {
        if (!idEntered) {
            handleIdInput();
        } else {
            handleAddressInput();
        }
    }

    @FXML
    void initialize() {
        assert commandLabel != null : "commandLabel was not injected";
        assert confirmButton != null : "confirmButton was not injected";
        assert inputField != null : "inputField was not injected";
        assert messageLabel != null : "messageLabel was not injected";

        Platform.runLater(() -> {
            Stage stage = (Stage) inputField.getScene().getWindow();

            stage.setOnCloseRequest(event -> {
                Platform.exit();
                System.exit(0);
            });
        });
    }

    private void handleIdInput() {
        id = inputField.getText().trim();

        if (id.length() != 9) {
            showError("id should be 9 digits long");
            return;
        }

        try {
            int value = Integer.parseInt(id);

            if (value <= 0) {
                showError("id should be positive");
                return;
            }

            idEntered = true;
            messageLabel.setText("");
            inputField.clear();
            commandLabel.setText("Enter server address");

        } catch (NumberFormatException e) {
            showError("id should be a number");
        }
    }

    private void handleAddressInput() {
        address = inputField.getText().trim();

        if (address.isEmpty()) {
            showError("server address cannot be empty");
            return;
        }

        launchOrderTable();
    }

    private void launchOrderTable() {
        Stage stage = (Stage) inputField.getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(getClass().getResource(ConstantsUI.orderTable));
        Parent root;

        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            Platform.exit();
            System.exit(1);
            return;
        }

        OrderTableDisplayController controller = loader.getController();

        ClientController clientController;

        try {
            clientController = new ClientController(address, CommonConstants.DEFAULT_PORT, id);

            /*
             * These two lines allow other screens, such as ReportsPageController,
             * to use the same client connection.
             */
            ClientScreenManager.setPrimaryStage(stage);
            ClientScreenManager.setClientController(clientController);

        } catch (IOException e) {
            showError("Bad server address");
            inputField.clear();
            return;
        }

        controller.setClientController(clientController);

        clientController.requestOrders();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Order Table");
        stage.show();

        Platform.runLater(controller);
    }

    private void showError(String message) {
        messageLabel.setTextFill(Color.RED);
        messageLabel.setText(message);
    }
}