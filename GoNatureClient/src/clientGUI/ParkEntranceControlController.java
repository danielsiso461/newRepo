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
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/*
 * This controller handles the park entrance control page.
 *
 * The page allows a park employee to check visitors into the park using a
 * confirmation code as a QR code simulation, check visitors out of the park,
 * create occasional visits, and view the current number of visitors in the park.
 */
public class ParkEntranceControlController implements ParkEntranceObserver {

	/*
	 * Represents the action mode used by the park entrance page.
	 */
	public enum EntranceMode {
		CHECK_IN,
		OCCASIONAL_VISIT,
		CHECK_OUT,
		CURRENT_VISITORS
	}

	/*
	 * The client controller used to communicate with the server.
	 */
	private ClientController clientController;

	/*
	 * The employee currently using the park entrance page.
	 */
	private Employee loggedInEmployee;

	/*
	 * The current mode of the park entrance page.
	 */
	private EntranceMode entranceMode;

	@FXML // fx:id="confirmationCodeField"
	private TextField confirmationCodeField; // Value injected by FXMLLoader

	@FXML // fx:id="parkIdField"
	private TextField parkIdField; // Value injected by FXMLLoader

	@FXML // fx:id="employeeIdField"
	private TextField employeeIdField; // Value injected by FXMLLoader

	@FXML // fx:id="visitorsField"
	private TextField visitorsField; // Value injected by FXMLLoader

	@FXML // fx:id="checkInButton"
	private Button checkInButton; // Value injected by FXMLLoader

	@FXML // fx:id="checkOutButton"
	private Button checkOutButton; // Value injected by FXMLLoader

	@FXML // fx:id="occasionalVisitButton"
	private Button occasionalVisitButton; // Value injected by FXMLLoader

	@FXML // fx:id="currentVisitorsButton"
	private Button currentVisitorsButton; // Value injected by FXMLLoader

	@FXML // fx:id="currentVisitorsLabel"
	private Label currentVisitorsLabel; // Value injected by FXMLLoader

	@FXML // fx:id="messageLabel"
	private Label messageLabel; // Value injected by FXMLLoader

	/*
	 * Sets the ClientController and registers this page as a park entrance observer.
	 *
	 * @param clientController the controller used to communicate with the server
	 */
	public void setClientController(ClientController clientController) {
		this.clientController = clientController;

		if (this.clientController != null) {
			this.clientController.addParkEntranceObserver(this);
		}
	}

	/*
	 * Sets the logged-in employee and fills the employee and park fields.
	 * 
	 * @param loggedInEmployee the employee currently using the page
	 */
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

	/*
	 * Sets the current entrance mode and updates the page accordingly.
	 * 
	 * @param entranceMode the selected entrance action mode
	 */
	public void setEntranceMode(EntranceMode entranceMode) {
		this.entranceMode = entranceMode;
		applyEntranceMode();
	}

	/*
	 * Initializes the park entrance control page.
	 */
	@FXML
	void initialize() {
		assert confirmationCodeField != null : "fx:id=\"confirmationCodeField\" was not injected.";
		assert parkIdField != null : "fx:id=\"parkIdField\" was not injected.";
		assert employeeIdField != null : "fx:id=\"employeeIdField\" was not injected.";
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
		messageLabel.setTextFill(Color.BLUE);
		messageLabel.setText("Use the confirmation code as QR code simulation.");
	}

	/*
	 * Applies the selected entrance mode to the page.
	 * 
	 * Each dashboard action opens the same page, but enables only the relevant
	 * action button and fields.
	 */
	private void applyEntranceMode() {
		if (entranceMode == null ||
				checkInButton == null ||
				checkOutButton == null ||
				occasionalVisitButton == null ||
				currentVisitorsButton == null ||
				confirmationCodeField == null ||
				parkIdField == null ||
				employeeIdField == null ||
				visitorsField == null ||
				currentVisitorsLabel == null ||
				messageLabel == null) {
			return;
		}

		checkInButton.setDisable(true);
		checkOutButton.setDisable(true);
		occasionalVisitButton.setDisable(true);
		currentVisitorsButton.setDisable(true);

		currentVisitorsButton.setText("Current Visitors");
		currentVisitorsButton.setVisible(true);
		currentVisitorsButton.setManaged(true);

		confirmationCodeField.setDisable(false);
		confirmationCodeField.setPromptText("Confirmation Code / QR Code");
		parkIdField.setDisable(false);
		employeeIdField.setDisable(false);
		visitorsField.setDisable(false);

		currentVisitorsLabel.setText("Current visitors: -");
		currentVisitorsLabel.setVisible(false);
		currentVisitorsLabel.setManaged(false);

		messageLabel.setTextFill(Color.BLUE);

		switch (entranceMode) {
		case CHECK_IN:
			checkInButton.setDisable(false);
			messageLabel.setText("Check visitor entry using the confirmation code.");
			break;

		case OCCASIONAL_VISIT:
			occasionalVisitButton.setDisable(false);
			confirmationCodeField.clear();
			confirmationCodeField.setDisable(true);
			messageLabel.setText("Create an occasional visit for visitors without an order.");
			break;

		case CHECK_OUT:
			checkOutButton.setDisable(false);
			confirmationCodeField.setPromptText("Confirmation Code / Visit ID");
			visitorsField.clear();
			visitorsField.setDisable(true);
			messageLabel.setText("Record visitor exit using a confirmation code or occasional visit ID.");
			break;

		case CURRENT_VISITORS:
			currentVisitorsButton.setDisable(true);
			currentVisitorsButton.setVisible(false);
			currentVisitorsButton.setManaged(false);

			confirmationCodeField.clear();
			visitorsField.clear();

			confirmationCodeField.setDisable(true);
			visitorsField.setDisable(true);

			currentVisitorsLabel.setVisible(true);
			currentVisitorsLabel.setManaged(true);
			currentVisitorsLabel.setText("Current visitors: -");

			messageLabel.setText("Loading the current number of visitors in the park.");

			currentVisitorsButtonClick();
			break;

		default:
			break;
		}
	}

	/*
	 * Handles clicking the Check In button.
	 *
	 * The method sends a request to create a visit from an existing order using the
	 * confirmation code as a QR code simulation.
	 */
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

			if (confirmationCode <= 0 || parkId <= 0 || employeeId <= 0 || visitors <= 0) {
				showError("All values must be positive numbers.");
				return;
			}

			ParkEntranceMessage message =
					new ParkEntranceMessage(confirmationCode, parkId, employeeId, visitors);

			messageLabel.setTextFill(Color.BLUE);
			messageLabel.setText("Sending check-in request...");

			clientController.requestCheckInOrder(message);

		} catch (NumberFormatException e) {
			showError("Confirmation code, park ID, employee ID and visitors must be numbers.");
		}
	}

	/*
	 * Handles clicking the Check Out button.
	 *
	 * The method closes an open visit using the confirmation code as a QR code
	 * simulation.
	 */
	@FXML
	void checkOutButtonClick() {
		if (clientController == null) {
			showError("Server connection is not ready.");
			return;
		}

		try {
			int exitCode = Integer.parseInt(confirmationCodeField.getText().trim());
			int parkId = Integer.parseInt(parkIdField.getText().trim());
			int employeeId = Integer.parseInt(employeeIdField.getText().trim());

			if (exitCode <= 0 || parkId <= 0 || employeeId <= 0) {
				showError("Confirmation code / visit ID, park ID and employee ID must be positive numbers.");
				return;
			}

			ParkEntranceMessage message = new ParkEntranceMessage();
			message.setConfirmationCode(exitCode);
			message.setVisitId(exitCode);
			message.setParkId(parkId);
			message.setEmployeeId(employeeId);
			message.setIdentificationMethod("confirmation_code");

			messageLabel.setTextFill(Color.BLUE);
			messageLabel.setText("Sending check-out request...");

			clientController.requestCheckOutVisit(message);

		} catch (NumberFormatException e) {
			showError("Confirmation code / visit ID, park ID and employee ID must be numbers.");
		}
	}

	/*
	 * Handles clicking the Occasional Visit button.
	 *
	 * The method creates a visit for visitors who arrived without an order.
	 */
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

			if (parkId <= 0 || employeeId <= 0 || visitors <= 0) {
				showError("Park ID, employee ID and visitors must be positive numbers.");
				return;
			}

			ParkEntranceMessage message = new ParkEntranceMessage();
			message.setParkId(parkId);
			message.setEmployeeId(employeeId);
			message.setActualNumberOfVisitors(visitors);
			message.setIdentificationMethod("id_number");

			messageLabel.setTextFill(Color.BLUE);
			messageLabel.setText("Sending occasional visit request...");

			clientController.requestOccasionalVisit(message);

		} catch (NumberFormatException e) {
			showError("Park ID, employee ID and visitors must be numbers.");
		}
	}

	/*
	 * Handles clicking the Current Visitors button.
	 *
	 * The method requests the current number of visitors inside the selected park.
	 */
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

			messageLabel.setTextFill(Color.BLUE);
			messageLabel.setText("Loading current visitors...");

			clientController.requestCurrentVisitors(message);

		} catch (NumberFormatException e) {
			showError("Park ID must be a number.");
		}
	}

	/*
	 * Displays an error message in the page.
	 *
	 * @param message the message to display
	 */
	private void showError(String message) {
		messageLabel.setTextFill(Color.RED);
		messageLabel.setText(message);
	}

	/*
	 * Updates the current visitors label.
	 *
	 * @param parkEntranceMessage the response data returned from the server
	 */
	private void updateCurrentVisitors(ParkEntranceMessage parkEntranceMessage) {
		if (parkEntranceMessage != null) {
			currentVisitorsLabel.setText("Current visitors: " + parkEntranceMessage.getCurrentVisitors());
		}
	}

	@Override
	public void onCheckInOrderResult(boolean success, ParkEntranceMessage parkEntranceMessage) {
		Platform.runLater(() -> {
			if (parkEntranceMessage == null) {
				showError("Invalid check-in response from server.");
				return;
			}

			updateCurrentVisitors(parkEntranceMessage);

			if (success) {
				messageLabel.setTextFill(Color.GREEN);
			} else {
				messageLabel.setTextFill(Color.RED);
			}

			messageLabel.setText(parkEntranceMessage.getResponseMessage());
		});
	}

	@Override
	public void onCheckOutVisitResult(boolean success, ParkEntranceMessage parkEntranceMessage) {
		Platform.runLater(() -> {
			if (parkEntranceMessage == null) {
				showError("Invalid check-out response from server.");
				return;
			}

			updateCurrentVisitors(parkEntranceMessage);

			if (success) {
				messageLabel.setTextFill(Color.GREEN);
			} else {
				messageLabel.setTextFill(Color.RED);
			}

			messageLabel.setText(parkEntranceMessage.getResponseMessage());
		});
	}

	@Override
	public void onOccasionalVisitResult(boolean success, ParkEntranceMessage parkEntranceMessage) {
		Platform.runLater(() -> {
			if (parkEntranceMessage == null) {
				showError("Invalid occasional visit response from server.");
				return;
			}

			updateCurrentVisitors(parkEntranceMessage);

			if (success) {
				messageLabel.setTextFill(Color.GREEN);
			} else {
				messageLabel.setTextFill(Color.RED);
			}

			messageLabel.setText(parkEntranceMessage.getResponseMessage());
		});
	}

	@Override
	public void onCurrentVisitorsReceived(boolean success, ParkEntranceMessage parkEntranceMessage) {
		Platform.runLater(() -> {
			if (parkEntranceMessage == null) {
				showError("Invalid current visitors response from server.");
				return;
			}

			updateCurrentVisitors(parkEntranceMessage);

			if (success) {
				messageLabel.setTextFill(Color.GREEN);
			} else {
				messageLabel.setTextFill(Color.RED);
			}

			messageLabel.setText(parkEntranceMessage.getResponseMessage());
		});
	}

	/*
	 * Returns the park worker to the park worker home page.
	 * 
	 * @param event the button click event
	 */
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
		}
	}

	/*
	 * Handles server shutdown/disconnect.
	 */
	@Override
	public void handleExit() {
		Platform.runLater(() -> {
			Platform.exit();
			System.exit(0);
		});
	}
}

