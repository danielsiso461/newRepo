package clientGUI;

import java.io.IOException;

import clientCommon.EmployeeLoginObserver;
import clientController.ClientController;
import common.Employee;
import common.OperationResponse;
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
 * This class is the controller for the employee login screen.
 * 
 * The screen allows an employee to enter a username and password.
 * The login request is sent to the server and checked against the employee
 * table in the database.
 */
public class EmployeeLoginController implements EmployeeLoginObserver {

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
	 * response for employee login.
	 */
	@FXML
	private void initialize() {
		if (clientController != null) {
			clientController.addEmployeeLoginObserver(this);
		}
	}

	/*
	 * Handles the click on the Login button.
	 * 
	 * The method validates that the username and password fields are not empty.
	 * Then it sends an employee login request to the server.
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

		clientController.requestEmployeeLogin(username.trim(), password.trim());
	}

	/*
	 * Receives the employee login result from the ClientController.
	 * 
	 * @param response the response received from the server
	 */
	@Override
	public void onEmployeeLoginResult(OperationResponse response) {
		Platform.runLater(() -> {
			if (response == null) {
				messageLabel.setText("No response from server.");
				return;
			}

			if (response.isSuccess()) {
				Employee employee = (Employee) response.getData();

				messageLabel.setText("Login successful. Welcome " + employee.getFirstName());

				System.out.println("Employee login successful:");
				System.out.println("Employee = " + employee);

				/*
				 * Later:
				 * Navigate to the relevant employee screen according to employee role.
				 */

			} else {
				messageLabel.setText(response.getMessage());
			}
		});
	}

	/*
	 * Handles the click on the Back button.
	 * 
	 * This method removes this screen from the observer list and navigates the user
	 * back to the opening screen.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleBack(ActionEvent event) {
		try {
			if (clientController != null) {
				clientController.removeEmployeeLoginObserver(this);
			}

			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/OpeningScreen.fxml")
			);

			Parent root = loader.load();

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("GoNature");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
			messageLabel.setText("Could not return to opening screen.");
		}
	}
}