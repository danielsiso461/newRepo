package clientGUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/*
 * This class is used only for testing the ExistingCustomerLogin.fxml file.
 * 
 * It allows running the existing customer login screen directly without
 * starting the full client-server application.
 */
public class TestExistingCustomerLoginScreen extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader(
				getClass().getResource("/clientGUI/ExistingCustomerLogin.fxml")
		);

		Scene scene = new Scene(loader.load());

		primaryStage.setTitle("Existing Customer Login");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}