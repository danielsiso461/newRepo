package clientGUI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class OpeningScreenController {

    @FXML
    private void handleEmployeeLogin(ActionEvent event) {
        System.out.println("Employee Login clicked");
        // בהמשך נעבור למסך EmployeeLogin.fxml
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