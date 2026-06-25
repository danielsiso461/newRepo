
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
import clientGUI.ParkEntranceControlController.EntranceMode;

/**
 * Controller for the park worker home page.
 *
 * The park worker is responsible for operational park entrance actions,
 * such as checking visitor entry, handling walk-in visitors, recording exits,
 * and viewing current park occupancy.
 */
public class ParkWorkerHomePageController {

	/**
	 * the client controller used to communicate with the server
	 */
	private ClientController clientController;

	/**
	 * the currently logged-in employee
	 */
	private Employee loggedInEmployee;

	/**
	 * the label used to welcome the logged-in park worker
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
	 * Handles the click on the check visitor entry button.
	 *
	 * @param event the button click event
	 */
	@FXML
	private void handleCheckVisitorEntry(ActionEvent event) {
		openParkEntranceControlPage(event, EntranceMode.CHECK_IN);
	}

	/**
	 * Handles the click on the walk-in visitor button.
	 *
	 * @param event the button click event
	 */
	@FXML
	private void handleWalkInVisitor(ActionEvent event) {
		openParkEntranceControlPage(event, EntranceMode.OCCASIONAL_VISIT);
	}

	/**
	 * Handles the click on the record visitor exit button.
	 *
	 * @param event the button click event
	 */
	@FXML
	private void handleRecordVisitorExit(ActionEvent event) {
		openParkEntranceControlPage(event, EntranceMode.CHECK_OUT);
	}

	/**
	 * Handles the click on the view park occupancy button.
	 *
	 * @param event the button click event
	 */
	@FXML
	private void handleViewParkOccupancy(ActionEvent event) {
		openParkEntranceControlPage(event, EntranceMode.CURRENT_VISITORS);
	}

	/**
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
	
	/**
	 * Handles the click on the entry payment receipt button.
	 *
	 * This method opens the entry payment page.
	 *
	 * @param event the button click event
	 */
	@FXML
	private void handleEntryPaymentReceipt(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/EntryPaymentPage.fxml")
			);

			Parent root = loader.load();

			EntryPaymentPageController controller = loader.getController();
			controller.setClientController(clientController);
			controller.setPrevScene(((Node) event.getSource()).getScene());

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("Entry Payment Receipt");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Handles the click on the My Details button.
	 *
	 * This method opens the user information page for the logged-in employee.
	 *
	 * @param event the button click event
	 */
	@FXML
	private void handleMyDetails(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/UserInformationPage.fxml")
			);

			Parent root = loader.load();

			UserInformationPageController controller = loader.getController();
			controller.setClientController(clientController);
			controller.setLoggedInEmployee(loggedInEmployee);
			controller.setPreviousScreen(
					"/clientGUI/ParkWorkerHomePage.fxml",
					"Park Worker Dashboard"
			);
			controller.configureForMyDetails();

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("My Details");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles logout from the park worker screen.
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
