package clientGUI;

import clientController.ClientController;
import common.Employee;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;


/*
 * This class is the controller for the service representative home page.
 * 
 * The service representative is responsible for customer service actions,
 * such as registering subscribers, registering guides, and assisting customers.
 */
public class ServiceRepresentativeHomePageController {

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
	private void handleRegisterSubscriber() {
		System.out.println("Register Subscriber clicked");
	}

	/**
	 * Opens the guide registration screen.
	 * 
	 * This screen is used by the service representative to register subscribers
	 * as authorized guides for organized group orders.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleRegisterGuide(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/RegisterGuide.fxml")
			);

			Parent root = loader.load();

			RegisterGuideController controller = loader.getController();
			controller.setClientController(clientController);
			controller.setLoggedInEmployee(loggedInEmployee);

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("Register Guide");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void handleViewCustomerOrders() {
		System.out.println("View Customer Orders clicked");
	}

	@FXML
	private void handleLogout() {
		System.out.println("Logout clicked");
	}
}