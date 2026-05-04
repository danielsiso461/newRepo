package client;

/**
 * Sample Skeleton for 'Untitled' Controller Class
 */

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import common.OrderRow;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

public class OrderTableDisplayPage {

	@FXML // ResourceBundle that was given to the FXMLLoader
	private ResourceBundle resources;

	@FXML // URL location of the FXML file that was given to the FXMLLoader
	private URL location;

	@FXML // fx:id="orderTable"
	private TableView<OrderRow> orderTable; // Value injected by FXMLLoader

	@FXML // fx:id="updateButton"
	private Button updateButton; // Value injected by FXMLLoader

	@FXML
	void updateButtonClick(ActionEvent event) {

	}

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		assert orderTable != null : "fx:id=\"orderTable\" was not injected: check your FXML file 'Untitled'.";
		assert updateButton != null : "fx:id=\"updateButton\" was not injected: check your FXML file 'Untitled'.";
		orderTable = new TableView<>();
	}

	// DATA FUNCTIONS
	public void setData(List<OrderRow> l) {
		orderTable.setItems(FXCollections.observableArrayList(l));
	}
	
	
}
