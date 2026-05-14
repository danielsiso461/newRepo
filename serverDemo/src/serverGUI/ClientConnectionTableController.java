package serverGUI;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import serverCommon.User;
import serverController.ServerController;
// this class is the UI controller for the server
public class ClientConnectionTableController {
	ServerController serverController;

	@FXML // ResourceBundle that was given to the FXMLLoader
	private ResourceBundle resources;

	@FXML // URL location of the FXML file that was given to the FXMLLoader
	private URL location;

	@FXML // fx:id="hostName"
	private TableColumn<User, String> hostName; // Value injected by FXMLLoader

	@FXML // fx:id="status"
	private TableColumn<User, Boolean> status; // Value injected by FXMLLoader

	@FXML // fx:id="userId"
	private TableColumn<User, String> userId; // Value injected by FXMLLoader

	@FXML // fx:id="userIp"
	private TableColumn<User, String> userIp; // Value injected by FXMLLoader

	@FXML // fx:id="userNumber"
	private TableColumn<User, Integer> userNumber; // Value injected by FXMLLoader

	@FXML // fx:id="userTable"
	private TableView<User> userTable; // Value injected by FXMLLoader
	private ObservableList<User> data = FXCollections.observableArrayList();

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		assert hostName != null
				: "fx:id=\"hostName\" was not injected: check your FXML file 'ClientConnectionTable.fxml'.";
		assert status != null : "fx:id=\"status\" was not injected: check your FXML file 'ClientConnectionTable.fxml'.";
		assert userId != null : "fx:id=\"userId\" was not injected: check your FXML file 'ClientConnectionTable.fxml'.";
		assert userIp != null : "fx:id=\"userIp\" was not injected: check your FXML file 'ClientConnectionTable.fxml'.";
		assert userTable != null
				: "fx:id=\"userTable\" was not injected: check your FXML file 'ClientConnectionTable.fxml'.";

		// sets where the table columns get their data from (of the given object)
		userNumber.setCellValueFactory(new PropertyValueFactory<>("userNumber"));
		hostName.setCellValueFactory(new PropertyValueFactory<>("hostName"));
		status.setCellValueFactory(new PropertyValueFactory<>("status"));
		userIp.setCellValueFactory(new PropertyValueFactory<>("userIp"));
		userId.setCellValueFactory(new PropertyValueFactory<>("userId"));
		// sets the table to get it's data from the ObservableList set up to hold user
		// data
		userTable.setItems(data);

		// this handles closing the program when pressing the red X button
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Stage stage = (Stage) userTable.getScene().getWindow();
				stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
					@Override
					public void handle(WindowEvent event) {
						serverController.closeServer();
						Platform.exit();
						System.exit(0);
					}
				});
			}
		});
	}
	
	/*
	 * this method handles adding new users to the user table
	 * 
	 * @param u the user to add
	 */
	public void onUserConnected(User u) {
		Platform.runLater(() -> {
			data.add(u);
		});
	}
	
	/*
	 * this method handles updating the data of given user on disconnect
	 * 
	 * @param u the user to update
	 */
	public void onUserDisconnected(User u) {
		Platform.runLater(() -> {
			data.set(u.getUserNumber() - 1, u);
		});
	}
	
	/*
	 * this method connects the ServerController to the UI controller
	 * 
	 * @param serverController the controller to connect
	 */
	public void setServerController(ServerController serverController) {
		this.serverController = serverController;
	}
}
