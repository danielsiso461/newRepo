package clientGUI;

import java.io.IOException;

import java.net.URL;
import java.util.ResourceBundle;

import clientController.ClientController;
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
import javafx.scene.control.TableView;

/**
 * this class controls the welcome page of the client application
 * 
 * the page asks the user to enter an ID and a server address
 * and then opens the order table page
 */
public class WelcomePageController {

	/*
	 * indicates whether a valid ID was already entered
	 */
	//private boolean idEntered = false;

	/*
	 * stores the user ID
	 */
	//private String id;

	/**
	 * stores the server address
	 */
	private String address;
	/** ResourceBundle that was given to the FXMLLoader */
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;
    /** URL location of the FXML file that was given to the FXMLLoader */
    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;
    /**
     * label used to command the user
     */
    @FXML // fx:id="commandLabel"
    private Label commandLabel; // Value injected by FXMLLoader
    /**
     * button used to confirm entered server address
     */
    @FXML // fx:id="confirmButton"
    private Button confirmButton; // Value injected by FXMLLoader
    /**
     * input field to enter server address
     */
    @FXML // fx:id="inputField"
    private TextField inputField; // Value injected by FXMLLoader
    /**
     * message label to give feedback to user
     */
    @FXML // fx:id="messageLabel"
    private Label messageLabel; // Value injected by FXMLLoader

    
    /**
     * This function handles pressing the confirm button.
     * 
     * The user enters the server address.
     * Then the client connects to the server and opens the opening screen.
     * 
     * @param event the event of pressing the confirm button
     */
    @FXML
    void btnClick(ActionEvent event) {
    	address = inputField.getText();

    	if (address == null || address.trim().isEmpty()) {
    		messageLabel.setTextFill(Color.RED);
    		messageLabel.setText("Please enter server address");
    		return;
    	}

    	launchOpeningScreen();
    }

    /**
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
        
        commandLabel.setText("Enter server address");
        inputField.setPromptText("Server address");
        messageLabel.setText("");

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
    /**
     * This function loads and opens the opening screen.
     * 
     * It creates the client controller, gives it to the screens that need server
     * communication, and replaces the current scene with the opening screen.
     */
    private void launchOpeningScreen() {
    	Stage stage = (Stage) inputField.getScene().getWindow();

    	ClientController clientController;

    	try {
    		/*
    		 * At this stage the user is not logged in yet, so we pass an empty id.
    		 * The actual user identity will be handled later through the login screens.
    		 */
    		clientController = new ClientController(address, common.CommonConstants.DEFAULT_PORT, "");

    		// Gives the occasional customer access screen the active ClientController.
    		OccasionalCustomerAccessController.setClientController(clientController);

    		// Gives the employee login screen the active ClientController.
    		EmployeeLoginController.setClientController(clientController);

    		// Gives the registered customer login screen the active ClientController.
    		ExistingCustomerLoginController.setClientController(clientController);

    	} catch (IOException e) {
    		messageLabel.setTextFill(Color.RED);
    		messageLabel.setText("Bad server address");
    		inputField.clear();
    		return;
    	}

    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/OpeningScreen.fxml"));

    	Parent root = null;

    	try {
    		root = loader.load();

    		OpeningScreenController controller = loader.getController();
    		controller.setClientController(clientController);
    		
    	} catch (IOException e) {
    		e.printStackTrace();
    		Platform.exit();
    		System.exit(1);
    	}

    	Scene scene = new Scene(root);
    	stage.setScene(scene);
    	stage.setTitle("GoNature");
    	stage.show();
    }
    
}