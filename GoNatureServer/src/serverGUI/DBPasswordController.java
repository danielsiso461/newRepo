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

	/**
	 * The password field where the user enters the DB password.
	 */
	@FXML
	private PasswordField textID;

	/**
	 * The label that shows an error message if the password is wrong.
	 */
	@FXML
	private Label errorLabel;

	/**
	 * Handles the click on the send button.
	 *
	 * Gets the password from the password field, checks if it is correct,
	 * saves it, and opens the main server GUI.
	 */
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

	/**
	 * Opens the main server table screen.
	 *
	 * Loads the server table FXML file, creates the ServerController,
	 * connects it to the GUI controller, and replaces the password screen.
	 */
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