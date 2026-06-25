
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
import java.io.IOException;

import common.Employee;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Allows park workers and park managers to update the real-time visitor counter.
 */
public class ParkVisitorCounterUpdatePageController implements ParkVisitorCounterObserver {

    /**
     * the role name of a park worker
     */
    private static final String ROLE_PARK_WORKER = "park_worker";

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
     * the label used to display the park ID
     */
    @FXML
    private Label parkIdLabel;

    /**
     * the combo box used to select entry or exit action
     */
    @FXML
    private ComboBox<String> actionComboBox;

    /**
     * the slider used to select the number of visitors
     */
    @FXML
    private Slider amountSlider;

    /**
     * the label used to display the selected visitor amount
     */
    @FXML
    private Label amountLabel;

    /**
     * the button used to update the visitor counter
     */
    @FXML
    private Button updateButton;

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
     * Initializes the visitor counter update page.
     *
     * This method prepares the action combo box, slider,
     * and checks whether the current employee can update the counter.
     */
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

    /**
     * Sets the action combo box options.
     */
    private void setupActionComboBox() {
        actionComboBox.getItems().setAll(
                ParkVisitorCounterUpdateRequest.ACTION_ENTRY,
                ParkVisitorCounterUpdateRequest.ACTION_EXIT
        );

        actionComboBox.setValue(ParkVisitorCounterUpdateRequest.ACTION_ENTRY);
    }

    /**
     * Sets the slider listener.
     */
    private void setupSlider() {
        amountSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> updateAmountLabel()
        );

        updateAmountLabel();
    }

    /**
     * Updates the amount label according to the selected slider value.
     */
    private void updateAmountLabel() {
        amountLabel.setText(String.valueOf(getSelectedAmount()));
    }

    /**
     * Returns the selected visitor amount.
     *
     * @return the selected visitor amount
     */
    private int getSelectedAmount() {
        return (int) Math.round(amountSlider.getValue());
    }

    /**
     * Checks whether the current employee can update the visitor counter.
     *
     * @return true if the current employee can update the visitor counter
     */
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

    /**
     * Enables or disables the page controls.
     *
     * @param disabled true if the controls should be disabled
     */
    private void setControlsDisabled(boolean disabled) {
        updateButton.setDisable(disabled);
        actionComboBox.setDisable(disabled);
        amountSlider.setDisable(disabled);
    }

    /**
     * Handles the click on the update counter button.
     *
     * This method validates permissions and sends a visitor counter update
     * request to the server.
     */
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

    /**
     * This method is called when park visitor counters are received from the server.
     *
     * @param counters the list of visitor counter snapshots
     */
    @Override
    public void onParkVisitorCountersReceived(
            List<ParkVisitorCounterSnapshot> counters) {

        /*
         * This page does not need the full counters list.
         */
    }

    /**
     * This method is called when the server returns a visitor counter operation response.
     *
     * @param response the operation response received from the server
     * @param responseType the protocol type of the response
     */
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

    /**
     * This method is called when park visitor counters are updated.
     */
    @Override
    public void onParkVisitorCountersUpdated() {
        /*
         * No automatic action is required here.
         */
    }

    /**
     * Checks whether visitor counter test mode is active.
     *
     * @return true if test mode is active
     */
    private boolean isTestMode() {
        return Boolean.getBoolean("visitorCounterTestMode");
    }

    /**
     * Returns the current employee role.
     *
     * @return the current employee role
     */
    private String getCurrentEmployeeRole() {
        if (isTestMode()) {
            return System.getProperty("visitorCounterTestRole", "");
        }

        return ClientSession.getEmployeeRole();
    }

    /**
     * Returns the current employee ID.
     *
     * @return the current employee ID
     */
    private int getCurrentEmployeeId() {
        if (isTestMode()) {
            return Integer.getInteger("visitorCounterTestEmployeeId", -1);
        }

        return ClientSession.getEmployeeId();
    }

    /**
     * Returns the current employee park ID.
     *
     * @return the current employee park ID
     */
    private int getCurrentEmployeeParkId() {
        if (isTestMode()) {
            return Integer.getInteger("visitorCounterTestParkId", -1);
        }

        return ClientSession.getEmployeeParkId();
    }

    /**
     * Sets an information status message.
     *
     * @param message the status message
     */
    private void setInfoStatus(String message) {
        updateStatusLabel(message, "status-info");
        System.out.println("[ParkVisitorCounterUpdatePage] " + message);
    }

    /**
     * Sets a success status message.
     *
     * @param message the status message
     */
    private void setSuccessStatus(String message) {
        updateStatusLabel(message, "status-success");
        System.out.println("[ParkVisitorCounterUpdatePage] SUCCESS - " + message);
    }

    /**
     * Sets an error status message.
     *
     * @param message the status message
     */
    private void setErrorStatus(String message) {
        updateStatusLabel(message, "status-error");
        System.out.println("[ParkVisitorCounterUpdatePage] ERROR - " + message);
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
     * Handles the click on the back button.
     *
     * This method returns to the park worker dashboard.
     *
     * @param event the button click event
     */
    @FXML
    private void handleBack(ActionEvent event) {
    	try {
    		FXMLLoader loader = new FXMLLoader(
    				getClass().getResource("/clientGUI/ParkWorkerHomePage.fxml")
    		);

    		Parent root = loader.load();

    		ParkWorkerHomePageController controller = loader.getController();
    		controller.setClientController(clientController);
    		controller.setLoggedInEmployee(loggedInEmployee);

    		Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    		stage.setTitle("Park Worker Dashboard");
    		stage.setScene(new Scene(root));
    		stage.show();

    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
}