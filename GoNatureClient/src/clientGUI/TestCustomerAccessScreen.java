package clientGUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/*
 * This class is used only for testing the CustomerAccess.fxml file.
 * 
 * It allows running the customer access screen directly without starting the
 * full client-server application.
 */
public class TestCustomerAccessScreen extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader(
				getClass().getResource("/clientGUI/CustomerAccess.fxml")
		);

		Scene scene = new Scene(loader.load());

		primaryStage.setTitle("Customer Access");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}