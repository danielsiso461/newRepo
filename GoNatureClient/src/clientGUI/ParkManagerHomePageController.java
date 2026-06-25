
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
import javafx.scene.control.Alert;

/**
 * Controller for the park manager home page.
 *
 * The park manager is responsible for managing a specific park,
 * including park parameters, reports, and operational data.
 */
public class ParkManagerHomePageController {

	/**
	 * the client controller used to communicate with the server
	 */
	private ClientController clientController;
	
	/**
	 * the currently logged-in employee
	 */
	private Employee loggedInEmployee;

	/**
	 * the label used to welcome the logged-in park manager
	 */
	@FXML
	private Label welcomeLabel;

	/**
	 * Sets the client controller.
	 *
	 * @param clientController the client controller
	 */
	public void setClientController(ClientController clientController) {
		this.clientController = clientController;
	}

	/**
	 * Sets the logged-in employee.
	 *
	 * This method also updates the welcome label.
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
	 * Handles click on the view park orders button.
	 *
	 * This screen allows the park manager to view the orders of his assigned park.
	 *
	 * @param event the button click event
	 */
	@FXML
	private void handleViewParkOrders(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/OrderTableDisplayPage.fxml")
			);

			Parent root = loader.load();

			OrderTableDisplayController controller = loader.getController();
			controller.setClientController(clientController);
			controller.setLoggedInEmployee(loggedInEmployee);
			controller.configureForParkManagerView();

			if (loggedInEmployee == null || loggedInEmployee.getParkId() == null) {
				System.out.println("Cannot load park orders: employee park ID is missing.");
			} else {
				controller.loadOrdersForPark(loggedInEmployee.getParkId());
			}

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("Park Orders");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles click on the request park parameter change button.
	 *
	 * This screen allows the park manager to request changes for park parameters.
	 *
	 * @param event the button click event
	 */
	@FXML
	private void handleRequestParameterChange(ActionEvent event) {
		try {
			System.out.println("Request Park Parameter Change clicked");
			System.out.println("clientController = " + clientController);
			System.out.println("loggedInEmployee = " + loggedInEmployee);

			if (clientController == null) {
				showError("ClientController is null. Cannot open request page.");
				return;
			}

			if (loggedInEmployee == null) {
				showError("Logged employee is missing. Cannot open request page.");
				return;
			}

			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/ParkParameterRequestPage.fxml")
			);

			Parent root = loader.load();

			ParkParameterRequestPageController controller = loader.getController();
			controller.setClientController(clientController);
			controller.setLoggedInEmployee(loggedInEmployee);

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("Request Park Parameter Change");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (Exception e) {
			e.printStackTrace();
			showError("Failed to open Park Parameter Request page. Check console.");
		}
	}

	/**
	 * Handles click on the view park reports button.
	 *
	 * This screen allows the park manager to view reports for his assigned park.
	 *
	 * @param event the button click event
	 */
	@FXML
	private void handleViewParkReports(ActionEvent event) {
		try {
			System.out.println("View Park Reports clicked");
			System.out.println("clientController = " + clientController);
			System.out.println("loggedInEmployee = " + loggedInEmployee);

			if (clientController == null) {
				showError("ClientController is null. Cannot open reports page.");
				return;
			}

			if (loggedInEmployee == null) {
				showError("Logged employee is missing. Cannot open reports page.");
				return;
			}

			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/ReportsPage.fxml")
			);

			Parent root = loader.load();

			ReportsPageController controller = loader.getController();
			controller.setClientController(clientController);
			controller.setLoggedInEmployee(loggedInEmployee);

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("Park Reports");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (Exception e) {
			e.printStackTrace();
			showError("Failed to open Reports page. Check console.");
		}
	}
	
	/**
	 * Handles click on the view user information button.
	 *
	 * This screen allows the park manager to search and view user information.
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
					"/clientGUI/ParkManagerHomePage.fxml",
					"Park Manager Dashboard"
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
	 * Shows an error alert.
	 *
	 * @param message the error message
	 */
	private void showError(String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Navigation Error");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	/**
	 * Handles logout from the park manager screen.
	 *
	 * This method notifies the server about logout, clears the client session,
	 * and returns the user to the opening screen.
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
