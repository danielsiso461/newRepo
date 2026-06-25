package clientGUI;

import java.io.IOException;

import clientController.ClientController;
import common.Employee;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import clientCommon.ClientSession;


/**
 * This class is the controller for the service representative home page.
 * 
 * The service representative is responsible for customer service actions,
 * such as registering subscribers, registering guides, and assisting customers.
 */
public class ServiceRepresentativeHomePageController {
	/**
	 * Client controller used to communicate with the server.
	 */
	private ClientController clientController;
	/**
	 * The employee currently logged in.
	 */
	private Employee loggedInEmployee;
	/**
	 * Displays the welcome message for the logged-in employee.
	 */
	@FXML
	private Label welcomeLabel;
	/**
	 * Sets the client controller.
	 *
	 * @param clientController the client controller to use
	 */
	public void setClientController(ClientController clientController) {
		this.clientController = clientController;
	}
	/**
	 * Sets the logged-in employee and updates the welcome label.
	 *
	 * @param employee the logged-in employee
	 */
	public void setLoggedInEmployee(Employee employee) {
		this.loggedInEmployee = employee;

		if (employee != null && welcomeLabel != null) {
			welcomeLabel.setText("Welcome " + employee.getFirstName() + " " + employee.getLastName());
		}
	}

	/**
	 * Opens the subscriber registration screen.
	 * 
	 * This screen is used by the service representative to register a new family
	 * subscriber in the system.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleRegisterSubscriber(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/RegisterSubscriber.fxml")
			);

			Parent root = loader.load();

			RegisterSubscriberController controller = loader.getController();
			controller.setClientController(clientController);
			controller.setLoggedInEmployee(loggedInEmployee);

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("Register Subscriber");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Opens the guide registration screen.
	 * 
	 * This screen is used by the service representative to register subscribers
	 * as authorized guides for organized group orders.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleRegisterGuide(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/RegisterGuide.fxml")
			);

			Parent root = loader.load();

			RegisterGuideController controller = loader.getController();
			controller.setClientController(clientController);
			controller.setLoggedInEmployee(loggedInEmployee);

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("Register Guide");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Opens the customer orders screen.
	 *
	 * This screen allows the service representative to view customer orders.
	 *
	 * @param event the button click event
	 */
	@FXML
	private void handleViewCustomerOrders(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/OrderTableDisplayPage.fxml")
			);

			Parent root = loader.load();

			OrderTableDisplayController controller = loader.getController();
			controller.setClientController(clientController);
			controller.setLoggedInEmployee(loggedInEmployee);
			controller.configureForServiceRepresentativeView();
			controller.loadAllOrdersForServiceRepresentative();

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("Customer Orders");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Opens the user information screen.
	 * 
	 * This screen allows the service representative to view information
	 * about subscribers and employees.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleViewUserInformation(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/UserInformationPage.fxml")
			);

			Parent root = loader.load();

			UserInformationPageController controller = loader.getController();
			controller.setClientController(clientController);
			controller.setLoggedInEmployee(loggedInEmployee);
			
			controller.setPreviousScreen(
					"/clientGUI/ServiceRepresentativeHomePage.fxml",
					"Service Representative Dashboard"
			);

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("View User Information");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles logout from the park worker screen.
	 * 
	 * For now, this returns the user to the opening screen.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleLogout(ActionEvent event) {
		try {
			if (clientController != null) {
				clientController.logoutCurrentUserFromServer();
			}

			ClientSession.clear();

			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/OpeningScreen.fxml")
			);

			Parent root = loader.load();

			OpeningScreenController controller = loader.getController();
			controller.setClientController(clientController);

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("GoNature");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}