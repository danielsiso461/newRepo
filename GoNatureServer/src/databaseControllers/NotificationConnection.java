package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the DB connector used when working with the notification table.
 * 
 * The class is implemented as a Singleton, so the server will use only one
 * database connection object for notifications during runtime.
 * 
 * The notification table stores system messages that should be sent or simulated,
 * such as order confirmations, visit reminders, order cancellations and waiting
 * list offers.
 */
public class NotificationConnection extends AbstractDBConnection {

	/**
	 * The single instance of NotificationConnection.
	 */
	private static NotificationConnection instance;

	/**
	 * The notification ID column in the notification table.
	 */
	private final String NOTIFICATION_ID = "notification_id";

	/**
	 * The subscriber ID column in the notification table.
	 */
	private final String SUBSCRIBER_ID = "subscriber_id";

	/**
	 * The order number column in the notification table.
	 */
	private final String ORDER_NUMBER = "order_number";

	/**
	 * The waiting list ID column in the notification table.
	 */
	private final String WAITING_ID = "waiting_id";

	/**
	 * The notification type column in the notification table.
	 */
	private final String NOTIFICATION_TYPE = "notification_type";

	/**
	 * The send channel column in the notification table.
	 */
	private final String SEND_CHANNEL = "send_channel";

	/**
	 * The recipient email column in the notification table.
	 */
	private final String RECIPIENT_EMAIL = "recipient_email";

	/**
	 * The recipient phone column in the notification table.
	 */
	private final String RECIPIENT_PHONE = "recipient_phone";

	/**
	 * The message title column in the notification table.
	 */
	private final String MESSAGE_TITLE = "message_title";

	/**
	 * The message body column in the notification table.
	 */
	private final String MESSAGE_BODY = "message_body";

	/**
	 * The scheduled time column in the notification table.
	 */
	private final String SCHEDULED_AT = "scheduled_at";

	/**
	 * The sent time column in the notification table.
	 */
	private final String SENT_AT = "sent_at";

	/**
	 * The notification status column in the notification table.
	 */
	private final String NOTIFICATION_STATUS = "notification_status";

	/**
	 * Notification type for order confirmation.
	 */
	private final String ORDER_CONFIRMATION = "order_confirmation";

	/**
	 * Notification type for visit reminder.
	 */
	private final String VISIT_REMINDER = "visit_reminder";

	/**
	 * Notification type for order cancellation.
	 */
	private final String ORDER_CANCELLED = "order_cancelled";

	/**
	 * Notification type for automatic order cancellation.
	 */
	private final String AUTO_CANCELLED = "auto_cancelled";

	/**
	 * Notification type for waiting list offer.
	 */
	private final String WAITING_OFFER = "waiting_offer";

	/**
	 * Notification type for expired waiting list offer.
	 */
	private final String WAITING_OFFER_EXPIRED = "waiting_offer_expired";

	/**
	 * Notification channel for popup simulation.
	 */
	private final String POPUP = "popup";

	/**
	 * Notification status for pending notifications.
	 */
	private final String PENDING = "pending";

	/**
	 * Notification status for sent notifications.
	 */
	private final String SENT = "sent";

	/**
	 * Notification status for failed notifications.
	 */
	private final String FAILED = "failed";

	/**
	 * Notification status for cancelled notifications.
	 */
	private final String CANCELLED = "cancelled";

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
	 * This method checks that the database connection is open.
	 * 
	 * @throws SQLException if reconnecting to the database fails
	 */
	private void ensureConnection() throws SQLException {
		if (conn == null || conn.isClosed()) {
			connect();
		}
	}

	/**
	 * This method checks whether the notification type is valid.
	 * 
	 * @param notificationType the notification type
	 * @return true if the notification type is valid, false otherwise
	 */
	private boolean isValidNotificationType(String notificationType) {
		return ORDER_CONFIRMATION.equals(notificationType) || VISIT_REMINDER.equals(notificationType)
				|| ORDER_CANCELLED.equals(notificationType) || AUTO_CANCELLED.equals(notificationType)
				|| WAITING_OFFER.equals(notificationType) || WAITING_OFFER_EXPIRED.equals(notificationType);
	}

	/**
	 * This method checks whether the notification status is valid.
	 * 
	 * @param notificationStatus the notification status
	 * @return true if the notification status is valid, false otherwise
	 */
	private boolean isValidNotificationStatus(String notificationStatus) {
		return PENDING.equals(notificationStatus) || SENT.equals(notificationStatus)
				|| FAILED.equals(notificationStatus) || CANCELLED.equals(notificationStatus);
	}

	/**
	 * This method creates a notification record in the database.
	 * 
	 * The method uses insertFields from AbstractDBConnection.
	 * 
	 * Fields that can be null, such as order_number, waiting_id, sent_at,
	 * recipient_email or recipient_phone, are inserted only when they have a value.
	 * 
	 * @param subscriberId       the subscriber ID that should receive the notification
	 * @param orderNumber        the related order number, or null if not relevant
	 * @param waitingId          the related waiting list ID, or null if not relevant
	 * @param notificationType   the notification type
	 * @param recipientEmail     the recipient email, or null if not relevant
	 * @param recipientPhone     the recipient phone, or null if not relevant
	 * @param messageTitle       the message title
	 * @param messageBody        the message body
	 * @param scheduledAt        the scheduled display time
	 * @param sentAt             the actual sent time, or null if not sent yet
	 * @param notificationStatus the notification status
	 * @return the created notification ID, or -1 if the request is invalid or the
	 *         notification was not found after insert
	 * @throws SQLException if the insert or select query fails
	 */
	private int createNotification(Integer subscriberId, Integer orderNumber, Integer waitingId,
			String notificationType, String recipientEmail, String recipientPhone, String messageTitle,
			String messageBody, LocalDateTime scheduledAt, LocalDateTime sentAt, String notificationStatus)
			throws SQLException {

		ensureConnection();

		if (subscriberId == null || subscriberId <= 0 || notificationType == null
				|| !isValidNotificationType(notificationType) || messageTitle == null || messageTitle.isBlank()
				|| messageBody == null || messageBody.isBlank() || scheduledAt == null || notificationStatus == null
				|| !isValidNotificationStatus(notificationStatus) || (orderNumber != null && orderNumber <= 0)
				|| (waitingId != null && waitingId <= 0)) {
			return -1;
		}

		List<String> columnNames = new ArrayList<>();
		List<Object> values = new ArrayList<>();

		columnNames.add(SUBSCRIBER_ID);
		values.add(subscriberId);

		if (orderNumber != null) {
			columnNames.add(ORDER_NUMBER);
			values.add(orderNumber);
		}

		if (waitingId != null) {
			columnNames.add(WAITING_ID);
			values.add(waitingId);
		}

		columnNames.add(NOTIFICATION_TYPE);
		values.add(notificationType);

		columnNames.add(SEND_CHANNEL);
		values.add(POPUP);

		if (recipientEmail != null && !recipientEmail.isBlank()) {
			columnNames.add(RECIPIENT_EMAIL);
			values.add(recipientEmail);
		}

		if (recipientPhone != null && !recipientPhone.isBlank()) {
			columnNames.add(RECIPIENT_PHONE);
			values.add(recipientPhone);
		}

		columnNames.add(MESSAGE_TITLE);
		values.add(messageTitle);

		columnNames.add(MESSAGE_BODY);
		values.add(messageBody);

		columnNames.add(SCHEDULED_AT);
		values.add(Timestamp.valueOf(scheduledAt));

		if (sentAt != null) {
			columnNames.add(SENT_AT);
			values.add(Timestamp.valueOf(sentAt));
		}

		columnNames.add(NOTIFICATION_STATUS);
		values.add(notificationStatus);

		insertFields(columnNames.toArray(new String[columnNames.size()]), values);

		return getCreatedNotificationId(subscriberId, orderNumber, waitingId, notificationType, messageTitle,
				scheduledAt, notificationStatus);
	}

	/**
	 * This method finds the notification ID that was created after inserting a new
	 * notification.
	 * 
	 * The method uses selectByFields from AbstractDBConnection.
	 * We use MAX(notification_id) because the new notification should be the latest
	 * matching notification.
	 * 
	 * @param subscriberId       the subscriber ID
	 * @param orderNumber        the related order number, or null if not relevant
	 * @param waitingId          the related waiting ID, or null if not relevant
	 * @param notificationType   the notification type
	 * @param messageTitle       the message title
	 * @param scheduledAt        the scheduled display time
	 * @param notificationStatus the notification status
	 * @return the created notification ID, or -1 if no matching notification was
	 *         found
	 * @throws SQLException if the select query fails
	 */
	private int getCreatedNotificationId(Integer subscriberId, Integer orderNumber, Integer waitingId,
			String notificationType, String messageTitle, LocalDateTime scheduledAt, String notificationStatus)
			throws SQLException {

		ensureConnection();

		if (subscriberId == null || subscriberId <= 0 || notificationType == null
				|| !isValidNotificationType(notificationType) || messageTitle == null || messageTitle.isBlank()
				|| scheduledAt == null || notificationStatus == null || !isValidNotificationStatus(notificationStatus)
				|| (orderNumber != null && orderNumber <= 0) || (waitingId != null && waitingId <= 0)) {
			return -1;
		}

		List<String> keyColumns = new ArrayList<>();
		List<Object> keyValues = new ArrayList<>();

		keyColumns.add(SUBSCRIBER_ID);
		keyValues.add(subscriberId);

		if (orderNumber != null) {
			keyColumns.add(ORDER_NUMBER);
			keyValues.add(orderNumber);
		}

		if (waitingId != null) {
			keyColumns.add(WAITING_ID);
			keyValues.add(waitingId);
		}

		keyColumns.add(NOTIFICATION_TYPE);
		keyValues.add(notificationType);

		keyColumns.add(SEND_CHANNEL);
		keyValues.add(POPUP);

		keyColumns.add(MESSAGE_TITLE);
		keyValues.add(messageTitle);

		keyColumns.add(SCHEDULED_AT);
		keyValues.add(Timestamp.valueOf(scheduledAt));

		keyColumns.add(NOTIFICATION_STATUS);
		keyValues.add(notificationStatus);

		String sql = selectByFields(new String[] { "MAX(" + NOTIFICATION_ID + ") AS " + NOTIFICATION_ID },
				keyColumns.toArray(new String[keyColumns.size()]));

		PreparedStatement pstmt = conn.prepareStatement(sql);

		for (int i = 0; i < keyValues.size(); i++) {
			pstmt.setObject(i + 1, keyValues.get(i));
		}

		java.sql.ResultSet rs = pstmt.executeQuery();

		int notificationId = -1;

		if (rs.next()) {
			notificationId = rs.getInt(NOTIFICATION_ID);
		}

		rs.close();
		pstmt.close();

		return notificationId;
	}

	/**
	 * This method creates an order confirmation popup notification.
	 * 
	 * The notification is created as pending so the client can display it as a
	 * popup, and after displaying it the server can mark it as sent.
	 * 
	 * @param subscriberId   the subscriber ID
	 * @param orderNumber    the confirmed order number
	 * @param recipientEmail the recipient email
	 * @param recipientPhone the recipient phone
	 * @param subscriberName the subscriber name
	 * @param parkName       the park name
	 * @return the created notification ID, or -1 if creation failed
	 * @throws SQLException if the insert or select query fails
	 */
	public int createOrderConfirmationNotification(int subscriberId, int orderNumber, String recipientEmail,
			String recipientPhone, String subscriberName, String parkName) throws SQLException {

		if (subscriberId <= 0 || orderNumber <= 0 || subscriberName == null || subscriberName.isBlank()
				|| parkName == null || parkName.isBlank()) {
			return -1;
		}

		String title = "Order Confirmation";
		String body = "Hello " + subscriberName + ", your visit order number " + orderNumber + " to park "
				+ parkName + " has been approved.";

		return createNotification(subscriberId, orderNumber, null, ORDER_CONFIRMATION, recipientEmail,
				recipientPhone, title, body, LocalDateTime.now(), null, PENDING);
	}

	/**
	 * This method creates a scheduled visit reminder popup notification.
	 * 
	 * According to the system story, a reminder should be sent one day before the
	 * planned visit time. In this project, the reminder is simulated by popup.
	 * 
	 * The notification is created with pending status, because it should be displayed
	 * only when its scheduled time arrives.
	 * 
	 * @param subscriberId   the subscriber ID
	 * @param orderNumber    the order number
	 * @param recipientEmail the recipient email
	 * @param recipientPhone the recipient phone
	 * @param subscriberName the subscriber name
	 * @param visitTime      the planned visit time
	 * @return the created notification ID, or -1 if creation failed
	 * @throws SQLException if the insert or select query fails
	 */
	public int createVisitReminderNotification(int subscriberId, int orderNumber, String recipientEmail,
			String recipientPhone, String subscriberName, LocalDateTime visitTime) throws SQLException {

		if (subscriberId <= 0 || orderNumber <= 0 || subscriberName == null || subscriberName.isBlank()
				|| visitTime == null) {
			return -1;
		}

		LocalDateTime scheduledAt = visitTime.minusDays(1);

		String title = "Visit Reminder";
		String body = "Reminder: your visit order number " + orderNumber
				+ " is scheduled for tomorrow. Please confirm or cancel the visit within two hours.";

		return createNotification(subscriberId, orderNumber, null, VISIT_REMINDER, recipientEmail, recipientPhone,
				title, body, scheduledAt, null, PENDING);
	}

	/**
	 * This method creates an order cancelled popup notification.
	 * 
	 * @param subscriberId   the subscriber ID
	 * @param orderNumber    the cancelled order number
	 * @param recipientEmail the recipient email
	 * @param recipientPhone the recipient phone
	 * @param parkName       the park name
	 * @return the created notification ID, or -1 if creation failed
	 * @throws SQLException if the insert or select query fails
	 */
	public int createOrderCancelledNotification(int subscriberId, int orderNumber, String recipientEmail,
			String recipientPhone, String parkName) throws SQLException {

		if (subscriberId <= 0 || orderNumber <= 0 || parkName == null || parkName.isBlank()) {
			return -1;
		}

		String title = "Order Cancelled";
		String body = "Your order number " + orderNumber + " for park " + parkName + " was cancelled.";

		return createNotification(subscriberId, orderNumber, null, ORDER_CANCELLED, recipientEmail, recipientPhone,
				title, body, LocalDateTime.now(), null, PENDING);
	}

	/**
	 * This method creates an automatic cancellation popup notification.
	 * 
	 * @param subscriberId   the subscriber ID
	 * @param orderNumber    the automatically cancelled order number
	 * @param recipientEmail the recipient email
	 * @param recipientPhone the recipient phone
	 * @return the created notification ID, or -1 if creation failed
	 * @throws SQLException if the insert or select query fails
	 */
	public int createAutoCancelledNotification(int subscriberId, int orderNumber, String recipientEmail,
			String recipientPhone) throws SQLException {

		if (subscriberId <= 0 || orderNumber <= 0) {
			return -1;
		}

		String title = "Order Automatically Cancelled";
		String body = "Your order number " + orderNumber
				+ " was automatically cancelled because the visit was not confirmed in time.";

		return createNotification(subscriberId, orderNumber, null, AUTO_CANCELLED, recipientEmail, recipientPhone,
				title, body, LocalDateTime.now(), null, PENDING);
	}

	/**
	 * This method creates a waiting list offer popup notification.
	 * 
	 * @param subscriberId   the subscriber ID
	 * @param waitingId      the waiting list ID
	 * @param recipientEmail the recipient email
	 * @param recipientPhone the recipient phone
	 * @param parkName       the park name
	 * @return the created notification ID, or -1 if creation failed
	 * @throws SQLException if the insert or select query fails
	 */
	public int createWaitingOfferNotification(int subscriberId, int waitingId, String recipientEmail,
			String recipientPhone, String parkName) throws SQLException {

		if (subscriberId <= 0 || waitingId <= 0 || parkName == null || parkName.isBlank()) {
			return -1;
		}

		String title = "Waiting List Offer";
		String body = "A place is now available for your requested visit to park " + parkName
				+ ". Please confirm the offer within one hour.";

		return createNotification(subscriberId, null, waitingId, WAITING_OFFER, recipientEmail, recipientPhone,
				title, body, LocalDateTime.now(), null, PENDING);
	}

	/**
	 * This method creates a waiting list offer expired popup notification.
	 * 
	 * @param subscriberId   the subscriber ID
	 * @param waitingId      the waiting list ID
	 * @param recipientEmail the recipient email
	 * @param recipientPhone the recipient phone
	 * @param parkName       the park name
	 * @return the created notification ID, or -1 if creation failed
	 * @throws SQLException if the insert or select query fails
	 */
	public int createWaitingOfferExpiredNotification(int subscriberId, int waitingId, String recipientEmail,
			String recipientPhone, String parkName) throws SQLException {

		if (subscriberId <= 0 || waitingId <= 0 || parkName == null || parkName.isBlank()) {
			return -1;
		}

		String title = "Waiting List Offer Expired";
		String body = "The waiting list offer for park " + parkName
				+ " expired because it was not confirmed within one hour.";

		return createNotification(subscriberId, null, waitingId, WAITING_OFFER_EXPIRED, recipientEmail,
				recipientPhone, title, body, LocalDateTime.now(), null, PENDING);
	}

	/**
	 * This method checks whether a visit reminder popup should be displayed now.
	 * 
	 * A reminder should be displayed only if:
	 * the notification belongs to the given order,
	 * the notification type is visit_reminder,
	 * the send channel is popup,
	 * the notification status is pending,
	 * and the scheduled time has already arrived.
	 * 
	 * @param orderNumber the order number
	 * @return true if a reminder popup should be displayed, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean hasDueVisitReminderNotification(int orderNumber) throws SQLException {
		return !getDueVisitReminderNotification(orderNumber).isEmpty();
	}

	/**
	 * This method returns the pending visit reminder notification data for a specific order.
	 * 
	 * The method does not display a popup. It only checks the database and returns
	 * the notification data that should be sent to the client.
	 * 
	 * The returned ArrayList contains:
	 * notification_id, subscriber_id, order_number, message_title, message_body,
	 * recipient_email, recipient_phone.
	 * 
	 * @param orderNumber the order number
	 * @return an ArrayList with notification data, or an empty ArrayList if no
	 *         reminder notification is due
	 * @throws SQLException if the select query fails
	 */
	public ArrayList<Object> getDueVisitReminderNotification(int orderNumber) throws SQLException {
		ensureConnection();

		ArrayList<Object> notificationData = new ArrayList<>();

		if (orderNumber <= 0) {
			return notificationData;
		}

		String[] columnNames = {
				NOTIFICATION_ID,
				SUBSCRIBER_ID,
				ORDER_NUMBER,
				MESSAGE_TITLE,
				MESSAGE_BODY,
				RECIPIENT_EMAIL,
				RECIPIENT_PHONE
		};

		String[] keyColumns = {
				ORDER_NUMBER,
				NOTIFICATION_TYPE,
				SEND_CHANNEL,
				NOTIFICATION_STATUS
		};

		String sql = selectByFields(columnNames, keyColumns);

		/*
		 * selectByFields creates only equality conditions.
		 * Because we also need scheduled_at <= NOW(), we add this condition manually.
		 */
		sql = sql.substring(0, sql.length() - 1) + " AND " + SCHEDULED_AT + " <= NOW();";

		PreparedStatement pstmt = conn.prepareStatement(sql);

		pstmt.setInt(1, orderNumber);
		pstmt.setString(2, VISIT_REMINDER);
		pstmt.setString(3, POPUP);
		pstmt.setString(4, PENDING);

		java.sql.ResultSet rs = pstmt.executeQuery();

		if (rs.next()) {
			notificationData.add(rs.getInt(NOTIFICATION_ID));
			notificationData.add(rs.getInt(SUBSCRIBER_ID));
			notificationData.add(rs.getInt(ORDER_NUMBER));
			notificationData.add(rs.getString(MESSAGE_TITLE));
			notificationData.add(rs.getString(MESSAGE_BODY));
			notificationData.add(rs.getString(RECIPIENT_EMAIL));
			notificationData.add(rs.getString(RECIPIENT_PHONE));
		}

		rs.close();
		pstmt.close();

		return notificationData;
	}

	/**
	 * This method returns pending notification data for a specific order and
	 * notification type.
	 * 
	 * The method does not display a popup. It only checks the database and returns
	 * the notification data that should be sent to the client. The client GUI is
	 * responsible for displaying the popup simulation.
	 * 
	 * A notification is returned only if:
	 * the notification belongs to the given order,
	 */
	public ArrayList<Object> getDueOrderNotificationByType(int orderNumber, String notificationType) throws SQLException {
		ensureConnection();

		ArrayList<Object> popupData = new ArrayList<>();

		if (orderNumber <= 0 || notificationType == null || !isValidNotificationType(notificationType)) {
			return popupData;
		}

		String[] columnNames = {
				NOTIFICATION_ID,
				SUBSCRIBER_ID,
				ORDER_NUMBER,
				MESSAGE_TITLE,
				MESSAGE_BODY,
				RECIPIENT_EMAIL,
				RECIPIENT_PHONE
		};

		String[] keyColumns = {
				ORDER_NUMBER,
				NOTIFICATION_TYPE,
				SEND_CHANNEL,
				NOTIFICATION_STATUS
		};

		String sql = selectByFields(columnNames, keyColumns);
		sql = sql.substring(0, sql.length() - 1) + " AND " + SCHEDULED_AT + " <= NOW();";

		PreparedStatement pstmt = conn.prepareStatement(sql);

		pstmt.setInt(1, orderNumber);
		pstmt.setString(2, notificationType);
		pstmt.setString(3, POPUP);
		pstmt.setString(4, PENDING);

		java.sql.ResultSet rs = pstmt.executeQuery();

		if (rs.next()) {
			popupData.add(rs.getInt(NOTIFICATION_ID));
			popupData.add(rs.getInt(SUBSCRIBER_ID));
			popupData.add(rs.getInt(ORDER_NUMBER));
			popupData.add(rs.getString(MESSAGE_TITLE));
			popupData.add(rs.getString(MESSAGE_BODY));
			popupData.add(rs.getString(RECIPIENT_EMAIL));
			popupData.add(rs.getString(RECIPIENT_PHONE));
		}

		rs.close();
		pstmt.close();

		return popupData;
	}

	/**
	 * This method marks a notification as sent.
	 * 
	 * The method uses updateFields from AbstractDBConnection.
	 * 
	 * @param notificationId the notification ID
	 * @return true if the update request was valid, false otherwise
	 * @throws SQLException if the update query fails
	 */
	public boolean markAsSent(int notificationId) throws SQLException {
		ensureConnection();

		if (notificationId <= 0) {
			return false;
		}

		List<String> columnNames = new ArrayList<>();
		List<Object> newValues = new ArrayList<>();
		List<String> keyColumns = new ArrayList<>();
		List<Object> keyValues = new ArrayList<>();

		columnNames.add(NOTIFICATION_STATUS);
		newValues.add(SENT);

		columnNames.add(SENT_AT);
		newValues.add(Timestamp.valueOf(LocalDateTime.now()));

		keyColumns.add(NOTIFICATION_ID);
		keyValues.add(notificationId);

		updateFields(columnNames.toArray(new String[columnNames.size()]), newValues,
				keyColumns.toArray(new String[keyColumns.size()]), keyValues);

		return true;
	}

	/**
	 * This method marks a notification as failed.
	 * 
	 * The method uses updateFields from AbstractDBConnection.
	 * 
	 * @param notificationId the notification ID
	 * @return true if the update request was valid, false otherwise
	 * @throws SQLException if the update query fails
	 */
	public boolean markAsFailed(int notificationId) throws SQLException {
		ensureConnection();

		if (notificationId <= 0) {
			return false;
		}

		List<String> columnNames = new ArrayList<>();
		List<Object> newValues = new ArrayList<>();
		List<String> keyColumns = new ArrayList<>();
		List<Object> keyValues = new ArrayList<>();

		columnNames.add(NOTIFICATION_STATUS);
		newValues.add(FAILED);

		keyColumns.add(NOTIFICATION_ID);
		keyValues.add(notificationId);

		updateFields(columnNames.toArray(new String[columnNames.size()]), newValues,
				keyColumns.toArray(new String[keyColumns.size()]), keyValues);

		return true;
	}

	/**
	 * This method cancels a pending notification.
	 * 
	 * The method uses updateFields from AbstractDBConnection.
	 * 
	 * @param notificationId the notification ID
	 * @return true if the update request was valid, false otherwise
	 * @throws SQLException if the update query fails
	 */
	public boolean cancelNotification(int notificationId) throws SQLException {
		ensureConnection();

		if (notificationId <= 0) {
			return false;
		}

		List<String> columnNames = new ArrayList<>();
		List<Object> newValues = new ArrayList<>();
		List<String> keyColumns = new ArrayList<>();
		List<Object> keyValues = new ArrayList<>();

		columnNames.add(NOTIFICATION_STATUS);
		newValues.add(CANCELLED);

		keyColumns.add(NOTIFICATION_ID);
		keyValues.add(notificationId);

		updateFields(columnNames.toArray(new String[columnNames.size()]), newValues,
				keyColumns.toArray(new String[keyColumns.size()]), keyValues);

		return true;
	}
}