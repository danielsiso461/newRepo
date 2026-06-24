package clientGUI;

import java.util.List;
import java.util.function.Consumer;

import clientCommon.ParkObserver;
import common.Message;
import common.Park;
import common.Protocol;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;


/**
 * This class is the JavaFX controller for the park selection screen.
 * 
 * The screen displays public park information received from the server.
 * It is used by the client before creating an order, so the user can choose a
 * park from the list of active parks.
 * 
 * The controller works with Park objects only, meaning it receives only
 * public park data and not internal management data such as max capacity or
 * reserved places for unplanned visitors.
 */
public class ParkSelectionPageController implements ParkObserver {

	/**
	 * The table that displays the parks.
	 */
	@FXML
	private TableView<Park> parksTable;

	/**
	 * The column that displays the park ID.
	 */
	@FXML
	private TableColumn<Park, Integer> parkIdColumn;

	/**
	 * The column that displays the park name.
	 */
	@FXML
	private TableColumn<Park, String> parkNameColumn;

	/**
	 * The column that displays the estimated visit duration.
	 */
	@FXML
	private TableColumn<Park, Double> estimatedDurationColumn;

	/**
	 * The column that displays the full entry price.
	 */
	@FXML
	private TableColumn<Park, Double> fullEntryPriceColumn;

	/**
	 * Button used to request the updated park list from the server.
	 */
	@FXML
	private Button refreshButton;

	/**
	 * Button used to select the currently selected park.
	 */
	@FXML
	private Button selectParkButton;

	/**
	 * Button used to go back to the previous screen.
	 */
	@FXML
	private Button backButton;

	/**
	 * Label used to show messages to the user.
	 */
	@FXML
	private Label messageLabel;

	/**
	 * The list of parks displayed in the table.
	 */
	private ObservableList<Park> parks = FXCollections.observableArrayList();

	/**
	 * A handler used to send messages from this controller to the server.
	 * 
	 * This is injected from the main client controller after loading the FXML.
	 */
	private Consumer<Message> serverRequestHandler;

	/**
	 * The park selected by the user.
	 */
	private Park selectedPark;

	/**
	 * Initializes the park selection screen.
	 * 
	 * This method is called automatically by JavaFX after the FXML fields are loaded.
	 */
	@FXML
	private void initialize() {
		parkIdColumn.setCellValueFactory(new PropertyValueFactory<>("parkId"));
		parkNameColumn.setCellValueFactory(new PropertyValueFactory<>("parkName"));
		estimatedDurationColumn.setCellValueFactory(new PropertyValueFactory<>("estimatedVisitDurationHours"));
		fullEntryPriceColumn.setCellValueFactory(new PropertyValueFactory<>("fullEntryPrice"));

		parksTable.setItems(parks);

		messageLabel.setText("");
	}

	/**
	 * Sets the handler used for sending requests to the server.
	 * 
	 * The main client controller should call this method after loading this screen.
	 * After the handler is set, the parks list is loaded automatically.
	 * 
	 * @param serverRequestHandler the function that sends messages to the server
	 */
	public void setServerRequestHandler(Consumer<Message> serverRequestHandler) {
		this.serverRequestHandler = serverRequestHandler;

		refreshParks();
	}

	/**
	 * Requests the active parks list from the server.
	 * 
	 * This method is called when the user clicks the refresh button.
	 */
	@FXML
	private void refreshParks() {
		messageLabel.setText("");

		if (serverRequestHandler == null) {
			messageLabel.setText("Server connection is not ready.");
			return;
		}

		serverRequestHandler.accept(new Message(null, Protocol.GET_ACTIVE_PARKS));
		messageLabel.setText("Loading parks...");
	}

	/**
	 * Selects the park currently selected in the table.
	 * 
	 * This method can later be connected to the order creation screen, so the chosen
	 * park will be passed into the order form.
	 */
	@FXML
	private void selectPark() {
		selectedPark = parksTable.getSelectionModel().getSelectedItem();

		if (selectedPark == null) {
			messageLabel.setText("Please select a park.");
			return;
		}

		messageLabel.setText("Selected park: " + selectedPark.getParkName());

		/*
		 * Later, when the order screen is ready, this is the place to pass selectedPark
		 * to the order creation controller.
		 */
	}

	/**
	 * Goes back to the previous screen.
	 * 
	 * This method should be connected later to your screen navigation logic.
	 */
	@FXML
	private void goBack() {
		messageLabel.setText("Back button clicked.");

		/*
		 * Later, replace this with your actual screen navigation code.
		 */
	}

	/**
	 * Handles messages received from the server that are relevant to this screen.
	 * 
	 * This method should be called by the main client message handler when a message
	 * arrives from the server.
	 * 
	 * @param message the message received from the server
	 */
	@SuppressWarnings("unchecked")
	public void handleServerMessage(Message message) {
		if (message == null || message.getType() == null) {
			return;
		}

		if (message.getType() == Protocol.ACTIVE_PARKS_RESULT || message.getType() == Protocol.PARKS_UPDATED) {
			List<Park> updatedParks = (List<Park>) message.getData();

			Platform.runLater(() -> setParks(updatedParks));
		}
	}

	/**
	 * Updates the table with a new list of parks.
	 * 
	 * @param updatedParks the updated list of public park information
	 */
	public void setParks(List<Park> updatedParks) {
		parks.clear();

		if (updatedParks != null) {
			parks.addAll(updatedParks);
		}

		messageLabel.setText("Loaded " + parks.size() + " parks.");
	}

	/**
	 * Returns the park selected by the user.
	 * 
	 * @return the selected Park object, or null if no park was selected
	 */
	public Park getSelectedPark() {
		return selectedPark;
	}

	/**
	 * This method is called when park data is received from the server.
	 * 
	 * @param parks the active parks received from the server
	 */
	@Override
	public void onParksReceived(List<Park> parks) {
		setParks(parks);
	}
}