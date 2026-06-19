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

	/*
	 * Opens the visitor entry check screen.
	 * 
	 * This functionality will allow the park worker to search for an existing
	 * order by visitor ID and confirmation code, then approve the entry.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleCheckVisitorEntry(ActionEvent event) {
		System.out.println("Check Visitor Entry clicked");

		/*
		 * Later:
		 * Open ParkWorkerCheckEntry.fxml
		 */
	}

	/*
	 * Opens the walk-in visitor handling screen.
	 * 
	 * This functionality will allow the park worker to check available space
	 * and approve entry for visitors without a prior order.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleWalkInVisitor(ActionEvent event) {
		System.out.println("Handle Walk-In Visitor clicked");

		/*
		 * Later:
		 * Open ParkWorkerWalkInVisitor.fxml
		 */
	}

	/*
	 * Opens the visitor exit recording screen.
	 * 
	 * This functionality will allow the park worker to record visitors leaving
	 * the park and update the current number of visitors.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleRecordVisitorExit(ActionEvent event) {
		System.out.println("Record Visitor Exit clicked");

		/*
		 * Later:
		 * Open ParkWorkerRecordExit.fxml
		 */
	}

	/*
	 * Opens the park occupancy screen.
	 * 
	 * This functionality will show the current number of visitors in the park,
	 * the park capacity, and the number of available places.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleViewParkOccupancy(ActionEvent event) {
		System.out.println("View Park Occupancy clicked");

		/*
		 * Later:
		 * Open ParkWorkerParkOccupancy.fxml
		 */
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