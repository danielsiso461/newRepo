package clientGUI;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import clientCommon.MakeOrderObserver;
import clientCommon.WaitingListObserver;
import clientController.ClientController;
import common.CommonConstants;
import common.Message;
import common.Order;
import common.Protocol;
import common.WaitingListMessage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/*
 * this class is the UI controller for the make order page
 */
public class MakeOrderPageController implements MakeOrderObserver, WaitingListObserver {

	@FXML // ResourceBundle that was given to the FXMLLoader
	private ResourceBundle resources;

	@FXML // URL location of the FXML file that was given to the FXMLLoader
	private URL location;

	@FXML // fx:id="buttonBar"
	private ButtonBar buttonBar; // Value injected by FXMLLoader

	@FXML // fx:id="datePicker"
	private DatePicker datePicker; // Value injected by FXMLLoader

	@FXML // fx:id="email"
	private TextField email; // Value injected by FXMLLoader

	@FXML // fx:id="enterWaitingListButton"
	private Button enterWaitingListButton; // Value injected by FXMLLoader

	@FXML // fx:id="errorLabel"
	private Label errorLabel; // Value injected by FXMLLoader

	@FXML // fx:id="guideCheckbox"
	private CheckBox guideCheckbox; // Value injected by FXMLLoader

	@FXML // fx:id="hourPicker"
	private ComboBox<Integer> hourPicker; // Value injected by FXMLLoader

	@FXML // fx:id="makeOrderButton"
	private Button makeOrderButton; // Value injected by FXMLLoader

	@FXML // fx:id="parkPicker"
	private ComboBox<String> parkPicker; // Value injected by FXMLLoader

	@FXML // fx:id="rescheduleButton"
	private Button rescheduleButton; // Value injected by FXMLLoader

	@FXML // fx:id="visitorNumberPicker"
	private Spinner<Integer> visitorNumberPicker; // Value injected by FXMLLoader

	/* controller that handles the communication with the server */
	private ClientController clientController;

	/* holds the last controller, which is supposed to be a runnable object */
	private Runnable prevController;

	/* holds the scene of the previous page */
	private Scene prevScene;

	/*
	 * holds the previous date
	 * this is used to make sure a change happened when rescheduling
	 */
	private LocalDate date = null;

	/*
	 * holds the previous hour
	 * this is used to make sure a change happened when rescheduling
	 */
	private Integer hour = null;

	/* keeps track if the user left the make order scene */
	private boolean switchedScene = false;

	/* keeps track if the user made an order through this instance of make order controller */
	private boolean orderMade = false;

	/*
	 * this method triggers when the enter waiting list button is clicked
	 * @param event the button click event
	 */
	@FXML
	void enterWaitingListButtonClicked(ActionEvent event) {
		// make sure the client controller is ready before sending the request
		if (clientController == null) {
			warningMessage("Client connection is not ready.");
			return;
		}

		// make sure the user selected a park
		if (parkPicker.getValue() == null) {
			warningMessage("No Park Was Picked");
			return;
		}

		// make sure the user selected a date
		if (datePicker.getValue() == null) {
			warningMessage("No Date Was Picked");
			return;
		}

		// make sure the user selected an hour
		if (hourPicker.getValue() == null) {
			warningMessage("No Hour Was Picked");
			return;
		}

		// create the requested visit date and time from the selected date and hour
		LocalDateTime requestedOrderDate = datePicker.getValue().atTime(hourPicker.getValue(), 0);

		// create a waiting list request using the park name from the ComboBox
		WaitingListMessage waitingListMessage = new WaitingListMessage(
				Integer.parseInt(clientController.getId()),
				parkPicker.getValue(),
				requestedOrderDate,
				visitorNumberPicker.getValue()
		);

		// disable the action buttons while waiting for the server response
		buttonBar.setDisable(true);

		warningMessage("Sending waiting list request...");

		clientController.requestJoinWaitingList(waitingListMessage);
	}

	/*
	 * this method triggers when the enter reschedule button is clicked
	 * it handles enabling relevant fields
	 * @param event the button click event
	 */
	@FXML
	void rescheduleButtonClicked(ActionEvent event) {
		buttonBar.setDisable(true);
		date = datePicker.getValue();
		hour = hourPicker.getValue();
		hourPicker.setDisable(false);
		datePicker.setDisable(false);
		makeOrderButton.setDisable(false);
	}

	/*
	 * this method triggers when the enter make order button is clicked
	 * it handles checking the entered values and sending the order to the server
	 * @param event the button click event
	 */
	@FXML
	void makeOrderButtonClicked(ActionEvent event) {
		// make sure we get the relevant data
		if (parkPicker.getValue() == null) {
			warningMessage("No Park Was Picked");
			return;
		}
		if (datePicker.getValue() == null) {
			warningMessage("No Date Was Picked");
			return;
		}
		if ((date != null && date == datePicker.getValue())
				&& (hour != null && hour == hourPicker.getValue())) {
			warningMessage("No Rescheduling happened");
			return;
		}
		if (hourPicker.getValue() == null) {
			warningMessage("No Hour Was Picked");
			return;
		}
		if (email.getText().isEmpty()) {
			warningMessage("No Email Was Entered");
			return;
		}
		if (!email.getText().contains("@")) {
			warningMessage("Bad Email");
			return;
		}

		makeOrderButton.setDisable(true);

		// we lock order details in
		disableFields();

		Order o = new Order(
				datePicker.getValue(),
				visitorNumberPicker.getValue(),
				Integer.parseInt(clientController.getId()),
				parkPicker.getValue(),
				hourPicker.getValue(),
				email.getText()
		);

		if (guideCheckbox.isSelected()) {
			o.setOrderType(Order.ORDER_TYPE_ORGANIZED);
		} else {
			o.setOrderType(Order.ORDER_TYPE_PRIVATE);
			o.setGuideId(null);
		}

		orderMade = true;
		clientController.sendMessageToServer(new Message(o, Protocol.MAKE_ORDER));
	}

	/*
	 * this method initializes the make order page
	 * handles certain components behaviors and the red X button behavior
	 */
	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		assert buttonBar != null : "fx:id=\"buttonBar\" was not injected: check your FXML file 'makeOrderPage.fxml'.";
		assert datePicker != null : "fx:id=\"datePicker\" was not injected: check your FXML file 'makeOrderPage.fxml'.";
		assert email != null : "fx:id=\"email\" was not injected: check your FXML file 'makeOrderPage.fxml'.";
		assert enterWaitingListButton != null : "fx:id=\"enterWaitingListButton\" was not injected: check your FXML file 'makeOrderPage.fxml'.";
		assert errorLabel != null : "fx:id=\"errorLabel\" was not injected: check your FXML file 'makeOrderPage.fxml'.";
		assert guideCheckbox != null : "fx:id=\"guideCheckbox\" was not injected: check your FXML file 'makeOrderPage.fxml'.";
		assert hourPicker != null : "fx:id=\"hourPicker\" was not injected: check your FXML file 'makeOrderPage.fxml'.";
		assert makeOrderButton != null : "fx:id=\"makeOrderButton\" was not injected: check your FXML file 'makeOrderPage.fxml'.";
		assert parkPicker != null : "fx:id=\"parkPicker\" was not injected: check your FXML file 'makeOrderPage.fxml'.";
		assert rescheduleButton != null : "fx:id=\"rescheduleButton\" was not injected: check your FXML file 'makeOrderPage.fxml'.";
		assert visitorNumberPicker != null : "fx:id=\"visitorNumberPicker\" was not injected: check your FXML file 'makeOrderPage.fxml'.";

		// disable button bar since we don't want those buttons available from the start
		buttonBar.setDisable(true);

		// make sure make order button is enabled
		makeOrderButton.setDisable(false);

		// set the hour comboBox to have range 0-23
		hourPicker.getItems().addAll(IntStream.rangeClosed(
				CommonConstants.MIN_HOUR,
				CommonConstants.MAX_HOUR)
				.boxed().collect(Collectors.toList()));

		// set the number of visitors picker to have range 1-15, starting from 1
		visitorNumberPicker.setValueFactory(
				new SpinnerValueFactory.IntegerSpinnerValueFactory(
						CommonConstants.MIN_VISITOR_COUNT,
						CommonConstants.MAX_VISITOR_COUNT,
						ConstantsUI.START_VISITORS));

		// setting up the date picker
		// grays out all dates before the current date, makes them unselectable
		datePicker.setEditable(false);
		datePicker.setDayCellFactory(picker -> new DateCell() {
			public void updateItem(LocalDate date, boolean empty) {
				super.updateItem(date, empty);
				if (empty) return;
				if (date.isBefore(LocalDate.now())) {
					setDisable(true);
					setStyle("-fx-background-color: #eeeeee;");
				}
			}
		});

		/*
		 * Park names are requested after the client controller is set.
		 */

		// this handles returning to order table when pressing the red X button
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Stage stage = (Stage) makeOrderButton.getScene().getWindow();
				stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
					@Override
					public void handle(WindowEvent event) {
						event.consume();
						switchScene();
					}
				});
			}
		});
	}

	/*
	 * this method changes the error label of the form according to the given message
	 * @param s the given message
	 */
	private void warningMessage(String s) {
		errorLabel.setText(s);
	}

	/* this method disables all of the form's fields */
	private void disableFields() {
		parkPicker.setDisable(true);
		datePicker.setDisable(true);
		hourPicker.setDisable(true);
		visitorNumberPicker.setDisable(true);
		email.setDisable(true);
		guideCheckbox.setDisable(true);
	}

	/* this method enables all of the form's fields */
	private void enableFields() {
		parkPicker.setDisable(false);
		datePicker.setDisable(false);
		hourPicker.setDisable(false);
		visitorNumberPicker.setDisable(false);
		email.setDisable(false);
		guideCheckbox.setDisable(false);
	}

	/*
	 * this method loads the given park names into the relevant controller component
	 * @param parkNames the list of park Names
	 */
	public void loadParkNames(List<String> parkNames) {
		if (parkNames == null) {
			return;
		}
		parkPicker.getItems().addAll(parkNames);
	}

	/*
	 * this method sends a request to the server to get the list of park names
	 */
	public void requestActiveParkList() {
		clientController.sendMessageToServer(new Message(null, Protocol.GET_PARK_NAMES));
	}

	/*
	 * this method handles setting a client controller for this UI controller
	 * it also adds this controller as a make order observer on the client controller
	 * @param c the client controller
	 */
	public void setClientController(ClientController c) {
		if (c == null) {
			return;
		}

		clientController = c;

		// register this screen to receive make-order responses
		c.addMakeOrderObserver(this);

		// register this screen to receive waiting-list responses
		c.addWaitingListObserver(this);

		// request park names only after the client controller is ready
		requestActiveParkList();
	}

	/*
	 * this method handles keeping track of the previous scene
	 * @param prevScene the previous scene
	 */
	public void setPrevScene(Scene prevScene) {
		if (prevScene == null) {
			return;
		}
		this.prevScene = prevScene;
		switchedScene = false;
	}

	/*
	 * this method handles keeping track of the previous controller
	 * @param prevController the previous controller
	 */
	public void setPrevController(Runnable prevController) {
		if (prevController == null) {
			return;
		}
		this.prevController = prevController;
	}

	/*
	 * this method handles receiving the park names from the server
	 * @param parkNames the park name list
	 */
	@Override
	public void onParkNamesReceived(List<String> parkNames) {
		loadParkNames(parkNames);
	}

	/*
	 * this method handles the response from the server
	 * @param m the message from the server
	 */
	@Override
	public void onMakeOrderServerResponse(Message m) {
		String declineMsg = null;

		if (!orderMade) {
			return;
		}

		if (m == null) {
			declineMsg = "Unknown";
			warningMessage("An unknown error occurred.");
			enableFields();
			makeOrderButton.setDisable(false);
		} else if (m.getType() == Protocol.MAKE_ORDER_SUCCESS) {
			Order o = (Order) m.getData();

			MakePopUp.makePopup("Order made!", null);
			MakePopUp.makePopup("EMAIL SIMULATION", "Order made! " + o.getEmail());

			if (o.getIsSubscribed()) {
				MakePopUp.makePopup("PHONE SIMULATION", "Order made! " + o.getPhoneNumber());
			}

			if (!switchedScene) {
				switchScene();
			}

			clientController.removeMakeOrderObserver(this);
			return;
		} else if (m.getType() == Protocol.MAKE_ORDER_FAIL_TIME) {
			declineMsg = "Visit time is unavailable";
			warningMessage("Visit time is unavailable, please Enter Waiting List or Reschedule");
			buttonBar.setDisable(false);
		} else if (m.getType() == Protocol.MAKE_ORDER_FAIL_NOT_GUIDE) {
			declineMsg = "You are not a guide";
			warningMessage("You are not a guide, cannot book organized visit");
			guideCheckbox.setSelected(false);
			enableFields();
			guideCheckbox.setDisable(true);
			makeOrderButton.setDisable(false);
		} else if (m.getType() == Protocol.MAKE_ORDER_FAIL_NOT_SUBSCRIBED) {
			declineMsg = "Too many visitors";
			warningMessage("You are not subscribed, cannot book a visit for multiple people");
			visitorNumberPicker.setDisable(false);
			makeOrderButton.setDisable(false);
		} else if (m.getType() == Protocol.MAKE_ORDER_FAIL) {
			declineMsg = "Unknown server error";
			warningMessage("Unknown Error occurred on the server");
			enableFields();
			makeOrderButton.setDisable(false);
		} else {
			declineMsg = "Unknown parsing error";
			warningMessage("Unknown Error occurred parsing response");
			enableFields();
			makeOrderButton.setDisable(false);
		}

		if (switchedScene) {
			clientController.removeMakeOrderObserver(this);

			if (orderMade) {
				MakePopUp.makePopup("Order declined!", declineMsg);
			}

			return;
		}

		orderMade = false;
	}

	/* this method handles switching the current scene back the previous one */
	private void switchScene() {
		Stage stage = (Stage) makeOrderButton.getScene().getWindow();
		stage.setScene(prevScene);
		stage.setTitle("Order Table Page");
		switchedScene = true;
		prevController.run();
		stage.show();
	}

	/*
	 * This method is called when the server returns a response for joining
	 * the waiting list.
	 *
	 * @param success true if the request was saved successfully, false otherwise
	 * @param waitingListMessage the waiting list message returned by the server
	 */
	@Override
	public void onJoinWaitingListResult(boolean success, WaitingListMessage waitingListMessage) {
		if (success && waitingListMessage != null) {
			warningMessage("Added to waiting list. Queue position: "
					+ waitingListMessage.getQueuePosition());
		} else {
			warningMessage("Could not enter waiting list.");
			buttonBar.setDisable(false);
		}
	}

	/*
	 * Handles the result of rejecting a waiting list offer.
	 *
	 * This screen currently does not display waiting list offers, so the method only
	 * clears the warning message when the request succeeds or shows a failure message
	 * when the request fails.
	 *
	 * @param success            true if the waiting list offer was rejected successfully
	 * @param waitingListMessage the waiting list message returned from the server
	 */
	@Override
	public void onRejectWaitingOfferResult(boolean success, WaitingListMessage waitingListMessage) {
		if (success) {
			warningMessage("");
		} else {
			warningMessage("Failed to reject waiting list offer.");
		}
	}

	/*
	 * Handles the result of accepting a waiting list offer.
	 *
	 * This screen currently does not display waiting list offers, so the method only
	 * clears the warning message when the request succeeds or shows a failure message
	 * when the request fails.
	 *
	 * @param success            true if the waiting list offer was accepted successfully
	 * @param waitingListMessage the waiting list message returned from the server
	 */
	@Override
	public void onAcceptWaitingOfferResult(boolean success, WaitingListMessage waitingListMessage) {
		if (success) {
			warningMessage("");
		} else {
			warningMessage("Failed to accept waiting list offer.");
		}
	}

	/*
	 * This method is called when the server disconnects the client.
	 */
	@Override
	public void handleExit() {
		Platform.exit();
		System.exit(0);
	}
}

