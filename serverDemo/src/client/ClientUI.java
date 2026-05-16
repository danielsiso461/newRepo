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
		// load the FXML file of the table of orders
		FXMLLoader loader = new FXMLLoader(getClass().getResource(ConstantsUI.welcomePage));
		Parent root = loader.load();
		
		// show UI
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setTitle("Welcome Page");
		stage.show();
	}
	
	public ClientUI() {
		// for javafx
	}

	public static void main(String[] args) {
		launch(args);
	}
}