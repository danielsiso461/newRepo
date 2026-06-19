package clientGUI;

import javafx.event.ActionEvent;
import java.io.IOException;

import javafx.scene.Node;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class OpeningScreenController {

	/*
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

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("Employee Login");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    /*
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

    		Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    		stage.setTitle("Customer Access");
    		stage.setScene(new Scene(root));
    		stage.show();

    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
}