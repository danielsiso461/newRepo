
package clientGUI;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import clientCommon.ClientSession;
import clientCommon.ParkParameterObserver;
import clientController.ClientController;
import common.Employee;
import common.OperationResponse;
import common.ParkParameterChangeRequest;
import common.Protocol;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;


/**
 * Controls the park parameter approval page.
 * 
 * This page is used by department managers.
 */
public class ParkParameterApprovalPageController implements ParkParameterObserver {

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
	 * the role name of a department manager
	 */
	private static final String ROLE_DEPARTMENT_MANAGER = "department_manager";

	/**
	 * the client controller used to communicate with the server
	 */
	private ClientController clientController;

	/**
	 * the currently logged-in employee
	 */
	private Employee loggedInEmployee;

	/**
	 * the last review note submitted by the department manager
	 */
	private String lastSubmittedReviewNote = "";

	/**
	 * the last operation message that should stay after refreshing the table
	 */
	private String lastOperationMessageToKeep;

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
	 * the table view that displays park parameter change requests
	 */
	@FXML
	private TableView<ParkParameterChangeRequest> requestsTableView;

	/**
	 * the column that displays the request ID
	 */
	@FXML
	private TableColumn<ParkParameterChangeRequest, Integer> requestIdColumn;

	/**
	 * the column that displays the park ID
	 */
	@FXML
	private TableColumn<ParkParameterChangeRequest, Integer> parkIdColumn;

	/**
	 * The current DB table no longer stores requested_by_employee_id.
	 * This column is kept only so the existing FXML will not break.
	 */
	@FXML
	private TableColumn<ParkParameterChangeRequest, String> requestedByColumn;

	/**
	 * the column that displays the parameter name
	 */
	@FXML
	private TableColumn<ParkParameterChangeRequest, String> parameterColumn;

	/**
	 * the column that displays the old parameter value
	 */
	@FXML
	private TableColumn<ParkParameterChangeRequest, String> oldValueColumn;

	/**
	 * the column that displays the new parameter value
	 */
	@FXML
	private TableColumn<ParkParameterChangeRequest, String> newValueColumn;

	/**
	 * the column that displays the request status
	 */
	@FXML
	private TableColumn<ParkParameterChangeRequest, String> statusColumn;

	/**
	 * the text area used to enter a review note
	 */
	@FXML
	private TextArea reviewNoteArea;

	/**
	 * the button used to approve a request
	 */
	@FXML
	private Button approveButton;

	/**
	 * the button used to reject a request
	 */
	@FXML
	private Button rejectButton;

	/**
	 * the button used to refresh the pending requests table
	 */
	@FXML
	private Button refreshButton;

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
			this.clientController.addParkParameterObserver(this);
			requestPendingRequests(false);
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
	 * Initializes the park parameter approval page.
	 *
	 * This method sets the page labels, prepares the table,
	 * and checks department manager permissions.
	 */
	@FXML
	private void initialize() {
		headerLabel.setText("Park Parameter Change Approval");
		subHeaderLabel.setText("Review pending park parameter change requests");

		setupTableColumns();
		setupSelectionListener();

		requestsTableView.setPlaceholder(
				new Label("No pending park parameter change requests")
		);

		approveButton.setDisable(true);
		rejectButton.setDisable(true);

		if (!ROLE_DEPARTMENT_MANAGER.equals(ClientSession.getEmployeeRole())) {
			setErrorStatus("Only department managers can review requests.");

			approveButton.setDisable(true);
			rejectButton.setDisable(true);
			refreshButton.setDisable(true);
			return;
		}

		setInfoStatus("Ready");
	}

	/**
	 * Sets the table column value factories.
	 */
	private void setupTableColumns() {
		requestIdColumn.setCellValueFactory(
				new PropertyValueFactory<>("requestId")
		);

		parkIdColumn.setCellValueFactory(
				new PropertyValueFactory<>("parkId")
		);



		statusColumn.setCellValueFactory(
				new PropertyValueFactory<>("requestStatus")
		);

		parameterColumn.setCellValueFactory(cellData ->
				new SimpleStringProperty(
						formatParameterName(cellData.getValue().getParameterName())
				)
		);

		oldValueColumn.setCellValueFactory(cellData ->
				new SimpleStringProperty(
						formatParameterValue(
								cellData.getValue().getParameterName(),
								cellData.getValue().getCurrentValue()
						)
				)
		);

		newValueColumn.setCellValueFactory(cellData ->
				new SimpleStringProperty(
						formatParameterValue(
								cellData.getValue().getParameterName(),
								cellData.getValue().getNewValue()
						)
				)
		);
	}

	/**
	 * Sets the selection listener for the requests table.
	 */
	private void setupSelectionListener() {
		requestsTableView.getSelectionModel().selectedItemProperty().addListener(
				(observable, oldValue, selectedRequest) -> {

					boolean noSelection = selectedRequest == null;
					boolean notDepartmentManager =
							!ROLE_DEPARTMENT_MANAGER.equals(ClientSession.getEmployeeRole());

					approveButton.setDisable(noSelection || notDepartmentManager);
					rejectButton.setDisable(noSelection || notDepartmentManager);
				}
		);
	}

	/**
	 * Formats a parameter name for display in the table.
	 *
	 * @param parameterName the parameter name from the database
	 * @return the formatted parameter name
	 */
	private String formatParameterName(String parameterName) {
		if (parameterName == null) {
			return "-";
		}

		switch (parameterName) {

		case PARAMETER_MAX_CAPACITY:
			return "Maximum capacity";

		case PARAMETER_PLACES_FOR_UNPLANNED_VISITORS:
			return "Places for unplanned visitors";

		case PARAMETER_ESTIMATED_VISIT_DURATION_HOURS:
			return "Estimated visit duration (hours)";

		case PARAMETER_PROMOTIONS:
			return "Promotion discount (%)";

		default:
			return parameterName;
		}
	}

	/**
	 * Formats a parameter value for display in the table.
	 *
	 * @param parameterName the parameter name
	 * @param value the parameter value
	 * @return the formatted parameter value
	 */
	private String formatParameterValue(String parameterName, String value) {
		if (value == null || value.isBlank()) {
			return "-";
		}

		if (PARAMETER_PROMOTIONS.equals(parameterName)) {
			try {
				double percent = Double.parseDouble(value);
				return String.format(Locale.US, "%.2f%%", percent);
			} catch (NumberFormatException e) {
				return value;
			}
		}

		return value;
	}

	/**
	 * Handles the click on the refresh button.
	 */
	@FXML
	private void handleRefresh() {
		requestPendingRequests(true);
	}

	/**
	 * Handles the click on the approve button.
	 *
	 * This method validates the selected request and sends an approval request
	 * to the server.
	 */
	@FXML
	private void handleApprove() {
		ParkParameterChangeRequest selectedRequest =
				requestsTableView.getSelectionModel().getSelectedItem();

		if (selectedRequest == null) {
			String message = "Please select a request first.";

			setErrorStatus(message);
			showWarningAlert("No request selected", message);
			return;
		}

		if (clientController == null) {
			String message = "Client is not connected.";

			setErrorStatus(message);
			showErrorAlert("Client Error", message);
			return;
		}

		lastSubmittedReviewNote = normalizeReviewNote(reviewNoteArea.getText());

		setInfoStatus("Approving request...");

		clientController.approveParkParameterChangeRequest(
				selectedRequest.getRequestId(),
				ClientSession.getEmployeeId(),
				lastSubmittedReviewNote
		);
	}

	/**
	 * Handles the click on the reject button.
	 *
	 * This method validates the selected request and sends a rejection request
	 * to the server.
	 */
	@FXML
	private void handleReject() {
		ParkParameterChangeRequest selectedRequest =
				requestsTableView.getSelectionModel().getSelectedItem();

		if (selectedRequest == null) {
			String message = "Please select a request first.";

			setErrorStatus(message);
			showWarningAlert("No request selected", message);
			return;
		}

		if (clientController == null) {
			String message = "Client is not connected.";

			setErrorStatus(message);
			showErrorAlert("Client Error", message);
			return;
		}

		lastSubmittedReviewNote = normalizeReviewNote(reviewNoteArea.getText());

		setInfoStatus("Rejecting request...");

		clientController.rejectParkParameterChangeRequest(
				selectedRequest.getRequestId(),
				ClientSession.getEmployeeId(),
				lastSubmittedReviewNote
		);
	}

	/**
	 * Requests pending park parameter change requests from the server.
	 *
	 * @param showLoadingStatus true if a loading status should be displayed
	 */
	private void requestPendingRequests(boolean showLoadingStatus) {
		if (clientController == null) {
			setErrorStatus("Client is not connected.");
			return;
		}

		if (!ClientSession.isEmployeeLoggedIn()) {
			setErrorStatus("Employee is not logged in.");
			return;
		}

		if (!ROLE_DEPARTMENT_MANAGER.equals(ClientSession.getEmployeeRole())) {
			setErrorStatus("Only department managers can load pending requests.");
			return;
		}

		if (showLoadingStatus) {
			setInfoStatus("Loading pending requests...");
		}

		clientController.requestPendingParkParameterChangeRequests(
				ClientSession.getEmployeeId()
		);
	}

	/**
	 * This method is called when pending park parameter requests are received.
	 *
	 * @param requests the list of pending park parameter change requests
	 */
	@Override
	public void onPendingParkParameterRequestsReceived(
			List<ParkParameterChangeRequest> requests) {

		ObservableList<ParkParameterChangeRequest> items =
				FXCollections.observableArrayList();

		if (requests != null) {
			items.addAll(requests);
		}

		requestsTableView.setItems(items);
		reviewNoteArea.clear();

		approveButton.setDisable(true);
		rejectButton.setDisable(true);

		if (lastOperationMessageToKeep != null) {
			setSuccessStatus(lastOperationMessageToKeep);
			lastOperationMessageToKeep = null;
			return;
		}

		if (items.isEmpty()) {
			setInfoStatus("No pending requests.");
		} else {
			setInfoStatus(items.size() + " pending requests loaded.");
		}
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

		String message = buildMessageWithReviewNote(
				response.getMessage(),
				lastSubmittedReviewNote
		);

		if (!response.isSuccess()) {
			setErrorStatus(message);
			showErrorAlert("Operation Failed", message);
			return;
		}

		if (responseType == Protocol.PARK_PARAMETER_CHANGE_REQUEST_APPROVED) {
			setSuccessStatus(message);
			showInfoAlert("Request Approved", message);

			reviewNoteArea.clear();
			lastOperationMessageToKeep = message;
			requestPendingRequests(false);
			return;
		}

		if (responseType == Protocol.PARK_PARAMETER_CHANGE_REQUEST_REJECTED) {
			setSuccessStatus(message);
			showInfoAlert("Request Rejected", message);

			reviewNoteArea.clear();
			lastOperationMessageToKeep = message;
			requestPendingRequests(false);
			return;
		}

		setSuccessStatus(message);
		showInfoAlert("Operation Completed", message);
	}

	/**
	 * Normalizes a review note.
	 *
	 * @param reviewNote the review note
	 * @return the normalized review note
	 */
	private String normalizeReviewNote(String reviewNote) {
		if (reviewNote == null) {
			return "";
		}

		return reviewNote.trim();
	}

	/*
	 * The review note is currently not stored in the DB because the matching column
	 * no longer exists. It is only shown in the current operation message.
	 */
	/**
	 * Builds an operation message with the review note.
	 *
	 * @param baseMessage the base operation message
	 * @param reviewNote the review note
	 * @return the full operation message
	 */
	private String buildMessageWithReviewNote(String baseMessage, String reviewNote) {
		String safeBaseMessage = baseMessage == null || baseMessage.isBlank()
				? "Operation completed."
				: baseMessage.trim();

		if (reviewNote == null || reviewNote.isBlank()) {
			return safeBaseMessage;
		}

		return safeBaseMessage + " Review note: \"" + reviewNote.trim() + "\"";
	}

	/**
	 * Sets an information status message.
	 *
	 * @param message the status message
	 */
	private void setInfoStatus(String message) {
		updateStatusLabel(message, "status-info");
		System.out.println("[ParkParameterApprovalPage] " + message);
	}

	/**
	 * Sets a success status message.
	 *
	 * @param message the status message
	 */
	private void setSuccessStatus(String message) {
		updateStatusLabel(message, "status-success");
		System.out.println("[ParkParameterApprovalPage] SUCCESS - " + message);
	}

	/**
	 * Sets an error status message.
	 *
	 * @param message the status message
	 */
	private void setErrorStatus(String message) {
		updateStatusLabel(message, "status-error");
		System.out.println("[ParkParameterApprovalPage] ERROR - " + message);
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
	 * Handles the click on the back button.
	 *
	 * This method removes the observer and returns to the department manager dashboard.
	 *
	 * @param event the button click event
	 */
	@FXML
	private void handleBack(ActionEvent event) {
		try {
			if (clientController != null) {
				clientController.removeParkParameterObserver(this);
			}

			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/DepartmentManagerHomePage.fxml")
			);

			Parent root = loader.load();

			DepartmentManagerHomePageController controller = loader.getController();
			controller.setClientController(clientController);

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("Department Manager Dashboard");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
			statusLabel.setText("Status: Could not return to department manager dashboard.");
		}
	}
}

