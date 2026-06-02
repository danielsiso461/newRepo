package clientGUI;

import java.io.IOException;
import java.util.ArrayList;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * This class is responsible for opening notification popup windows.
 */
public final class PopupNotificationViewer {

	/**
	 * Private constructor to prevent object creation.
	 */
	private PopupNotificationViewer() {
	}

	/**
	 * Opens a popup notification window using the given notification data.
	 * 
	 * The expected ArrayList order is:
	 * notification_id, subscriber_id, order_number, message_title, message_body,
	 * recipient_email, recipient_phone.
	 * 
	 * This structure matches the data returned from NotificationConnection methods
	 * such as getDueVisitReminderNotification and getDueOrderPopup.
	 * 
	 * @param notificationData the notification data received from the server
	 */
	public static void showNotificationPopup(ArrayList<Object> notificationData) {
		if (notificationData == null || notificationData.size() < 7) {
			return;
		}

		int notificationId = ((Number) notificationData.get(0)).intValue();
		String title = (String) notificationData.get(3);
		String body = (String) notificationData.get(4);
		String recipientEmail = (String) notificationData.get(5);
		String recipientPhone = (String) notificationData.get(6);

		showNotificationPopup(notificationId, title, body, recipientEmail, recipientPhone);
	}

	/**
	 * Opens a popup notification window.
	 * 
	 * @param notificationId  the notification ID
	 * @param title           the notification title
	 * @param body            the notification body
	 * @param recipientEmail  the recipient email
	 * @param recipientPhone  the recipient phone
	 */
	public static void showNotificationPopup(int notificationId, String title, String body, String recipientEmail,
			String recipientPhone) {

		try {
			FXMLLoader loader = new FXMLLoader(
					PopupNotificationViewer.class.getResource("/clientGUI/PopupNotificationPage.fxml"));

			Parent root = loader.load();

			PopupNotificationPageController controller = loader.getController();
			controller.setNotificationData(notificationId, title, body, recipientEmail, recipientPhone);

			Stage popupStage = new Stage();
			popupStage.setTitle("Notification");
			popupStage.initModality(Modality.APPLICATION_MODAL);
			popupStage.setScene(new Scene(root));
			popupStage.setResizable(false);
			popupStage.showAndWait();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}