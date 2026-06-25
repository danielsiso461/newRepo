package clientGUI;

import java.io.IOException;

import clientCommon.UserInformationObserver;
import clientController.ClientController;
import common.Employee;
import common.OperationResponse;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;


/**
 * This controller handles the user information page.
 * 
 * The page allows a service representative to search and view information
 * about customers and employees.
 */
public class UserInformationPageController implements UserInformationObserver {
	/**
	 * Customer user type option.
	 */
	private static final String USER_TYPE_CUSTOMER = "Customer";
	/**
	 * Employee user type option.
	 */
	private static final String USER_TYPE_EMPLOYEE = "Employee";
	/**
	 * Client controller used to communicate with the server.
	 */
	private ClientController clientController;
	/**
	 * The employee currently logged in.
	 */
	private Employee loggedInEmployee;
	/**
	 * FXML path of the previous screen.
	 */
	private String previousScreenFxml;
	/**
	 * Title of the previous screen.
	 */
	private String previousScreenTitle;
	/**
	 * Previous scene to return to.
	 */
	private Scene previousScene;
	/**
	 * Customer ID whose details should be displayed.
	 */
	private String customerIdForMyDetails;
	/**
	 * Sets the previous screen to return to.
	 *
	 * @param previousScreenFxml the previous screen FXML path
	 * @param previousScreenTitle the previous screen title
	 */
	public void setPreviousScreen(String previousScreenFxml, String previousScreenTitle) {
		this.previousScreenFxml = previousScreenFxml;
		this.previousScreenTitle = previousScreenTitle;
	}
	/**
	 * Sets the previous scene to return to.
	 *
	 * @param previousScene the previous scene
	 * @param previousScreenTitle the previous screen title
	 */
	public void setPreviousScene(Scene previousScene, String previousScreenTitle) {
		this.previousScene = previousScene;
		this.previousScreenTitle = previousScreenTitle;
	}
	/** Displays the page title. */
	@FXML
	private Label titleLabel;
	/** Displays status messages. */
	@FXML
	private Label messageLabel;
	/** User ID input field. */
	@FXML
	private TextField userIdField;
	/** Displays the retrieved user information. */
	@FXML
	private TextArea detailsTextArea;
	/** User type selector. */
	@FXML
	private ChoiceBox<String> userTypeChoiceBox;
	/** Contains the search controls. */
	@FXML
	private HBox searchBox;
	/**
	 * Indicates whether the page is displaying personal details.
	 */
	private boolean myDetailsMode = false;
	/**
	 * Sets the client controller and registers this controller as an observer.
	 *
	 * @param clientController the client controller to use
	 */
	public void setClientController(ClientController clientController) {
		this.clientController = clientController;

		if (this.clientController != null) {
			this.clientController.addUserInformationObserver(this);
		}
		
		loadMyDetailsIfReady();
	}
	/**
	 * Sets the logged-in employee.
	 *
	 * @param employee the logged-in employee
	 */
	public void setLoggedInEmployee(Employee employee) {
		this.loggedInEmployee = employee;
		loadMyDetailsIfReady();
	}
	/**
	 * Initializes the user information page.
	 */
	@FXML
	private void initialize() {
		if (titleLabel != null) {
			titleLabel.setText("View User Information");
		}

		if (userTypeChoiceBox != null) {
			userTypeChoiceBox.getItems().add(USER_TYPE_CUSTOMER);
			userTypeChoiceBox.getItems().add(USER_TYPE_EMPLOYEE);
			userTypeChoiceBox.setValue(USER_TYPE_CUSTOMER);
		}

		if (messageLabel != null) {
			messageLabel.setText("Choose a user type, enter a value, and click Search.");
		}

		if (detailsTextArea != null) {
			detailsTextArea.setEditable(false);
			detailsTextArea.setText("");
		}
	}
	/**
	 * Handles searching for customer or employee information.
	 *
	 * @param event the button click event
	 */
	@FXML
	private void handleSearch(ActionEvent event) {
		String searchValue = userIdField.getText();

		if (searchValue == null || searchValue.trim().isEmpty()) {
			messageLabel.setText("Please enter a search value.");
			return;
		}

		searchValue = searchValue.trim();

		if (clientController == null) {
			messageLabel.setText("Client is not connected to server.");
			return;
		}

		String selectedUserType = userTypeChoiceBox.getValue();

		if (USER_TYPE_CUSTOMER.equals(selectedUserType)) {
			if (!searchValue.matches("\\d+")) {
				messageLabel.setText("Customer ID must contain digits only.");
				detailsTextArea.setText("");
				return;
			}

			if (searchValue.length() != 9) {
				messageLabel.setText("Customer ID must contain exactly 9 digits.");
				detailsTextArea.setText("");
				return;
			}

			messageLabel.setText("Searching customer information...");
			detailsTextArea.setText("");

			clientController.requestSearchUserInformation(searchValue);
			return;
		}

		if (USER_TYPE_EMPLOYEE.equals(selectedUserType)) {
			if (!searchValue.matches("\\d+")) {
				messageLabel.setText("Employee number must contain digits only.");
				detailsTextArea.setText("");
				return;
			}

			if (searchValue.length() != 4) {
				messageLabel.setText("Employee number must contain exactly 4 digits.");
				detailsTextArea.setText("");
				return;
			}

			messageLabel.setText("Searching employee information...");
			detailsTextArea.setText("");

			clientController.requestSearchUserInformation("EMPLOYEE|" + searchValue);
			return;
		}

		messageLabel.setText("Please choose a valid user type.");
	}
	/**
	 * Handles the server response for a user information search.
	 *
	 * @param response the operation response returned by the server
	 */
	@Override
	public void onUserInformationResult(OperationResponse response) {
		Platform.runLater(() -> {
			if (response == null) {
				messageLabel.setText("No response from server.");
				detailsTextArea.setText("");
				return;
			}

			messageLabel.setText(response.getMessage());

			if (response.isSuccess() && response.getData() != null) {
				detailsTextArea.setText(String.valueOf(response.getData()));
			} else {
				detailsTextArea.setText("");
			}
		});
	}
	/**
	 * Configures the page to display the logged-in employee's details.
	 */
	public void configureForMyDetails() {
		myDetailsMode = true;

		if (titleLabel != null) {
			titleLabel.setText("My Details");
		}

		if (messageLabel != null) {
			messageLabel.setText("Loading your details...");
		}

		if (searchBox != null) {
			searchBox.setVisible(false);
			searchBox.setManaged(false);
		}

		loadMyDetailsIfReady();
	}
	/**
	 * Configures the page to display the logged-in customer's details.
	 *
	 * @param customerId the customer ID to load
	 */
	public void configureForCustomerMyDetails(String customerId) {
		myDetailsMode = true;
		customerIdForMyDetails = customerId;

		if (titleLabel != null) {
			titleLabel.setText("My Details");
		}

		if (messageLabel != null) {
			messageLabel.setText("Loading your details...");
		}

		if (searchBox != null) {
			searchBox.setVisible(false);
			searchBox.setManaged(false);
		}

		loadMyDetailsIfReady();
	}
	/**
	 * Loads personal details when the required data is available.
	 */
	private void loadMyDetailsIfReady() {
		if (!myDetailsMode) {
			return;
		}

		if (clientController == null) {
			return;
		}

		if (detailsTextArea != null) {
			detailsTextArea.setText("");
		}

		if (customerIdForMyDetails != null && !customerIdForMyDetails.isBlank()) {
			clientController.requestSearchUserInformation(customerIdForMyDetails);
			return;
		}

		if (loggedInEmployee != null) {
			clientController.requestSearchUserInformation(
					"EMPLOYEE|" + loggedInEmployee.getEmployeeId()
			);
		}
	}
	/**
	 * Handles returning to the previous screen.
	 *
	 * @param event the button click event
	 */
	@FXML
	private void handleBack(ActionEvent event) {
		try {
			if (clientController != null) {
				clientController.removeUserInformationObserver(this);
			}
			
			if (previousScene != null) {
				Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

				if (previousScreenTitle == null || previousScreenTitle.isBlank()) {
					previousScreenTitle = "Customer Orders";
				}

				stage.setTitle(previousScreenTitle);
				stage.setScene(previousScene);
				stage.show();
				return;
			}

			String fxmlToLoad = previousScreenFxml;
			String titleToSet = previousScreenTitle;

			if (fxmlToLoad == null || fxmlToLoad.isBlank()) {
				fxmlToLoad = "/clientGUI/ServiceRepresentativeHomePage.fxml";
			}

			if (titleToSet == null || titleToSet.isBlank()) {
				titleToSet = "Service Representative Dashboard";
			}

			FXMLLoader loader = new FXMLLoader(
					getClass().getResource(fxmlToLoad)
			);

			Parent root = loader.load();

			Object controller = loader.getController();

			if (controller instanceof ServiceRepresentativeHomePageController) {
				ServiceRepresentativeHomePageController serviceController =
						(ServiceRepresentativeHomePageController) controller;

				serviceController.setClientController(clientController);
				serviceController.setLoggedInEmployee(loggedInEmployee);

			} else if (controller instanceof ParkManagerHomePageController) {
				ParkManagerHomePageController parkManagerController =
						(ParkManagerHomePageController) controller;

				parkManagerController.setClientController(clientController);
				parkManagerController.setLoggedInEmployee(loggedInEmployee);

			} else if (controller instanceof DepartmentManagerHomePageController) {
				DepartmentManagerHomePageController departmentManagerController =
						(DepartmentManagerHomePageController) controller;

				departmentManagerController.setClientController(clientController);
				departmentManagerController.setLoggedInEmployee(loggedInEmployee);
			}
			
			else if (controller instanceof ParkWorkerHomePageController) {
				ParkWorkerHomePageController parkWorkerController =
						(ParkWorkerHomePageController) controller;

				parkWorkerController.setClientController(clientController);
				parkWorkerController.setLoggedInEmployee(loggedInEmployee);
			}

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setTitle(titleToSet);
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
}