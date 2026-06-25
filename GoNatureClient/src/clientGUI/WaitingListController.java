package clientGUI;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import clientCommon.WaitingListObserver;
import clientController.ClientController;
import common.WaitingListMessage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/*
 * This controller handles the waiting list page.
 *
 * The page displays the current visitor's active waiting list requests.
 * Requests with status "waiting" are displayed only.
 * Requests with status "offered" can be accepted or rejected.
 */
public class WaitingListController implements WaitingListObserver {
	/**
	 * The client controller used to send requests to the server.
	 */
	private ClientController clientController;

	/*
	 * The previous scene, used when returning to the order table page.
	 */
	private Scene prevScene;
	
	/*
	 * Keeps track of offered waiting list requests that already triggered
	 * notification simulation popups in this screen session.
	 */
	private Set<Integer> simulatedOfferIds = new HashSet<>();
	
	@FXML // fx:id="offersTable"
	private TableView<WaitingListMessage> offersTable; // Value injected by FXMLLoader

	@FXML // fx:id="waitingIdColumn"
	private TableColumn<WaitingListMessage, Integer> waitingIdColumn; // Value injected by FXMLLoader

	@FXML // fx:id="parkIdColumn"
	private TableColumn<WaitingListMessage, Integer> parkIdColumn; // Value injected by FXMLLoader

	@FXML // fx:id="requestedDateColumn"
	private TableColumn<WaitingListMessage, java.time.LocalDateTime> requestedDateColumn; // Value injected by FXMLLoader

	@FXML // fx:id="visitorsColumn"
	private TableColumn<WaitingListMessage, Integer> visitorsColumn; // Value injected by FXMLLoader

	@FXML // fx:id="queuePositionColumn"
	private TableColumn<WaitingListMessage, Integer> queuePositionColumn; // Value injected by FXMLLoader

	@FXML // fx:id="statusColumn"
	private TableColumn<WaitingListMessage, String> statusColumn; // Value injected by FXMLLoader

	@FXML // fx:id="acceptButton"
	private Button acceptButton; // Value injected by FXMLLoader

	@FXML // fx:id="rejectButton"
	private Button rejectButton; // Value injected by FXMLLoader

	@FXML // fx:id="backButton"
	private Button backButton; // Value injected by FXMLLoader

	@FXML // fx:id="messageLabel"
	private Label messageLabel; // Value injected by FXMLLoader

	/*
	 * Sets the ClientController for this page, registers this controller as a
	 * waiting list observer, and loads the current visitor's waiting list requests.
	 *
	 * @param clientController the controller used to communicate with the server
	 */
	public void setClientController(ClientController clientController) {
		this.clientController = clientController;

		if (this.clientController != null) {
			this.clientController.addWaitingListObserver(this);
			loadWaitingListRequests();
		}
	}

	/*
	 * Sets the previous scene so the user can return to the order table page.
	 *
	 * @param prevScene the previous scene
	 */
	public void setPrevScene(Scene prevScene) {
		this.prevScene = prevScene;
	}

	/*
	 * Initializes the waiting list page.
	 *
	 * This method connects the table columns to the WaitingListMessage fields and
	 * disables the Accept / Reject buttons until an offered request is selected.
	 */
	@FXML
	void initialize() {
		assert offersTable != null : "fx:id=\"offersTable\" was not injected.";
		assert waitingIdColumn != null : "fx:id=\"waitingIdColumn\" was not injected.";
		assert parkIdColumn != null : "fx:id=\"parkIdColumn\" was not injected.";
		assert requestedDateColumn != null : "fx:id=\"requestedDateColumn\" was not injected.";
		assert visitorsColumn != null : "fx:id=\"visitorsColumn\" was not injected.";
		assert queuePositionColumn != null : "fx:id=\"queuePositionColumn\" was not injected.";
		assert statusColumn != null : "fx:id=\"statusColumn\" was not injected.";
		assert acceptButton != null : "fx:id=\"acceptButton\" was not injected.";
		assert rejectButton != null : "fx:id=\"rejectButton\" was not injected.";
		assert backButton != null : "fx:id=\"backButton\" was not injected.";
		assert messageLabel != null : "fx:id=\"messageLabel\" was not injected.";

		waitingIdColumn.setCellValueFactory(new PropertyValueFactory<>("waitingId"));
		parkIdColumn.setCellValueFactory(new PropertyValueFactory<>("parkId"));
		requestedDateColumn.setCellValueFactory(new PropertyValueFactory<>("requestedOrderDate"));
		visitorsColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfVisitors"));
		queuePositionColumn.setCellValueFactory(new PropertyValueFactory<>("queuePosition"));
		statusColumn.setCellValueFactory(new PropertyValueFactory<>("waitingStatus"));

		offersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		acceptButton.setDisable(true);
		rejectButton.setDisable(true);

		offersTable.getSelectionModel().selectedItemProperty().addListener((observable, oldOffer, newOffer) -> {
			updateActionButtons(newOffer);
		});

		messageLabel.setTextFill(Color.BLUE);
		messageLabel.setText("Loading your waiting list requests...");
	}

	/*
	 * Loads the current visitor's active waiting list requests from the server.
	 */
	private void loadWaitingListRequests() {
		if (clientController == null) {
			messageLabel.setTextFill(Color.RED);
			messageLabel.setText("Server connection is not ready.");
			return;
		}

		try {
			int subscriberId = Integer.parseInt(clientController.getId());
			clientController.requestWaitingOffers(subscriberId);
		} catch (NumberFormatException e) {
			messageLabel.setTextFill(Color.RED);
			messageLabel.setText("Invalid subscriber ID.");
			
		}
	}

	/*
	 * Enables Accept / Reject only for requests that are currently offered.
	 *
	 * @param selectedOffer the selected waiting list request
	 */
	private void updateActionButtons(WaitingListMessage selectedOffer) {
		boolean canRespond =
				selectedOffer != null &&
				"offered".equalsIgnoreCase(selectedOffer.getWaitingStatus());

		acceptButton.setDisable(!canRespond);
		rejectButton.setDisable(!canRespond);
	}

	/*
	 * Handles clicking the Accept button.
	 *
	 * The method sends the selected offered waiting list request to the server so it
	 * will be accepted.
	 */
	@FXML
	void acceptSelectedOfferClick() {
		WaitingListMessage selectedOffer = offersTable.getSelectionModel().getSelectedItem();

		if (selectedOffer == null) {
			messageLabel.setTextFill(Color.RED);
			messageLabel.setText("Please select a waiting list request first.");
			return;
		}

		if (!"offered".equalsIgnoreCase(selectedOffer.getWaitingStatus())) {
			messageLabel.setTextFill(Color.RED);
			messageLabel.setText("Only offered requests can be accepted.");
			return;
		}

		acceptButton.setDisable(true);
		rejectButton.setDisable(true);

		messageLabel.setTextFill(Color.BLUE);
		messageLabel.setText("Accepting waiting list offer...");

		clientController.requestAcceptWaitingOffer(selectedOffer.getWaitingId());
	}

	/*
	 * Handles clicking the Reject button.
	 *
	 * The method sends the selected offered waiting list request to the server so it
	 * will be rejected.
	 */
	@FXML
	void rejectSelectedOfferClick() {
		WaitingListMessage selectedOffer = offersTable.getSelectionModel().getSelectedItem();

		if (selectedOffer == null) {
			messageLabel.setTextFill(Color.RED);
			messageLabel.setText("Please select a waiting list request first.");
			return;
		}

		if (!"offered".equalsIgnoreCase(selectedOffer.getWaitingStatus())) {
			messageLabel.setTextFill(Color.RED);
			messageLabel.setText("Only offered requests can be rejected.");
			return;
		}

		acceptButton.setDisable(true);
		rejectButton.setDisable(true);

		messageLabel.setTextFill(Color.BLUE);
		messageLabel.setText("Rejecting waiting list offer...");

		clientController.requestRejectWaitingOffer(selectedOffer.getWaitingId());
	}

	/*
	 * Handles clicking the Back button.
	 *
	 * The method returns the user to the previous scene.
	 */
	@FXML
	void backButtonClick() {
		if (prevScene == null) {
			messageLabel.setTextFill(Color.RED);
			messageLabel.setText("Previous page is not available.");
			return;
		}

		Stage stage = (Stage) backButton.getScene().getWindow();
		stage.setScene(prevScene);
		stage.setTitle("Order Table");
		stage.show();
	}
	/*
	 * Shows notification simulation popups for offered waiting list requests.
	 *
	 * The popups simulate SMS and email messages that would be sent to the visitor
	 * when a place becomes available.
	 *
	 * @param offers the waiting list requests returned from the server
	 */
	private void showOfferSimulationMessages(List<WaitingListMessage> offers) {
		for (WaitingListMessage offer : offers) {
			if (offer == null) {
				continue;
			}

			boolean shouldSimulate =
					"offered".equalsIgnoreCase(offer.getWaitingStatus()) &&
					simulatedOfferIds.add(offer.getWaitingId());

			if (shouldSimulate) {
				showSimulationAlert(
						"PHONE SIMULATION",
						"SMS would be sent to: " + getDisplayValue(offer.getSubscriberPhone())
								+ "\n\nA place is available for your waiting list request."
								+ "\nPark ID: " + offer.getParkId()
								+ "\nDate: " + offer.getRequestedOrderDate()
								+ "\nVisitors: " + offer.getNumberOfVisitors()
								+ "\n\nPlease accept the offer within one hour."
				);

				showSimulationAlert(
						"EMAIL SIMULATION",
						"Email would be sent to: " + getDisplayValue(offer.getSubscriberEmail())
								+ "\n\nA place is available for your waiting list request."
								+ "\nPark ID: " + offer.getParkId()
								+ "\nDate: " + offer.getRequestedOrderDate()
								+ "\nVisitors: " + offer.getNumberOfVisitors()
								+ "\n\nPlease accept the offer within one hour."
				);
			}
		}
	}

	/*
	 * Shows a notification simulation popup.
	 *
	 * @param title   the popup title
	 * @param message the popup message
	 */
	private void showSimulationAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	/*
	 * Returns a readable value for notification simulation.
	 *
	 * @param value the value to display
	 * @return the original value, or "Not available" if it is missing
	 */
	private String getDisplayValue(String value) {
		if (value == null || value.isBlank()) {
			return "Not available";
		}

		return value;
	}

	/*
	 * Handles the result of loading waiting list requests for the current subscriber.
	 *
	 * @param success true if the requests were loaded successfully
	 * @param offers  the waiting list requests returned from the server
	 */
	@Override
	public void onWaitingOffersReceived(boolean success, List<WaitingListMessage> offers) {
		Platform.runLater(() -> {
			if (!success || offers == null) {
				messageLabel.setTextFill(Color.RED);
				messageLabel.setText("Failed to load waiting list requests.");
				return;
			}

			offersTable.getItems().setAll(offers);
			offersTable.getSelectionModel().clearSelection();
			
			showOfferSimulationMessages(offers);
			
			acceptButton.setDisable(true);
			rejectButton.setDisable(true);

			if (offers.isEmpty()) {
				messageLabel.setTextFill(Color.BLUE);
				messageLabel.setText("You do not have active waiting list requests.");
			} else {
				messageLabel.setTextFill(Color.GREEN);
				messageLabel.setText("Loaded waiting list requests: " + offers.size());
			}
		});
	}

	/*
	 * Handles the server response after requesting to join the waiting list.
	 *
	 * This screen does not create waiting list requests directly, so no UI action is
	 * needed here.
	 *
	 * @param success            true if the visitor was added successfully
	 * @param waitingListMessage the waiting list data returned by the server
	 */
	@Override
	public void onJoinWaitingListResult(boolean success, WaitingListMessage waitingListMessage) {
		// No action is needed here at this stage.
	}

	/*
	 * Handles the result of rejecting a waiting list offer.
	 *
	 * @param success            true if the waiting list offer was rejected successfully
	 * @param waitingListMessage the waiting list message returned from the server
	 */
	@Override
	public void onRejectWaitingOfferResult(boolean success, WaitingListMessage waitingListMessage) {
		Platform.runLater(() -> {
			if (success) {
				messageLabel.setTextFill(Color.GREEN);
				messageLabel.setText("Waiting list offer rejected successfully.");
				loadWaitingListRequests();
			} else {
				messageLabel.setTextFill(Color.RED);
				messageLabel.setText("Failed to reject waiting list offer.");
			}
		});
	}

	/*
	 * Handles the result of accepting a waiting list offer.
	 *
	 * @param success            true if the waiting list offer was accepted successfully
	 * @param waitingListMessage the waiting list message returned from the server
	 */
	@Override
	public void onAcceptWaitingOfferResult(boolean success, WaitingListMessage waitingListMessage) {
		Platform.runLater(() -> {
			if (success) {
				messageLabel.setTextFill(Color.GREEN);
				messageLabel.setText("Waiting list offer accepted successfully. The order was added to your orders.");

				loadWaitingListRequests();

				if (clientController != null) {
					clientController.requestOrders();
				}
			} else {
				messageLabel.setTextFill(Color.RED);
				messageLabel.setText("Failed to accept waiting list offer.");
			}
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