package clientGUI;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import clientController.ClientController;
import common.CommonConstants;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Controls the welcome page.
 * 
 * The user first enters a user ID.
 * Then the user enters the server IP address.
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
    private Label inputTitleLabel;

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
        assert inputTitleLabel != null : "inputTitleLabel was not injected";
        assert confirmButton != null : "confirmButton was not injected";
        assert inputField != null : "inputField was not injected";
        assert messageLabel != null : "messageLabel was not injected";

        showIdStep();

        messageLabel.textProperty().addListener((observable, oldText, newText) -> {
            updateMessageLabelVisibility();
        });

        updateMessageLabelVisibility();

        Platform.runLater(() -> {
            Stage stage = (Stage) inputField.getScene().getWindow();

            stage.setOnCloseRequest(event -> {
                Platform.exit();
                System.exit(0);
            });
        });
    }

    private void showIdStep() {
        idEntered = false;

        commandLabel.setText("Step 1: Enter your user ID");
        inputTitleLabel.setText("User ID");
        inputField.setPromptText("Enter 9-digit user ID");
        confirmButton.setText("Continue");

        inputField.clear();
        clearMessage();
    }

    private void showServerAddressStep() {
        idEntered = true;

        commandLabel.setText("Step 2: Enter the server IP address");
        inputTitleLabel.setText("Server IP Address");
        inputField.setPromptText("");
        confirmButton.setText("Connect");

        inputField.clear();
        clearMessage();
    }

    private void handleIdInput() {
        id = inputField.getText().trim();

        if (!id.matches("\\d{9}")) {
            showError("ID should contain exactly 9 digits");
            return;
        }

        if ("000000000".equals(id)) {
            showError("ID should be a valid positive ID");
            return;
        }

        showServerAddressStep();
    }

    private void handleAddressInput() {
        address = inputField.getText().trim();

        if (address.isEmpty()) {
            showError("Server IP address cannot be empty");
            return;
        }

        launchOrderTable();
    }

    private void launchOrderTable() {
        Stage stage = (Stage) inputField.getScene().getWindow();

        ClientController clientController;

        try {
            clientController = new ClientController(
                    address,
                    CommonConstants.DEFAULT_PORT,
                    id
            );

            /*
             * Save the active stage and client controller,
             * so all other screens will use the same connection.
             */
            ClientScreenManager.setPrimaryStage(stage);
            ClientScreenManager.setClientController(clientController);

        } catch (IOException e) {
            showError("Could not connect to server. Check the server IP address.");
            inputField.clear();
            return;
        }

        ClientScreenManager.showOrderTableDisplayPage();
    }

    private void updateMessageLabelVisibility() {
        boolean hasMessage = messageLabel.getText() != null
                && !messageLabel.getText().isBlank();

        messageLabel.setVisible(hasMessage);
        messageLabel.setManaged(hasMessage);
    }

    private void clearMessage() {
        messageLabel.setText("");
    }

    private void showError(String message) {
        messageLabel.setTextFill(Color.RED);
        messageLabel.setText(message);
    }
}