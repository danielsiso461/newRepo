package clientGUI;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/*
 * This class is the controller for the occasional customer access screen.
 * 
 * The screen allows an occasional customer to identify using an order number.
 * Later, the order number will be sent to the server and checked against
 * the order table in the database.
 */
public class OccasionalCustomerAccessController {

	/*
	 * Text field used for entering the order number.
	 */
	@FXML
	private TextField orderNumberField;

	/*
	 * Label used for showing validation messages or access result messages.
	 */
	@FXML
	private Label messageLabel;

	/*
	 * Handles the click on the Continue button.
	 * 
	 * At this stage, the method only validates that the order number field
	 * is not empty and contains a valid number.
	 * Later, it will send the order number to the server.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleContinue(ActionEvent event) {
		String orderNumberText = orderNumberField.getText();

		if (orderNumberText == null || orderNumberText.trim().isEmpty()) {
			messageLabel.setText("Please enter order number.");
			return;
		}

		int orderNumber;

		try {
			orderNumber = Integer.parseInt(orderNumberText.trim());
		} catch (NumberFormatException e) {
			messageLabel.setText("Order number must contain digits only.");
			return;
		}

		System.out.println("Occasional customer access clicked");
		System.out.println("Order Number = " + orderNumber);

		messageLabel.setText("Order access request sent.");
	}

	/*
	 * Handles the click on the Back button.
	 * 
	 * This method navigates the user back to the Customer Access screen.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleBack(ActionEvent event) {
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