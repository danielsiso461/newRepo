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

/**
 * This class is the controller for the register subscriber screen.
 * 
 * The screen is used by a service representative to register a new family
 * subscriber in the GoNature system.
 */
public class RegisterSubscriberController implements RegisterSubscriberObserver {
	/**
	 * Cash payment method.
	 */
	private static final String PAYMENT_CASH = "cash";
	/**
	 * Credit card payment method.
	 */
	private static final String PAYMENT_CREDIT_CARD = "credit_card";
	/**
	 * Client controller used to communicate with the server.
	 */
	private ClientController clientController;
	/**
	 * The logged-in service representative.
	 */
	private Employee loggedInEmployee;
	/** Subscriber first name field. */
	@FXML
	private TextField firstNameField;
	/** Subscriber last name field. */
	@FXML
	private TextField lastNameField;
	/** Subscriber ID number field. */
	@FXML
	private TextField idNumberField;
	/** Subscriber phone number field. */
	@FXML
	private TextField phoneField;
	/** Subscriber email field. */
	@FXML
	private TextField emailField;
	/** Family members count field. */
	@FXML
	private TextField familyMembersCountField;
	/** Credit card last four digits field. */
	@FXML
	private TextField creditCardLast4Field;
	/** Payment method selector. */
	@FXML
	private ComboBox<String> paymentMethodComboBox;
	/** Displays registration status messages. */
	@FXML
	private Label messageLabel;
	/** Username field. */
	@FXML
	private TextField usernameField;
	/** Password field. */
	@FXML
	private PasswordField passwordField;
	/** Password confirmation field. */
	@FXML
	private PasswordField confirmPasswordField;
	/**
	 * Initializes the register subscriber page.
	 */
	@FXML
	private void initialize() {
		paymentMethodComboBox.getItems().setAll(PAYMENT_CASH, PAYMENT_CREDIT_CARD);
		paymentMethodComboBox.setValue(PAYMENT_CASH);

		updatePaymentMethodFields();

		paymentMethodComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
			updatePaymentMethodFields();
		});

		messageLabel.setText("");
	}

	/**
	 * Updates the credit card field according to the selected payment method.
	 * 
	 * If payment is cash, the credit card field is blocked and cleared.
	 * If payment is credit_card, the credit card field is enabled and required.
	 */
	private void updatePaymentMethodFields() {
		String paymentMethod = paymentMethodComboBox.getValue();

		if (PAYMENT_CASH.equals(paymentMethod)) {
			creditCardLast4Field.clear();
			creditCardLast4Field.setDisable(true);
			creditCardLast4Field.setPromptText("Not required for cash payment");
			return;
		}

		if (PAYMENT_CREDIT_CARD.equals(paymentMethod)) {
			creditCardLast4Field.setDisable(false);
			creditCardLast4Field.setPromptText("Enter Credit Card Last 4 Digits");
		}
	}

	/**
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

	/**
	 * Sets the logged-in service representative.
	 * 
	 * @param employee the employee that opened this screen
	 */
	public void setLoggedInEmployee(Employee employee) {
		this.loggedInEmployee = employee;
	}

	/**
	 * Handles the register subscriber button.
	 * 
	 * Validates the form locally and sends the subscriber data to the server.
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
			showError("Please enter first name.");
			return;
		}

		if (lastName == null || lastName.trim().isEmpty()) {
			showError("Please enter last name.");
			return;
		}

		if (idNumber == null || idNumber.trim().isEmpty()) {
			showError("Please enter ID number.");
			return;
		}

		if (!idNumber.trim().matches("\\d{9}")) {
			showError("ID number must contain 9 digits.");
			return;
		}

		if (phone == null || phone.trim().isEmpty()) {
			showError("Please enter phone number.");
			return;
		}

		if (email == null || email.trim().isEmpty()) {
			showError("Please enter email.");
			return;
		}

		if (familyMembersCountText == null || familyMembersCountText.trim().isEmpty()) {
			showError("Please enter family members count.");
			return;
		}

		int familyMembersCount;

		try {
			familyMembersCount = Integer.parseInt(familyMembersCountText.trim());
		} catch (NumberFormatException e) {
			showError("Family members count must be a number.");
			return;
		}

		if (familyMembersCount <= 0) {
			showError("Family members count must be greater than 0.");
			return;
		}

		if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
			showError("Please select payment method.");
			return;
		}

		if (PAYMENT_CASH.equals(paymentMethod)) {
			creditCardLast4 = null;
		}

		if (PAYMENT_CREDIT_CARD.equals(paymentMethod)) {
			if (creditCardLast4 == null || creditCardLast4.trim().isEmpty()) {
				showError("Please enter credit card last 4 digits.");
				return;
			}

			if (!creditCardLast4.trim().matches("\\d{4}")) {
				showError("Credit card last 4 must contain 4 digits.");
				return;
			}

			creditCardLast4 = creditCardLast4.trim();
		}

		if (username == null || username.trim().isEmpty()) {
			showError("Please enter username.");
			return;
		}

		username = username.trim();

		if (username.length() < 4) {
			showError("Username must contain at least 4 characters.");
			return;
		}

		if (!username.matches("[a-zA-Z0-9._]+")) {
			showError("Username can contain letters, digits, dot and underscore only.");
			return;
		}

		if (password == null || password.trim().isEmpty()) {
			showError("Please enter password.");
			return;
		}

		if (password.length() < 4) {
			showError("Password must contain at least 4 characters.");
			return;
		}

		if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
			showError("Please confirm password.");
			return;
		}

		if (!password.equals(confirmPassword)) {
			showError("Passwords do not match.");
			return;
		}

		if (clientController == null) {
			showError("Client is not connected to server.");
			return;
		}

		RegisterSubscriberRequest request = new RegisterSubscriberRequest(
				firstName.trim(),
				lastName.trim(),
				idNumber.trim(),
				username,
				password,
				phone.trim(),
				email.trim(),
				familyMembersCount,
				paymentMethod,
				creditCardLast4
		);

		showInfo("Registering subscriber...");

		clientController.requestRegisterSubscriber(request);
	}

	/**
	 * Receives the register subscriber result from the ClientController.
	 * 
	 * @param response the response received from the server
	 */
	@Override
	public void onRegisterSubscriberResult(OperationResponse response) {
		Platform.runLater(() -> {
			if (response == null) {
				showError("Registration failed. No response from server.");
				return;
			}

			if (response.isSuccess()) {
				clearFieldsOnly();
				showSuccess(response.getMessage() == null
						? "Subscriber registered successfully."
						: response.getMessage());
			} else {
				showError(response.getMessage() == null
						? "Subscriber registration failed."
						: response.getMessage());
			}
		});
	}

	/**
	 * Clears all form fields.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleClear(ActionEvent event) {
		clearFieldsOnly();
		showInfo("");
	}
	/**
	 * Clears all input fields.
	 */
	private void clearFieldsOnly() {
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

		paymentMethodComboBox.setValue(PAYMENT_CASH);
		updatePaymentMethodFields();
	}
	/**
	 * Displays a success message.
	 *
	 * @param message the message to display
	 */
	private void showSuccess(String message) {
		messageLabel.setStyle("-fx-text-fill: green;");
		messageLabel.setText(message);
	}
	/**
	 * Displays an error message.
	 *
	 * @param message the message to display
	 */
	private void showError(String message) {
		messageLabel.setStyle("-fx-text-fill: red;");
		messageLabel.setText(message);
	}
	/**
	 * Displays an informational message.
	 *
	 * @param message the message to display
	 */
	private void showInfo(String message) {
		messageLabel.setStyle("-fx-text-fill: #2f5d8c;");
		messageLabel.setText(message);
	}

	/**
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
			showError("Could not return to service representative screen.");
		}
	}
}