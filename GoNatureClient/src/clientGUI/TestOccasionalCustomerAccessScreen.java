package clientGUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/*
 * This class is used only for testing the OccasionalCustomerAccess.fxml file.
 */
public class TestOccasionalCustomerAccessScreen extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader(
				getClass().getResource("/clientGUI/OccasionalCustomerAccess.fxml")
		);

		Scene scene = new Scene(loader.load());

		primaryStage.setTitle("Occasional Customer Access");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}