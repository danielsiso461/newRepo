package clientGUI;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import clientController.ClientController;
import common.CommonConstants;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Controls the welcome page.
 * 
 * The user first enters a user ID.
 * Then the user enters the server IP address.
 * After a successful connection, the order table page is opened.
 */
public class WelcomePageController {

    private boolean idEntered = false;
    private String id;
    private String address;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Label commandLabel;

    @FXML
    private Label inputTitleLabel;

    @FXML
    private Button confirmButton;

    @FXML
    private TextField inputField;

    @FXML
    private Label messageLabel;

    
    /*
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

    @FXML
    void initialize() {
        assert commandLabel != null : "fx:id=\"commandLabel\" was not injected: check your FXML file 'welcomePage.fxml'.";
        assert confirmButton != null : "fx:id=\"confirmButton\" was not injected: check your FXML file 'welcomePage.fxml'.";
        assert inputField != null : "fx:id=\"inputField\" was not injected: check your FXML file 'welcomePage.fxml'.";
        assert messageLabel != null : "fx:id=\"messageLabel\" was not injected: check your FXML file 'welcomePage.fxml'.";
        
        commandLabel.setText("Enter server address");
        inputField.setPromptText("Server address");
        messageLabel.setText("");

        showIdStep();

        messageLabel.textProperty().addListener((observable, oldText, newText) -> {
            updateMessageLabelVisibility();
        });

        updateMessageLabelVisibility();

        Platform.runLater(() -> {
            Stage stage = (Stage) inputField.getScene().getWindow();

            stage.setOnCloseRequest(event -> {
                Platform.exit();
                System.exit(0);
            });
        });
    }

    private void showIdStep() {
        idEntered = false;

        commandLabel.setText("Step 1: Enter your user ID");
        inputTitleLabel.setText("User ID");
        inputField.setPromptText("Enter 9-digit user ID");
        confirmButton.setText("Continue");

        inputField.clear();
        clearMessage();
    }

    private void showServerAddressStep() {
        idEntered = true;

        commandLabel.setText("Step 2: Enter the server IP address");
        inputTitleLabel.setText("Server IP Address");
        inputField.setPromptText("");
        confirmButton.setText("Connect");

        inputField.clear();
        clearMessage();
    }

    private void handleIdInput() {
        id = inputField.getText().trim();

        if (!id.matches("\\d{9}")) {
            showError("ID should contain exactly 9 digits");
            return;
        }

        if ("000000000".equals(id)) {
            showError("ID should be a valid positive ID");
            return;
        }

        showServerAddressStep();
    }

    private void handleAddressInput() {
        address = inputField.getText().trim();

        if (address.isEmpty()) {
            showError("Server IP address cannot be empty");
            return;
        }

        launchOrderTable();
    }
    /*
     * This function loads and opens the opening screen.
     * 
     * It creates the client controller, gives it to the screens that need server
     * communication, and replaces the current scene with the opening screen.
     */
    private void launchOpeningScreen() {
    	Stage stage = (Stage) inputField.getScene().getWindow();

    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/OpeningScreen.fxml"));

    	Parent root = null;

    	try {
    		root = loader.load();
    	} catch (IOException e) {
    		e.printStackTrace();
    		Platform.exit();
    		System.exit(1);
    	}

    	ClientController clientController;

    	try {
    		/*
    		 * At this stage the user is not logged in yet, so we pass an empty id.
    		 * The actual user identity will be handled later through the login screens.
    		 */
    		clientController = new ClientController(address, common.CommonConstants.DEFAULT_PORT, "");

    		// Gives the occasional customer access screen the active ClientController, so it can send requests to the server.
    		OccasionalCustomerAccessController.setClientController(clientController);
    		
    		// Gives the employee login screen the active ClientController, so it can send login requests to the server.
    		EmployeeLoginController.setClientController(clientController);
    		
    		// Gives the registered customer login screen the active ClientController, so it can send requests to the server.
    		ExistingCustomerLoginController.setClientController(clientController);

    	} catch (IOException e) {
    		messageLabel.setTextFill(Color.RED);
    		messageLabel.setText("Bad server address");
    		inputField.clear();
    		return;
    	}

    	Scene scene = new Scene(root);
    	stage.setScene(scene);
    	stage.setTitle("GoNature");
    	stage.show();
    }
    
}