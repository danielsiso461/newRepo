package clientGUI;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;

import javafx.scene.Node;
import java.io.IOException;
import clientController.ClientController;
import common.Employee;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * This class is the controller for the department manager home page.
 * 
 * The department manager is responsible for central management actions,
 * such as approving park manager requests and viewing department-level reports.
 */
public class DepartmentManagerHomePageController {
	/**
	 * the client controller instance
	 */
	private ClientController clientController;
	/**
	 * the employee entity
	 */
	private Employee loggedInEmployee;
	/**
	 * label to welcome the user
	 */
	@FXML
	private Label welcomeLabel;
	/**
	 * method that sets the client controller for this controller
	 * @param clientController the client controller
	 */
	public void setClientController(ClientController clientController) {
		this.clientController = clientController;
	}
	/**
	 * method that sets the current logged in employee
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
	 * This screen will allow the department manager to view department-level reports,
	 * such as visit reports and cancellation reports.
	 */
	@FXML
	private void handleViewReports() {
		System.out.println("View Reports clicked");

		/*
		 * Later:
		 * Open DepartmentManagerReports.fxml
		 */
	}
	/**
	 * method that let's you know approve park requests was clicked
	 */
	@FXML
	private void handleApproveParkRequests() {
		System.out.println("Approve Park Requests clicked");
	}

	
	/**
	 * Handles click on the park occupancy button.
	 * 
	 * This screen will show the current occupancy of parks,
	 * including current visitors, capacity, and available space.
	 */
	@FXML
	private void handleViewParkOccupancy() {
		System.out.println("View Park Occupancy clicked");

		/*
		 * Later:
		 * Open DepartmentManagerParkOccupancy.fxml
		 */
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
		}
	}
}
