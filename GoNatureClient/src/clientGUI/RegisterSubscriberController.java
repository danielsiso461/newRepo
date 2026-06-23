package clientGUI;

import java.io.IOException;

import clientCommon.RegisterSubscriberObserver;
import clientController.ClientController;
import common.Employee;
import common.OperationResponse;
import common.RegisterSubscriberRequest;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/*
 * This class is the controller for the register subscriber screen.
 * 
 * The screen is used by a service representative to register a new family
 * subscriber in the GoNature system.
 */
public class RegisterSubscriberController implements RegisterSubscriberObserver {

	private ClientController clientController;
	private Employee loggedInEmployee;

	@FXML
	private TextField firstNameField;

	@FXML
	private TextField lastNameField;

	@FXML
	private TextField idNumberField;

	@FXML
	private TextField phoneField;

	@FXML
	private TextField emailField;

	@FXML
	private TextField familyMembersCountField;

	@FXML
	private TextField creditCardLast4Field;

	@FXML
	private ComboBox<String> paymentMethodComboBox;

	@FXML
	private Label messageLabel;
	
	@FXML
	private TextField usernameField;

	@FXML
	private PasswordField passwordField;

	@FXML
	private PasswordField confirmPasswordField;

	@FXML
	private void initialize() {
		paymentMethodComboBox.getItems().addAll("cash", "credit_card");
		paymentMethodComboBox.setValue("cash");

		messageLabel.setText("");
	}

	/*
	 * Sets the ClientController used by this screen.
	 * 
	 * @param clientController the active client controller
	 */
	public void setClientController(ClientController clientController) {
		this.clientController = clientController;

		if (this.clientController != null) {
			this.clientController.addRegisterSubscriberObserver(this);
		}
	}

	/*
	 * Sets the logged-in service representative.
	 * 
	 * @param employee the employee that opened this screen
	 */
	public void setLoggedInEmployee(Employee employee) {
		this.loggedInEmployee = employee;
	}

	/*
	 * Handles the register subscriber button.
	 * 
	 * For now, this method validates the form locally.
	 * Later, it will send the subscriber data to the server and database.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleRegisterSubscriber(ActionEvent event) {
		String firstName = firstNameField.getText();
		String lastName = lastNameField.getText();
		String idNumber = idNumberField.getText();
		String username = usernameField.getText();
		String password = passwordField.getText();
		String confirmPassword = confirmPasswordField.getText();
		String phone = phoneField.getText();
		String email = emailField.getText();
		String familyMembersCountText = familyMembersCountField.getText();
		String creditCardLast4 = creditCardLast4Field.getText();
		String paymentMethod = paymentMethodComboBox.getValue();

		if (firstName == null || firstName.trim().isEmpty()) {
			messageLabel.setText("Please enter first name.");
			return;
		}

		if (lastName == null || lastName.trim().isEmpty()) {
			messageLabel.setText("Please enter last name.");
			return;
		}

		if (idNumber == null || idNumber.trim().isEmpty()) {
			messageLabel.setText("Please enter ID number.");
			return;
		}

		if (!idNumber.trim().matches("\\d{9}")) {
			messageLabel.setText("ID number must contain 9 digits.");
			return;
		}

		if (phone == null || phone.trim().isEmpty()) {
			messageLabel.setText("Please enter phone number.");
			return;
		}

		if (email == null || email.trim().isEmpty()) {
			messageLabel.setText("Please enter email.");
			return;
		}

		if (familyMembersCountText == null || familyMembersCountText.trim().isEmpty()) {
			messageLabel.setText("Please enter family members count.");
			return;
		}

		int familyMembersCount;

		try {
			familyMembersCount = Integer.parseInt(familyMembersCountText.trim());
		} catch (NumberFormatException e) {
			messageLabel.setText("Family members count must be a number.");
			return;
		}

		if (familyMembersCount <= 0) {
			messageLabel.setText("Family members count must be greater than 0.");
			return;
		}

		if ("credit_card".equals(paymentMethod)) {
			if (creditCardLast4 == null || creditCardLast4.trim().isEmpty()) {
				messageLabel.setText("Please enter credit card last 4 digits.");
				return;
			}

			if (!creditCardLast4.trim().matches("\\d{4}")) {
				messageLabel.setText("Credit card last 4 must contain 4 digits.");
				return;
			}
		}
		
		if (username == null || username.trim().isEmpty()) {
			messageLabel.setText("Please enter username.");
			return;
		}

		username = username.trim();

		if (username.length() < 4) {
			messageLabel.setText("Username must contain at least 4 characters.");
			return;
		}

		if (!username.matches("[a-zA-Z0-9._]+")) {
			messageLabel.setText("Username can contain letters, digits, dot and underscore only.");
			return;
		}

		if (password == null || password.trim().isEmpty()) {
			messageLabel.setText("Please enter password.");
			return;
		}

		if (password.length() < 4) {
			messageLabel.setText("Password must contain at least 4 characters.");
			return;
		}

		if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
			messageLabel.setText("Please confirm password.");
			return;
		}

		if (!password.equals(confirmPassword)) {
			messageLabel.setText("Passwords do not match.");
			return;
		}

		System.out.println("Register subscriber clicked");
		System.out.println("First Name = " + firstName);
		System.out.println("Last Name = " + lastName);
		System.out.println("ID Number = " + idNumber);
		System.out.println("Phone = " + phone);
		System.out.println("Email = " + email);
		System.out.println("Family Members Count = " + familyMembersCount);
		System.out.println("Payment Method = " + paymentMethod);
		System.out.println("Credit Card Last 4 = " + creditCardLast4);
		System.out.println("Username = " + username);

		if (clientController == null) {
			messageLabel.setText("Client is not connected to server.");
			return;
		}

		RegisterSubscriberRequest request = new RegisterSubscriberRequest(
				firstName.trim(),
				lastName.trim(),
				idNumber.trim(),
				username.trim(),
				password,
				phone.trim(),
				email.trim(),
				familyMembersCount,
				paymentMethod,
				creditCardLast4 == null ? null : creditCardLast4.trim()
		);

		messageLabel.setText("Registering subscriber...");

		clientController.requestRegisterSubscriber(request);
	}
	
	/*
	 * Receives the register subscriber result from the ClientController.
	 * 
	 * @param response the response received from the server
	 */
	@Override
	public void onRegisterSubscriberResult(OperationResponse response) {
		Platform.runLater(() -> {
			if (response == null) {
				messageLabel.setText("No response from server.");
				return;
			}

			if (response.isSuccess()) {
				messageLabel.setText(response.getMessage());
				handleClear(null);
			} else {
				messageLabel.setText(response.getMessage());
			}
		});
	}

	/*
	 * Clears all form fields.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleClear(ActionEvent event) {
		firstNameField.clear();
		lastNameField.clear();
		idNumberField.clear();
		phoneField.clear();
		emailField.clear();
		familyMembersCountField.clear();
		creditCardLast4Field.clear();
		usernameField.clear();
		passwordField.clear();
		confirmPasswordField.clear();

		paymentMethodComboBox.setValue("cash");

		messageLabel.setText("");
	}

	/*
	 * Returns to the service representative home page.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleBack(ActionEvent event) {
		try {
			if (clientController != null) {
				clientController.removeRegisterSubscriberObserver(this);
			}
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/ServiceRepresentativeHomePage.fxml")
			);

			Parent root = loader.load();

			ServiceRepresentativeHomePageController controller = loader.getController();
			controller.setClientController(clientController);
			controller.setLoggedInEmployee(loggedInEmployee);

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("Service Representative Dashboard");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
			messageLabel.setText("Could not return to service representative screen.");
		}
	}
}