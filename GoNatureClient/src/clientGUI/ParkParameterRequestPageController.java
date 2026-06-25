
package clientGUI;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import clientCommon.ClientSession;
import clientCommon.ParkObserver;
import clientCommon.ParkParameterObserver;
import clientController.ClientController;
import common.Employee;
import common.OperationResponse;
import common.Park;
import common.ParkParameterChangeRequest;
import common.Protocol;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * Controls the park parameter request page.
 * 
 * This page is used by park managers.
 */
public class ParkParameterRequestPageController 
        implements ParkObserver, ParkParameterObserver {

    /**
     * the parameter name for maximum capacity
     */
    private static final String PARAMETER_MAX_CAPACITY = "max_capacity";

    /**
     * the parameter name for places for unplanned visitors
     */
    private static final String PARAMETER_PLACES_FOR_UNPLANNED_VISITORS =
            "places_for_unplanned_visitors";

    /**
     * the parameter name for estimated visit duration
     */
    private static final String PARAMETER_ESTIMATED_VISIT_DURATION_HOURS =
            "estimated_visit_duration_hours";

    /**
     * the parameter name for promotions
     */
    private static final String PARAMETER_PROMOTIONS = "promotions";

    /**
     * the role name of a park manager
     */
    private static final String ROLE_PARK_MANAGER = "park_manager";

    /**
     * the client controller used to communicate with the server
     */
    private ClientController clientController;

    /**
     * the currently logged-in employee
     */
    private Employee loggedInEmployee;

    /**
     * the label used to display the page header
     */
    @FXML
    private Label headerLabel;

    /**
     * the label used to display the page sub-header
     */
    @FXML
    private Label subHeaderLabel;

    /**
     * the combo box used to select the park
     */
    @FXML
    private ComboBox<Park> parkComboBox;

    /**
     * the combo box used to select the parameter
     */
    @FXML
    private ComboBox<ParameterOption> parameterComboBox;

    /**
     * the label used to display the current parameter value
     */
    @FXML
    private Label currentValueLabel;

    /**
     * the text field used to enter the new parameter value
     */
    @FXML
    private TextField newValueField;

    /**
     * the button used to submit the request
     */
    @FXML
    private Button submitButton;

    /**
     * the label used to display status messages
     */
    @FXML
    private Label statusLabel;

    /**
     * Sets the client controller.
     *
     * @param clientController the client controller
     */
    public void setClientController(ClientController clientController) {
        this.clientController = clientController;

        if (this.clientController != null) {
            this.clientController.addParkObserver(this);
            this.clientController.addParkParameterObserver(this);
            this.clientController.requestActiveParks();
        }
    }

    /**
     * Sets the logged-in employee.
     *
     * @param employee the logged-in employee
     */
    public void setLoggedInEmployee(Employee employee) {
        this.loggedInEmployee = employee;
    }

    /**
     * Initializes the park parameter request page.
     *
     * This method sets the page labels, combo boxes, listeners,
     * and checks park manager permissions.
     */
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

    /**
     * Sets the park combo box converter.
     */
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

    /**
     * Sets the parameter combo box options and converter.
     */
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

    /**
     * Sets listeners for updating the current parameter value.
     */
    private void setupListeners() {
        parkComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) -> updateCurrentValueLabel()
        );

        parameterComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) -> updateCurrentValueLabel()
        );
    }

    /**
     * Handles the click on the submit request button.
     *
     * This method validates the selected park, selected parameter,
     * and new value before sending the request to the server.
     */
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

    /**
     * Checks whether the new parameter value is valid.
     *
     * @param parameterName the parameter name
     * @param newValue the new value entered by the user
     * @return true if the new value is valid
     */
    private boolean isNewValueValid(String parameterName, String newValue) {
        try {
            switch (parameterName) {

            case PARAMETER_MAX_CAPACITY:
            case PARAMETER_ESTIMATED_VISIT_DURATION_HOURS:
                int positiveNumber = Integer.parseInt(newValue);

                if (positiveNumber <= 0) {
                    String message = "The new value must be a positive number.";

                    setErrorStatus(message);
                    showWarningAlert("Invalid Value", message);
                    return false;
                }

                return true;

            case PARAMETER_PLACES_FOR_UNPLANNED_VISITORS:
                int nonNegativeNumber = Integer.parseInt(newValue);

                if (nonNegativeNumber < 0) {
                    String message = "Places for unplanned visitors cannot be negative.";

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

    /**
     * Updates the current value label according to the selected park and parameter.
     */
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

    /**
     * Returns the current parameter value text.
     *
     * @param park the selected park
     * @param parameterName the selected parameter name
     * @return the current value text
     */
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

    /**
     * This method is called when parks are received from the server.
     *
     * @param parks the list of parks received from the server
     */
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

    /**
     * This method is called when pending park parameter requests are received.
     *
     * @param requests the list of pending park parameter change requests
     */
    @Override
    public void onPendingParkParameterRequestsReceived(
            List<ParkParameterChangeRequest> requests) {

        /*
         * This page does not display pending requests.
         * Pending requests are displayed in the department manager page.
         */
    }

    /**
     * This method is called when the server returns a park parameter operation response.
     *
     * @param response the operation response received from the server
     * @param responseType the protocol type of the response
     */
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

    /**
     * Sets an information status message.
     *
     * @param message the status message
     */
    private void setInfoStatus(String message) {
        updateStatusLabel(message, "status-info");
        System.out.println("[ParkParameterRequestPage] " + message);
    }

    /**
     * Sets a success status message.
     *
     * @param message the status message
     */
    private void setSuccessStatus(String message) {
        updateStatusLabel(message, "status-success");
        System.out.println("[ParkParameterRequestPage] SUCCESS - " + message);
    }

    /**
     * Sets an error status message.
     *
     * @param message the status message
     */
    private void setErrorStatus(String message) {
        updateStatusLabel(message, "status-error");
        System.out.println("[ParkParameterRequestPage] ERROR - " + message);
    }

    /**
     * Updates the status label text and style.
     *
     * @param message the status message
     * @param statusStyleClass the status style class
     */
    private void updateStatusLabel(String message, String statusStyleClass) {
        statusLabel.setText("Status: " + message);

        statusLabel.getStyleClass().remove("status-info");
        statusLabel.getStyleClass().remove("status-success");
        statusLabel.getStyleClass().remove("status-error");

        if (!statusLabel.getStyleClass().contains(statusStyleClass)) {
            statusLabel.getStyleClass().add(statusStyleClass);
        }
    }

    /**
     * Shows an information alert.
     *
     * @param title the alert title
     * @param message the alert message
     */
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows a warning alert.
     *
     * @param title the alert title
     * @param message the alert message
     */
    private void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows an error alert.
     *
     * @param title the alert title
     * @param message the alert message
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Represents one parameter option in the parameter combo box.
     */
    private static class ParameterOption {

        /**
         * the parameter key used by the system
         */
        private final String key;

        /**
         * the parameter display name shown to the user
         */
        private final String displayName;

        /**
         * Creates a parameter option.
         *
         * @param key the parameter key
         * @param displayName the parameter display name
         */
        ParameterOption(String key, String displayName) {
            this.key = key;
            this.displayName = displayName;
        }

        /**
         * Returns the parameter key.
         *
         * @return the parameter key
         */
        String getKey() {
            return key;
        }

        /**
         * Returns the parameter display name.
         *
         * @return the parameter display name
         */
        String getDisplayName() {
            return displayName;
        }

        /**
         * Returns the parameter display name.
         *
         * @return the parameter display name
         */
        @Override
        public String toString() {
            return displayName;
        }
    }

    /**
     * Handles the click on the back button.
     *
     * This method returns to the park manager dashboard.
     *
     * @param event the button click event
     */
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/clientGUI/ParkManagerHomePage.fxml")
            );

            Parent root = loader.load();

            ParkManagerHomePageController controller = loader.getController();
            controller.setClientController(clientController);
            controller.setLoggedInEmployee(loggedInEmployee);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Park Manager Dashboard");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

