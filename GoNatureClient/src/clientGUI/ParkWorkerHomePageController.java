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
import clientGUI.ParkEntranceControlController.EntranceMode;

/*
 * This class is the controller for the park worker home page.
 * 
 * The park worker is responsible for operational park entrance actions,
 * such as checking visitor entry, handling walk-in visitors, recording exits,
 * and viewing current park occupancy.
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
	@FXML
	private void handleCheckVisitorEntry(ActionEvent event) {
		openParkEntranceControlPage(event, EntranceMode.CHECK_IN);
	}

	@FXML
	private void handleWalkInVisitor(ActionEvent event) {
		openParkEntranceControlPage(event, EntranceMode.OCCASIONAL_VISIT);
	}

	@FXML
	private void handleRecordVisitorExit(ActionEvent event) {
		openParkEntranceControlPage(event, EntranceMode.CHECK_OUT);
	}

	@FXML
	private void handleViewParkOccupancy(ActionEvent event) {
		openParkEntranceControlPage(event, EntranceMode.CURRENT_VISITORS);
	}

	/*
	 * Opens the park entrance control page.
	 * 
	 * This page includes check-in, check-out, occasional visit,
	 * and current visitors actions.
	 * 
	 * @param event the button click event
	 * @param entranceMode the entrance action mode to open
	 */
	private void openParkEntranceControlPage(ActionEvent event, EntranceMode entranceMode) {
		try {
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/ParkEntranceControlPage.fxml")
			);

			Parent root = loader.load();

			ParkEntranceControlController controller = loader.getController();
			controller.setClientController(clientController);
			controller.setLoggedInEmployee(loggedInEmployee);
			controller.setEntranceMode(entranceMode);

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("Park Entrance Control");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
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