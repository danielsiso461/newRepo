package clientGUI;

import clientCommon.ClientSession;
import clientCommon.EntryPriceObserver;
import clientController.ClientController;
import common.EntryPriceReceipt;
import common.OperationResponse;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Controls the entry payment page.
 *
 * This page is used by an employee to calculate the price a customer
 * should pay according to a specific order number.
 */
public class EntryPaymentPageController implements EntryPriceObserver {

    private ClientController clientController;
    
    private Scene prevScene;

    @FXML
    private TextField orderNumberField;

    @FXML
    private Button calculateButton;

    @FXML
    private TextArea receiptTextArea;

    @FXML
    private Label statusLabel;

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;

        if (this.clientController != null) {
            this.clientController.addEntryPriceObserver(this);
        }
    }
    
    public void setPrevScene(Scene prevScene) {
    	this.prevScene = prevScene;
    }

    @FXML
    private void initialize() {
        boolean testMode = Boolean.getBoolean("entryPaymentTestMode");

        if (!testMode && !ClientSession.isEmployeeLoggedIn()) {
            calculateButton.setDisable(true);
            orderNumberField.setDisable(true);
            receiptTextArea.setText("Only employees can use this page.");
            setErrorStatus("Only employees can use this page.");
            return;
        }

        if (testMode) {
            setInfoStatus("Ready - test mode");
        } else {
            setInfoStatus("Ready");
        }
    }

    @FXML
    private void handleCalculatePrice() {
        if (clientController == null) {
            String message = "Client is not connected.";

            setErrorStatus(message);
            showErrorAlert("Client Error", message);
            return;
        }

        String orderNumberText = orderNumberField.getText();

        if (orderNumberText == null || orderNumberText.isBlank()) {
            String message = "Please enter order number.";

            setErrorStatus(message);
            showWarningAlert("Missing Order Number", message);
            return;
        }

        orderNumberText = orderNumberText.trim();

        if (!orderNumberText.matches("\\d+")) {
            String message = "Order number must contain digits only.";

            setErrorStatus(message);
            showWarningAlert("Invalid Order Number", message);
            return;
        }

        int orderNumber;

        try {
            orderNumber = Integer.parseInt(orderNumberText);
        } catch (NumberFormatException e) {
            String message = "Order number is too large.";

            setErrorStatus(message);
            showWarningAlert("Invalid Order Number", message);
            return;
        }

        receiptTextArea.clear();
        setInfoStatus("Calculating price...");

        clientController.calculateEntryPrice(orderNumber);
    }

    @Override
    public void onEntryPriceCalculated(OperationResponse response) {
        if (response == null) {
            String message = "No response was received from the server.";

            setErrorStatus(message);
            showErrorAlert("No Response", message);
            return;
        }

        if (!response.isSuccess()) {
            setErrorStatus(response.getMessage());
            showErrorAlert("Calculation Failed", response.getMessage());
            return;
        }

        if (!(response.getData() instanceof EntryPriceReceipt receipt)) {
            String message = "Invalid receipt data was received from the server.";

            setErrorStatus(message);
            showErrorAlert("Invalid Data", message);
            return;
        }

        receiptTextArea.setText(receipt.toReceiptText());
        setSuccessStatus("Price calculated successfully.");
    }

    private void setInfoStatus(String message) {
        updateStatusLabel(message, "status-info");
        System.out.println("[EntryPaymentPage] " + message);
    }

    private void setSuccessStatus(String message) {
        updateStatusLabel(message, "status-success");
        System.out.println("[EntryPaymentPage] SUCCESS - " + message);
    }

    private void setErrorStatus(String message) {
        updateStatusLabel(message, "status-error");
        System.out.println("[EntryPaymentPage] ERROR - " + message);
    }

    private void updateStatusLabel(String message, String statusStyleClass) {
        statusLabel.setText("Status: " + message);

        statusLabel.getStyleClass().remove("status-info");
        statusLabel.getStyleClass().remove("status-success");
        statusLabel.getStyleClass().remove("status-error");

        if (!statusLabel.getStyleClass().contains(statusStyleClass)) {
            statusLabel.getStyleClass().add(statusStyleClass);
        }
    }

    private void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
    private void handleBack(ActionEvent event) {
    	if (clientController != null) {
    		clientController.removeEntryPriceObserver(this);
    	}

    	if (prevScene == null) {
    		setErrorStatus("Previous page is not available.");
    		return;
    	}

    	Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    	stage.setTitle("Park Worker Home Page");
    	stage.setScene(prevScene);
    	stage.show();
    }
}