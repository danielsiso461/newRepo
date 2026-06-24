package clientGUI;

import java.io.IOException;
import java.util.ArrayList;

import clientCommon.OccasionalCustomerAccessObserver;
import clientController.ClientController;
import common.OperationResponse;
import common.Order;
import javafx.application.Platform;
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
 * The order number is sent to the server, and the server checks whether the
 * order exists in the database.
 */
public class OccasionalCustomerAccessController implements OccasionalCustomerAccessObserver {

	/*
	 * The client controller used for communication with the server.
	 */
	private static ClientController clientController;
	
	/*
	 * The current stage of the occasional customer access screen.
	 */
	private Stage currentStage;

	/*
	 * Text field used for entering the order number.
	 */
	@FXML
	private TextField idNumberField;

	/*
	 * Label used for showing validation messages or access result messages.
	 */
	@FXML
	private Label messageLabel;

	/*
	 * Sets the ClientController used by this screen.
	 * 
	 * @param controller the client controller instance
	 */
	public static void setClientController(ClientController controller) {
		OccasionalCustomerAccessController.clientController = controller;
	}

	/*
	 * Initializes the screen.
	 * 
	 * This method registers the screen as an observer so it can receive the server
	 * response for occasional customer access.
	 */
	@FXML
	private void initialize() {
		if (clientController != null) {
			clientController.addOccasionalCustomerAccessObserver(this);
		}
	}

	/*
	 * Handles the click on the Continue button.
	 * 
	 * The method validates the entered customer ID number and sends a request
	 * to the server in order to load all orders that belong to this ID number.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleContinue(ActionEvent event) {
		String customerIdNumber = idNumberField.getText();

		if (customerIdNumber == null || customerIdNumber.trim().isEmpty()) {
			messageLabel.setText("Please enter ID number.");
			return;
		}

		customerIdNumber = customerIdNumber.trim();

		if (!customerIdNumber.matches("\\d{9}")) {
			messageLabel.setText("ID number must contain 9 digits.");
			return;
		}
		
		if (clientController == null) {
			messageLabel.setText("Client is not connected to server.");
			return;
		}
		
		clientController.setLoggedInSubscriberId(Integer.parseInt(customerIdNumber));
		
		messageLabel.setText("Checking orders...");

		// Save the current customer ID in the shared ClientController.
		// This ID is later used by Make Order and Waiting List screens.
		clientController.setId(customerIdNumber);

		System.out.println("Saved customer ID in ClientController: " + clientController.getId());

		clientController.requestOccasionalCustomerAccess(customerIdNumber);
	}

	/*
	 * Receives the occasional customer access result from the ClientController.
	 * 
	 * If orders were found, the method opens the order table screen and displays
	 * all orders that belong to the entered ID number.
	 * 
	 * @param response the response received from the server
	 */
	@Override
	public void onOccasionalCustomerAccessResult(OperationResponse response) {
		Platform.runLater(() -> {
			if (response == null) {
				messageLabel.setText("No response from server.");
				return;
			}

			if (response.isSuccess()) {
				ArrayList<Order> orders = (ArrayList<Order>) response.getData();

				messageLabel.setText("Orders found.");
				Platform.runLater(()->{openOrderTableScreen(orders);});

			} else {
				messageLabel.setText(response.getMessage());
			}
		});
	}
	
	/*
	 * Opens the order table screen and displays all orders that belong to the
	 * occasional customer.
	 * 
	 * @param orders the orders received from the server
	 */
	private void openOrderTableScreen(ArrayList<Order> orders) {
		try {
			if (clientController != null) {
				clientController.removeOccasionalCustomerAccessObserver(this);
			}

			FXMLLoader loader = new FXMLLoader(
					getClass().getResource(ConstantsUI.orderTable)
			);

			Parent root = loader.load();

			OrderTableDisplayController controller = loader.getController();

			controller.setClientController(clientController);
			controller.configureForCustomerView();
			controller.onOrdersReceived(orders);

			Scene scene = new Scene(root);

			Stage stage = (Stage) idNumberField.getScene().getWindow();
			stage.setScene(scene);
			stage.setTitle("Order Table");
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
			messageLabel.setText("Failed to open order table screen.");
		}
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
			if (clientController != null) {
				clientController.removeOccasionalCustomerAccessObserver(this);
			}

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