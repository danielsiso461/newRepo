package clientGUI;

import clientCommon.ParkEntranceObserver;
import clientController.ClientController;
import common.ParkEntranceMessage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

/*
 * This controller handles the park entrance control page.
 *
 * The page allows a park employee to check visitors into the park using a
 * confirmation code as a QR code simulation, check visitors out of the park,
 * create occasional visits, and view the current number of visitors in the park.
 */
public class ParkEntranceControlController implements ParkEntranceObserver {
	/*
	 * The client controller used to communicate with the server.
	 */
	private ClientController clientController;

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

		currentVisitorsLabel.setText("Current visitors: -");
		messageLabel.setTextFill(Color.BLUE);
		messageLabel.setText("Use the confirmation code as QR code simulation.");
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

			messageLabel.setTextFill(Color.BLUE);
			messageLabel.setText("Sending check-out request...");

			clientController.requestCheckOutVisit(message);

		} catch (NumberFormatException e) {
			showError("Confirmation code, park ID and employee ID must be numbers.");
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
