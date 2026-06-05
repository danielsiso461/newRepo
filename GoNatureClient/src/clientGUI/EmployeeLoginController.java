package clientGUI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class EmployeeLoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.trim().isEmpty()) {
            messageLabel.setText("Please enter username.");
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            messageLabel.setText("Please enter password.");
            return;
        }

        System.out.println("Employee login clicked");
        System.out.println("Username = " + username);
        System.out.println("Password = " + password);

        // זמני בלבד עד שנחבר לשרת ול-DB
        messageLabel.setText("Login request sent.");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        System.out.println("Back clicked");
        // בהמשך נחזור ל-OpeningScreen.fxml
    }
}