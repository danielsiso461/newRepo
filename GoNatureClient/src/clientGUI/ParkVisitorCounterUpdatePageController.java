package clientGUI;

import java.util.List;

import clientCommon.ClientSession;
import clientCommon.ParkVisitorCounterObserver;
import clientController.ClientController;
import common.OperationResponse;
import common.ParkVisitorCounterSnapshot;
import common.ParkVisitorCounterUpdateRequest;
import common.Protocol;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

/**
 * Allows park workers and park managers to update the real-time visitor counter.
 */
public class ParkVisitorCounterUpdatePageController implements ParkVisitorCounterObserver {

    private static final String ROLE_PARK_WORKER = "park_worker";
    private static final String ROLE_PARK_MANAGER = "park_manager";

    private ClientController clientController;

    @FXML
    private Label parkIdLabel;

    @FXML
    private ComboBox<String> actionComboBox;

    @FXML
    private Slider amountSlider;

    @FXML
    private Label amountLabel;

    @FXML
    private Button updateButton;

    @FXML
    private Label statusLabel;

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    @FXML
    private void initialize() {
        setupActionComboBox();
        setupSlider();

        System.out.println("[ParkVisitorCounterUpdatePage] initialize");
        System.out.println("[ParkVisitorCounterUpdatePage] testMode = " + isTestMode());
        System.out.println("[ParkVisitorCounterUpdatePage] role = " + getCurrentEmployeeRole());
        System.out.println("[ParkVisitorCounterUpdatePage] employeeId = " + getCurrentEmployeeId());
        System.out.println("[ParkVisitorCounterUpdatePage] parkId = " + getCurrentEmployeeParkId());

        if (!canCurrentEmployeeUpdateCounter()) {
            setControlsDisabled(true);
            parkIdLabel.setText("-");
            setErrorStatus("Only park workers and park managers can update visitor counter.");
            return;
        }

        parkIdLabel.setText(String.valueOf(getCurrentEmployeeParkId()));
        setControlsDisabled(false);
        setInfoStatus("Ready");
    }

    private void setupActionComboBox() {
        actionComboBox.getItems().setAll(
                ParkVisitorCounterUpdateRequest.ACTION_ENTRY,
                ParkVisitorCounterUpdateRequest.ACTION_EXIT
        );

        actionComboBox.setValue(ParkVisitorCounterUpdateRequest.ACTION_ENTRY);
    }

    private void setupSlider() {
        amountSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> updateAmountLabel()
        );

        updateAmountLabel();
    }

    private void updateAmountLabel() {
        amountLabel.setText(String.valueOf(getSelectedAmount()));
    }

    private int getSelectedAmount() {
        return (int) Math.round(amountSlider.getValue());
    }

    private boolean canCurrentEmployeeUpdateCounter() {
        if (!isTestMode() && !ClientSession.isEmployeeLoggedIn()) {
            return false;
        }

        String role = getCurrentEmployeeRole();
        int employeeParkId = getCurrentEmployeeParkId();

        return employeeParkId > 0
                && (ROLE_PARK_WORKER.equals(role)
                || ROLE_PARK_MANAGER.equals(role));
    }

    private void setControlsDisabled(boolean disabled) {
        updateButton.setDisable(disabled);
        actionComboBox.setDisable(disabled);
        amountSlider.setDisable(disabled);
    }

    @FXML
    private void handleUpdateCounter() {
        if (clientController == null) {
            setErrorStatus("Client is not connected.");
            showErrorAlert("Client Error", "Client is not connected.");
            return;
        }

        if (!canCurrentEmployeeUpdateCounter()) {
            setErrorStatus("You are not allowed to update visitor counter.");
            showErrorAlert("Permission Error", "You are not allowed to update visitor counter.");
            return;
        }

        String actionType = actionComboBox.getValue();

        if (actionType == null || actionType.isBlank()) {
            setErrorStatus("Please select action type.");
            return;
        }

        int amount = getSelectedAmount();

        ParkVisitorCounterUpdateRequest request =
                new ParkVisitorCounterUpdateRequest(
                        getCurrentEmployeeParkId(),
                        getCurrentEmployeeId(),
                        actionType,
                        amount
                );

        setInfoStatus("Updating visitor counter...");

        clientController.updateParkVisitorCounter(request);
    }

    @Override
    public void onParkVisitorCountersReceived(
            List<ParkVisitorCounterSnapshot> counters) {

        /*
         * This page does not need the full counters list.
         */
    }

    @Override
    public void onParkVisitorCounterOperationResponse(
            OperationResponse response, Protocol responseType) {

        if (response == null) {
            setErrorStatus("No response was received from the server.");
            return;
        }

        if (responseType != Protocol.PARK_VISITOR_COUNTER_UPDATE_RESULT) {
            return;
        }

        if (!response.isSuccess()) {
            setErrorStatus(response.getMessage());
            showErrorAlert("Update Failed", response.getMessage());
            return;
        }

        String message = response.getMessage();

        if (response.getData() instanceof ParkVisitorCounterSnapshot snapshot) {
            message = message
                    + " Current visitors: "
                    + snapshot.getCurrentVisitors()
                    + "/"
                    + snapshot.getMaxCapacity();
        }

        setSuccessStatus(message);
    }

    @Override
    public void onParkVisitorCountersUpdated() {
        /*
         * No automatic action is required here.
         */
    }

    private boolean isTestMode() {
        return Boolean.getBoolean("visitorCounterTestMode");
    }

    private String getCurrentEmployeeRole() {
        if (isTestMode()) {
            return System.getProperty("visitorCounterTestRole", "");
        }

        return ClientSession.getEmployeeRole();
    }

    private int getCurrentEmployeeId() {
        if (isTestMode()) {
            return Integer.getInteger("visitorCounterTestEmployeeId", -1);
        }

        return ClientSession.getEmployeeId();
    }

    private int getCurrentEmployeeParkId() {
        if (isTestMode()) {
            return Integer.getInteger("visitorCounterTestParkId", -1);
        }

        return ClientSession.getEmployeeParkId();
    }

    private void setInfoStatus(String message) {
        updateStatusLabel(message, "status-info");
        System.out.println("[ParkVisitorCounterUpdatePage] " + message);
    }

    private void setSuccessStatus(String message) {
        updateStatusLabel(message, "status-success");
        System.out.println("[ParkVisitorCounterUpdatePage] SUCCESS - " + message);
    }

    private void setErrorStatus(String message) {
        updateStatusLabel(message, "status-error");
        System.out.println("[ParkVisitorCounterUpdatePage] ERROR - " + message);
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

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}