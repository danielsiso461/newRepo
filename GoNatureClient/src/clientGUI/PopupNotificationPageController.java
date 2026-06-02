package clientGUI;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * This controller displays a simulated notification popup to the user.
 * 
 * The popup is used for notifications such as visit reminders, order
 * confirmations, cancellations, and waiting list offers.
 */
public class PopupNotificationPageController {

	/**
	 * The notification title label.
	 */
	@FXML
	private Label titleLabel;

	/**
	 * The notification body label.
	 */
	@FXML
	private Label bodyLabel;

	/**
	 * The recipient email label.
	 */
	@FXML
	private Label emailLabel;

	/**
	 * The recipient phone label.
	 */
	@FXML
	private Label phoneLabel;

	/**
	 * The notification ID in the database.
	 */
	private int notificationId;

	/**
	 * Sets the notification data that should be displayed in the popup.
	 * 
	 * @param notificationId the notification ID
	 * @param title          the notification title
	 * @param body           the notification body
	 * @param recipientEmail the email address used for real email sending later
	 * @param recipientPhone the phone number used for real SMS sending later
	 */
	public void setNotificationData(int notificationId, String title, String body, String recipientEmail,
			String recipientPhone) {

		this.notificationId = notificationId;

		titleLabel.setText(title);
		bodyLabel.setText(body);

		if (recipientEmail != null && !recipientEmail.isBlank()) {
			emailLabel.setText("Email: " + recipientEmail);
		} else {
			emailLabel.setText("Email: not available");
		}

		if (recipientPhone != null && !recipientPhone.isBlank()) {
			phoneLabel.setText("Phone: " + recipientPhone);
		} else {
			phoneLabel.setText("Phone: not available");
		}
	}

	/**
	 * Closes the popup window.
	 */
	@FXML
	private void handleClose() {
		Stage stage = (Stage) titleLabel.getScene().getWindow();
		stage.close();
	}

	/**
	 * Returns the notification ID.
	 * 
	 * @return the notification ID
	 */
	public int getNotificationId() {
		return notificationId;
	}
}