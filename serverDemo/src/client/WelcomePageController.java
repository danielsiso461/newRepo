package client;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/*
 * this class controls the welcome page of the client application
 * 
 * the page asks the user to enter an ID and a server address
 * and then opens the order table page
 */
public class WelcomePageController {

	/*
	 * indicates whether a valid ID was already entered
	 */
	private boolean idEntered = false;

	/*
	 * stores the user ID
	 */
	private String id;

	/*
	 * stores the server address
	 */
	private String address;

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="commandLabel"
    private Label commandLabel; // Value injected by FXMLLoader

    @FXML // fx:id="confirmButton"
    private Button confirmButton; // Value injected by FXMLLoader

    @FXML // fx:id="inputField"
    private TextField inputField; // Value injected by FXMLLoader

    @FXML // fx:id="messageLabel"
    private Label messageLabel; // Value injected by FXMLLoader

    /*
     * this function handles pressing the confirm button
     * 
     * first validates the user ID
     * then receives the server address
     * and finally launches the order table page
     * 
     * @param event the event of pressing the confirm button
     */
    @FXML
    void btnClick(ActionEvent event) {
    	if(!idEntered) {
    		id = inputField.getText();

    		// ID should have 9 characters
			if (id.length() != 9) {
				messageLabel.setTextFill(Color.RED);
				messageLabel.setText("id should be 9 digits long");
			} else {
				try {
					int val = Integer.parseInt(id);

					if (val > 0) {
						idEntered = true;
						messageLabel.setText("");
						inputField.clear();
						commandLabel.setText("Enter server address");
					}
				} catch (NumberFormatException e) {
					messageLabel.setTextFill(Color.RED);
					messageLabel.setText("id should be a number");
				}
			}
    	} else {
    		address = inputField.getText();

    		// should launch the order table page
    		launchOrderTable();
    	}
    }

    /*
     * this function initializes the welcome page
     * 
     * checks that all FXML components were injected correctly
     * and handles closing the application when pressing the red X button
     */
    @FXML
    void initialize() {
        assert commandLabel != null : "fx:id=\"commandLabel\" was not injected: check your FXML file 'welcomePage.fxml'.";
        assert confirmButton != null : "fx:id=\"confirmButton\" was not injected: check your FXML file 'welcomePage.fxml'.";
        assert inputField != null : "fx:id=\"inputField\" was not injected: check your FXML file 'welcomePage.fxml'.";
        assert messageLabel != null : "fx:id=\"messageLabel\" was not injected: check your FXML file 'welcomePage.fxml'.";

        // this handles closing when pressing the red X button
     	Platform.runLater(new Runnable() {
     		@Override
     		public void run() {
     			Stage stage = (Stage) inputField.getScene().getWindow();

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

    /*
     * this function loads and opens the order table page
     * 
     * creates the client controller
     * requests the user's orders
     * and replaces the current scene with the order table scene
     */
    private void launchOrderTable() {
    	Stage stage = (Stage) inputField.getScene().getWindow();

    	// load the FXML file of the table of orders
    	FXMLLoader loader = new FXMLLoader(getClass().getResource(ConstantsUI.orderTable));
    	Parent root = null;

		try {
			root = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
			Platform.exit();
			System.exit(1);
		}

    	// get controller
    	OrderTableDisplayPage controller = loader.getController();

    	// establish connection between UI controller and client controller
    	ClientController clientController =
    			new ClientController(address, ConstantsUI.DEFAULT_PORT, id);

    	controller.setClientController(clientController);

    	// get the orders of the user and load them into the order table
    	clientController.requestOrders();

    	// show UI
    	Scene scene = new Scene(root);
    	stage.setScene(scene);
    	stage.setTitle("Order Table");
    	stage.show();
    }
}