package clientGUI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.io.IOException;

import clientController.ClientController;
import common.Employee;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
public class RegisterGuideController {

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
        String subscriberId = subscriberIdField.getText();

        if (subscriberId == null || subscriberId.trim().isEmpty()) {
            messageLabel.setText("Please enter subscriber ID.");
            return;
        }

        System.out.println("Search subscriber clicked. ID = " + subscriberId);

        // זמני בלבד עד שנחבר לשרת ול-DB
        subscriberNameLabel.setText("Example Subscriber");
        subscriberEmailLabel.setText("example@email.com");
        messageLabel.setText("Subscriber found.");
    }

    @FXML
    private void handleRegisterGuide(ActionEvent event) {
        String subscriberId = subscriberIdField.getText();
        String organizationName = organizationNameField.getText();
        String guideStatus = guideStatusComboBox.getValue();

        if (subscriberId == null || subscriberId.trim().isEmpty()) {
            messageLabel.setText("Please search subscriber first.");
            return;
        }

        if (organizationName == null || organizationName.trim().isEmpty()) {
            messageLabel.setText("Please enter organization name.");
            return;
        }

        System.out.println("Register guide clicked");
        System.out.println("Subscriber ID = " + subscriberId);
        System.out.println("Organization Name = " + organizationName);
        System.out.println("Guide Status = " + guideStatus);

        // זמני בלבד עד שנחבר לשרת ול-DB
        messageLabel.setText("Guide registered successfully.");
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

    @FXML
    private void handleBack(ActionEvent event) {
        try {
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