package clientGUI;

import clientController.ClientController;
import common.Employee;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/*
 * This class is the controller for the park worker home page.
 * 
 * The park worker is responsible for operational park entrance actions,
 * such as checking visitor orders, approving entry, and recording exits.
 */
public class ParkWorkerHomePageController {

	private ClientController clientController;
	private Employee loggedInEmployee;

	@FXML
	private Label welcomeLabel;

	/*
	 * Sets the ClientController used by this screen.
	 * 
	 * @param clientController the active client controller
	 */
	public void setClientController(ClientController clientController) {
		this.clientController = clientController;
	}

	/*
	 * Sets the employee that logged in successfully.
	 * 
	 * @param employee the logged-in employee
	 */
	public void setLoggedInEmployee(Employee employee) {
		this.loggedInEmployee = employee;

		if (employee != null && welcomeLabel != null) {
			welcomeLabel.setText("Welcome " + employee.getFirstName() + " " + employee.getLastName());
		}
	}

	/*
	 * Handles click on search visitor order button.
	 */
	@FXML
	private void handleSearchVisitorOrder() {
		System.out.println("Search Visitor Order clicked");
	}

	/*
	 * Handles click on record visitor exit button.
	 */
	@FXML
	private void handleRecordVisitorExit() {
		System.out.println("Record Visitor Exit clicked");
	}

	/*
	 * Handles logout button.
	 */
	@FXML
	private void handleLogout() {
		System.out.println("Logout clicked");
	}
}