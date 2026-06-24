package clientGUI;

import java.io.IOException;

import clientCommon.ExistingCustomerLoginObserver;
import clientController.ClientController;
import common.OperationResponse;
import common.Subscriber;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/*
 * This class is the controller for the existing customer login screen.
 * 
 * The screen allows an existing customer to log in using username and password.
 * The login request is sent to the server and checked against the subscriber
 * table in the database.
 */
public class ExistingCustomerLoginController implements ExistingCustomerLoginObserver {

	/*
	 * The client controller used for communication with the server.
	 */
	private static ClientController clientController;

	@FXML
	private TextField usernameField;

	@FXML
	private PasswordField passwordField;

	@FXML
	private Label messageLabel;

	/*
	 * Sets the ClientController used by this screen.
	 * 
	 * @param controller the client controller instance
	 */
	public static void setClientController(ClientController controller) {
		clientController = controller;
	}

	/*
	 * Initializes the screen.
	 * 
	 * This method registers the screen as an observer so it can receive the server
	 * response for existing customer login.
	 */
	@FXML
	private void initialize() {
		if (clientController != null) {
			clientController.addExistingCustomerLoginObserver(this);
		}
	}

	/*
	 * Handles the click on the Login button.
	 * 
	 * The method validates that the username and password fields are not empty.
	 * Then it sends an existing customer login request to the server.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleLogin(ActionEvent event) {
		String username = usernameField.getText();
		String password = passwordField.getText();

		if (username == null || username.trim().isEmpty()) {
			messageLabel.setText("Please enter username.");
			return;
		}

		if (password == null || password.trim().isEmpty()) {
			messageLabel.setText("Please enter password.");
			return;
		}

		if (clientController == null) {
			messageLabel.setText("Client is not connected to server.");
			return;
		}

		messageLabel.setText("Checking login details...");

		clientController.requestExistingCustomerLogin(username.trim(), password.trim());
	}

	/*
	 * Receives the existing customer login result from the ClientController.
	 * 
	 * If the customer login succeeds, the method opens the order table screen
	 * and loads all orders of the logged-in subscriber.
	 * 
	 * @param response the response received from the server
	 */
	@Override
	public void onExistingCustomerLoginResult(OperationResponse response) {
		Platform.runLater(() -> {
			if (response == null) {
				messageLabel.setText("No response from server.");
				return;
			}

			if (response.isSuccess()) {
				Subscriber subscriber = (Subscriber) response.getData();
				
				clientController.setLoggedInSubscriberId(subscriber.getSubscriberId());
				System.out.println("Logged in subscriber ID = " + clientController.getLoggedInSubscriberId());

				messageLabel.setText("Login successful. Welcome " + subscriber.getSubscriberName());

				System.out.println("Existing customer login successful:");
				System.out.println("Subscriber = " + subscriber);

				openOrderTableScreen(subscriber);

			} else {
				messageLabel.setText(response.getMessage());
			}
		});
	}
	
	/*
	 * Opens the order table screen and loads all orders that belong to the
	 * logged-in subscriber.
	 * 
	 * @param subscriber the subscriber that logged in successfully
	 */
	private void openOrderTableScreen(Subscriber subscriber) {
		try {
			if (clientController != null) {
				clientController.removeExistingCustomerLoginObserver(this);
			}

			FXMLLoader loader = new FXMLLoader(
					getClass().getResource(ConstantsUI.orderTable)
			);

			Parent root = loader.load();

			OrderTableDisplayController controller = loader.getController();

			/*
			 * Gives the order table screen the active ClientController.
			 */
			controller.setClientController(clientController);
			controller.configureForCustomerView();

			Scene scene = new Scene(root);

			Stage stage = (Stage) usernameField.getScene().getWindow();
			stage.setScene(scene);
			stage.setTitle("Order Table");
			stage.show();

			/*
			 * Requests all orders that belong to the logged-in subscriber.
			 */
			clientController.requestOrdersBySubscriberId(subscriber.getSubscriberId());

			Platform.runLater(controller);

		} catch (IOException e) {
			e.printStackTrace();
			messageLabel.setText("Failed to open order table screen.");
		}
	}

	/*
	 * Handles the click on the Back button.
	 * 
	 * This method removes this screen from the observer list and navigates the user
	 * back to the Customer Access screen.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleBack(ActionEvent event) {
		try {
			if (clientController != null) {
				clientController.removeExistingCustomerLoginObserver(this);
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

		} catch (IOException e) {
			e.printStackTrace();
			messageLabel.setText("Could not return to customer access screen.");
		}
	}
}