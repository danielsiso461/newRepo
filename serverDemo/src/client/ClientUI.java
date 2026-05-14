package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
// this class launches the client application
public class ClientUI extends Application {
	@Override
	public void start(Stage stage) throws Exception {
		// load the FXML file of the table of orders
		FXMLLoader loader = new FXMLLoader(getClass().getResource(ConstantsUI.OrderTable));
		Parent root = loader.load();
		
		// get controller
		OrderTableDisplayPage controller = loader.getController();
		// establish connection between UI controller and client controller
		ClientController clientController = new ClientController(ConstantsUI.DEFAULT_HOST, ConstantsUI.DEFAULT_PORT);
		controller.setClientController(clientController);
		
		// get the orders of the user and load them into the order table
		clientController.requestOrders();
		
		// show UI
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setTitle("Order Table");
		stage.show();        
	}
	
	public ClientUI() {
		// for javafx
	}

	public static void main(String[] args) {
		launch(args);
	}
}