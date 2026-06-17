package clientGUI;

import clientController.ClientController;
import common.Employee;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/*
 * This class is the controller for the department manager home page.
 * 
 * The department manager is responsible for central management actions,
 * such as approving park manager requests and viewing department-level reports.
 */
public class DepartmentManagerHomePageController {

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
	private void handleApproveParkRequests() {
		System.out.println("Approve Park Requests clicked");
	}

	@FXML
	private void handleViewDepartmentReports() {
		System.out.println("View Department Reports clicked");
	}

	@FXML
	private void handleViewParksStatus() {
		System.out.println("View Parks Status clicked");
	}

	@FXML
	private void handleViewCancellationsReport() {
		System.out.println("View Cancellations Report clicked");
	}

	@FXML
	private void handleLogout() {
		System.out.println("Logout clicked");
	}
}
