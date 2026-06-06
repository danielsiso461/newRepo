package clientGUI;

/**
 * Sample Skeleton for 'Untitled' Controller Class
 */

import java.net.URL;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import clientCommon.OrderObserver;
import clientController.ClientController;
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
 * this class is the UI controller for the update page
 */
@SuppressWarnings("deprecation")
public class OrderTableDisplayController implements OrderObserver, Runnable {
	private ClientController clientController;
	private Set<Integer> awaitingUpdate = new HashSet<>();

	@FXML // ResourceBundle that was given to the FXMLLoader
	private ResourceBundle resources;

	@FXML // URL location of the FXML file that was given to the FXMLLoader
	private URL location;

	@FXML // fx:id="orderTable"
	private TableView<Order> orderTable; // Value injected by FXMLLoader
	private ObservableList<Order> data = FXCollections.observableArrayList();
	private Order selectedRow = null;

	@FXML // fx:id="confCode"
	private TableColumn<Order, Integer> confCode; // Value injected by FXMLLoader
	@FXML // fx:id="orderDate"
	private TableColumn<Order, LocalDate> orderDate; // Value injected by FXMLLoader
	@FXML // fx:id="orderId"
	private TableColumn<Order, Integer> orderId; // Value injected by FXMLLoader
	@FXML // fx:id="placementDate"
	private TableColumn<Order, LocalDate> placementDate; // Value injected by FXMLLoader
	@FXML // fx:id="userId"
	private TableColumn<Order, Integer> userId; // Value injected by FXMLLoader
	@FXML // fx:id="visitorNumber"
	private TableColumn<Order, Integer> visitorNumber; // Value injected by FXMLLoader
	@FXML // fx:id="orderNumber"
	private TableColumn<Order, Integer> orderNumber; // Value injected by FXMLLoader
	
	@FXML // fx:id="notifLabel"
    private Label notifLabel; // Value injected by FXMLLoader
	
	@FXML // fx:id="updateButton"
	private Button updateButton; // Value injected by FXMLLoader
	
	/*
	 * this method handles click the update button
	 * it loads the update page, 
	 * puts selected order into a waiting list and 
	 * hides current screen
	 * 
	 * @param event 	the update button click
	 */
	@FXML
	void updateButtonClick(ActionEvent event) throws Exception {
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
	
	/*
	 * this method recognizes a change in selected row and 
	 * calls the relevant handler for updating
	 * (some parameters are here to match the javafx contract)
	 * 
	 * @param obs				the observable property of the tableView (OrderRow)
	 * @param oldSelection		the old row selection
	 * @param newSelection		the newly selected row
	 */
	private void handleRowSelection(ObservableValue<? extends Order> obs, 
			Order oldSelection, Order newSelection) {
		if (newSelection != null) {
			onRowSelected(newSelection);
		}
	}
	/*
	 * this method handles updating which row is selected
	 * and whether the update button should be available for it
	 * 
	 * @param row 	the selected row		
	 */
	private void onRowSelected(Order row) {
		updateButton.setDisable(true);
		selectedRow = row;
		LocalDate orderDate = row.getOrderDate();
		// making sure user is trying to update relevant order
		boolean expired = orderDate.isBefore(LocalDate.now());
		// making sure the order is not awaiting update already
		if(!awaitingUpdate.contains(row.getOrderId()))
			updateButton.setDisable(expired);
	}

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		assert confCode != null : "fx:id=\"confCode\" was not injected: check your FXML file 'Untitled'.";
		assert orderDate != null : "fx:id=\"orderDate\" was not injected: check your FXML file 'Untitled'.";
		assert orderId != null : "fx:id=\"orderId\" was not injected: check your FXML file 'Untitled'.";
		assert orderTable != null : "fx:id=\"orderTable\" was not injected: check your FXML file 'Untitled'.";
		assert placementDate != null : "fx:id=\"placementDate\" was not injected: check your FXML file 'Untitled'.";
		assert updateButton != null : "fx:id=\"updateButton\" was not injected: check your FXML file 'Untitled'.";
		assert userId != null : "fx:id=\"userId\" was not injected: check your FXML file 'Untitled'.";
		assert visitorNumber != null : "fx:id=\"visitorNumber\" was not injected: check your FXML file 'Untitled'.";
		assert notifLabel != null : "fx:id=\"notifLabel\" was not injected: check your FXML file 'OrderTableDisplayPage.fxml'.";
		
		// initializing the update button
		updateButton.setDisable(true);
		
		// sets where the table columns get their data from (of the given object) 
		orderNumber.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
		orderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
		orderDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
		visitorNumber.setCellValueFactory(new PropertyValueFactory<>("visitorNumber"));
		confCode.setCellValueFactory(new PropertyValueFactory<>("confCode"));
		userId.setCellValueFactory(new PropertyValueFactory<>("userId"));
		placementDate.setCellValueFactory(new PropertyValueFactory<>("placementDate"));
		// sets the table to get it's data from the ObservableList set up to hold order
		// data
		orderTable.setItems(data);
		// adds listener to row selection
		orderTable.getSelectionModel().selectedItemProperty().addListener(this::handleRowSelection);		
		
	}
	
	/* 
	 * this method handles closing the client program if the server 
	 * closed the user connection
	 */
	public void handleExit() {
		Platform.runLater(() -> {
			Platform.exit();
	        System.exit(0);
		});
	}
	
	/*
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
	/*
	 * this method disconnects the user from the server
	 */
	private void userIssuedDisconnect() {
		clientController.setUserIssuedDisconnect(true);
		clientController.disconnectFromServer();
	}
	
	/*
	 * this method sets the order data to the ObservableList the tableView is connected to
	 * 
	 * @param rows		the order data
	 */
	public void setData(List<Order> rows) {
		Platform.runLater(() -> {
			data.setAll(rows);
		});
	}
	/*
	 * this method sets the ClientController on the UI side
	 * add this UI controller to the ClientController observer List
	 * 
	 * @param clientController 		the ClientController
	 */
	public void setClientController(ClientController clientController) {
		this.clientController = clientController;
		this.clientController.addObserver(this);
	}
	
	/*
	 * this method adds orders to the update waiting list
	 * 
	 * @param orderNumber 	the number of the order to add to the waiting list
	 */
	private void addOrderToUpdateWaitingList(int orderNumber) {
		awaitingUpdate.add(orderNumber);
	}
	
	/*
	 * this method removes orders from the update waiting list
	 * 
	 * @param orderNumber 	the number of the order to remove from the waiting list
	 */
	protected void removeOrderFromUpdateWaitingList(int orderNumber) {
		awaitingUpdate.remove(orderNumber);
	}
	
	/*
	 * this method sets the object holding the orders to the data received from the server
	 * 
	 * @param rows 	the order list
	 */
	@Override
	public void onOrdersReceived(List<Order> rows) {
		setData(rows);
	}
	
	/*
	 * this method handles updating the UI upon reply from 
	 * the server to requesting an order update
	 * 
	 * @param success 			whether the update was successful
	 * @param updateMessage 	the update request's data
	 */
	@Override
	public void onUpdateResult(boolean success, UpdateMessage updateMessage) {
		if(updateMessage == null) {
			System.out.println("Error: invalid data");
			return;
		}
		if (success) {
			notifLabel.setTextFill(Color.GREEN);
			notifLabel.setText("Order update for order#:" + 
					updateMessage.getOrderNumber() + " succeeded.");
			
			// updating order in tableview - update is local, as it was confirmed by server
			Order updatedRow = data.get(updateMessage.getOrderNumber() - 1);
			if(updateMessage.getUpdateDate() != null)
				updatedRow.setOrderDate(updateMessage.getUpdateDate());
			if(updateMessage.getNumberOfVisitors() > 0)
				updatedRow.setNumberOfVisitors(updateMessage.getNumberOfVisitors());
			data.set(updateMessage.getOrderNumber() - 1, updatedRow);
		} else {
			notifLabel.setTextFill(Color.RED);
			notifLabel.setText("Order update for order#:" + 
					updateMessage.getOrderNumber() + " failed.");
		}
		// removing order from waiting list
		removeOrderFromUpdateWaitingList(updateMessage.getOrderId());
	}
}
