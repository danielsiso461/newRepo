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

/*
 * this class is the controller of the DB password screen
 * 
 * the class gets the DB password from the user
 * checks if the password is correct
 * saves it in AbstractDBConnection
 * and then opens the main server table screen
 */
public class DBPasswordController {

	/*
	 * the password field where the user enters the DB password
	 */
	@FXML
	private PasswordField textID;

	/*
	 * the label that shows an error message if the password is wrong
	 */
	@FXML
	private Label errorLabel;

	/*
	 * this function is called when the user clicks the send button
	 * 
	 * gets the password from the password field
	 * checks if the password is correct
	 * if the password is wrong, shows an error message
	 * if the password is correct, saves it and opens the main server GUI
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

		// hide the error message if the password is correct
		errorLabel.setVisible(false);

		// save the password before any DB connection is created
		AbstractDBConnection.setPassword(password);

		// open the main server screen only after the password was checked and saved
		openServerTableScreen();
	}

	/*
	 * this function opens the main server table screen
	 * 
	 * loads the server table FXML file
	 * creates and connects the controllers
	 * and replaces the password screen with the server table screen
	 */
	private void openServerTableScreen() {
		try {
			// load the FXML file of the user table
			FXMLLoader loader =
					new FXMLLoader(getClass().getResource(ConstantsServerGUI.USER_TABLE));
			Parent root = loader.load();

			// get controllers and connect them
			ClientConnectionTableController guiController =
					loader.getController();

			ServerController serverController =
					new ServerController(guiController);

			guiController.setServerController(serverController);

			// show the main server UI in the same window
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