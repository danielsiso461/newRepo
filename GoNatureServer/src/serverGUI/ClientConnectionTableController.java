package serverGUI;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import serverCommon.User;
import serverController.ServerController;

/**
 * This class is the UI controller for the server window.
 * 
 * It manages the connected users table,
 * displays server host and IP details,
 * and shows server actions in the log area.
 */

public class ClientConnectionTableController {

	/**
	 * The controller that connects the GUI with the server logic.
	 */
	private ServerController serverController;

	/**
	 * The text area that displays server log messages.
	 */
	@FXML
	private TextArea serverLogArea;
	/**
	 * resource bundle of the fxml loader
	 */
	@FXML // ResourceBundle that was given to the FXMLLoader
	private ResourceBundle resources;
	/**
	 * location of the file of the fxml loader
	 */
	@FXML // URL location of the FXML file that was given to the FXMLLoader
	private URL location;
	/**
	 * label that displays the server's hostname
	 */
	@FXML // fx:id="hostNameLabel"
	private Label hostNameLabel; // Value injected by FXMLLoader
	/**
	 * label that displays the server's ip address
	 */
	@FXML // fx:id="ipAddressLabel"
	private Label ipAddressLabel; // Value injected by FXMLLoader
	/**
	 * column that displays the users' hostnames
	 */
	@FXML // fx:id="hostName"
	private TableColumn<User, String> hostName; // Value injected by FXMLLoader
	
	/**
	 * column that displays the users' status
	 */
	@FXML // fx:id="status"
	private TableColumn<User, String> status; // Value injected by FXMLLoader
	
	/**
	 * column that displays the users' id
	 */
	@FXML // fx:id="userId"
	private TableColumn<User, String> userId; // Value injected by FXMLLoader
	
	/**
	 * column that displays the users' ip
	 */
	@FXML // fx:id="userIp"
	private TableColumn<User, String> userIp; // Value injected by FXMLLoader
	
	/**
	 * column that displays the users' number
	 */
	@FXML // fx:id="userNumber"
	private TableColumn<User, Integer> userNumber; // Value injected by FXMLLoader
	
	/**
	 * the table that display all of the above columns
	 */
	@FXML // fx:id="userTable"
	private TableView<User> userTable; // Value injected by FXMLLoader

	/**
	 * The observable list that stores the users displayed in the table.
	 */
	private ObservableList<User> data = FXCollections.observableArrayList();

	/**
	 * This method is called automatically by the FXMLLoader.
	 * 
	 * It initializes the table columns,
	 * connects the table to the observable list,
	 * initializes the log area,
	 * and handles the server window close action.
	 */
	@FXML
	void initialize() {
		
		assert hostNameLabel != null
				: "fx:id=\"hostNameLabel\" was not injected: check your FXML file 'ClientConnectionTable.fxml'.";
		assert ipAddressLabel != null
				: "fx:id=\"ipAddressLabel\" was not injected: check your FXML file 'ClientConnectionTable.fxml'.";
		assert hostName != null
				: "fx:id=\"hostName\" was not injected: check your FXML file 'ClientConnectionTable.fxml'.";
		assert status != null
				: "fx:id=\"status\" was not injected: check your FXML file 'ClientConnectionTable.fxml'.";
		assert userId != null
				: "fx:id=\"userId\" was not injected: check your FXML file 'ClientConnectionTable.fxml'.";
		assert userIp != null
				: "fx:id=\"userIp\" was not injected: check your FXML file 'ClientConnectionTable.fxml'.";
		assert userNumber != null
				: "fx:id=\"userNumber\" was not injected: check your FXML file 'ClientConnectionTable.fxml'.";
		assert userTable != null
				: "fx:id=\"userTable\" was not injected: check your FXML file 'ClientConnectionTable.fxml'.";
		assert serverLogArea != null
				: "fx:id=\"serverLogArea\" was not injected: check your FXML file 'ClientConnectionTable.fxml'.";
		
		userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);		
		// Set where each table column gets its data from in the User object.
		userNumber.setCellValueFactory(new PropertyValueFactory<>("userNumber"));
		hostName.setCellValueFactory(new PropertyValueFactory<>("hostName"));
		status.setCellValueFactory(new PropertyValueFactory<>("status"));
		userIp.setCellValueFactory(new PropertyValueFactory<>("userIp"));
		userId.setCellValueFactory(new PropertyValueFactory<>("userId"));

		// Connect the table to the observable list that stores the users.
		userTable.setItems(data);

		// Add the first message to the server log.
		addLog("Server GUI loaded successfully.");

		// Handle closing the program when pressing the red X button.
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Stage stage = (Stage) userTable.getScene().getWindow();

				stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
					@Override
					public void handle(WindowEvent event) {
						addLog("Server is closing.");

						// Close the server only if the controller was already connected.
						if (serverController != null) {
							serverController.closeServer();
						}

						Platform.exit();
						System.exit(0);
					}
				});
			}
		});
	}

	/**
	 * This method handles adding a new connected user to the user table.
	 * 
	 * It also adds a log message that describes the connected user.
	 * 
	 * @param u the user to add
	 */
	public void onUserConnected(User u) {
		Platform.runLater(() -> {
			data.add(u);

			addLog("User connected: ID = " + u.getUserId()
					+ ", IP = " + u.getUserIp()
					+ ", Host = " + u.getHostName());
		});
	}
	/**
	 * This method handles updating the data of a disconnected user.
	 * 
	 * It updates the user row in the table
	 * and adds a log message about the disconnection.
	 * 
	 * @param u the user to update
	 */
	public void onUserDisconnected(User u) {
		Platform.runLater(() -> {
			int index = data.indexOf(u);

			if (index >= 0) {
				data.set(index, u);
				userTable.refresh();
			}

			addLog("User disconnected: ID = " + u.getUserId()
					+ ", IP = " + u.getUserIp()
					+ ", Host = " + u.getHostName());
		});
	}
	/**
	 * This method adds a new message to the server log area.
	 * 
	 * The message is shown with the current time.
	 * Platform.runLater is used because GUI updates must run on the JavaFX thread.
	 * 
	 * @param message the message to show in the server log
	 */
	public void addLog(String message) {
		Platform.runLater(() -> {
			if (serverLogArea == null) {
				System.out.println(message);
				return;
			}

			String time = LocalDateTime.now()
					.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

			serverLogArea.appendText("[" + time + "] " + message + "\n");
		});
	}

	/**
	 * This method connects the ServerController to the UI controller.
	 * 
	 * @param serverController the controller to connect
	 */
	public void setServerController(ServerController serverController) {
		this.serverController = serverController;

		addLog("Server controller connected to GUI.");
	}

	/**
	 * This method updates the labels with server data.
	 * 
	 * It also writes the server host and IP details to the log area.
	 * 
	 * @param hostName the server's host name
	 * @param ip       the server's IP address
	 */
	public void setLabels(String hostName, String ip) {
		Platform.runLater(() -> {
			hostNameLabel.setText(hostName);
			ipAddressLabel.setText("Server IP: " + ip);
		});

		addLog("Server started on host: " + hostName + ", IP: " + ip);
	}
}