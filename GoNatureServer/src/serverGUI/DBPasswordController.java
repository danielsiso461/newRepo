package serverGUI;

import databaseControllers.AbstractDBConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import serverController.ServerController;

/**
 * This class is the controller of the DB password screen.
 *
 * The class gets the DB password from the user, checks if the password is
 * correct, saves it in AbstractDBConnection, and then opens the main server
 * table screen.
 */
public class DBPasswordController {

	@FXML
	private PasswordField textID;

	@FXML
	private Label errorLabel;

	@FXML
	private void handleSend() {
		String password = textID.getText();

		if (password == null || password.isEmpty()) {
			errorLabel.setText("Please enter DB password");
			errorLabel.setVisible(true);
			return;
		}

		boolean isPasswordCorrect =
				AbstractDBConnection.testConnection(password);

		if (!isPasswordCorrect) {
			errorLabel.setText("Wrong DB password");
			errorLabel.setVisible(true);
			textID.clear();
			return;
		}

		errorLabel.setVisible(false);

		AbstractDBConnection.setPassword(password);

		openServerTableScreen();
	}

	private void openServerTableScreen() {
		try {
			FXMLLoader loader =
					new FXMLLoader(getClass().getResource(ConstantsServerGUI.USER_TABLE));

			Parent root = loader.load();

			ClientConnectionTableController guiController =
					loader.getController();

			ServerController serverController =
					new ServerController(guiController);

			guiController.setServerController(serverController);

			Stage stage = (Stage) textID.getScene().getWindow();
			Scene scene = new Scene(root);

			stage.setScene(scene);
			stage.setTitle("User Table");
			stage.show();

		} catch (Exception e) {
			errorLabel.setText("Could not open server screen");
			errorLabel.setVisible(true);
			e.printStackTrace();
		}
	}
}