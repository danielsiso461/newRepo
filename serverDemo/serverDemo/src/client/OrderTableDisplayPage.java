package client;

/**
 * Sample Skeleton for 'Untitled' Controller Class
 */

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import common.OrderRow;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class OrderTableDisplayPage {
	private ClientService service;

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

	@FXML // fx:id="updateButton"
	private Button updateButton; // Value injected by FXMLLoader

	@FXML
	void updateButtonClick(ActionEvent event) {
		// get order id from selected row
		int id = selectedRow.getOrderId();
		// @todo should launch the order update screen
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
		boolean expired = orderDate.isBefore(LocalDate.now());
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
		
		orderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
		orderDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
		visitorNumber.setCellValueFactory(new PropertyValueFactory<>("visitorNumber"));
		confCode.setCellValueFactory(new PropertyValueFactory<>("confCode"));
		userId.setCellValueFactory(new PropertyValueFactory<>("userId"));
		placementDate.setCellValueFactory(new PropertyValueFactory<>("placementDate"));
		
		orderTable.setItems(data);
		orderTable.getSelectionModel().selectedItemProperty().addListener(this::handleRowSelection);
	}

	// DATA FUNCTIONS @todo this is weird need to get a better understanding of it
	public void setData(List<OrderRow> rows) {
		Platform.runLater(() -> {
			data.setAll(rows);
		});
	}

	public void setClientService(ClientService service) {
		this.service = service;
	}

}
