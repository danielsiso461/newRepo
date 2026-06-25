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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/*
 * This class is the UI controller for the make order page.
 */
public class MakeOrderPageController implements MakeOrderObserver, WaitingListObserver {

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private ButtonBar buttonBar;

	@FXML
	private DatePicker datePicker;

	@FXML
	private TextField email;

	@FXML
	private Button enterWaitingListButton;

	@FXML
	private Label errorLabel;

	@FXML
	private CheckBox guideCheckbox;

	@FXML
	private ComboBox<Integer> hourPicker;

	@FXML
	private Button makeOrderButton;

	@FXML
	private ComboBox<String> parkPicker;

	@FXML
	private Button rescheduleButton;

	@FXML
	private HBox visitorNumberRow;

	@FXML
	private Spinner<Integer> visitorNumberPicker;

	private ClientController clientController;

	private Runnable prevController;

	private Scene prevScene;

	private LocalDate date = null;

	private Integer hour = null;

	private boolean switchedScene = false;

	private boolean orderMade = false;

	private boolean occasionalCustomerMode = false;

	/*
	 * Activates occasional customer mode.
	 * In this mode the visitor amount is forced to 1 and the visitor selector is hidden.
	 */
	public void setOccasionalCustomerMode(boolean occasionalCustomerMode) {
		this.occasionalCustomerMode = occasionalCustomerMode;
		applyOccasionalCustomerMode();
	}

	private void applyOccasionalCustomerMode() {
		if (visitorNumberPicker == null || visitorNumberRow == null) {
			return;
		}

		if (occasionalCustomerMode) {
			setVisitorAmountToOne();

			visitorNumberRow.setVisible(false);
			visitorNumberRow.setManaged(false);

			guideCheckbox.setSelected(false);
			guideCheckbox.setVisible(false);
			guideCheckbox.setManaged(false);

			return;
		}

		visitorNumberRow.setVisible(true);
		visitorNumberRow.setManaged(true);

		guideCheckbox.setVisible(true);
		guideCheckbox.setManaged(true);
	}

	private void setVisitorAmountToOne() {
		if (visitorNumberPicker.getValueFactory() != null) {
			visitorNumberPicker.getValueFactory().setValue(CommonConstants.MIN_VISITOR_COUNT);
		}
	}

	private int getSelectedVisitorAmount() {
		if (occasionalCustomerMode) {
			return CommonConstants.MIN_VISITOR_COUNT;
		}

		return visitorNumberPicker.getValue();
	}

	private int getCurrentSubscriberId() {
		if (clientController == null ||
				clientController.getId() == null ||
				clientController.getId().trim().isEmpty()) {
			warningMessage("Invalid subscriber ID.");
			return -1;
		}

		try {
			return Integer.parseInt(clientController.getId().trim());
		} catch (NumberFormatException e) {
			warningMessage("Invalid subscriber ID.");
			return -1;
		}
	}

	@FXML
	void enterWaitingListButtonClicked(ActionEvent event) {
		if (clientController == null) {
			warningMessage("Client connection is not ready.");
			return;
		}

		if (parkPicker.getValue() == null) {
			warningMessage("No Park Was Picked");
			return;
		}

		if (datePicker.getValue() == null) {
			warningMessage("No Date Was Picked");
			return;
		}

		if (hourPicker.getValue() == null) {
			warningMessage("No Hour Was Picked");
			return;
		}

		int subscriberId = getCurrentSubscriberId();

		if (subscriberId == -1) {
			return;
		}

		LocalDateTime requestedOrderDate =
				datePicker.getValue().atTime(hourPicker.getValue(), 0);

		WaitingListMessage waitingListMessage = new WaitingListMessage(
				subscriberId,
				parkPicker.getValue(),
				requestedOrderDate,
				getSelectedVisitorAmount()
		);

		hideButtonBar();

		warningMessage("Sending waiting list request...");

		clientController.requestJoinWaitingList(waitingListMessage);
	}

	@FXML
	void rescheduleButtonClicked(ActionEvent event) {
		hideButtonBar();

		date = datePicker.getValue();
		hour = hourPicker.getValue();

		hourPicker.setDisable(false);
		datePicker.setDisable(false);
		makeOrderButton.setDisable(false);
	}

	@FXML
	void makeOrderButtonClicked(ActionEvent event) {
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

		int subscriberId = getCurrentSubscriberId();

		if (subscriberId == -1) {
			return;
		}

		if (clientController.getLoggedInSubscriberId() == null) {
			warningMessage("No logged-in customer was found.");
			System.out.println("No logged-in customer was found.");
			return;
		}

		if (occasionalCustomerMode) {
			setVisitorAmountToOne();
			guideCheckbox.setSelected(false);
		}

		makeOrderButton.setDisable(true);
		disableFields();

		Order order = new Order(
				datePicker.getValue(),
				getSelectedVisitorAmount(),
				subscriberId,
				parkPicker.getValue(),
				hourPicker.getValue(),
				email.getText()
		);

		if (guideCheckbox.isSelected() && !occasionalCustomerMode) {
			order.setOrderType(Order.ORDER_TYPE_ORGANIZED);
		} else {
			order.setOrderType(Order.ORDER_TYPE_PRIVATE);
			order.setGuideId(null);
		}

		orderMade = true;
		clientController.sendMessageToServer(new Message(order, Protocol.MAKE_ORDER));
	}

	@FXML
	void initialize() {
		assert buttonBar != null : "fx:id=\"buttonBar\" was not injected.";
		assert datePicker != null : "fx:id=\"datePicker\" was not injected.";
		assert email != null : "fx:id=\"email\" was not injected.";
		assert enterWaitingListButton != null : "fx:id=\"enterWaitingListButton\" was not injected.";
		assert errorLabel != null : "fx:id=\"errorLabel\" was not injected.";
		assert guideCheckbox != null : "fx:id=\"guideCheckbox\" was not injected.";
		assert hourPicker != null : "fx:id=\"hourPicker\" was not injected.";
		assert makeOrderButton != null : "fx:id=\"makeOrderButton\" was not injected.";
		assert parkPicker != null : "fx:id=\"parkPicker\" was not injected.";
		assert rescheduleButton != null : "fx:id=\"rescheduleButton\" was not injected.";
		assert visitorNumberRow != null : "fx:id=\"visitorNumberRow\" was not injected.";
		assert visitorNumberPicker != null : "fx:id=\"visitorNumberPicker\" was not injected.";

		hideButtonBar();

		makeOrderButton.setDisable(false);

		hourPicker.getItems().addAll(IntStream.rangeClosed(
				CommonConstants.MIN_HOUR,
				CommonConstants.MAX_HOUR)
				.boxed().collect(Collectors.toList()));

		visitorNumberPicker.setValueFactory(
				new SpinnerValueFactory.IntegerSpinnerValueFactory(
						CommonConstants.MIN_VISITOR_COUNT,
						CommonConstants.MAX_VISITOR_COUNT,
						ConstantsUI.START_VISITORS));

		applyOccasionalCustomerMode();

		datePicker.setEditable(false);
		datePicker.setDayCellFactory(picker -> new DateCell() {
			public void updateItem(LocalDate date, boolean empty) {
				super.updateItem(date, empty);

				if (empty) {
					return;
				}

				if (date.isBefore(LocalDate.now())) {
					setDisable(true);
					setStyle("-fx-background-color: #eeeeee;");
				}
			}
		});

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

	private void warningMessage(String message) {
		errorLabel.setText(message);
	}

	private void disableFields() {
		parkPicker.setDisable(true);
		datePicker.setDisable(true);
		hourPicker.setDisable(true);
		email.setDisable(true);

		if (!occasionalCustomerMode) {
			visitorNumberPicker.setDisable(true);
			guideCheckbox.setDisable(true);
		}
	}

	private void enableFields() {
		parkPicker.setDisable(false);
		datePicker.setDisable(false);
		hourPicker.setDisable(false);
		email.setDisable(false);

		if (!occasionalCustomerMode) {
			visitorNumberPicker.setDisable(false);
			guideCheckbox.setDisable(false);
		}

		applyOccasionalCustomerMode();
	}

	private void hideButtonBar() {
		buttonBar.setDisable(false);
		buttonBar.setVisible(false);
		buttonBar.setManaged(false);
	}

	private void showButtonBar() {
		buttonBar.setDisable(false);
		buttonBar.setVisible(true);
		buttonBar.setManaged(true);
	}

	public void loadParkNames(List<String> parkNames) {
		if (parkNames == null) {
			return;
		}

		parkPicker.getItems().addAll(parkNames);
	}

	public void requestActiveParkList() {
		clientController.sendMessageToServer(new Message(null, Protocol.GET_PARK_NAMES));
	}

	public void setClientController(ClientController clientController) {
		if (clientController == null) {
			return;
		}

		this.clientController = clientController;

		clientController.addMakeOrderObserver(this);
		clientController.addWaitingListObserver(this);

		requestActiveParkList();
	}

	public void setPrevScene(Scene prevScene) {
		if (prevScene == null) {
			return;
		}

		this.prevScene = prevScene;
		switchedScene = false;
	}

	public void setPrevController(Runnable prevController) {
		if (prevController == null) {
			return;
		}

		this.prevController = prevController;
	}

	@Override
	public void onParkNamesReceived(List<String> parkNames) {
		loadParkNames(parkNames);
	}

	@Override
	public void onMakeOrderServerResponse(Message message) {
		String declineMsg = null;

		if (!orderMade) {
			return;
		}

		if (message == null) {
			declineMsg = "Unknown";
			warningMessage("An unknown error occurred.");
			enableFields();
			makeOrderButton.setDisable(false);

		} else if (message.getType() == Protocol.MAKE_ORDER_SUCCESS) {
			Order order = (Order) message.getData();

			MakePopUp.makePopup("Order made!", null);
			MakePopUp.makePopup("EMAIL SIMULATION", "Order made! " + order.getEmail());

			if (order.getIsSubscribed()) {
				MakePopUp.makePopup("PHONE SIMULATION", "Order made! " + order.getPhoneNumber());
			}

			if (!switchedScene) {
				switchScene();
			}

			clientController.removeMakeOrderObserver(this);
			return;

		} else if (message.getType() == Protocol.MAKE_ORDER_FAIL_TIME) {
			declineMsg = "Visit time is unavailable";
			warningMessage("Visit time is unavailable, please enter waiting list or reschedule");
			showButtonBar();

		} else if (message.getType() == Protocol.MAKE_ORDER_FAIL_NOT_GUIDE) {
			declineMsg = "You are not a guide";
			warningMessage("You are not a guide, cannot book organized visit");
			guideCheckbox.setSelected(false);
			enableFields();
			guideCheckbox.setDisable(true);
			makeOrderButton.setDisable(false);

		} else if (message.getType() == Protocol.MAKE_ORDER_FAIL_NOT_SUBSCRIBED) {
			declineMsg = "Too many visitors";
			warningMessage("You are not subscribed, cannot book a visit for multiple people");
			setVisitorAmountToOne();
			enableFields();
			makeOrderButton.setDisable(false);

		} else if (message.getType() == Protocol.MAKE_ORDER_FAIL) {
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

	private void switchScene() {
		Stage stage = (Stage) makeOrderButton.getScene().getWindow();

		stage.setScene(prevScene);
		stage.setTitle("Order Table Page");

		switchedScene = true;

		if (prevController != null) {
			prevController.run();
		}

		stage.show();
	}

	@Override
	public void onJoinWaitingListResult(boolean success,
			WaitingListMessage waitingListMessage) {

		if (success && waitingListMessage != null) {
			warningMessage("Added to waiting list. Queue position: "
					+ waitingListMessage.getQueuePosition());
		} else {
			warningMessage("Could not enter waiting list.");
			showButtonBar();
		}
	}

	@Override
	public void onRejectWaitingOfferResult(boolean success,
			WaitingListMessage waitingListMessage) {

		if (success) {
			warningMessage("");
		} else {
			warningMessage("Failed to reject waiting list offer.");
		}
	}

	@Override
	public void onWaitingOffersReceived(boolean success,
			List<WaitingListMessage> offers) {
		// No action is needed here.
	}

	@Override
	public void onAcceptWaitingOfferResult(boolean success,
			WaitingListMessage waitingListMessage) {

		if (success) {
			warningMessage("");
		} else {
			warningMessage("Failed to accept waiting list offer.");
		}
	}

	@Override
	public void handleExit() {
		Platform.exit();
		System.exit(0);
	}

	@FXML
	private void handleBack(ActionEvent event) {
		if (prevScene == null) {
			warningMessage("Previous page is not available.");
			return;
		}

		if (clientController != null) {
			clientController.removeMakeOrderObserver(this);
			clientController.removeWaitingListObserver(this);
		}

		switchScene();
	}
}