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

/**
 * This class is the controller for the department manager home page.
 * 
 * The department manager is responsible for central management actions,
 * such as approving park manager requests and viewing department-level reports.
 */
public class DepartmentManagerHomePageController {

	/**
	 * The client controller instance.
	 */
	private ClientController clientController;

	/**
	 * The currently logged-in employee.
	 */
	private Employee loggedInEmployee;

	/**
	 * Label used to welcome the logged-in user.
	 */
	@FXML
	private Label welcomeLabel;

	/**
	 * Sets the client controller for this controller.
	 * 
	 * @param clientController the client controller
	 */
	public void setClientController(ClientController clientController) {
		this.clientController = clientController;
	}

	/**
	 * Sets the current logged-in employee.
	 * 
	 * @param employee the employee to set
	 */
	public void setLoggedInEmployee(Employee employee) {
		this.loggedInEmployee = employee;

		if (employee != null && welcomeLabel != null) {
			welcomeLabel.setText("Welcome " + employee.getFirstName() + " " + employee.getLastName());
		}
	}

	/**
	 * Handles click on the reports button.
	 * 
	 * This screen allows the department manager to view department-level reports,
	 * such as visit reports and cancellation reports.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleViewReports(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/ReportsPage.fxml")
			);

			Parent root = loader.load();

			ReportsPageController controller = loader.getController();
			controller.setClientController(clientController);
			controller.setLoggedInEmployee(loggedInEmployee);

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("Reports");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles click on the approve park requests button.
	 * 
	 * This screen allows the department manager to review, approve, and reject
	 * pending park parameter change requests.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleApproveParkRequests(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/ParkParameterApprovalPage.fxml")
			);

			Parent root = loader.load();

			ParkParameterApprovalPageController controller = loader.getController();
			controller.setClientController(clientController);
			controller.setLoggedInEmployee(loggedInEmployee);

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("Approve Park Requests");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles click on the park occupancy button.
	 * 
	 * This screen shows the current occupancy of parks, including current visitors,
	 * capacity, and available space.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleViewParksOccupancy(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/ParkVisitorCounterViewPage.fxml")
			);

			Parent root = loader.load();

			ParkVisitorCounterViewPageController controller = loader.getController();
			controller.setClientController(clientController);
			controller.setLoggedInEmployee(loggedInEmployee);

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("Parks Occupancy");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles logout from the department manager screen.
	 * 
	 * For now, this returns the user to the opening screen.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleLogout(ActionEvent event) {
		try {
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