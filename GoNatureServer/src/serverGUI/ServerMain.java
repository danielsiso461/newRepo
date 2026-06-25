
package serverGUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main class for launching the server application.
 * 
 * This class loads the database password window first. After the user enters the
 * database password, the main server table GUI is opened by DBPasswordController.
 */
public class ServerMain extends Application {

	/**
	 * Default constructor required by JavaFX.
	 */
	public ServerMain() {
		// for javafx
	}

	/**
	 * Starts the server GUI application.
	 * 
	 * The method loads the database password FXML file and displays the password
	 * window as the first screen of the server application.
	 * 
	 * @param stage the primary stage of the application
	 * @throws Exception if loading the FXML file fails
	 */
	@Override
	public void start(Stage stage) throws Exception {

		// load the FXML file of the DB password screen
		FXMLLoader loader = new FXMLLoader(getClass().getResource(ConstantsServerGUI.DB_PASSWORD));
		Parent root = loader.load();

		// show UI
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setTitle("DB Password");
		stage.show();
	}

	/**
	 * Launches the server application.
	 * 
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}
}

