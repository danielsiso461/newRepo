package clientGUI;

import java.io.IOException;

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
 * Later, the login request will be sent to the server and checked against
 * the relevant customer/subscriber data in the database.
 */
public class ExistingCustomerLoginController {

	/*
	 * Text field used for entering the customer username.
	 */
	@FXML
	private TextField usernameField;

	/*
	 * Password field used for entering the customer password.
	 */
	@FXML
	private PasswordField passwordField;

	/*
	 * Label used for showing validation messages or login result messages.
	 */
	@FXML
	private Label messageLabel;

	/*
	 * Handles the click on the Login button.
	 * 
	 * At this stage, the method only validates that the fields are not empty.
	 * Later, it will send a login request to the server.
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

		System.out.println("Existing customer login clicked");
		System.out.println("Username = " + username);
		System.out.println("Password = " + password);

		messageLabel.setText("Login request sent.");
	}

	/*
	 * Handles the click on the Back button.
	 * 
	 * This method navigates the user back to the Customer Access screen.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleBack(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/CustomerAccess.fxml")
			);

			Parent root = loader.load();

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("Customer Access");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}