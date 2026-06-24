package clientGUI;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import java.io.IOException;

import javafx.scene.Node;

import clientCommon.EmployeeLoginObserver;
import clientController.ClientController;
import common.Employee;
import common.OperationResponse;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

/**
 * This class is the controller for the employee login screen.
 * 
 * The screen allows an employee to enter a username and password.
 * The login request is sent to the server and checked against the employee
 * table in the database.
 */
public class EmployeeLoginController implements EmployeeLoginObserver {

	/**
	 * The client controller used for communication with the server.
	 */
	private static ClientController clientController;
	
	/**
	 * the username text field
	 */
	@FXML
	private TextField usernameField;
	/**
	 * the password passwordField
	 */
	@FXML
	private PasswordField passwordField;
	/**
	 * message label for feedback to the user
	 */
	@FXML
	private Label messageLabel;

	/**
	 * Sets the ClientController used by this screen.
	 * 
	 * @param controller the client controller instance
	 */
	public static void setClientController(ClientController controller) {
		clientController = controller;
	}

	/**
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

	/**
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

	/**
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

				openEmployeeHomeScreen(employee);

			} else {
				messageLabel.setText(response.getMessage());
			}
		});
	}
	
	/**
	 * Opens the correct employee home screen according to the employee role.
	 * 
	 * @param employee the employee that logged in successfully
	 */
	private void openEmployeeHomeScreen(Employee employee) {
		try {
			if (clientController != null) {
				clientController.removeEmployeeLoginObserver(this);
			}

			String role = employee.getRole();

			FXMLLoader loader;
			Parent root;
			Stage stage = (Stage) usernameField.getScene().getWindow();

			switch (role) {

			case "park_worker":
				loader = new FXMLLoader(getClass().getResource("/clientGUI/ParkWorkerHomePage.fxml"));
				root = loader.load();

				ParkWorkerHomePageController parkWorkerController = loader.getController();
				parkWorkerController.setClientController(clientController);
				parkWorkerController.setLoggedInEmployee(employee);

				stage.setTitle("Park Worker Dashboard");
				stage.setScene(new Scene(root));
				stage.show();
				break;

			case "service_representative":
				loader = new FXMLLoader(getClass().getResource("/clientGUI/ServiceRepresentativeHomePage.fxml"));
				root = loader.load();

				ServiceRepresentativeHomePageController serviceController = loader.getController();
				serviceController.setClientController(clientController);
				serviceController.setLoggedInEmployee(employee);

				stage.setTitle("Service Representative Dashboard");
				stage.setScene(new Scene(root));
				stage.show();
				break;

			case "park_manager":
				loader = new FXMLLoader(getClass().getResource("/clientGUI/ParkManagerHomePage.fxml"));
				root = loader.load();

				ParkManagerHomePageController parkManagerController = loader.getController();
				parkManagerController.setClientController(clientController);
				parkManagerController.setLoggedInEmployee(employee);

				stage.setTitle("Park Manager Dashboard");
				stage.setScene(new Scene(root));
				stage.show();
				break;

			case "department_manager":
				loader = new FXMLLoader(getClass().getResource("/clientGUI/DepartmentManagerHomePage.fxml"));
				root = loader.load();

				DepartmentManagerHomePageController departmentController = loader.getController();
				departmentController.setClientController(clientController);
				departmentController.setLoggedInEmployee(employee);

				stage.setTitle("Department Manager Dashboard");
				stage.setScene(new Scene(root));
				stage.show();
				break;

			default:
				messageLabel.setText("Unknown employee role: " + role);
				break;
			}

		} catch (IOException e) {
			e.printStackTrace();
			messageLabel.setText("Failed to open employee screen.");
		}
	}

	/**
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