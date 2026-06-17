package clientGUI;

import clientController.ClientController;
import common.Employee;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/*
 * This class is the controller for the park manager home page.
 * 
 * The park manager is responsible for managing a specific park,
 * including park parameters, reports, promotions, and operational data.
 */
public class ParkManagerHomePageController {

	private ClientController clientController;
	private Employee loggedInEmployee;

	@FXML
	private Label welcomeLabel;

	public void setClientController(ClientController clientController) {
		this.clientController = clientController;
	}

	public void setLoggedInEmployee(Employee employee) {
		this.loggedInEmployee = employee;

		if (employee != null && welcomeLabel != null) {
			welcomeLabel.setText("Welcome " + employee.getFirstName() + " " + employee.getLastName());
		}
	}

	@FXML
	private void handleViewParkOrders() {
		System.out.println("View Park Orders clicked");
	}

	@FXML
	private void handleRequestParameterChange() {
		System.out.println("Request Park Parameter Change clicked");
	}

	@FXML
	private void handleViewParkReports() {
		System.out.println("View Park Reports clicked");
	}

	@FXML
	private void handleManagePromotions() {
		System.out.println("Manage Promotions clicked");
	}

	@FXML
	private void handleLogout() {
		System.out.println("Logout clicked");
	}
}
