package client;

import java.util.Scanner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
// this class launches the client application
@SuppressWarnings("deprecation")
public class ClientUI extends Application {
	private static final Scanner s = new Scanner(System.in);
	@Override
	public void start(Stage stage) throws Exception {
		/*// load the FXML file of the table of orders
		FXMLLoader loader = new FXMLLoader(getClass().getResource(ConstantsUI.OrderTable));
		Parent root = loader.load();
		
		// get controller
		OrderTableDisplayPage controller = loader.getController();
		// get server hostName
		String hostName = getServerHost();
		// establish connection between UI controller and client controller
		ClientController clientController = new ClientController(
				hostName, ConstantsUI.DEFAULT_PORT, s);
		s.close();
		controller.setClientController(clientController);
		
		// get the orders of the user and load them into the order table
		clientController.requestOrders();
		
		// show UI
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setTitle("Order Table");
		stage.show();
		controller.run();*/
		
		// load the FXML file of the table of orders
		FXMLLoader loader = new FXMLLoader(getClass().getResource(ConstantsUI.welcomePage));
		Parent root = loader.load();
		
		// show UI
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setTitle("Welcome Page");
		stage.show();
	}
	
	/*
	 * this method gets the server hostName from the user
	 */
	private String getServerHost() {
		String hostName;
		
		System.out.println("Enter server host name: ");
		hostName = s.nextLine();
		
		return hostName;
	}
	
	public ClientUI() {
		// for javafx
	}

	public static void main(String[] args) {
		launch(args);
	}
}