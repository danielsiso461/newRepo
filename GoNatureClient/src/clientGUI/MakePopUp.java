
package clientGUI;

import java.util.Optional;

import clientController.ClientController;
import common.Message;
import common.Order;
import common.Protocol;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

/**
 * This class allows showing popup messages to the user.
 */
public class MakePopUp {

	/**
	 * Displays a popup message to the user.
	 *
	 * @param title the title of the popup window
	 * @param msg the message to display in the popup window
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
	
	/**
	 * Shows an order reminder popup to the user.
	 *
	 * @param cc the client controller used to send the answer
	 * @param o the order that the reminder is about
	 * @param title the title of the popup window
	 */
	public static void makeReminderPopup(ClientController cc, Order o, String title) {
		ButtonType acceptButton = new ButtonType("Accept", ButtonBar.ButtonData.OK_DONE);
		ButtonType declineButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		Platform.runLater(() -> {
		    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		    alert.setTitle(title);
		    alert.setHeaderText(null);
		    alert.setContentText("Accept / decline order reminder");
		    alert.getButtonTypes().setAll(acceptButton, declineButton);
		    
		    Optional<ButtonType> result = alert.showAndWait();
		    if (result.isPresent() && result.get() == acceptButton)
		        acceptClicked(cc, o);
		    else
		        declineClicked(cc, o);
		});
	}
	
	/**
	 * Sends the user's accept answer to the server.
	 *
	 * @param cc the client controller used to send the answer
	 * @param o the order that the answer is about
	 */
	private static void acceptClicked(ClientController cc, Order o) {
		cc.sendMessageToServer(new Message(o, Protocol.ACCEPT_ORDER_REMINDER));
	}
	
	/**
	 * Sends the user's decline answer to the server.
	 *
	 * @param cc the client controller used to send the answer
	 * @param o the order that the answer is about
	 */
	private static void declineClicked(ClientController cc, Order o) {
		cc.sendMessageToServer(new Message(o, Protocol.DECLINE_ORDER_REMINDER));
	}
}

