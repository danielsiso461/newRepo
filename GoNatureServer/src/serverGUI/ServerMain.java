package serverGUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import serverController.ServerController;

/*
 * this class is the main class for launching the server application
 * 
 * the class loads the server GUI and connects the GUI controller
 * with the server controller
 */
@SuppressWarnings("deprecation")
public class ServerMain extends Application {

	/*
	 * default constructor for JavaFX
	 */
	public ServerMain() {
		// for javafx
	}

	/*
	 * this function starts the server GUI application
	 * 
	 * loads the FXML file
	 * creates and connects the controllers
	 * and displays the main server window
	 * 
	 * @param stage the primary stage of the application
	 */
	@Override
	public void start(Stage stage) throws Exception {

		// load the FXML file of the table of orders
		FXMLLoader loader =
				new FXMLLoader(getClass().getResource(ConstantsServerGUI.USER_TABLE));

		Parent root = loader.load();

		// get controllers and connect them
		ClientConnectionTableController guiController = loader.getController();

		ServerController serverController =
				new ServerController(guiController);

		guiController.setServerController(serverController);

		// show UI
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setTitle("User Table");
		stage.show();
	}

	/*
	 * main function that launches the server application
	 * 
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}
}