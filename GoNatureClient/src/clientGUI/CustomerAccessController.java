package clientGUI;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import clientController.ClientController;


/**
 * This class is the controller for the customer access selection screen.
 * 
 * The screen allows the customer to choose between two access options:
 * existing customer login using username and password, or occasional customer
 * access using an order number.
 */
public class CustomerAccessController {
	
	
	private ClientController clientController;

	public void setClientController(ClientController clientController) {
		this.clientController = clientController;
	}

	/**
	 * Handles the click on the Existing Customer Login button.
	 * 
	 * This method navigates the user to the existing customer login screen.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleExistingCustomerLogin(ActionEvent event) {
		try {
			ExistingCustomerLoginController.setClientController(clientController);

			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/ExistingCustomerLogin.fxml")
			);

			Parent root = loader.load();

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("Existing Customer Login");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles the click on the Occasional Customer Access button.
	 * 
	 * This method navigates the user to the occasional customer access screen.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleOccasionalCustomerAccess(ActionEvent event) {
		try {
			OccasionalCustomerAccessController.setClientController(clientController);

			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/OccasionalCustomerAccess.fxml")
			);

			Parent root = loader.load();

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setScene(new Scene(root));
			stage.setTitle("Occasional Customer Access");
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles the click on the Back button.
	 * 
	 * This method navigates the user back to the opening screen.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleBack(ActionEvent event) {
		try {
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