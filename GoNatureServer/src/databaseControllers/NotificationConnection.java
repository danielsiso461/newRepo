
package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles database operations related to system notifications.
 * 
 * This connector is responsible for creating simulated notification records,
 * retrieving notification report data, and updating notification statuses.
 * The class is implemented as a singleton so the server uses one notification
 * database connector during runtime.
 */
public class NotificationConnection extends AbstractDBConnection {

	/**
	 * The single instance of NotificationConnection.
	 */
	private static NotificationConnection instance;

	/**
	 * Creates a new NotificationConnection instance.
	 * 
	 * The constructor is private because this class is implemented as a singleton.
	 * It opens the database connection when the instance is created.
	 * 
	 * @throws SQLException if the connection to the database fails
	 */
	private NotificationConnection() throws SQLException {
		connect();
	}
 
	/**
	 * Returns the single instance of NotificationConnection.
	 * 
	 * If no instance exists, or if the current database connection is closed, a new
	 * instance is created.
	 * 
	 * @return the active NotificationConnection instance
	 * @throws SQLException if creating the database connection fails
	 */
	public static NotificationConnection getInstance() throws SQLException {
		if (instance == null || instance.conn == null || instance.conn.isClosed()) {
			instance = new NotificationConnection();
		}
		return instance;
	}

	/**
	 * Returns the database table name used by this connector.
	 * 
	 * @return the notification table name
	 */
	@Override
	public String getTableName()
	{
		return ConstantsDBTableNames.NOTIFICATION;
	}

	/**
	 * Creates an order confirmation notification for a specific order.
	 * 
	 * The notification data is built using details from the order, subscriber, and
	 * park tables. The created notification is marked as sent because it represents
	 * a simulated popup message.
	 * 
	 * @param orderNumber the order number for which the confirmation notification
	 *                    should be created
	 * @return true if the notification was created successfully, otherwise false
	 * @throws SQLException if the insert query fails
	 */
	public boolean createOrderConfirmationNotification(int orderNumber) throws SQLException {
		String sql = "INSERT INTO notification "
				+ "(subscriber_id, order_number, notification_type, send_channel, recipient_email, "
				+ "recipient_phone, message_title, message_body, scheduled_at, sent_at, notification_status) "
				+ "SELECT s.subscriber_id, o.order_number, 'order_confirmation', 'popup', "
				+ "s.subscriber_email, s.subscriber_phone, 'Order Confirmation', "
				+ "CONCAT('Hello ', s.subscriber_name, ', your visit order number ', "
				+ "o.order_number, ' to park ', p.park_name, ' has been approved.'), "
				+ "NOW(), NOW(), 'sent' "
				+ "FROM `order` o "
				+ "JOIN subscriber s ON o.subscriber_id = s.subscriber_id "
				+ "JOIN park p ON o.park_id = p.park_id "
				+ "WHERE o.order_number = ?;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, orderNumber);

		return pstmt.executeUpdate() > 0;
	}

	/**
	 * Retrieves all notifications from the notification report view.
	 * 
	 * The results are ordered from the newest notification to the oldest.
	 * 
	 * @return a ResultSet containing all records from the notification report view
	 * @throws SQLException if the select query fails
	 */
	public ResultSet getAllNotifications() throws SQLException {
		String sql = "SELECT * FROM notification_report ORDER BY notification_id DESC;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		return pstmt.executeQuery();
	}

	/**
	 * Marks a notification as sent.
	 * 
	 * The method updates the notification status to sent and stores the current
	 * sending time.
	 * 
	 * @param notificationId the ID of the notification to update
	 * @return true if the notification was updated successfully, otherwise false
	 * @throws SQLException if the update query fails
	 */
	public boolean markAsSent(int notificationId) throws SQLException {
		String sql = "UPDATE notification "
				+ "SET notification_status = 'sent', sent_at = NOW() "
				+ "WHERE notification_id = ?;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, notificationId);

		return pstmt.executeUpdate() > 0;
	}
}
