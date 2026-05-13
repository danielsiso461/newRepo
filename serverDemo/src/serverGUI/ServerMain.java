package serverGUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import serverController.ServerController;
// this class is the main for launching server
public class ServerMain extends Application {
	
	public ServerMain() {
		//for javafx
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		// load the FXML file of the table of orders
		FXMLLoader loader = new FXMLLoader(getClass().getResource(ConstantsServerGUI.USER_TABLE));
		Parent root = loader.load();
		
		// get controllers and connect them
		ClientConnectionTableController guiController = loader.getController();
		ServerController serverController = new ServerController(guiController);
		guiController.setServerController(serverController);
		
		// show UI
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setTitle("User Table");
		stage.show();        
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
