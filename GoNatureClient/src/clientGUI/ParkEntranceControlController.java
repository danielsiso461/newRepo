package clientGUI;

import java.io.IOException;

import clientCommon.ParkEntranceObserver;
import clientController.ClientController;
import common.Employee;
import common.ParkEntranceMessage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ParkEntranceControlController implements ParkEntranceObserver {

	private static final int MIN_VISITORS = 1;
	private static final int MAX_VISITORS = 15;

	public enum EntranceMode {
		CHECK_IN,
		OCCASIONAL_VISIT,
		CHECK_OUT,
		CURRENT_VISITORS
	}

	private ClientController clientController;
	private Employee loggedInEmployee;
	private EntranceMode entranceMode;

	@FXML
	private Label instructionLabel;

	@FXML
	private Label confirmationCodeLabel;

	@FXML
	private TextField confirmationCodeField;

	@FXML
	private Label parkIdLabel;

	@FXML
	private TextField parkIdField;

	@FXML
	private Label employeeIdLabel;

	@FXML
	private TextField employeeIdField;

	@FXML
	private Label visitorsLabel;

	@FXML
	private TextField visitorsField;

	@FXML
	private Button checkInButton;

	@FXML
	private Button checkOutButton;

	@FXML
	private Button occasionalVisitButton;

	@FXML
	private Button currentVisitorsButton;

	@FXML
	private Label currentVisitorsLabel;

	@FXML
	private Label messageLabel;

	public void setClientController(ClientController clientController) {
		this.clientController = clientController;

		if (this.clientController != null) {
			this.clientController.addParkEntranceObserver(this);
		}
	}

	public void setLoggedInEmployee(Employee loggedInEmployee) {
		this.loggedInEmployee = loggedInEmployee;

		if (this.loggedInEmployee != null) {
			employeeIdField.setText(String.valueOf(this.loggedInEmployee.getEmployeeId()));

			if (this.loggedInEmployee.getParkId() != null) {
				parkIdField.setText(String.valueOf(this.loggedInEmployee.getParkId()));
			}
		}

		applyEntranceMode();
	}

	public void setEntranceMode(EntranceMode entranceMode) {
		this.entranceMode = entranceMode;
		applyEntranceMode();
	}

	@FXML
	void initialize() {
		assert instructionLabel != null : "fx:id=\"instructionLabel\" was not injected.";
		assert confirmationCodeLabel != null : "fx:id=\"confirmationCodeLabel\" was not injected.";
		assert confirmationCodeField != null : "fx:id=\"confirmationCodeField\" was not injected.";
		assert parkIdLabel != null : "fx:id=\"parkIdLabel\" was not injected.";
		assert parkIdField != null : "fx:id=\"parkIdField\" was not injected.";
		assert employeeIdLabel != null : "fx:id=\"employeeIdLabel\" was not injected.";
		assert employeeIdField != null : "fx:id=\"employeeIdField\" was not injected.";
		assert visitorsLabel != null : "fx:id=\"visitorsLabel\" was not injected.";
		assert visitorsField != null : "fx:id=\"visitorsField\" was not injected.";
		assert checkInButton != null : "fx:id=\"checkInButton\" was not injected.";
		assert checkOutButton != null : "fx:id=\"checkOutButton\" was not injected.";
		assert occasionalVisitButton != null : "fx:id=\"occasionalVisitButton\" was not injected.";
		assert currentVisitorsButton != null : "fx:id=\"currentVisitorsButton\" was not injected.";
		assert currentVisitorsLabel != null : "fx:id=\"currentVisitorsLabel\" was not injected.";
		assert messageLabel != null : "fx:id=\"messageLabel\" was not injected.";

		parkIdField.setEditable(false);
		employeeIdField.setEditable(false);

		currentVisitorsLabel.setText("Current visitors: -");
		showInfo("Choose an entrance action.");
	}

	private void applyEntranceMode() {
		if (entranceMode == null
				|| checkInButton == null
				|| checkOutButton == null
				|| occasionalVisitButton == null
				|| currentVisitorsButton == null
				|| confirmationCodeField == null
				|| parkIdField == null
				|| employeeIdField == null
				|| visitorsField == null
				|| messageLabel == null) {
			return;
		}

		hideButton(checkInButton);
		hideButton(checkOutButton);
		hideButton(occasionalVisitButton);
		hideButton(currentVisitorsButton);

		showField(confirmationCodeLabel, confirmationCodeField);
		showField(parkIdLabel, parkIdField);
		showField(employeeIdLabel, employeeIdField);
		showField(visitorsLabel, visitorsField);

		confirmationCodeField.clear();
		visitorsField.clear();

		confirmationCodeLabel.setText("Confirmation Code:");
		confirmationCodeField.setPromptText("Enter confirmation code");

		parkIdLabel.setText("Employee Park ID:");
		parkIdField.setPromptText("Park assigned to employee");

		employeeIdLabel.setText("Employee ID:");
		employeeIdField.setPromptText("Logged employee ID");

		visitorsLabel.setText("Number of Visitors:");
		visitorsField.setPromptText("Enter number of visitors");

		currentVisitorsLabel.setText("Current visitors: -");
		currentVisitorsLabel.setVisible(false);
		currentVisitorsLabel.setManaged(false);

		switch (entranceMode) {

		case CHECK_IN:
			instructionLabel.setText("Check visitors into the park");
			showButton(checkInButton);
			showInfo("Enter confirmation code and number of visitors.");
			break;

		case CHECK_OUT:
			instructionLabel.setText("Check visitors out of the park");
			showButton(checkOutButton);
			hideField(visitorsLabel, visitorsField);
			showInfo("Enter confirmation code to check out visitors.");
			break;

		case OCCASIONAL_VISIT:
			instructionLabel.setText("Create an occasional visit");
			showButton(occasionalVisitButton);
			hideField(confirmationCodeLabel, confirmationCodeField);
			showInfo("Enter number of visitors. After success, the Visit ID will be shown.");
			break;

		case CURRENT_VISITORS:
			instructionLabel.setText("View current visitors in the park");
			showButton(currentVisitorsButton);
			hideField(confirmationCodeLabel, confirmationCodeField);
			hideField(visitorsLabel, visitorsField);
			currentVisitorsLabel.setVisible(true);
			currentVisitorsLabel.setManaged(true);
			showInfo("Click Current Visitors to load the current number of visitors.");
			break;

		default:
			break;
		}
	}

	private void showButton(Button button) {
		button.setDisable(false);
		button.setVisible(true);
		button.setManaged(true);
	}

	private void hideButton(Button button) {
		button.setDisable(false);
		button.setVisible(false);
		button.setManaged(false);
	}

	private void showField(Label label, TextField field) {
		label.setVisible(true);
		label.setManaged(true);
		field.setVisible(true);
		field.setManaged(true);
	}

	private void hideField(Label label, TextField field) {
		label.setVisible(false);
		label.setManaged(false);
		field.setVisible(false);
		field.setManaged(false);
		field.clear();
	}

	@FXML
	void checkInButtonClick() {
		if (clientController == null) {
			showError("Server connection is not ready.");
			return;
		}

		try {
			int confirmationCode = Integer.parseInt(confirmationCodeField.getText().trim());
			int parkId = Integer.parseInt(parkIdField.getText().trim());
			int employeeId = Integer.parseInt(employeeIdField.getText().trim());
			int visitors = Integer.parseInt(visitorsField.getText().trim());

			if (confirmationCode <= 0 || parkId <= 0 || employeeId <= 0) {
				showError("Confirmation code, park ID and employee ID must be positive numbers.");
				return;
			}

			if (!isVisitorsAmountValid(visitors)) {
				showError("Number of visitors must be between 1 and 15.");
				return;
			}

			ParkEntranceMessage message =
					new ParkEntranceMessage(confirmationCode, parkId, employeeId, visitors);

			showInfo("Sending check-in request...");

			clientController.requestCheckInOrder(message);

		} catch (NumberFormatException e) {
			showError("Confirmation code, park ID, employee ID and visitors must be numbers.");
		}
	}

	@FXML
	void checkOutButtonClick() {
		if (clientController == null) {
			showError("Server connection is not ready.");
			return;
		}

		try {
			int confirmationCode = Integer.parseInt(confirmationCodeField.getText().trim());
			int parkId = Integer.parseInt(parkIdField.getText().trim());
			int employeeId = Integer.parseInt(employeeIdField.getText().trim());

			if (confirmationCode <= 0 || parkId <= 0 || employeeId <= 0) {
				showError("Confirmation code, park ID and employee ID must be positive numbers.");
				return;
			}

			ParkEntranceMessage message = new ParkEntranceMessage();
			message.setConfirmationCode(confirmationCode);
			message.setParkId(parkId);
			message.setEmployeeId(employeeId);
			message.setIdentificationMethod("confirmation_code");

			showInfo("Sending check-out request...");

			clientController.requestCheckOutVisit(message);

		} catch (NumberFormatException e) {
			showError("Confirmation code, park ID and employee ID must be numbers.");
		}
	}

	@FXML
	void occasionalVisitButtonClick() {
		if (clientController == null) {
			showError("Server connection is not ready.");
			return;
		}

		try {
			int parkId = Integer.parseInt(parkIdField.getText().trim());
			int employeeId = Integer.parseInt(employeeIdField.getText().trim());
			int visitors = Integer.parseInt(visitorsField.getText().trim());

			if (parkId <= 0 || employeeId <= 0) {
				showError("Park ID and employee ID must be positive numbers.");
				return;
			}

			if (!isVisitorsAmountValid(visitors)) {
				showError("Number of visitors must be between 1 and 15.");
				return;
			}

			ParkEntranceMessage message = new ParkEntranceMessage();
			message.setParkId(parkId);
			message.setEmployeeId(employeeId);
			message.setActualNumberOfVisitors(visitors);
			message.setIdentificationMethod("id_number");

			showInfo("Sending occasional visit request...");

			clientController.requestOccasionalVisit(message);

		} catch (NumberFormatException e) {
			showError("Park ID, employee ID and visitors must be numbers.");
		}
	}

	@FXML
	void currentVisitorsButtonClick() {
		if (clientController == null) {
			showError("Server connection is not ready.");
			return;
		}

		try {
			int parkId = Integer.parseInt(parkIdField.getText().trim());

			if (parkId <= 0) {
				showError("Park ID must be a positive number.");
				return;
			}

			ParkEntranceMessage message = new ParkEntranceMessage();
			message.setParkId(parkId);

			showInfo("Loading current visitors...");

			clientController.requestCurrentVisitors(message);

		} catch (NumberFormatException e) {
			showError("Park ID must be a number.");
		}
	}

	private boolean isVisitorsAmountValid(int visitors) {
		return visitors >= MIN_VISITORS && visitors <= MAX_VISITORS;
	}

	private void updateCurrentVisitors(ParkEntranceMessage parkEntranceMessage) {
		if (parkEntranceMessage != null) {
			currentVisitorsLabel.setVisible(true);
			currentVisitorsLabel.setManaged(true);
			currentVisitorsLabel.setText("Current visitors: "
					+ parkEntranceMessage.getCurrentVisitors());
		}
	}

	private String safeMessage(String message, String defaultMessage) {
		if (message == null || message.trim().isEmpty()) {
			return defaultMessage;
		}

		return message;
	}

	private void showInfo(String message) {
		messageLabel.getStyleClass().removeAll(
				"status-success",
				"status-error",
				"error-label",
				"success-label",
				"info-label"
		);

		if (!messageLabel.getStyleClass().contains("status-label")) {
			messageLabel.getStyleClass().add("status-label");
		}

		if (!messageLabel.getStyleClass().contains("status-info")) {
			messageLabel.getStyleClass().add("status-info");
		}

		messageLabel.setText(message);
	}

	private void showSuccess(String message) {
		messageLabel.getStyleClass().removeAll(
				"status-info",
				"status-error",
				"error-label",
				"success-label",
				"info-label"
		);

		if (!messageLabel.getStyleClass().contains("status-label")) {
			messageLabel.getStyleClass().add("status-label");
		}

		if (!messageLabel.getStyleClass().contains("status-success")) {
			messageLabel.getStyleClass().add("status-success");
		}

		messageLabel.setText(message);
	}

	private void showError(String message) {
		messageLabel.getStyleClass().removeAll(
				"status-info",
				"status-success",
				"error-label",
				"success-label",
				"info-label"
		);

		if (!messageLabel.getStyleClass().contains("status-label")) {
			messageLabel.getStyleClass().add("status-label");
		}

		if (!messageLabel.getStyleClass().contains("status-error")) {
			messageLabel.getStyleClass().add("status-error");
		}

		messageLabel.setText(message);
	}

	@Override
	public void onCheckInOrderResult(boolean success,
			ParkEntranceMessage parkEntranceMessage) {

		Platform.runLater(() -> {
			if (parkEntranceMessage == null) {
				showError("Invalid check-in response from server.");
				return;
			}

			updateCurrentVisitors(parkEntranceMessage);

			if (success) {
				showSuccess("Check-in completed successfully.");
			} else {
				showError(safeMessage(
						parkEntranceMessage.getResponseMessage(),
						"Check-in failed."
				));
			}
		});
	}
	
	
	@Override
	public void onCheckOutVisitResult(boolean success,
			ParkEntranceMessage parkEntranceMessage) {

		Platform.runLater(() -> {
			if (parkEntranceMessage == null) {
				showError("Invalid check-out response from server.");
				return;
			}

			updateCurrentVisitors(parkEntranceMessage);

			if (success) {
				showSuccess(safeMessage(
						parkEntranceMessage.getResponseMessage(),
						"Check-out completed successfully."
				));
			} else {
				showError(safeMessage(
						parkEntranceMessage.getResponseMessage(),
						"Check-out failed."
				));
			}
		});
	}

	@Override
	public void onOccasionalVisitResult(boolean success,
			ParkEntranceMessage parkEntranceMessage) {

		Platform.runLater(() -> {
			if (parkEntranceMessage == null) {
				showError("Invalid occasional visit response from server.");
				return;
			}

			updateCurrentVisitors(parkEntranceMessage);

			if (success) {
				showSuccess(safeMessage(
						parkEntranceMessage.getResponseMessage(),
						"Occasional visit created successfully. The Visit ID was created by the system."
				));
			} else {
				showError(safeMessage(
						parkEntranceMessage.getResponseMessage(),
						"Occasional visit failed."
				));
			}
		});
	}

	@Override
	public void onCurrentVisitorsReceived(boolean success,
			ParkEntranceMessage parkEntranceMessage) {

		Platform.runLater(() -> {
			if (parkEntranceMessage == null) {
				showError("Invalid current visitors response from server.");
				return;
			}

			updateCurrentVisitors(parkEntranceMessage);

			if (success) {
				showSuccess(safeMessage(
						parkEntranceMessage.getResponseMessage(),
						"Current visitors loaded successfully."
				));
			} else {
				showError(safeMessage(
						parkEntranceMessage.getResponseMessage(),
						"Failed to load current visitors."
				));
			}
		});
	}

	@FXML
	private void backButtonClick(ActionEvent event) {
		try {
			if (clientController != null) {
				clientController.removeParkEntranceObserver(this);
			}

			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/ParkWorkerHomePage.fxml")
			);

			Parent root = loader.load();

			ParkWorkerHomePageController controller = loader.getController();
			controller.setClientController(clientController);
			controller.setLoggedInEmployee(loggedInEmployee);

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("Park Worker Home Page");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
			showError("Could not return to park worker home page.");
		}
	}

	@Override
	public void handleExit() {
		Platform.runLater(() -> {
			Platform.exit();
			System.exit(0);
		});
	}
}