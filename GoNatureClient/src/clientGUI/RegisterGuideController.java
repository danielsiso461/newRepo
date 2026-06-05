package clientGUI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class RegisterGuideController {

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
        System.out.println("Back clicked");
        // בהמשך נחבר חזרה למסך הקודם
    }
}