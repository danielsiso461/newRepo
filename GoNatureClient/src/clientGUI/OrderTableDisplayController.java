
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
import common.Employee;
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

/*
 * This class is the UI controller for the order table page.
 */
/**
 * Controller for the order table page.
 *
 * This screen displays orders and allows the user to make, update,
 * cancel, or view orders according to the current screen mode.
 */
@SuppressWarnings("deprecation")
public class OrderTableDisplayController implements OrderObserver, Runnable {

	/**
	 * the client controller used to communicate with the server
	 */
	private ClientController clientController;

	/**
	 * the currently logged-in employee
	 */
	private Employee loggedInEmployee;

	/**
	 * indicates whether the screen is shown for a park manager
	 */
	private boolean parkManagerView = false;

	/**
	 * indicates whether the screen is shown for a service representative
	 */
	private boolean serviceRepresentativeView = false;

	/**
	 * indicates whether the screen is shown for a customer
	 */
	private boolean customerView = false;

	/**
	 * indicates whether the screen is shown for an occasional customer
	 */
	private boolean occasionalCustomerView = false;

	/**
	 * the set of order IDs that are waiting for update results
	 */
	private Set<Integer> awaitingUpdate = new HashSet<>();

	/**
	 * the set of order IDs that are waiting for cancellation results
	 */
	private Set<Integer> awaitingCancel = new HashSet<>();

	/**
	 * the observable list of orders displayed in the table
	 */
	private ObservableList<Order> data = FXCollections.observableArrayList();

	/**
	 * the currently selected order row
	 */
	private Order selectedRow = null;

	/**
	 * the resource bundle used by the FXML loader
	 */
	@FXML
	private ResourceBundle resources;

	/**
	 * the location URL used by the FXML loader
	 */
	@FXML
	private URL location;

	/**
	 * the table view that displays the orders
	 */
	@FXML
	private TableView<Order> orderTable;

	/**
	 * the column that displays the order number
	 */
	@FXML
	private TableColumn<Order, Integer> orderNumber;

	/**
	 * the column that displays the order ID
	 */
	@FXML
	private TableColumn<Order, Integer> orderId;

	/**
	 * the column that displays the park ID
	 */
	@FXML
	private TableColumn<Order, Integer> parkIdColumn;

	/**
	 * the column that displays the order date
	 */
	@FXML
	private TableColumn<Order, LocalDate> orderDate;

	/**
	 * the column that displays the number of visitors
	 */
	@FXML
	private TableColumn<Order, Integer> visitorNumber;

	/**
	 * the column that displays the confirmation code
	 */
	@FXML
	private TableColumn<Order, Integer> confCode;

	/**
	 * the column that displays the user ID
	 */
	@FXML
	private TableColumn<Order, Integer> userId;

	/**
	 * the column that displays the placement date
	 */
	@FXML
	private TableColumn<Order, LocalDate> placementDate;

	/**
	 * the column that displays the order status
	 */
	@FXML
	private TableColumn<Order, String> orderStatus;

	/**
	 * the button used to update an order
	 */
	@FXML
	private Button updateButton;

	/**
	 * the button used to make a new order
	 */
	@FXML
	private Button makeOrderButton;

	/**
	 * the button used to cancel an order
	 */
	@FXML
	private Button cancelButton;

	/**
	 * the button used to open the waiting list page
	 */
	@FXML
	private Button waitingListButton;

	/**
	 * the button used to return to the previous screen
	 */
	@FXML
	private Button backButton;
	
	/**
	 * the button used to open the customer details page
	 */
	@FXML
	private Button myDetailsButton;

	/**
	 * the label used to display status messages
	 */
	@FXML
	private Label notifLabel;

	/**
	 * Initializes the order table page.
	 *
	 * This method sets the table columns, hides action buttons,
	 * and prepares the row selection listener.
	 */
	@FXML
	void initialize() {
		assert confCode != null : "fx:id=\"confCode\" was not injected.";
		assert orderDate != null : "fx:id=\"orderDate\" was not injected.";
		assert orderId != null : "fx:id=\"orderId\" was not injected.";
		assert parkIdColumn != null : "fx:id=\"parkIdColumn\" was not injected.";
		assert orderTable != null : "fx:id=\"orderTable\" was not injected.";
		assert placementDate != null : "fx:id=\"placementDate\" was not injected.";
		assert updateButton != null : "fx:id=\"updateButton\" was not injected.";
		assert makeOrderButton != null : "fx:id=\"makeOrderButton\" was not injected.";
		assert cancelButton != null : "fx:id=\"cancelButton\" was not injected.";
		assert waitingListButton != null : "fx:id=\"waitingListButton\" was not injected.";
		assert userId != null : "fx:id=\"userId\" was not injected.";
		assert visitorNumber != null : "fx:id=\"visitorNumber\" was not injected.";
		assert notifLabel != null : "fx:id=\"notifLabel\" was not injected.";
		assert orderStatus != null : "fx:id=\"orderStatus\" was not injected.";
		assert backButton != null : "fx:id=\"backButton\" was not injected.";
		assert myDetailsButton != null : "fx:id=\"myDetailsButton\" was not injected: check your FXML file 'OrderTableDisplayPage.fxml'.";

		orderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		hideButton(updateButton);
		hideButton(cancelButton);

		orderNumber.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
		orderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
		parkIdColumn.setCellValueFactory(new PropertyValueFactory<>("parkId"));
		orderDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
		visitorNumber.setCellValueFactory(new PropertyValueFactory<>("visitorNumber"));
		confCode.setCellValueFactory(new PropertyValueFactory<>("confCode"));
		userId.setCellValueFactory(new PropertyValueFactory<>("userId"));
		placementDate.setCellValueFactory(new PropertyValueFactory<>("placementDate"));
		orderStatus.setCellValueFactory(new PropertyValueFactory<>("orderStatus"));

		orderTable.setItems(data);
		orderTable.getSelectionModel().selectedItemProperty().addListener(this::handleRowSelection);

		showInfo("Ready");
	}

	/**
	 * Handles the click on the update button.
	 *
	 * This method opens the order update page for the selected order.
	 *
	 * @param event the button click event
	 * @throws Exception if the update page cannot be opened
	 */
	@FXML
	void updateButtonClick(ActionEvent event) throws Exception {
		if (selectedRow == null) {
			showError("Please select an order to update.");
			return;
		}

		FXMLLoader loader = new FXMLLoader(getClass().getResource(ConstantsUI.updatePage));
		Pane root = loader.load();

		OrderUpdateController orderUpdatePageController = loader.getController();

		addOrderToUpdateWaitingList(selectedRow.getOrderId());

		orderUpdatePageController.setClientController(clientController);
		orderUpdatePageController.setOrderData(
				selectedRow.getOrderId(),
				selectedRow.getOrderDate(),
				selectedRow.getVisitorNumber(),
				selectedRow.getOrderNumber(),
				selectedRow.getUserId().toString()
		);

		Stage prevStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		prevStage.hide();

		orderUpdatePageController.setPrevStage(prevStage);
		orderUpdatePageController.setPrevController(this);

		Scene scene = new Scene(root);

		Stage primaryStage = new Stage();
		primaryStage.setTitle("Order Update Page");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	/**
	 * Handles the click on the make order button.
	 *
	 * This method opens the make order page.
	 *
	 * @param event the button click event
	 */
	@FXML
	void makeOrderButtonClick(ActionEvent event) {
		try {
			System.out.println("Make New Order button clicked");

			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/MakeOrderPage.fxml")
			);

			Parent root = loader.load();

			MakeOrderPageController controller = loader.getController();
			controller.setClientController(clientController);

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

			controller.setPrevScene(stage.getScene());
			controller.setPrevController(this);
			controller.setOccasionalCustomerMode(occasionalCustomerView);

			stage.setScene(new Scene(root));
			stage.setTitle("Make Order Page");
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
			showError("Failed to open Make Order page.");
		}
	}

	/**
	 * Handles the click on the waiting list button.
	 *
	 * This method opens the waiting list page.
	 *
	 * @param event the button click event
	 */
	@FXML
	void waitingListButtonClick(ActionEvent event) {
		Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

		URL waitingListUrl = getClass().getResource("/clientGUI/WaitingListPage.fxml");

		if (waitingListUrl == null) {
			notifLabel.setTextFill(Color.RED);
			notifLabel.setText("Waiting List page was not found.");
			System.out.println("ERROR: /clientGUI/WaitingListPage.fxml was not found.");
			return;
		}

		FXMLLoader loader = new FXMLLoader(waitingListUrl);
		Parent root = null;

		try {
			root = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
			notifLabel.setTextFill(Color.RED);
			notifLabel.setText("Failed to open Waiting List page.");
			return;
		}

		WaitingListController controller = loader.getController();
		controller.setClientController(clientController);
		controller.setPrevScene(stage.getScene());

		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setTitle("Waiting List Page");
		stage.show();
	}

	/**
	 * Handles the click on the cancel button.
	 *
	 * This method sends an order cancellation request to the server.
	 *
	 * @param event the button click event
	 */
	@FXML
	void cancelButtonClick(ActionEvent event) {
		if (selectedRow == null) {
			showError("Please select an order to cancel.");
			return;
		}

		int orderId = selectedRow.getOrderId();

		if (awaitingCancel.contains(orderId)) {
			showError("A cancellation request is already waiting for this order.");
			return;
		}

		CancelOrderMessage cancelOrderMessage = new CancelOrderMessage(
				selectedRow.getOrderId(),
				selectedRow.getOrderNumber(),
				selectedRow.getUserId().toString(),
				"Visitor cancelled the order"
		);

		awaitingCancel.add(orderId);
		hideButton(cancelButton);

		clientController.requestCancelOrder(cancelOrderMessage);

		showInfo("Cancellation request was sent for order ID: " + orderId);
	}

	/**
	 * Handles row selection in the order table.
	 *
	 * @param obs the observable selection value
	 * @param oldSelection the previous selected order
	 * @param newSelection the new selected order
	 */
	private void handleRowSelection(ObservableValue<? extends Order> obs,
			Order oldSelection, Order newSelection) {

		if (newSelection != null) {
			onRowSelected(newSelection);
		} else {
			selectedRow = null;
			hideButton(updateButton);
			hideButton(cancelButton);
		}
	}

	/**
	 * Handles the selected order row.
	 *
	 * @param row the selected order row
	 */
	private void onRowSelected(Order row) {
		selectedRow = row;

		if (!customerView) {
			hideButton(updateButton);
			hideButton(cancelButton);
			return;
		}

		hideButton(updateButton);
		hideButton(cancelButton);

		LocalDate selectedOrderDate = row.getOrderDate();

		if (selectedOrderDate == null) {
			return;
		}

		boolean expiredDate = selectedOrderDate.isBefore(LocalDate.now());

		String status = row.getOrderStatus();

		boolean finalStatus =
				"cancelled".equalsIgnoreCase(status) ||
				"expired".equalsIgnoreCase(status) ||
				"completed".equalsIgnoreCase(status) ||
				"no_show".equalsIgnoreCase(status);

		boolean awaitingCurrentUpdate = awaitingUpdate.contains(row.getOrderId());
		boolean awaitingCurrentCancel = awaitingCancel.contains(row.getOrderId());

		boolean canChangeOrder =
				!expiredDate &&
				!finalStatus &&
				!awaitingCurrentUpdate &&
				!awaitingCurrentCancel;

		if (canChangeOrder) {
			showButton(updateButton);
			showButton(cancelButton);
		}
	}
	/* 
	 * this method handles closing the client program if the server 
	 * closed the user connection
	 */
	/**
	 * Handles server shutdown or disconnect.
	 */
	public void handleExit() {
		Platform.runLater(() -> {
			Platform.exit();
			System.exit(0);
		});
	}

	/**
	 * Sets the close request behavior for the order table window.
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
	 * Handles a disconnect requested by the user.
	 */
	private void userIssuedDisconnect() {
		clientController.setUserIssuedDisconnect(true);
		clientController.disconnectFromServer();
	}

	/**
	 * Sets the order table data.
	 *
	 * @param rows the list of orders to display
	 */
	public void setData(List<Order> rows) {
		Platform.runLater(() -> {
			data.setAll(rows);
			orderTable.refresh();

			if (rows == null || rows.isEmpty()) {
				showInfo("No orders found.");
			} else {
				showInfo(rows.size() + " orders loaded.");
			}
		});
	}

	/**
	 * Sets the client controller.
	 *
	 * @param clientController the client controller
	 */
	public void setClientController(ClientController clientController) {
		this.clientController = clientController;

		if (this.clientController != null) {
			this.clientController.addObserver(this);
		}
	}

	/**
	 * Sets the logged-in employee.
	 *
	 * @param employee the logged-in employee
	 */
	public void setLoggedInEmployee(Employee employee) {
		this.loggedInEmployee = employee;
		backButton.setText("Back");
	}

	/**
	 * Adds an order to the update waiting list.
	 *
	 * @param orderNumber the order number
	 */
	private void addOrderToUpdateWaitingList(int orderNumber) {
		awaitingUpdate.add(orderNumber);
		hideButton(updateButton);
		hideButton(cancelButton);
	}

	/**
	 * Removes an order from the update waiting list.
	 *
	 * @param orderNumber the order number
	 */
	protected void removeOrderFromUpdateWaitingList(int orderNumber) {
		awaitingUpdate.remove(orderNumber);
	}

	/**
	 * This method is called when orders are received from the server.
	 *
	 * @param rows the list of orders
	 */
	@Override
	public void onOrdersReceived(List<Order> rows) {
		setData(rows);
	}

	/**
	 * This method is called when the server returns an order update result.
	 *
	 * @param success whether the update was successful
	 * @param updateMessage the update message data
	 */
	@Override
	public void onUpdateResult(boolean success, UpdateMessage updateMessage) {
		if (updateMessage == null) {
			System.out.println("Error: invalid data");
			return;
		}

		int updatedOrderId = updateMessage.getOrderId();

		if (success) {
			showSuccess("Order update for order ID: " + updatedOrderId + " succeeded.");

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
			showError("Order update for order ID: " + updatedOrderId + " failed.");
		}

		removeOrderFromUpdateWaitingList(updatedOrderId);

		if (selectedRow != null) {
			onRowSelected(selectedRow);
		}
	}

	/**
	 * This method is called when the server returns an order cancellation result.
	 *
	 * @param success whether the cancellation was successful
	 * @param cancelOrderMessage the cancellation message data
	 */
	@Override
	public void onCancelResult(boolean success, CancelOrderMessage cancelOrderMessage) {
		if (cancelOrderMessage == null) {
			System.out.println("Error: invalid cancellation data");
			return;
		}

		int cancelledOrderId = cancelOrderMessage.getOrderId();

		if (success) {
			showSuccess("Order cancellation for order ID: " + cancelledOrderId + " succeeded.");

			for (int i = 0; i < data.size(); i++) {
				Order row = data.get(i);

				if (row.getOrderId() == cancelledOrderId) {
					data.remove(i);
					break;
				}
			}

			selectedRow = null;
			orderTable.getSelectionModel().clearSelection();

		} else {
			showError("Order cancellation for order ID: " + cancelledOrderId + " failed.");
		}

		awaitingCancel.remove(cancelledOrderId);

		if (selectedRow != null) {
			onRowSelected(selectedRow);
		}
	}

	/**
	 * Loads orders for a specific park.
	 *
	 * @param parkId the park ID
	 */
	public void loadOrdersForPark(int parkId) {
		if (clientController == null) {
			showError("Client is not connected to server.");
			return;
		}

		if (parkId <= 0) {
			showError("Invalid park ID.");
			return;
		}

		showInfo("Loading park orders...");

		clientController.requestOrdersByParkId(parkId);
	}

	/**
	 * Configures the screen for park manager view.
	 */
	public void configureForParkManagerView() {
		parkManagerView = true;
		customerView = false;
		serviceRepresentativeView = false;
		occasionalCustomerView = false;

		hideButton(makeOrderButton);
		hideButton(updateButton);
		hideButton(cancelButton);
		hideButton(waitingListButton);

		backButton.setText("Back");
	}

	/**
	 * Configures the screen for service representative view.
	 */
	public void configureForServiceRepresentativeView() {
		serviceRepresentativeView = true;
		customerView = false;
		parkManagerView = false;
		occasionalCustomerView = false;

		hideButton(makeOrderButton);
		hideButton(updateButton);
		hideButton(cancelButton);
		hideButton(waitingListButton);

		backButton.setText("Back");
	}

	/**
	 * Configures the screen for customer view.
	 */
	public void configureForCustomerView() {
		customerView = true;
		parkManagerView = false;
		serviceRepresentativeView = false;
		occasionalCustomerView = false;

		showButton(makeOrderButton);
		showButton(waitingListButton);
		hideButton(updateButton);
		hideButton(cancelButton);

		backButton.setText("Logout");
	}

	/**
	 * Configures the screen for occasional customer view.
	 */
	public void configureForOccasionalCustomerView() {
		configureForCustomerView();
		occasionalCustomerView = true;
	}

	/**
	 * Loads all orders for a service representative.
	 */
	public void loadAllOrdersForServiceRepresentative() {
		if (clientController == null) {
			showError("Client is not connected to server.");
			return;
		}

		showInfo("Loading customer orders...");

		clientController.requestAllOrdersForServiceRepresentative();
	}

	/**
	 * Adds an order to the table.
	 *
	 * @param order the order to add
	 */
	@Override
	public void addOrder(Order order) {
		Platform.runLater(() -> {
			order.setOrderNumber(data.size() + 1);
			data.add(order);
			orderTable.refresh();
		});
	}

	/**
	 * Handles the click on the My Details button.
	 *
	 * This method opens the user information page for the current customer.
	 *
	 * @param event the button click event
	 */
	@FXML
	private void handleMyDetails(ActionEvent event) {
		try {
			if (!customerView) {
				notifLabel.setTextFill(Color.RED);
				notifLabel.setText("My Details is available only for customers.");
				return;
			}

			if (clientController == null) {
				notifLabel.setTextFill(Color.RED);
				notifLabel.setText("Client is not connected to server.");
				return;
			}

			String customerId = null;

			if (clientController.getLoggedInSubscriberId() != null) {
				customerId = String.valueOf(clientController.getLoggedInSubscriberId());
			} else if (clientController.getId() != null && !clientController.getId().isBlank()) {
				customerId = clientController.getId();
			}

			if (customerId == null || customerId.isBlank()) {
				notifLabel.setTextFill(Color.RED);
				notifLabel.setText("Could not identify the current customer.");
				return;
			}

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			Scene previousScene = stage.getScene();

			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/UserInformationPage.fxml")
			);

			Parent root = loader.load();

			UserInformationPageController controller = loader.getController();
			controller.setClientController(clientController);
			controller.setPreviousScene(previousScene, "Customer Orders");
			controller.configureForCustomerMyDetails(customerId);

			stage.setTitle("My Details");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Handles the click on the back button.
	 *
	 * This method returns the user to the correct previous screen
	 * according to the current screen mode.
	 *
	 * @param event the button click event
	 */
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
				if (clientController != null) {
					clientController.logoutCurrentUserFromServer();
					clientController.setLoggedInSubscriberId(null);
					clientController.removeObserver(this);
				}

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

			showError("Back is not configured for this screen mode.");

		} catch (IOException e) {
			e.printStackTrace();
			showError("Could not return to previous screen.");
		}
	}

	/**
	 * This method is called when an order was declined from a reminder.
	 *
	 * @param order the declined order
	 */
	@Override
	public void reminderDeclined(Order order) {
		Platform.runLater(() -> {
			int index = data.indexOf(order);

			if (index >= 0) {
				data.get(index).setOrderStatus("cancelled");
				data.set(index, data.get(index));
				orderTable.refresh();
			}
		});
	}

	/**
	 * Shows a button.
	 *
	 * @param button the button to show
	 */
	private void showButton(Button button) {
		if (button == null) {
			return;
		}

		button.setDisable(false);
		button.setVisible(true);
		button.setManaged(true);
	}

	/**
	 * Hides a button.
	 *
	 * @param button the button to hide
	 */
	private void hideButton(Button button) {
		if (button == null) {
			return;
		}

		button.setDisable(false);
		button.setVisible(false);
		button.setManaged(false);
	}

	/**
	 * Shows an information status message.
	 *
	 * @param message the status message
	 */
	private void showInfo(String message) {
		updateStatus(message, "status-info");
	}

	/**
	 * Shows a success status message.
	 *
	 * @param message the status message
	 */
	private void showSuccess(String message) {
		updateStatus(message, "status-success");
	}

	/**
	 * Shows an error status message.
	 *
	 * @param message the status message
	 */
	private void showError(String message) {
		updateStatus(message, "status-error");
	}

	/**
	 * Updates the status label text and style.
	 *
	 * @param message the status message
	 * @param styleClass the style class for the status label
	 */
	private void updateStatus(String message, String styleClass) {
		notifLabel.setText("Status: " + message);

		notifLabel.getStyleClass().remove("status-info");
		notifLabel.getStyleClass().remove("status-success");
		notifLabel.getStyleClass().remove("status-error");

		if (!notifLabel.getStyleClass().contains(styleClass)) {
			notifLabel.getStyleClass().add(styleClass);
		}
	}
}

