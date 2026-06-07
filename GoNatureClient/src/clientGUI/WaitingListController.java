package clientGUI;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import clientCommon.WaitingListObserver;
import clientController.ClientController;
import common.WaitingListMessage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

/**
 * This controller handles the waiting list page.
 *
 * The page allows a visitor to enter the requested visit details and send a
 * request to join the waiting list.
 */
public class WaitingListController implements WaitingListObserver {

	/**
	 * The client controller used to send requests to the server.
	 */
	private ClientController clientController;

	@FXML // fx:id="subscriberIdField"
	private TextField subscriberIdField; // Value injected by FXMLLoader

	@FXML // fx:id="parkIdField"
	private TextField parkIdField; // Value injected by FXMLLoader

	@FXML // fx:id="requestedDateField"
	private TextField requestedDateField; // Value injected by FXMLLoader

	@FXML // fx:id="visitorsField"
	private TextField visitorsField; // Value injected by FXMLLoader

	@FXML // fx:id="joinButton"
	private Button joinButton; // Value injected by FXMLLoader

	@FXML // fx:id="messageLabel"
	private Label messageLabel; // Value injected by FXMLLoader

	/**
	 * Sets the ClientController for this page and registers this controller as a
	 * waiting list observer.
	 *
	 * @param clientController the controller used to communicate with the server
	 */
	public void setClientController(ClientController clientController) {
		this.clientController = clientController;

		if (this.clientController != null) {
			this.clientController.addWaitingListObserver(this);
		}
	}

	/**
	 * Initializes the waiting list page.
	 *
	 * This method checks that all FXML fields were injected correctly and writes a
	 * short date format hint for the user.
	 */
	@FXML
	void initialize() {
		assert subscriberIdField != null : "fx:id=\"subscriberIdField\" was not injected.";
		assert parkIdField != null : "fx:id=\"parkIdField\" was not injected.";
		assert requestedDateField != null : "fx:id=\"requestedDateField\" was not injected.";
		assert visitorsField != null : "fx:id=\"visitorsField\" was not injected.";
		assert joinButton != null : "fx:id=\"joinButton\" was not injected.";
		assert messageLabel != null : "fx:id=\"messageLabel\" was not injected.";

		messageLabel.setTextFill(Color.BLUE);
		messageLabel.setText("Date format: yyyy-MM-ddTHH:mm, for example 2026-06-15T10:00");
	}

	/**
	 * Handles clicking the Join Waiting List button.
	 *
	 * The method validates the input fields, creates a WaitingListMessage, and sends
	 * it to the server through ClientController.
	 */
	@FXML
	void joinWaitingListClick() {
		if (clientController == null) {
			messageLabel.setTextFill(Color.RED);
			messageLabel.setText("Server connection is not ready.");
			return;
		}

		try {
			int subscriberId = Integer.parseInt(subscriberIdField.getText().trim());
			int parkId = Integer.parseInt(parkIdField.getText().trim());
			LocalDateTime requestedDate = LocalDateTime.parse(requestedDateField.getText().trim());
			int numberOfVisitors = Integer.parseInt(visitorsField.getText().trim());

			if (subscriberId <= 0 || parkId <= 0 || numberOfVisitors <= 0) {
				messageLabel.setTextFill(Color.RED);
				messageLabel.setText("Subscriber ID, park ID and visitors must be positive numbers.");
				return;
			}

			WaitingListMessage waitingListMessage =
					new WaitingListMessage(subscriberId, parkId, requestedDate, numberOfVisitors);

			joinButton.setDisable(true);

			messageLabel.setTextFill(Color.BLUE);
			messageLabel.setText("Sending waiting list request...");

			clientController.requestJoinWaitingList(waitingListMessage);

		} catch (NumberFormatException e) {
			messageLabel.setTextFill(Color.RED);
			messageLabel.setText("Subscriber ID, park ID and visitors must be numbers.");
		} catch (DateTimeParseException e) {
			messageLabel.setTextFill(Color.RED);
			messageLabel.setText("Invalid date format. Use yyyy-MM-ddTHH:mm, for example 2026-06-15T10:00.");
		}
	}

	/**
	 * Handles the server response after requesting to join the waiting list.
	 *
	 * @param success            true if the visitor was added successfully
	 * @param waitingListMessage the waiting list data returned by the server
	 */
	@Override
	public void onJoinWaitingListResult(boolean success, WaitingListMessage waitingListMessage) {
		Platform.runLater(() -> {
			joinButton.setDisable(false);

			if (waitingListMessage == null) {
				messageLabel.setTextFill(Color.RED);
				messageLabel.setText("Invalid response from server.");
				return;
			}

			if (success) {
				messageLabel.setTextFill(Color.GREEN);
				messageLabel.setText("Added to waiting list successfully. Queue position: "
						+ waitingListMessage.getQueuePosition());
			} else {
				messageLabel.setTextFill(Color.RED);
				messageLabel.setText("Failed to join waiting list.");
			}
		});
	}

	/**
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