package clientGUI;

import java.io.IOException;

import clientController.ClientController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * This class is responsible for loading and switching between client GUI screens.
 */
public final class ClientScreenManager {

	/**
	 * The main stage of the client application.
	 */
	private static Stage primaryStage;

	/**
	 * The client controller used for communication with the server.
	 */
	private static ClientController clientController;
	
	/**
	 * Private constructor to prevent object creation.
	 */
	private ClientScreenManager() {
	}

	/**
	 * Sets the primary stage of the client application.
	 * 
	 * @param stage the main JavaFX stage
	 */
	public static void setPrimaryStage(Stage stage) {
		primaryStage = stage;
	}
	
	/**
	 * Sets the client controller used by GUI screens.
	 * 
	 * @param controller the client controller
	 */
	public static void setClientController(ClientController controller) {
		clientController = controller;
	}

	/**
	 * Loads the welcome page.
	 */
	public static void showWelcomePage() {
		loadPage("/clientGUI/WelcomePage.fxml", "Welcome");
	}

	/**
	 * Loads the order table display page.
	 */
	public static void showOrderTableDisplayPage() {
		loadPage("/clientGUI/OrderTableDisplayPage.fxml", "Orders");
	}

	/**
	 * Loads the order update page.
	 */
	public static void showOrderUpdatePage() {
		loadPage("/clientGUI/OrderUpdatePage.fxml", "Update Order");
	}

	/**
	 * Loads the park selection page.
	 */
	public static void showParkSelectionPage() {
		loadPage("/clientGUI/ParkSelectionPage.fxml", "Park Selection");
	}

	/**
	 * Loads an FXML page into the main stage.
	 * 
	 * @param fxmlPath the path of the FXML file
	 * @param title    the window title
	 */
	private static void loadPage(String fxmlPath, String title) {
		try {
			if (primaryStage == null) {
				System.out.println("Primary stage was not set.");
				return;
			}

			FXMLLoader loader = new FXMLLoader(ClientScreenManager.class.getResource(fxmlPath));
			Parent root = loader.load();

			Object controller = loader.getController();

			if (controller instanceof ParkSelectionPageController parkController) {
				if (clientController != null) {
					parkController.setServerRequestHandler(message -> clientController.sendMessageToServer(message));
					clientController.addParkObserver(parkController);
				}
			}

			Scene scene = new Scene(root);
			primaryStage.setTitle(title);
			primaryStage.setScene(scene);
			primaryStage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}