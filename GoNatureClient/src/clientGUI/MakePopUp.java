package clientGUI;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
/*
 * this class allows us to make messages to the user burst to the screen
 */
public class MakePopUp {
	/*
	 * this method displays a bursting message to the screen, with custom user text
	 * @param title the title of the window
	 * @param msg the message to display on the window
	 */
	public static void makePopup(String title, String msg) {
		Platform.runLater(() -> {
		    Alert alert = new Alert(Alert.AlertType.INFORMATION);
		    alert.setTitle(title);
		    alert.setHeaderText(null);
		    alert.setContentText(msg);
		    alert.show();
		});
	}
}
