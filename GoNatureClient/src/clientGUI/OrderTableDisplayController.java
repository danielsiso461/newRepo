package clientGUI;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import clientCommon.OrderObserver;
import clientController.ClientController;
import common.CancelOrderMessage;
import common.Order;
import common.UpdateMessage;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import common.Employee;

/**
 * this class is the UI controller for the order table page
 */
public class OrderTableDisplayController implements OrderObserver, Runnable {
	/** the client controller */
	private ClientController clientController;
	
	private Employee loggedInEmployee;
	
	private boolean parkManagerView = false;
	
	private boolean serviceRepresentativeView = false;
	
	private boolean customerView = false;

	/** a set that keeps track of which orders have requested an update */
	private Set<Integer> awaitingUpdate = new HashSet<>();

	/** Stores orders that already have a cancellation request waiting for a server response.*/
	private Set<Integer> awaitingCancel = new HashSet<>();
	/**
	 * ResourceBundle that was given to the FXMLLoader
	 */
	@FXML // ResourceBundle that was given to the FXMLLoader
	private ResourceBundle resources;
	/**
	 * URL location of the FXML file that was given to the FXMLLoader
	 */
	@FXML // URL location of the FXML file that was given to the FXMLLoader
	private URL location;
	/**
	 * the button used to enter the make order page
	 */
	@FXML // fx:id="makeOrderButton"
	private Button makeOrderButton; // Value injected by FXMLLoader
	/**
	 * the table of the user's orders
	 */
	@FXML // fx:id="orderTable"
	private TableView<Order> orderTable; // Value injected by FXMLLoader

	/** the table data */
	private ObservableList<Order> data = FXCollections.observableArrayList();

	/** the currently selected row in the table */
	private Order selectedRow = null;
	/**
	 * the confCode column of the order table
	 */
	@FXML // fx:id="confCode"
	private TableColumn<Order, Integer> confCode; // Value injected by FXMLLoader
	/**
	 * the order's Date column of the order table
	 */
	@FXML // fx:id="orderDate"
	private TableColumn<Order, LocalDate> orderDate; // Value injected by FXMLLoader
	/**
	 * the order's id column of the order table
	 */
	@FXML // fx:id="orderId"
	private TableColumn<Order, Integer> orderId; // Value injected by FXMLLoader
	/**
	 * the order's placement Date column of the order table
	 */
	@FXML // fx:id="placementDate"
	private TableColumn<Order, LocalDate> placementDate; // Value injected by FXMLLoader
	/**
	 * the order's user id column of the order table
	 */
	@FXML // fx:id="userId"
	private TableColumn<Order, Integer> userId; // Value injected by FXMLLoader
	/**
	 * the order's visitor number column of the order table
	 */
	@FXML // fx:id="visitorNumber"
	private TableColumn<Order, Integer> visitorNumber; // Value injected by FXMLLoader
	/**
	 * the order's number column of the order table
	 * it represents the order's number in the table
	 */
	@FXML // fx:id="orderNumber"
	private TableColumn<Order, Integer> orderNumber; // Value injected by FXMLLoader
	/**
	 * the order's status column of the order table
	 */
	@FXML // fx:id="orderStatus"
	private TableColumn<Order, String> orderStatus; // Value injected by FXMLLoader
	/**
	 * a notification label to give feedback to the user
	 */
	@FXML // fx:id="notifLabel"
	private Label notifLabel; // Value injected by FXMLLoader
	/**
	 * an update button to update orders
	 */
	@FXML // fx:id="updateButton"
	private Button updateButton; // Value injected by FXMLLoader
	/**
	 * a cancel button to cancel orders
	 */
	@FXML // fx:id="cancelButton"
	private Button cancelButton; // Value injected by FXMLLoader
	/**
	 * a waiting list button to watch the waiting list of the user
	 */
	@FXML // fx:id="waitingListButton"
	private Button waitingListButton; // Value injected by FXMLLoader
	
	@FXML
	private Button backButton;

	/**
	 * this method handles click the update button
	 * it loads the update page, 
	 * puts selected order into a waiting list and 
	 * hides current screen
	 * 
	 * @param event 	the update button click
	 * @throws Exception in case loading the update page fails
	 */
	@FXML
	void updateButtonClick(ActionEvent event) throws Exception {
		// Make sure the user selected an order before trying to update it.
		if (selectedRow == null) {
			notifLabel.setTextFill(Color.RED);
			notifLabel.setText("Please select an order to update.");
			return;
		}

		// launch the order update screen
		FXMLLoader loader = new FXMLLoader(getClass().getResource(ConstantsUI.updatePage));
		Pane root = loader.load();

		OrderUpdateController OrderUpdatePageController = loader.getController();

		addOrderToUpdateWaitingList(selectedRow.getOrderId());

		OrderUpdatePageController.setClientController(clientController);
		OrderUpdatePageController.setOrderData(
				selectedRow.getOrderId(),
				selectedRow.getOrderDate(),
				selectedRow.getVisitorNumber(),
				selectedRow.getOrderNumber(),
				selectedRow.getUserId().toString()
		);

		Stage prevStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		prevStage.hide();

		OrderUpdatePageController.setPrevStage(prevStage);
		OrderUpdatePageController.setPrevController(this);

		Scene scene = new Scene(root);

		Stage primaryStage = new Stage();
		primaryStage.setTitle("Order Update Page");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	/**
	 * this method handles clicking the make order button.
	 * It loads the make order page and hides the current screen.
	 * 
	 * @param event the make order button click event
	 */
	@FXML
	void makeOrderButtonClick(ActionEvent event) {
		Stage stage = (Stage) makeOrderButton.getScene().getWindow();

		// load the FXML file of the make order page
		FXMLLoader loader = new FXMLLoader(getClass().getResource(ConstantsUI.makeOrderPage));
		Parent root = null;

		try {
			root = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
			Platform.exit();
			System.exit(1);
		}

		// get controller
		MakeOrderPageController controller = loader.getController();
		controller.setClientController(clientController);
		controller.setPrevScene(stage.getScene());
		controller.setPrevController(this);

		// show UI
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setTitle("Make Order Page");
		stage.show();
	}

	/**
	 * this method handles clicking the waiting list button.
	 * It loads the waiting list page and keeps the same client controller.
	 * 
	 * @param event the waiting list button click event
	 */
	@FXML
	void waitingListButtonClick(ActionEvent event) {
		Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

		// load the FXML file of the waiting list page
		FXMLLoader loader = new FXMLLoader(getClass().getResource(ConstantsUI.waitingListPage));
		Parent root = null;

		try {
			root = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
			Platform.exit();
			System.exit(1);
		}

		// get controller
		WaitingListController controller = loader.getController();
		controller.setClientController(clientController);
		controller.setPrevScene(stage.getScene());
		
		// show UI
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setTitle("Waiting List Page");
		stage.show();
	}

	/**
	 * Handles clicking the Cancel Order button.
	 *
	 * This method validates that an order was selected, prevents duplicate
	 * cancellation requests for the same order, creates a cancellation message,
	 * and sends it to the client controller.
	 *
	 * The order will not be deleted from the database. The server should update
	 * the order status to "cancelled" and keep the order for reports and history.
	 *
	 * @param event the cancel button click event
	 */
	@FXML
	void cancelButtonClick(ActionEvent event) {
		// Make sure the user selected an order before trying to cancel it.
		if (selectedRow == null) {
			notifLabel.setTextFill(Color.RED);
			notifLabel.setText("Please select an order to cancel.");
			return;
		}

		int orderId = selectedRow.getOrderId();

		// Prevent sending another cancellation request for the same order
		// before receiving a response from the server.
		if (awaitingCancel.contains(orderId)) {
			notifLabel.setTextFill(Color.RED);
			notifLabel.setText("A cancellation request is already waiting for this order.");
			return;
		}

		// Create the cancellation request data that will be sent to the server.
		CancelOrderMessage cancelOrderMessage = new CancelOrderMessage(
				selectedRow.getOrderId(),
				selectedRow.getOrderNumber(),
				selectedRow.getUserId().toString(),
				"Visitor cancelled the order"
		);

		// Mark this order as waiting for a cancellation response.
		awaitingCancel.add(orderId);

		// Disable the cancel button until the server responds.
		cancelButton.setDisable(true);

		// Send the cancellation request through ClientController.
		clientController.requestCancelOrder(cancelOrderMessage);

		notifLabel.setTextFill(Color.BLUE);
		notifLabel.setText("Cancellation request was sent for order ID: " + orderId);
	}
	/**
	 * this method handles the selection of a new row
	 * @param obs the observable value
	 * @param oldSelection the old order selected
	 * @param newSelection the new order selected
	 */
	private void handleRowSelection(ObservableValue<? extends Order> obs,
			Order oldSelection, Order newSelection) {
		if (newSelection != null) {
			onRowSelected(newSelection);
		}
	}

	/**
	 * this method handles updating which row is selected
	 * and whether the update and cancel buttons should be available for it
	 * 
	 * @param row 	the selected row		
	 */
	private void onRowSelected(Order row) {
		// Disable action buttons by default until we verify that the selected order can be changed.
		updateButton.setDisable(true);
		cancelButton.setDisable(true);

		selectedRow = row;
		LocalDate orderDate = row.getOrderDate();

		// An order with a past visit date should not be updated or cancelled from this screen.
		boolean expiredDate = orderDate.isBefore(LocalDate.now());

		// Orders that already reached a final status should not be changed again.
		String status = row.getOrderStatus();
		boolean finalStatus =
				"cancelled".equalsIgnoreCase(status) ||
				"expired".equalsIgnoreCase(status) ||
				"completed".equalsIgnoreCase(status) ||
				"no_show".equalsIgnoreCase(status);

		// Prevent the same order from being updated while an update request is already waiting.
		boolean awaitingCurrentUpdate = awaitingUpdate.contains(row.getOrderId());

		// Prevent the same order from being cancelled more than once while waiting for a server response.
		boolean awaitingCurrentCancel = awaitingCancel.contains(row.getOrderId());

		// The selected order can be changed only if it is still relevant and not already being handled.
		boolean canChangeOrder = !expiredDate && !finalStatus && !awaitingCurrentUpdate && !awaitingCurrentCancel;

		updateButton.setDisable(!canChangeOrder);
		cancelButton.setDisable(!canChangeOrder);
	}
	/**
	 * this method initializes the order table display page controller
	 */
	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		assert confCode != null : "fx:id=\"confCode\" was not injected: check your FXML file 'Untitled'.";
		assert orderDate != null : "fx:id=\"orderDate\" was not injected: check your FXML file 'Untitled'.";
		assert orderId != null : "fx:id=\"orderId\" was not injected: check your FXML file 'Untitled'.";
		assert orderTable != null : "fx:id=\"orderTable\" was not injected: check your FXML file 'Untitled'.";
		assert placementDate != null : "fx:id=\"placementDate\" was not injected: check your FXML file 'Untitled'.";
		assert updateButton != null : "fx:id=\"updateButton\" was not injected: check your FXML file 'Untitled'.";
		assert makeOrderButton != null : "fx:id=\"makeOrderButton\" was not injected: check your FXML file 'OrderTableDisplayPage.fxml'.";
		assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'OrderTableDisplayPage.fxml'.";
		assert waitingListButton != null : "fx:id=\"waitingListButton\" was not injected: check your FXML file 'OrderTableDisplayPage.fxml'.";
		assert userId != null : "fx:id=\"userId\" was not injected: check your FXML file 'Untitled'.";
		assert visitorNumber != null : "fx:id=\"visitorNumber\" was not injected: check your FXML file 'Untitled'.";
		assert notifLabel != null : "fx:id=\"notifLabel\" was not injected: check your FXML file 'OrderTableDisplayPage.fxml'.";
		assert orderStatus != null : "fx:id=\"orderStatus\" was not injected: check your FXML file 'OrderTableDisplayPage.fxml'.";
		assert backButton != null : "fx:id=\"backButton\" was not injected: check your FXML file 'OrderTableDisplayPage.fxml'.";

		// Disable action buttons until the user selects an order from the table.
		updateButton.setDisable(true);
		cancelButton.setDisable(true);

		// sets where the table columns get their data from (of the given object) 
		orderNumber.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
		orderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
		orderDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
		visitorNumber.setCellValueFactory(new PropertyValueFactory<>("visitorNumber"));
		confCode.setCellValueFactory(new PropertyValueFactory<>("confCode"));
		userId.setCellValueFactory(new PropertyValueFactory<>("userId"));
		placementDate.setCellValueFactory(new PropertyValueFactory<>("placementDate"));
		orderStatus.setCellValueFactory(new PropertyValueFactory<>("orderStatus"));

		// sets the table to get it's data from the ObservableList set up to hold order
		// data
		orderTable.setItems(data);

		// adds listener to row selection
		orderTable.getSelectionModel().selectedItemProperty().addListener(this::handleRowSelection);
	}

	/**
	 * this method handles closing the client program if the server 
	 * closed the user connection
	 */
	public void handleExit() {
		Platform.runLater(() -> {
			Platform.exit();
			System.exit(0);
		});
	}

	/**
	 * this method handles disconnecting the user and closing the program
	 * once the user clicked the red X button
	 */
	@Override
	public void run() {
		Stage stage = (Stage) orderTable.getScene().getWindow();
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				event.consume();
				userIssuedDisconnect();
			}
		});
	}

	/**
	 * this method disconnects the user from the server
	 */
	private void userIssuedDisconnect() {
		clientController.setUserIssuedDisconnect(true);
		clientController.disconnectFromServer();
	}

	/**
	 * this method sets the order data to the ObservableList the tableView is connected to
	 * 
	 * @param rows		the order data
	 */
	public void setData(List<Order> rows) {
		Platform.runLater(() -> {
			data.setAll(rows);
		});
	}

	/**
	 * this method sets the ClientController on the UI side
	 * add this UI controller to the ClientController observer List
	 * 
	 * @param clientController 		the ClientController
	 */
	public void setClientController(ClientController clientController) {
		this.clientController = clientController;
		this.clientController.addObserver(this);
	}
	
	public void setLoggedInEmployee(Employee employee) {
		this.loggedInEmployee = employee;
	}

	/**
	 * this method adds orders to the update waiting list
	 * 
	 * @param orderNumber 	the number of the order to add to the waiting list
	 */
	private void addOrderToUpdateWaitingList(int orderNumber) {
		awaitingUpdate.add(orderNumber);
	}

	/**
	 * this method removes orders from the update waiting list
	 * 
	 * @param orderNumber 	the number of the order to remove from the waiting list
	 */
	protected void removeOrderFromUpdateWaitingList(int orderNumber) {
		awaitingUpdate.remove(orderNumber);
	}

	/**
	 * this method sets the object holding the orders to the data received from the server
	 * 
	 * @param rows 	the order list
	 */
	@Override
	public void onOrdersReceived(List<Order> rows) {
		setData(rows);
	}
	/**
	 * this method handles updating the UI upon reply from 
	 * the server to requesting an order update
	 * 
	 * @param success 			whether the update was successful
	 * @param updateMessage 	the update request's data
	 */
	@Override
	public void onUpdateResult(boolean success, UpdateMessage updateMessage) {
		if (updateMessage == null) {
			System.out.println("Error: invalid data");
			return;
		}

		int updatedOrderId = updateMessage.getOrderId();

		if (success) {
			notifLabel.setTextFill(Color.GREEN);
			notifLabel.setText("Order update for order ID: " + updatedOrderId + " succeeded.");

			boolean found = false;

			for (int i = 0; i < data.size(); i++) {
				Order updatedRow = data.get(i);

				if (updatedRow.getOrderId() != null && updatedRow.getOrderId() == updatedOrderId) {
					if (updateMessage.getUpdateDate() != null) {
						updatedRow.setOrderDate(updateMessage.getUpdateDate());
					}

					if (updateMessage.getNumberOfVisitors() > 0) {
						updatedRow.setNumberOfVisitors(updateMessage.getNumberOfVisitors());
					}

					data.set(i, updatedRow);
					found = true;
					break;
				}
			}

			if (!found) {
				System.out.println("Updated order was not found in the local table: " + updatedOrderId);
			}
		} else {
			notifLabel.setTextFill(Color.RED);
			notifLabel.setText("Order update for order ID: " + updatedOrderId + " failed.");
		}

		removeOrderFromUpdateWaitingList(updatedOrderId);

		if (selectedRow != null) {
			onRowSelected(selectedRow);
		}
	}

	/**
	 * Handles the server response after an order cancellation request.
	 *
	 * If the cancellation succeeds, the selected order is kept in the table
	 * but its status is changed to "cancelled". The order is not deleted,
	 * because cancelled orders are still needed for reports and history.
	 *
	 * @param success            true if the cancellation was completed successfully
	 * @param cancelOrderMessage the cancellation request data returned by the server
	 */
	@Override
	public void onCancelResult(boolean success, CancelOrderMessage cancelOrderMessage) {
		if (cancelOrderMessage == null) {
			System.out.println("Error: invalid cancellation data");
			return;
		}

		int cancelledOrderId = cancelOrderMessage.getOrderId();

		if (success) {
			notifLabel.setTextFill(Color.GREEN);
			notifLabel.setText("Order cancellation for order ID: " + cancelledOrderId + " succeeded.");

			// Remove the matching row locally after the server confirms the cancellation.
			// The cancelled order stays in the database, but it is not displayed to the visitor.
			for (int i = 0; i < data.size(); i++) {
				Order row = data.get(i);

				if (row.getOrderId() == cancelledOrderId) {
					data.remove(i);
					break;
				}
			}
		} else {
			notifLabel.setTextFill(Color.RED);
			notifLabel.setText("Order cancellation for order ID: " + cancelledOrderId + " failed.");
		}

		// Remove the order from the local waiting list so the UI can be used again.
		awaitingCancel.remove(cancelledOrderId);

		// Re-check the selected row status and update the buttons availability.
		if (selectedRow != null) {
			onRowSelected(selectedRow);
		}
	}
	
	/**
	 * Loads orders for a specific park.
	 * 
	 * This method is used when a park manager opens the order table screen.
	 * 
	 * @param parkId the park ID assigned to the park manager
	 */
	public void loadOrdersForPark(int parkId) {
		if (clientController == null) {
			notifLabel.setTextFill(Color.RED);
			notifLabel.setText("Client is not connected to server.");
			return;
		}

		if (parkId <= 0) {
			notifLabel.setTextFill(Color.RED);
			notifLabel.setText("Invalid park ID.");
			return;
		}

		notifLabel.setTextFill(Color.BLUE);
		notifLabel.setText("Loading park orders...");

		clientController.requestOrdersByParkId(parkId);
	}
	
	/**
	 * Configures the order table screen for park manager view.
	 * 
	 * In this mode the park manager only views park orders.
	 */
	public void configureForParkManagerView() {
		parkManagerView = true;
		if (makeOrderButton != null) {
			makeOrderButton.setVisible(false);
			makeOrderButton.setManaged(false);
		}

		if (updateButton != null) {
			updateButton.setVisible(false);
			updateButton.setManaged(false);
		}

		if (cancelButton != null) {
			cancelButton.setVisible(false);
			cancelButton.setManaged(false);
		}

		if (waitingListButton != null) {
			waitingListButton.setVisible(false);
			waitingListButton.setManaged(false);
		}
	}
	
	public void configureForServiceRepresentativeView() {
		serviceRepresentativeView = true;

		if (makeOrderButton != null) {
			makeOrderButton.setVisible(false);
			makeOrderButton.setManaged(false);
		}

		if (updateButton != null) {
			updateButton.setVisible(false);
			updateButton.setManaged(false);
		}

		if (cancelButton != null) {
			cancelButton.setVisible(false);
			cancelButton.setManaged(false);
		}

		if (waitingListButton != null) {
			waitingListButton.setVisible(false);
			waitingListButton.setManaged(false);
		}
	}
	
	public void configureForCustomerView() {
		customerView = true;
		parkManagerView = false;
		serviceRepresentativeView = false;
	}

	public void loadAllOrdersForServiceRepresentative() {
		if (clientController == null) {
			notifLabel.setTextFill(Color.RED);
			notifLabel.setText("Client is not connected to server.");
			return;
		}

		notifLabel.setTextFill(Color.BLUE);
		notifLabel.setText("Loading customer orders...");

		clientController.requestAllOrdersForServiceRepresentative();
	}

	/**
	 * This method fulfills the OrderObserver contract.
	 * It is used to add a new order to the order table.
	 *
	 * @param o the order to add to the table
	 */
	@Override
	public void addOrder(Order o) {
		Platform.runLater(() -> {
			o.setOrderNumber(data.size() + 1);
			data.add(o);
		});
	}
	
	@FXML
	void backButtonClick(ActionEvent event) {
		try {
			if (parkManagerView) {
				FXMLLoader loader = new FXMLLoader(
						getClass().getResource("/clientGUI/ParkManagerHomePage.fxml")
				);

				Parent root = loader.load();

				ParkManagerHomePageController controller = loader.getController();
				controller.setClientController(clientController);
				controller.setLoggedInEmployee(loggedInEmployee);

				Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
				stage.setTitle("Park Manager Dashboard");
				stage.setScene(new Scene(root));
				stage.show();

				return;
			}
			
			if (serviceRepresentativeView) {
				FXMLLoader loader = new FXMLLoader(
						getClass().getResource("/clientGUI/ServiceRepresentativeHomePage.fxml")
				);

				Parent root = loader.load();

				ServiceRepresentativeHomePageController controller = loader.getController();
				controller.setClientController(clientController);
				controller.setLoggedInEmployee(loggedInEmployee);

				Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
				stage.setTitle("Service Representative Dashboard");
				stage.setScene(new Scene(root));
				stage.show();

				return;
			}
			
			if (customerView) {
				FXMLLoader loader = new FXMLLoader(
						getClass().getResource("/clientGUI/CustomerAccess.fxml")
				);

				Parent root = loader.load();

				CustomerAccessController controller = loader.getController();
				controller.setClientController(clientController);

				Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
				stage.setTitle("Customer Access");
				stage.setScene(new Scene(root));
				stage.show();

				return;
			}

			notifLabel.setTextFill(Color.RED);
			notifLabel.setText("Back is not configured for this screen mode.");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
