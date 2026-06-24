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
	private void handleCheckVisitorEntry(ActionEvent event) {
		System.out.println("Check Visitor Entry clicked");
	}

	@FXML
	private void handleWalkInVisitor(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/ParkVisitorCounterUpdatePage.fxml")
			);

			Parent root = loader.load();

			ParkVisitorCounterUpdatePageController controller = loader.getController();
			controller.setClientController(clientController);
			controller.setLoggedInEmployee(loggedInEmployee);

			if (clientController != null) {
				clientController.addParkVisitorCounterObserver(controller);
			}

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("Handle Walk-In Visitor");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void handleRecordVisitorExit(ActionEvent event) {
		System.out.println("Record Visitor Exit clicked");
	}

	/*
	 * Opens the park occupancy screen.
	 * 
	 * This screen shows the current number of visitors in the worker's park,
	 * the park capacity, and the number of available places.
	 */
	@FXML
	private void handleViewParkOccupancy(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/ParkVisitorCounterViewPage.fxml")
			);

			Parent root = loader.load();

			ParkVisitorCounterViewPageController controller = loader.getController();
			controller.setClientController(clientController);
			controller.setLoggedInEmployee(loggedInEmployee);


			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("Park Occupancy");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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