package serverGUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * this class is the main class for launching the server application
 * 
 * the class loads the DB password GUI first
 * only after the password is entered, the server table GUI will be opened
 */
public class ServerMain extends Application {

	/**
	 * default constructor for JavaFX
	 */
	public ServerMain() {
		// for javafx
	}

	/**
	 * this function starts the server GUI application
	 * 
	 * loads the DB password FXML file
	 * displays the password window first
	 * the main server window will be opened later from DBPasswordController
	 * after the user enters the DB password
	 * 
	 * @param stage the primary stage of the application
	 * @throws Exception in case of failure to load the GUI
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
	 * main function that launches the server application
	 * 
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}
}