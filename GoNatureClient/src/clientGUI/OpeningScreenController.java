package clientGUI;

import java.io.IOException;

import clientController.ClientController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Controller for the opening screen welcoming the user.
 */
public class OpeningScreenController {

	/**
	 * The client controller used for communication with the server.
	 */
	private ClientController clientController;

	/**
	 * Sets the client controller for this screen.
	 * 
	 * @param clientController the client controller
	 */
	public void setClientController(ClientController clientController) {
		this.clientController = clientController;
	}

	/**
	 * Handles the click on the Employee Login button.
	 * 
	 * This method navigates the user to the employee login screen.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleEmployeeLogin(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/EmployeeLogin.fxml")
			);

			Parent root = loader.load();

			EmployeeLoginController controller = loader.getController();
			controller.setClientController(clientController);

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("Employee Login");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles the click on the Customer Access button.
	 * 
	 * This method navigates the user to the customer access selection screen.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleCustomerAccess(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/CustomerAccess.fxml")
			);

			Parent root = loader.load();

			CustomerAccessController controller = loader.getController();
			controller.setClientController(clientController);

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("Customer Access");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}