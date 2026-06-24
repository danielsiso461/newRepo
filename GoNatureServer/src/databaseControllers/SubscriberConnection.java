package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import common.Subscriber;

import common.SystemUser;

/**
 * This class is the DB connector used when working with the subscriber table.
 * 
 * The class is implemented as a Singleton, so the server will use only one
 * database connection object for subscribers during runtime.
 * 
 * The subscriber table stores registered family subscribers, including their
 * personal details, contact details, number of family members, payment method,
 * and optional credit card details.
 */
public class SubscriberConnection extends AbstractDBConnection {

	/**
	 * The single instance of SubscriberConnection.
	 */
	private static SubscriberConnection instance;

	/**
	 * The subscriber ID column in the subscriber table.
	 */
	private final String SUBSCRIBER_ID = "subscriber_id";

	/**
	 * The subscriber full name column in the subscriber table.
	 */
	private final String SUBSCRIBER_NAME = "subscriber_name";

	/**
	 * The subscriber personal ID number column in the subscriber table.
	 */
	private final String SUBSCRIBER_ID_NUMBER = "subscriber_id_number";

	/**
	 * The subscriber phone column in the subscriber table.
	 */
	private final String SUBSCRIBER_PHONE = "subscriber_phone";

	/**
	 * The subscriber email column in the subscriber table.
	 */
	private final String SUBSCRIBER_EMAIL = "subscriber_email";

	/**
	 * The number of family members included in the subscription.
	 */
	private final String FAMILY_MEMBERS_COUNT = "family_members_count";

	/**
	 * The payment method column in the subscriber table.
	 */
	private final String PAYMENT_METHOD = "payment_method";

	/**
	 * The last four digits of the credit card column in the subscriber table.
	 */
	private final String CREDIT_CARD_LAST4 = "credit_card_last4";

	/**
	 * Payment method value for cash payment.
	 */
	private final String CASH = "cash";

	/**
	 * Payment method value for credit card payment.
	 */
	private final String CREDIT_CARD = "credit_card";

	/**
	 * Private constructor for Singleton.
	 * 
	 * It creates the database connection once.
	 * 
	 * @throws SQLException if the connection to the database fails
	 */
	private SubscriberConnection() throws SQLException {
		connect();
	}

	/**
	 * Returns the single instance of SubscriberConnection.
	 * 
	 * If no instance exists, or if the existing database connection is closed, a new
	 * instance is created.
	 * 
	 * @return the only SubscriberConnection instance
	 * @throws SQLException if creating the database connection fails
	 */
	public static SubscriberConnection getInstance() throws SQLException {
		if (instance == null || instance.conn == null || instance.conn.isClosed()) {
			instance = new SubscriberConnection();
		}

		return instance;
	}

	/**
	 * Returns the table name used by this DB connector.
	 * 
	 * @return the subscriber table name
	 */
	@Override
	protected String getTableName() {
		return ConstantsDBTableNames.SUBSCRIBER;
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
	 * This method adds a new subscriber to the database.
	 * 
	 * According to the system story, family subscribers are registered by a service
	 * representative. The permission check should be done in the server controller
	 * before calling this method.
	 * 
	 * The method uses insertFields from AbstractDBConnection.
	 * 
	 * If the payment method is cash, creditCardLast4 is not inserted and remains
	 * null in the database.
	 * 
	 * @param subscriberId       the subscriber ID used as the subscriber number
	 * @param subscriberName     the full name of the subscriber
	 * @param idNumber           the personal identification number of the subscriber
	 * @param phone              the subscriber phone number
	 * @param email              the subscriber email address
	 * @param familyMembersCount the number of family members included in the
	 *                           subscription
	 * @param paymentMethod      the payment method: cash or credit_card
	 * @param creditCardLast4    the last four digits of the credit card, or null for
	 *                           cash payment
	 * @return true if the subscriber was added successfully, false otherwise
	 * @throws SQLException if the insert or select query fails
	 */
	public boolean addSubscriber(int subscriberId, String subscriberName, String idNumber, String phone, String email,
			int familyMembersCount, String paymentMethod, String creditCardLast4) throws SQLException {

		ensureConnection();

		if (subscriberId <= 0 || subscriberName == null || subscriberName.isBlank() || idNumber == null
				|| idNumber.isBlank() || phone == null || phone.isBlank() || email == null || email.isBlank()
				|| familyMembersCount <= 0 || paymentMethod == null || paymentMethod.isBlank()
				|| (!paymentMethod.equals(CASH) && !paymentMethod.equals(CREDIT_CARD))
				|| (paymentMethod.equals(CREDIT_CARD) && (creditCardLast4 == null || creditCardLast4.isBlank()))
				|| (creditCardLast4 != null && creditCardLast4.length() > 4)) {
			return false;
		}

		List<String> columnNames = new ArrayList<>();
		List<Object> values = new ArrayList<>();

		columnNames.add(SUBSCRIBER_ID);
		values.add(subscriberId);

		columnNames.add(SUBSCRIBER_NAME);
		values.add(subscriberName);

		columnNames.add(SUBSCRIBER_ID_NUMBER);
		values.add(idNumber);

		columnNames.add(SUBSCRIBER_PHONE);
		values.add(phone);

		columnNames.add(SUBSCRIBER_EMAIL);
		values.add(email);

		columnNames.add(FAMILY_MEMBERS_COUNT);
		values.add(familyMembersCount);

		columnNames.add(PAYMENT_METHOD);
		values.add(paymentMethod);

		if (creditCardLast4 != null && !creditCardLast4.isBlank()) {
			columnNames.add(CREDIT_CARD_LAST4);
			values.add(creditCardLast4);
		}

		insertFields(columnNames.toArray(new String[columnNames.size()]), values);

		return subscriberExists(subscriberId);
	}

	/**
	 * This method checks whether a subscriber exists in the database by subscriber
	 * ID.
	 * 
	 * @param subscriberId the subscriber ID
	 * @return true if the subscriber exists, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean subscriberExists(int subscriberId) throws SQLException {
		return !getSubscriberById(subscriberId).isEmpty();
	}

	/**
	 * This method checks whether a subscriber exists in the database by personal ID
	 * number.
	 * 
	 * This is useful because visitors identify themselves using an ID number when
	 * handling orders and entrance identification.
	 * 
	 * @param idNumber the personal identification number
	 * @return true if a subscriber with this ID number exists, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean subscriberExistsByIdNumber(String idNumber) throws SQLException {
		return !getSubscriberByIdNumber(idNumber).isEmpty();
	}

	/**
	 * This method returns subscriber data by subscriber ID.
	 * 
	 * The method uses selectByFields from AbstractDBConnection.
	 * 
	 * The returned ArrayList contains the following values:
	 * subscriber_id, subscriber_name, subscriber_id_number, subscriber_phone,
	 * subscriber_email, family_members_count, payment_method, credit_card_last4.
	 * 
	 * @param subscriberId the subscriber ID
	 * @return an ArrayList containing subscriber data, or an empty ArrayList if the
	 *         subscriber was not found
	 * @throws SQLException if the select query fails
	 */
	public ArrayList<Object> getSubscriberById(int subscriberId) throws SQLException {
		ensureConnection();

		ArrayList<Object> subscriberData = new ArrayList<>();

		if (subscriberId <= 0) {
			return subscriberData;
		}

		String[] columnNames = {
				SUBSCRIBER_ID,
				SUBSCRIBER_NAME,
				SUBSCRIBER_ID_NUMBER,
				SUBSCRIBER_PHONE,
				SUBSCRIBER_EMAIL,
				FAMILY_MEMBERS_COUNT,
				PAYMENT_METHOD,
				CREDIT_CARD_LAST4
		};

		String[] keyColumns = {
				SUBSCRIBER_ID
		};

		String sql = selectByFields(columnNames, keyColumns);

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, subscriberId);

		java.sql.ResultSet rs = pstmt.executeQuery();

		if (rs.next()) {
			subscriberData.add(rs.getInt(SUBSCRIBER_ID));
			subscriberData.add(rs.getString(SUBSCRIBER_NAME));
			subscriberData.add(rs.getString(SUBSCRIBER_ID_NUMBER));
			subscriberData.add(rs.getString(SUBSCRIBER_PHONE));
			subscriberData.add(rs.getString(SUBSCRIBER_EMAIL));
			subscriberData.add(rs.getInt(FAMILY_MEMBERS_COUNT));
			subscriberData.add(rs.getString(PAYMENT_METHOD));

			if (rs.getObject(CREDIT_CARD_LAST4) != null) {
				subscriberData.add(rs.getString(CREDIT_CARD_LAST4));
			} else {
				subscriberData.add(null);
			}
		}

		rs.close();
		pstmt.close();

		return subscriberData;
	}

	/**
	 * This method returns subscriber data by personal ID number.
	 * 
	 * The method uses selectByFields from AbstractDBConnection.
	 * 
	 * The returned ArrayList contains the following values:
	 * subscriber_id, subscriber_name, subscriber_id_number, subscriber_phone,
	 * subscriber_email, family_members_count, payment_method, credit_card_last4.
	 * 
	 * @param idNumber the personal identification number
	 * @return an ArrayList containing subscriber data, or an empty ArrayList if the
	 *         subscriber was not found
	 * @throws SQLException if the select query fails
	 */
	public ArrayList<Object> getSubscriberByIdNumber(String idNumber) throws SQLException {
		ensureConnection();

		ArrayList<Object> subscriberData = new ArrayList<>();

		if (idNumber == null || idNumber.isBlank()) {
			return subscriberData;
		}

		String[] columnNames = {
				SUBSCRIBER_ID,
				SUBSCRIBER_NAME,
				SUBSCRIBER_ID_NUMBER,
				SUBSCRIBER_PHONE,
				SUBSCRIBER_EMAIL,
				FAMILY_MEMBERS_COUNT,
				PAYMENT_METHOD,
				CREDIT_CARD_LAST4
		};

		String[] keyColumns = {
				SUBSCRIBER_ID_NUMBER
		};

		String sql = selectByFields(columnNames, keyColumns);

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, idNumber);

		java.sql.ResultSet rs = pstmt.executeQuery();

		if (rs.next()) {
			subscriberData.add(rs.getInt(SUBSCRIBER_ID));
			subscriberData.add(rs.getString(SUBSCRIBER_NAME));
			subscriberData.add(rs.getString(SUBSCRIBER_ID_NUMBER));
			subscriberData.add(rs.getString(SUBSCRIBER_PHONE));
			subscriberData.add(rs.getString(SUBSCRIBER_EMAIL));
			subscriberData.add(rs.getInt(FAMILY_MEMBERS_COUNT));
			subscriberData.add(rs.getString(PAYMENT_METHOD));

			if (rs.getObject(CREDIT_CARD_LAST4) != null) {
				subscriberData.add(rs.getString(CREDIT_CARD_LAST4));
			} else {
				subscriberData.add(null);
			}
		}

		rs.close();
		pstmt.close();

		return subscriberData;
	}

	/**
	 * This method returns subscriber data as a SystemUser object.
	 * 
	 * This can be used after identifying a subscriber in order to send a shared user
	 * object to the client.
	 * 
	 * @param subscriberId the subscriber ID
	 * @return a SystemUser object if the subscriber exists, otherwise null
	 * @throws SQLException if the select query fails
	 */
	public SystemUser getSubscriberAsSystemUser(int subscriberId) throws SQLException {
		ArrayList<Object> subscriberData = getSubscriberById(subscriberId);

		if (subscriberData.isEmpty()) {
			return null;
		}

		return new SystemUser(
				((Number) subscriberData.get(0)).intValue(),
				(String) subscriberData.get(1),
				(String) subscriberData.get(4),
				null,
				"subscriber",
				"subscriber",
				null);
	}

	/**
	 * This method returns subscriber data as a SystemUser object by personal ID
	 * number.
	 * 
	 * @param idNumber the personal identification number
	 * @return a SystemUser object if the subscriber exists, otherwise null
	 * @throws SQLException if the select query fails
	 */
	public SystemUser getSubscriberAsSystemUserByIdNumber(String idNumber) throws SQLException {
		ArrayList<Object> subscriberData = getSubscriberByIdNumber(idNumber);

		if (subscriberData.isEmpty()) {
			return null;
		}

		return new SystemUser(
				((Number) subscriberData.get(0)).intValue(),
				(String) subscriberData.get(1),
				(String) subscriberData.get(4),
				null,
				"subscriber",
				"subscriber",
				null);
	}

	/**
	 * This method returns the number of family members in the subscription.
	 * 
	 * This can be used when checking visitor numbers for subscriber-related
	 * operations.
	 * 
	 * @param subscriberId the subscriber ID
	 * @return the number of family members, or -1 if the subscriber was not found
	 * @throws SQLException if the select query fails
	 */
	public int getFamilyMembersCount(int subscriberId) throws SQLException {
		ensureConnection();

		if (subscriberId <= 0) {
			return -1;
		}

		String sql = selectByFields(new String[] { FAMILY_MEMBERS_COUNT }, new String[] { SUBSCRIBER_ID });

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, subscriberId);

		java.sql.ResultSet rs = pstmt.executeQuery();

		int familyMembersCount = -1;

		if (rs.next()) {
			familyMembersCount = rs.getInt(FAMILY_MEMBERS_COUNT);
		}

		rs.close();
		pstmt.close();

		return familyMembersCount;
	}

	

	/**
	 * This method updates subscriber payment details.
	 * 
	 * The method uses updateFields from AbstractDBConnection.
	 * If the payment method is cash, the credit card last four digits are set to
	 * null.
	 * 
	 * @param subscriberId    the subscriber ID
	 * @param paymentMethod   the new payment method: cash or credit_card
	 * @param creditCardLast4 the last four digits of the credit card, or null for
	 *                        cash payment
	 * @return true if the update request was valid, false otherwise
	 * @throws SQLException if the update query fails
	 */
	public boolean updateSubscriberPayment(int subscriberId, String paymentMethod, String creditCardLast4)
			throws SQLException {

		ensureConnection();

		if (subscriberId <= 0 || paymentMethod == null || paymentMethod.isBlank()
				|| (!paymentMethod.equals(CASH) && !paymentMethod.equals(CREDIT_CARD))
				|| (paymentMethod.equals(CREDIT_CARD) && (creditCardLast4 == null || creditCardLast4.isBlank()))
				|| (creditCardLast4 != null && creditCardLast4.length() > 4)) {
			return false;
		}

		List<String> columnNames = new ArrayList<>();
		List<Object> newValues = new ArrayList<>();
		List<String> keyColumns = new ArrayList<>();
		List<Object> keyValues = new ArrayList<>();

		columnNames.add(PAYMENT_METHOD);
		newValues.add(paymentMethod);

		columnNames.add(CREDIT_CARD_LAST4);

		if (paymentMethod.equals(CASH)) {
			newValues.add(null);
		} else {
			newValues.add(creditCardLast4);
		}

		keyColumns.add(SUBSCRIBER_ID);
		keyValues.add(subscriberId);

		updateFields(columnNames.toArray(new String[columnNames.size()]), newValues,
				keyColumns.toArray(new String[keyColumns.size()]), keyValues);

		return true;
	}
	
	
	public Subscriber findSubscriberById(int subscriberId) throws SQLException {
		ArrayList<Object> subscriberData = getSubscriberById(subscriberId);

		if (subscriberData.isEmpty()) {
			return null;
		}

		return new Subscriber(
				((Number) subscriberData.get(0)).intValue(),
				(String) subscriberData.get(1),
				(String) subscriberData.get(2),
				(String) subscriberData.get(3),
				(String) subscriberData.get(4),
				((Number) subscriberData.get(5)).intValue(),
				(String) subscriberData.get(6),
				(String) subscriberData.get(7));
	}
}