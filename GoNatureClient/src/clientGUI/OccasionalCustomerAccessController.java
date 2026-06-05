package clientGUI;

import java.io.IOException;

import clientCommon.OccasionalCustomerAccessObserver;
import clientController.ClientController;
import common.OperationResponse;
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

import java.util.ArrayList;
import java.util.List;

import common.OrderRow;

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
	private TextField orderNumberField;

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
		clientController = controller;
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
	 * The method validates the order number entered by the occasional customer.
	 * If the value is valid, it sends a request to the server in order to check
	 * whether the order exists in the database.
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

		if (clientController == null) {
			messageLabel.setText("Client is not connected to server.");
			return;
		}

		currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		messageLabel.setText("Checking order number...");
		clientController.requestOccasionalCustomerAccess(orderNumber);

	}

	/*
	 * Receives the occasional customer access result from the ClientController.
	 * 
	 * If the order exists, the method opens the order table screen and displays
	 * only the order that was found.
	 * 
	 * @param response the response received from the server
	 */
	@Override
	public void onOccasionalCustomerAccessResult(OperationResponse response) {
		System.out.println("Occasional access response received in screen");

		javafx.application.Platform.runLater(() -> {
			if (response == null) {
				messageLabel.setText("No response from server.");
				return;
			}

			System.out.println("Response success = " + response.isSuccess());
			System.out.println("Response message = " + response.getMessage());

			if (response.isSuccess()) {
				OrderRow order = (OrderRow) response.getData();

				messageLabel.setText("Order found. Access approved.");

				openOrderTableScreen(order);

			} else {
				messageLabel.setText(response.getMessage());
			}
		});
	}
	
	/*
	 * Opens the order table screen after the occasional customer was identified
	 * successfully by order number.
	 */
	/*
	private void openOrderTableScreen() {
		try {
			if (clientController != null) {
				clientController.removeOccasionalCustomerAccessObserver(this);
			}

			FXMLLoader loader = new FXMLLoader(
					getClass().getResource(ConstantsUI.orderTable)
			);

			Parent root = loader.load();

			OrderTableDisplayController controller = loader.getController();

			/*
			 * Gives the order table screen the active ClientController.
			 */
	/*
			controller.setClientController(clientController);

			Scene scene = new Scene(root);

			currentStage.setScene(scene);
			currentStage.setTitle("Order Table");
			currentStage.show();

			Platform.runLater(controller);

		} catch (IOException e) {
			e.printStackTrace();
			messageLabel.setText("Failed to open order table screen.");
		}
	}*/
	
	/*
	 * Opens the order table screen and displays only the order that belongs to the
	 * occasional customer access request.
	 * 
	 * @param order the order that was found by order number
	 */
	private void openOrderTableScreen(OrderRow order) {
		try {
			if (clientController != null) {
				clientController.removeOccasionalCustomerAccessObserver(this);
			}

			FXMLLoader loader = new FXMLLoader(
					getClass().getResource(ConstantsUI.orderTable)
			);

			Parent root = loader.load();

			OrderTableDisplayController controller = loader.getController();

			/*
			 * Gives the order table screen the active ClientController.
			 */
			controller.setClientController(clientController);

			List<OrderRow> orders = new ArrayList<>();
			orders.add(order);

			/*
			 * Displays only the order that was found.
			 * 
			 * This assumes that OrderTableDisplayController has a public method named
			 * onOrdersReceived(List<OrderRow> rows), as part of the existing observer logic.
			 */
			controller.onOrdersReceived(orders);

			Scene scene = new Scene(root);

			Stage stage = (Stage) orderNumberField.getScene().getWindow();
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

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("Customer Access");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}