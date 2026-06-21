package clientGUI;

import java.util.List;

import clientCommon.ClientSession;
import clientCommon.ParkObserver;
import clientCommon.ParkParameterObserver;
import clientController.ClientController;
import common.OperationResponse;
import common.Park;
import common.ParkParameterChangeRequest;
import common.Protocol;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controls the park parameter change request page.
 * 
 * This page is used by park managers.
 */
public class ParkParameterRequestPageController
        implements ParkObserver, ParkParameterObserver {

    private ClientController clientController;

    @FXML
    private Label headerLabel;

    @FXML
    private Label subHeaderLabel;

    @FXML
    private ComboBox<Park> parkComboBox;

    @FXML
    private ComboBox<ParameterOption> parameterComboBox;

    @FXML
    private Label currentValueLabel;

    @FXML
    private TextField newValueField;

    @FXML
    private Button submitButton;

    @FXML
    private Label statusLabel;

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    @FXML
    private void initialize() {
        initParameters();

        parameterComboBox.setOnAction(event -> updateCurrentValueLabel());
        parkComboBox.setOnAction(event -> updateCurrentValueLabel());

        headerLabel.setText("Park Parameter Update Request");
        subHeaderLabel.setText(
                "Submit a request to update one of your park's operating parameters"
        );

        currentValueLabel.setText("Current value: -");
        submitButton.setDisable(true);

        setInfoStatus("Ready");
    }

    private void initParameters() {
        parameterComboBox.getItems().clear();

        parameterComboBox.getItems().addAll(
                new ParameterOption("Maximum capacity", "max_capacity"),
                new ParameterOption("Places for unplanned visitors", "places_for_unplanned_visitors"),
                new ParameterOption("Estimated visit duration (hours)", "estimated_visit_duration_hours")
        );

        if (!parameterComboBox.getItems().isEmpty()) {
            parameterComboBox.setValue(parameterComboBox.getItems().get(0));
        }
    }

    @Override
    public void onParksReceived(List<Park> parks) {
        if (!"park_manager".equals(ClientSession.getEmployeeRole())) {
            setErrorStatus("Only park managers can request parameter updates.");
            submitButton.setDisable(true);
            return;
        }

        if (parks == null || parks.isEmpty()) {
            setErrorStatus("No parks found.");
            submitButton.setDisable(true);
            return;
        }

        int employeeParkId = ClientSession.getEmployeeParkId();

        ObservableList<Park> visibleParks = FXCollections.observableArrayList();

        for (Park park : parks) {
            if (park.getParkId() == employeeParkId) {
                visibleParks.add(park);
            }
        }

        if (visibleParks.isEmpty()) {
            setErrorStatus("No park is assigned to this manager.");
            submitButton.setDisable(true);
            return;
        }

        parkComboBox.setItems(visibleParks);
        parkComboBox.setValue(visibleParks.get(0));

        submitButton.setDisable(false);
        updateCurrentValueLabel();

        setInfoStatus("Park data loaded successfully.");
    }

    private void updateCurrentValueLabel() {
        Park selectedPark = parkComboBox.getValue();
        ParameterOption selectedParameter = parameterComboBox.getValue();

        if (selectedPark == null || selectedParameter == null) {
            currentValueLabel.setText("Current value: -");
            return;
        }

        String currentValue = getCurrentParameterValue(selectedPark, selectedParameter.getValue());
        currentValueLabel.setText(currentValue);
    }

    private String getCurrentParameterValue(Park park, String parameterName) {
        switch (parameterName) {

        case "max_capacity":
            return String.valueOf(park.getMaxCapacity());

        case "places_for_unplanned_visitors":
            return String.valueOf(park.getPlacesForUnplannedVisitors());

        case "estimated_visit_duration_hours":
            return String.valueOf((int) park.getEstimatedVisitDurationHours());

        default:
            return "-";
        }
    }

    @FXML
    private void handleSubmitRequest() {
        if (clientController == null) {
            setErrorStatus("Client is not connected.");
            showErrorAlert("Client Error", "Client is not connected.");
            return;
        }

        if (!ClientSession.isEmployeeLoggedIn()) {
            setErrorStatus("Employee is not logged in.");
            showErrorAlert("Login Error", "Employee is not logged in.");
            return;
        }

        if (!"park_manager".equals(ClientSession.getEmployeeRole())) {
            setErrorStatus("Only park managers can create update requests.");
            showErrorAlert("Access Denied", "Only park managers can create update requests.");
            return;
        }

        Park selectedPark = parkComboBox.getValue();
        ParameterOption selectedParameter = parameterComboBox.getValue();
        String newValue = newValueField.getText().trim();

        if (selectedPark == null) {
            setErrorStatus("Please select a park.");
            return;
        }

        if (selectedParameter == null) {
            setErrorStatus("Please select a parameter.");
            return;
        }

        if (!isNewValueValid(selectedParameter.getValue(), newValue)) {
            setErrorStatus("Please enter a valid numeric value.");
            return;
        }

        String oldValue = getCurrentParameterValue(selectedPark, selectedParameter.getValue());

        if (oldValue.equals(newValue)) {
            setErrorStatus("The new value must be different from the current value.");
            return;
        }

        setInfoStatus("Sending update request...");

        clientController.createParkParameterChangeRequest(
                selectedPark.getParkId(),
                ClientSession.getEmployeeId(),
                selectedParameter.getValue(),
                newValue
        );
    }

    private boolean isNewValueValid(String parameterName, String newValue) {
        if (newValue == null || newValue.isBlank()) {
            return false;
        }

        try {
            int value = Integer.parseInt(newValue);

            switch (parameterName) {

            case "max_capacity":
                return value > 0;

            case "places_for_unplanned_visitors":
                return value >= 0;

            case "estimated_visit_duration_hours":
                return value > 0;

            default:
                return false;
            }

        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void onParkParameterOperationResponse(
            OperationResponse response, Protocol responseType) {

        if (response == null) {
            String message = "No response was received from the server.";

            setErrorStatus(message);
            showErrorAlert("No Response", message);
            return;
        }

        String message = response.getMessage();

        if (!response.isSuccess()) {
            setErrorStatus(message);
            showErrorAlert("Operation Failed", message);
            return;
        }

        if (responseType == Protocol.PARK_PARAMETER_CHANGE_REQUEST_CREATED) {
            setSuccessStatus(message);
            showInfoAlert("Request Created", message);

            newValueField.clear();
            return;
        }

        setSuccessStatus(message);
        showInfoAlert("Operation Completed", message);
    }

    @Override
    public void onPendingParkParameterRequestsReceived(
            List<ParkParameterChangeRequest> requests) {
        // Not used on this page.
    }

    private void setInfoStatus(String message) {
        updateStatusLabel(message, "status-info");
        System.out.println("[ParkParameterRequestPage] " + message);
    }

    private void setSuccessStatus(String message) {
        updateStatusLabel(message, "status-success");
        System.out.println("[ParkParameterRequestPage] SUCCESS - " + message);
    }

    private void setErrorStatus(String message) {
        updateStatusLabel(message, "status-error");
        System.out.println("[ParkParameterRequestPage] ERROR - " + message);
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

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
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

    /**
     * Represents one display option in the parameter combo box.
     */
    private static class ParameterOption {
        private final String displayName;
        private final String value;

        public ParameterOption(String displayName, String value) {
            this.displayName = displayName;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}