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
