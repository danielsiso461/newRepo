package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientUI extends Application {
	@Override
	public void start(Stage stage) throws Exception {
		// load the FXML file of the table of orders
		FXMLLoader loader = new FXMLLoader(getClass().getResource(ConstantsUI.OrderTable));
		Parent root = loader.load();
		
		// get controller
		OrderTableDisplayPage controller = loader.getController();
		
		ClientService service = new ClientService(ConstantsUI.DEFAULT_HOST, ConstantsUI.DEFAULT_PORT);
		service.setController(controller);
		controller.setClientService(service);
		service.requestOrders();
		
		// show UI
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setTitle("Order Table");
		stage.show();
		
		// get initial data for table view
		//client.handleMessageFromClientUI(new Message(id, Protocol.RETURN_ORDER));
	}

	// Constructors ****************************************************
	
	public ClientUI() {
		// for javafx
	}
	
	// Class methods ***************************************************

	/**
	 * This method is responsible for the creation of the Client UI.
	 *
	 * @param args[0] The host to connect to.
	 */
	public static void main(String[] args) {
		launch(args);
	}
}
//907428969
