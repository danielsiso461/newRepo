package clientGUI;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

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
import javafx.util.StringConverter;

/**
 * Controls the park parameter request page.
 * 
 * This page is used by park managers.
 */
public class ParkParameterRequestPageController
        implements ParkObserver, ParkParameterObserver {

    private static final String PARAMETER_MAX_CAPACITY = "max_capacity";
    private static final String PARAMETER_PLACES_FOR_UNPLANNED_VISITORS =
            "places_for_unplanned_visitors";
    private static final String PARAMETER_ESTIMATED_VISIT_DURATION_HOURS =
            "estimated_visit_duration_hours";
    private static final String PARAMETER_PROMOTIONS = "promotions";

    private static final String ROLE_PARK_MANAGER = "park_manager";

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
        headerLabel.setText("Park Parameter Update Request");
        subHeaderLabel.setText("Submit a request to update one of your park's operating parameters");

        setupParkComboBox();
        setupParameterComboBox();
        setupListeners();

        if (!ROLE_PARK_MANAGER.equals(ClientSession.getEmployeeRole())) {
            setErrorStatus("Only park managers can submit parameter change requests.");

            parkComboBox.setDisable(true);
            parameterComboBox.setDisable(true);
            newValueField.setDisable(true);
            submitButton.setDisable(true);
            return;
        }

        setInfoStatus("Ready");
    }

    private void setupParkComboBox() {
        parkComboBox.setConverter(new StringConverter<Park>() {

            @Override
            public String toString(Park park) {
                if (park == null) {
                    return "";
                }

                return park.getParkName();
            }

            @Override
            public Park fromString(String string) {
                return null;
            }
        });
    }

    private void setupParameterComboBox() {
        parameterComboBox.setConverter(new StringConverter<ParameterOption>() {

            @Override
            public String toString(ParameterOption option) {
                if (option == null) {
                    return "";
                }

                return option.getDisplayName();
            }

            @Override
            public ParameterOption fromString(String string) {
                return null;
            }
        });

        parameterComboBox.getItems().setAll(
                new ParameterOption(PARAMETER_MAX_CAPACITY, "Maximum capacity"),
                new ParameterOption(PARAMETER_PLACES_FOR_UNPLANNED_VISITORS,
                        "Places for unplanned visitors"),
                new ParameterOption(PARAMETER_ESTIMATED_VISIT_DURATION_HOURS,
                        "Estimated visit duration (hours)"),
                new ParameterOption(PARAMETER_PROMOTIONS, "Promotion discount (%)")
        );

        if (!parameterComboBox.getItems().isEmpty()) {
            parameterComboBox.setValue(parameterComboBox.getItems().get(0));
        }
    }

    private void setupListeners() {
        parkComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) -> updateCurrentValueLabel()
        );

        parameterComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) -> updateCurrentValueLabel()
        );
    }

    @FXML
    private void handleSubmitRequest() {
        if (clientController == null) {
            String message = "Client is not connected.";

            setErrorStatus(message);
            showErrorAlert("Client Error", message);
            return;
        }

        if (!ClientSession.isEmployeeLoggedIn()) {
            String message = "Employee is not logged in.";

            setErrorStatus(message);
            showErrorAlert("Login Error", message);
            return;
        }

        if (!ROLE_PARK_MANAGER.equals(ClientSession.getEmployeeRole())) {
            String message = "Only park managers can submit parameter change requests.";

            setErrorStatus(message);
            showErrorAlert("Permission Error", message);
            return;
        }

        Park selectedPark = parkComboBox.getValue();

        if (selectedPark == null) {
            String message = "Please select a park.";

            setErrorStatus(message);
            showWarningAlert("Missing Park", message);
            return;
        }

        ParameterOption selectedParameter = parameterComboBox.getValue();

        if (selectedParameter == null) {
            String message = "Please select a parameter.";

            setErrorStatus(message);
            showWarningAlert("Missing Parameter", message);
            return;
        }

        String newValue = newValueField.getText();

        if (newValue == null || newValue.isBlank()) {
            String message = "Please enter a new value.";

            setErrorStatus(message);
            showWarningAlert("Missing Value", message);
            return;
        }

        newValue = newValue.trim();

        if (!isNewValueValid(selectedParameter.getKey(), newValue)) {
            return;
        }

        setInfoStatus("Submitting request...");

        clientController.createParkParameterChangeRequest(
                selectedPark.getParkId(),
                ClientSession.getEmployeeId(),
                selectedParameter.getKey(),
                newValue
        );
    }

    private boolean isNewValueValid(String parameterName, String newValue) {
        try {
            switch (parameterName) {

            case PARAMETER_MAX_CAPACITY:
            case PARAMETER_PLACES_FOR_UNPLANNED_VISITORS:
            case PARAMETER_ESTIMATED_VISIT_DURATION_HOURS:
                int number = Integer.parseInt(newValue);

                if (number <= 0) {
                    String message = "The new value must be a positive number.";

                    setErrorStatus(message);
                    showWarningAlert("Invalid Value", message);
                    return false;
                }

                return true;

            case PARAMETER_PROMOTIONS:
                BigDecimal percent = new BigDecimal(newValue);

                if (percent.compareTo(BigDecimal.ZERO) < 0
                        || percent.compareTo(new BigDecimal("100")) > 0) {

                    String message = "Promotion discount must be between 0 and 100.";

                    setErrorStatus(message);
                    showWarningAlert("Invalid Discount", message);
                    return false;
                }

                return true;

            default:
                String message = "Unknown parameter.";

                setErrorStatus(message);
                showErrorAlert("Parameter Error", message);
                return false;
            }

        } catch (NumberFormatException e) {
            String message = "The new value must be numeric.";

            setErrorStatus(message);
            showWarningAlert("Invalid Number", message);
            return false;
        }
    }

    private void updateCurrentValueLabel() {
        Park selectedPark = parkComboBox.getValue();
        ParameterOption selectedParameter = parameterComboBox.getValue();

        if (selectedPark == null || selectedParameter == null) {
            currentValueLabel.setText("-");
            return;
        }

        currentValueLabel.setText(
                getCurrentValueText(selectedPark, selectedParameter.getKey())
        );
    }

    private String getCurrentValueText(Park park, String parameterName) {
        switch (parameterName) {

        case PARAMETER_MAX_CAPACITY:
            return String.valueOf(park.getMaxCapacity());

        case PARAMETER_PLACES_FOR_UNPLANNED_VISITORS:
            return String.valueOf(park.getPlacesForUnplannedVisitors());

        case PARAMETER_ESTIMATED_VISIT_DURATION_HOURS:
            return String.valueOf((int) park.getEstimatedVisitDurationHours());

        case PARAMETER_PROMOTIONS:
            return String.format(Locale.US, "%.2f%%", park.getPromotions());

        default:
            return "-";
        }
    }

    @Override
    public void onParksReceived(List<Park> parks) {
        ObservableList<Park> visibleParks = FXCollections.observableArrayList();

        if (parks != null) {
            int employeeParkId = ClientSession.getEmployeeParkId();

            for (Park park : parks) {
                if (park.getParkId() == employeeParkId) {
                    visibleParks.add(park);
                }
            }
        }

        parkComboBox.setItems(visibleParks);

        if (visibleParks.isEmpty()) {
            setErrorStatus("No park is assigned to this park manager.");
            currentValueLabel.setText("-");
            return;
        }

        parkComboBox.setValue(visibleParks.get(0));
        updateCurrentValueLabel();

        setInfoStatus("Park data loaded successfully.");
    }

    @Override
    public void onPendingParkParameterRequestsReceived(
            List<ParkParameterChangeRequest> requests) {

        /*
         * This page does not display pending requests.
         * Pending requests are displayed in the department manager page.
         */
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
            showErrorAlert("Request Failed", message);
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

    /**
     * Represents a selectable park parameter.
     */
    private static class ParameterOption {

        private final String key;
        private final String displayName;

        ParameterOption(String key, String displayName) {
            this.key = key;
            this.displayName = displayName;
        }

        String getKey() {
            return key;
        }

        String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}