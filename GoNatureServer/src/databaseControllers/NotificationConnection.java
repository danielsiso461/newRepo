package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class is the DB connector used when working with the notification table.
 * 
 * The class is implemented as a Singleton, so the server will use only one
 * database connection object for notifications during runtime.
 * 
 * The notification table stores simulated messages that the system prepares for
 * users, such as order confirmations, visit reminders, cancellations, and waiting
 * list offers.
 */
public class NotificationConnection extends AbstractDBConnection {

	/**
	 * The single instance of NotificationConnection.
	 */
	private static NotificationConnection instance;

	/**
	 * Private constructor for Singleton.
	 * 
	 * It creates the database connection once.
	 * 
	 * @throws SQLException if the connection to the database fails
	 */
	private NotificationConnection() throws SQLException {
		connect();
	}

	/**
	 * Returns the single instance of NotificationConnection.
	 * 
	 * If no instance exists, or if the existing database connection is closed, a new
	 * instance is created.
	 * 
	 * @return the only NotificationConnection instance
	 * @throws SQLException if creating the database connection fails
	 */
	public static NotificationConnection getInstance() throws SQLException {
		if (instance == null || instance.conn == null || instance.conn.isClosed()) {
			instance = new NotificationConnection();
		}
		return instance;
	}

	/**
	 * Returns the table name used by this DB connector.
	 * 
	 * @return the notification table name
	 */
	@Override
	protected String getTableName() {
		return ConstantsDBTableNames.NOTIFICATION;
	}

	/**
	 * This method creates an order confirmation notification for a specific order.
	 * 
	 * The notification is created using data from the order, subscriber, and park
	 * tables. The created notification is marked as sent because it represents a
	 * simulated popup message.
	 * 
	 * @param orderNumber the order number for which the confirmation notification
	 *                    should be created
	 * @return true if the notification was created successfully, false otherwise
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
	 * This method returns all notifications from the notification report view.
	 * 
	 * The results are ordered from the newest notification to the oldest.
	 * 
	 * @return a ResultSet containing all notifications from notification_report
	 * @throws SQLException if the select query fails
	 */
	public ResultSet getAllNotifications() throws SQLException {
		String sql = "SELECT * FROM notification_report ORDER BY notification_id DESC;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		return pstmt.executeQuery();
	}

	/**
	 * This method marks a notification as sent.
	 * 
	 * It updates the notification status to sent and stores the sending time.
	 * 
	 * @param notificationId the ID of the notification to mark as sent
	 * @return true if the notification was updated successfully, false otherwise
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