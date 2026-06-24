package clientGUI;

import java.io.IOException;

import clientCommon.RegisterGuideObserver;
import clientCommon.SearchSubscriberObserver;
import clientController.ClientController;
import common.Employee;
import common.GuideRegistrationRequest;
import common.OperationResponse;
import common.Subscriber;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/*
 * This class is the controller for the register guide screen.
 * 
 * The service representative can search an existing subscriber
 * and register that subscriber as a guide.
 */
public class RegisterGuideController implements SearchSubscriberObserver, RegisterGuideObserver {

	private ClientController clientController;
	private Employee loggedInEmployee;

	@FXML
	private TextField subscriberIdField;

	@FXML
	private Label subscriberNameLabel;

	@FXML
	private Label subscriberEmailLabel;

	@FXML
	private TextField organizationNameField;

	@FXML
	private ComboBox<String> guideStatusComboBox;

	@FXML
	private Label messageLabel;

	/*
	 * Sets the ClientController used by this screen.
	 * 
	 * @param clientController the active client controller
	 */
	public void setClientController(ClientController clientController) {
		this.clientController = clientController;

		if (this.clientController != null) {
			this.clientController.addSearchSubscriberObserver(this);
			this.clientController.addRegisterGuideObserver(this);
		}
	}

	/*
	 * Sets the logged-in service representative.
	 * 
	 * @param employee the employee that opened this screen
	 */
	public void setLoggedInEmployee(Employee employee) {
		this.loggedInEmployee = employee;
	}

	@FXML
	private void initialize() {
		guideStatusComboBox.getItems().addAll("active", "revoked");
		guideStatusComboBox.setValue("active");

		subscriberNameLabel.setText("-");
		subscriberEmailLabel.setText("-");
		messageLabel.setText("");
	}

	@FXML
	private void handleSearchSubscriber(ActionEvent event) {
		String subscriberIdText = subscriberIdField.getText();

		if (subscriberIdText == null || subscriberIdText.trim().isEmpty()) {
			messageLabel.setText("Please enter subscriber ID.");
			return;
		}

		int subscriberId;

		try {
			subscriberId = Integer.parseInt(subscriberIdText.trim());
		} catch (NumberFormatException e) {
			messageLabel.setText("Subscriber ID must be a number.");
			return;
		}

		if (clientController == null) {
			messageLabel.setText("Client is not connected to server.");
			return;
		}

		System.out.println("Search subscriber clicked. ID = " + subscriberId);

		messageLabel.setText("Searching subscriber...");
		subscriberNameLabel.setText("-");
		subscriberEmailLabel.setText("-");

		clientController.requestSearchSubscriber(subscriberId);
	}

	@FXML
	private void handleRegisterGuide(ActionEvent event) {
		String subscriberIdText = subscriberIdField.getText();
		String organizationName = organizationNameField.getText();
		String guideStatus = guideStatusComboBox.getValue();

		if (subscriberIdText == null || subscriberIdText.trim().isEmpty()) {
			messageLabel.setText("Please search subscriber first.");
			return;
		}

		int subscriberId;

		try {
			subscriberId = Integer.parseInt(subscriberIdText.trim());
		} catch (NumberFormatException e) {
			messageLabel.setText("Subscriber ID must be a number.");
			return;
		}

		if (organizationName == null || organizationName.trim().isEmpty()) {
			messageLabel.setText("Please enter organization name.");
			return;
		}

		if (guideStatus == null || guideStatus.trim().isEmpty()) {
			messageLabel.setText("Please select guide status.");
			return;
		}

		if (clientController == null) {
			messageLabel.setText("Client is not connected to server.");
			return;
		}

		if (loggedInEmployee == null) {
			messageLabel.setText("No logged-in employee was found.");
			return;
		}

		GuideRegistrationRequest request = new GuideRegistrationRequest(
				subscriberId,
				organizationName.trim(),
				guideStatus,
				loggedInEmployee.getEmployeeId()
		);

		messageLabel.setText("Registering guide...");

		clientController.requestRegisterGuide(request);
	}

	@Override
	public void onRegisterGuideResult(OperationResponse response) {
		Platform.runLater(() -> {
			if (response == null) {
				messageLabel.setText("No response from server.");
				return;
			}

			messageLabel.setText(response.getMessage());
		});
	}

	@FXML
	private void handleClear(ActionEvent event) {
		subscriberIdField.clear();
		organizationNameField.clear();

		guideStatusComboBox.setValue("active");

		subscriberNameLabel.setText("-");
		subscriberEmailLabel.setText("-");
		messageLabel.setText("");
	}

	/*
	 * Receives the search subscriber result from the ClientController.
	 * 
	 * @param response the response received from the server
	 */
	@Override
	public void onSearchSubscriberResult(OperationResponse response) {
		Platform.runLater(() -> {
			if (response == null) {
				messageLabel.setText("No response from server.");
				return;
			}

			if (response.isSuccess()) {
				Subscriber subscriber = (Subscriber) response.getData();

				subscriberNameLabel.setText(subscriber.getSubscriberName());
				subscriberEmailLabel.setText(subscriber.getSubscriberEmail());
				messageLabel.setText(response.getMessage());
			} else {
				subscriberNameLabel.setText("-");
				subscriberEmailLabel.setText("-");
				messageLabel.setText(response.getMessage());
			}
		});
	}

	@FXML
	private void handleBack(ActionEvent event) {
		try {
			if (clientController != null) {
				clientController.removeSearchSubscriberObserver(this);
				clientController.removeRegisterGuideObserver(this);
			}

			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/clientGUI/ServiceRepresentativeHomePage.fxml")
			);

			Parent root = loader.load();

			ServiceRepresentativeHomePageController controller = loader.getController();
			controller.setClientController(clientController);
			controller.setLoggedInEmployee(loggedInEmployee);

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle("Service Representative Dashboard");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
			messageLabel.setText("Could not return to service representative screen.");
		}
	}
}