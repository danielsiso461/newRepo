package client;

/**
 * Sample Skeleton for 'Untitled' Controller Class
 */

import java.net.URL;
import common.UpdateMessage;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import common.OrderRow;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class OrderTableDisplayPage implements OrderObserver{
	private ClientService service;
	private Set<Integer> awaitingUpdate = new HashSet<>();

	@FXML // ResourceBundle that was given to the FXMLLoader
	private ResourceBundle resources;

	@FXML // URL location of the FXML file that was given to the FXMLLoader
	private URL location;

	@FXML // fx:id="orderTable"
	private TableView<OrderRow> orderTable; // Value injected by FXMLLoader
	private ObservableList<OrderRow> data = FXCollections.observableArrayList();
	private OrderRow selectedRow = null;

	@FXML // fx:id="confCode"
	private TableColumn<OrderRow, Integer> confCode; // Value injected by FXMLLoader
	@FXML // fx:id="orderDate"
	private TableColumn<OrderRow, LocalDate> orderDate; // Value injected by FXMLLoader
	@FXML // fx:id="orderId"
	private TableColumn<OrderRow, Integer> orderId; // Value injected by FXMLLoader
	@FXML // fx:id="placementDate"
	private TableColumn<OrderRow, LocalDate> placementDate; // Value injected by FXMLLoader
	@FXML // fx:id="userId"
	private TableColumn<OrderRow, Integer> userId; // Value injected by FXMLLoader
	@FXML // fx:id="visitorNumber"
	private TableColumn<OrderRow, Integer> visitorNumber; // Value injected by FXMLLoader
	@FXML // fx:id="orderNumber"
	private TableColumn<OrderRow, Integer> orderNumber; // Value injected by FXMLLoader
	
	@FXML // fx:id="updateButton"
	private Button updateButton; // Value injected by FXMLLoader

	@FXML
	void updateButtonClick(ActionEvent event) throws Exception {
		// launch the order update screen
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/OrderUpdatePage.fxml"));
		Pane root = loader.load();

		OrderUpdatePage OrderUpdatePageController = loader.getController();

		addOrderToUpdateWaitingList(selectedRow.getOrderId());
		
		OrderUpdatePageController.setClientService(service);
		OrderUpdatePageController.setOrderData(
				selectedRow.getOrderId(),
				selectedRow.getOrderDate(),
				selectedRow.getVisitorNumber()
		);

		Stage prevStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		prevStage.hide();

		OrderUpdatePageController.loadPrevStage(prevStage);
		OrderUpdatePageController.loadPrevController(this);

		Scene scene = new Scene(root);

		Stage primaryStage = new Stage();
		primaryStage.setTitle("Order Update Page");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void handleRowSelection(ObservableValue<? extends OrderRow> obs, OrderRow oldSelection,
			OrderRow newSelection) {
		if (newSelection != null) {
			onRowSelected(newSelection);
		}
	}

	private void onRowSelected(OrderRow row) {
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

		updateButton.setDisable(true);
		
		orderNumber.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
		orderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
		orderDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
		visitorNumber.setCellValueFactory(new PropertyValueFactory<>("visitorNumber"));
		confCode.setCellValueFactory(new PropertyValueFactory<>("confCode"));
		userId.setCellValueFactory(new PropertyValueFactory<>("userId"));
		placementDate.setCellValueFactory(new PropertyValueFactory<>("placementDate"));
		
		orderTable.setItems(data);
		orderTable.getSelectionModel().selectedItemProperty().addListener(this::handleRowSelection);
		
		// this handles closing the program when pressing the red X button
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Stage stage = (Stage) orderTable.getScene().getWindow();
				stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				    @Override
				    public void handle(WindowEvent event) {
				    	Platform.exit();
				        System.exit(0);
				    }
				});
			}
		});
	}

	// DATA FUNCTIONS @todo this is weird need to get a better understanding of it
	public void setData(List<OrderRow> rows) {
		Platform.runLater(() -> {
			data.setAll(rows);
		});
	}

	public void setClientService(ClientService service) {
		this.service = service;
		this.service.addObserver(this);
	}
	
	protected void addOrderToUpdateWaitingList(int orderNumber) {
		awaitingUpdate.add(orderNumber);
	}
	
	protected void removeOrderFromUpdateWaitingList(int orderNumber) {
		awaitingUpdate.remove(orderNumber);
	}
	
	@Override
	public void onOrdersReceived(List<OrderRow> rows) {
		setData(rows);
	}

	@Override
	public void onUpdateResult(boolean success, UpdateMessage updateMessage) {
		if (success) {
			System.out.println("Order updated successfully.");


			if (service != null) {
				service.requestOrders();
			}
		} else {
			System.out.println("Order update failed.");
		}
		// removing order from waiting list
		removeOrderFromUpdateWaitingList(updateMessage.getOrderNumber());
	}
}
