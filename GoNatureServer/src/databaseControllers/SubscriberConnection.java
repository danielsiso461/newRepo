package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import common.Subscriber;
import common.RegisterSubscriberRequest;


/**
 * This class is the DB connector used when working with the subscriber table.
 * 
 * The class is implemented as a Singleton, so the server will use only one
 * database connection object for subscribers during runtime.
 * 
 * The subscriber table stores registered visitors and family subscribers,
 * including their personal details, contact details, family members count, and
 * payment method.
 */
public class SubscriberConnection extends AbstractDBConnection {

	/**
	 * The single instance of SubscriberConnection.
	 */
	private static SubscriberConnection instance;

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
	
	private final String SUBSCRIBER_ID = "subscriber_id";
	private final String SUBSCRIBER_NAME = "subscriber_name";
	private final String SUBSCRIBER_ID_NUMBER = "subscriber_id_number";
	private final String SUBSCRIBER_PHONE = "subscriber_phone";
	private final String SUBSCRIBER_EMAIL = "subscriber_email";
	private final String FAMILY_MEMBERS_COUNT = "family_members_count";
	private final String PAYMENT_METHOD = "payment_method";
	private final String CREDIT_CARD_LAST4 = "credit_card_last4";
	private final String USERNAME = "username";
	private final String PASSWORD = "password";
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
	 * This method returns a subscriber by subscriber ID.
	 * 
	 * The subscriber ID is used by visitors to identify themselves in the system and
	 * to make orders.
	 * 
	 * @param subscriberId the subscriber ID
	 * @return a ResultSet containing the subscriber data if the subscriber exists
	 * @throws SQLException if the select query fails
	 */
	public ResultSet getSubscriberById(int subscriberId) throws SQLException {
		String sql = "SELECT * FROM subscriber WHERE subscriber_id = ?;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, subscriberId);

		return pstmt.executeQuery();
	}

	/**
	 * This method checks whether a subscriber exists in the database.
	 * 
	 * @param subscriberId the subscriber ID to check
	 * @return true if the subscriber exists, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean subscriberExists(int subscriberId) throws SQLException {
		String sql = "SELECT subscriber_id FROM subscriber WHERE subscriber_id = ?;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, subscriberId);

		ResultSet rs = pstmt.executeQuery();

		return rs.next();
	}
	
	/**
	 * This method searches for a subscriber by subscriber ID and returns it as a
	 * Subscriber object.
	 * 
	 * The method uses selectByFields in order to build the SELECT query according
	 * to the shared DB connection structure.
	 * 
	 * @param subscriberId the subscriber ID to search for
	 * @return a Subscriber object if found, otherwise null
	 * @throws SQLException if the select query fails
	 */
	public Subscriber findSubscriberById(int subscriberId) throws SQLException {
		String query = selectByFields(
				new String[] { SUBSCRIBER_ID, SUBSCRIBER_NAME, SUBSCRIBER_EMAIL },
				new String[] { SUBSCRIBER_ID }
		);

		PreparedStatement pstmt = conn.prepareStatement(query);
		pstmt.setInt(1, subscriberId);

		ResultSet rs = pstmt.executeQuery();

		Subscriber subscriber = null;

		if (rs.next()) {
			subscriber = new Subscriber(
					rs.getInt(SUBSCRIBER_ID),
					rs.getString(SUBSCRIBER_NAME),
					rs.getString(SUBSCRIBER_EMAIL)
			);
		}

		rs.close();
		pstmt.close();

		return subscriber;
	}

	/**
	 * This method adds a new subscriber to the database.
	 * 
	 * A subscriber can use the system to make orders and may receive subscriber
	 * discounts according to the pricing model.
	 * 
	 * @param subscriberId        the subscriber ID used as the primary key
	 * @param subscriberName      the full name of the subscriber
	 * @param idNumber            the personal identification number of the subscriber
	 * @param phone               the subscriber phone number
	 * @param email               the subscriber email address
	 * @param familyMembersCount  the number of family members included in the
	 *                            subscription
	 * @param paymentMethod       the payment method, such as cash or credit_card
	 * @param creditCardLast4     the last four digits of the credit card, or null if
	 *                            the payment method is cash
	 * @return true if the subscriber was added successfully, false otherwise
	 * @throws SQLException if the insert query fails
	 */
	public boolean addSubscriber(
			int subscriberId,
			String subscriberName,
			String idNumber,
			String phone,
			String email,
			int familyMembersCount,
			String paymentMethod,
			String creditCardLast4) throws SQLException {

		String sql = "INSERT INTO subscriber "
				+ "(subscriber_id, subscriber_name, subscriber_id_number, subscriber_phone, "
				+ "subscriber_email, family_members_count, payment_method, credit_card_last4) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

		PreparedStatement pstmt = conn.prepareStatement(sql);

		pstmt.setInt(1, subscriberId);
		pstmt.setString(2, subscriberName);
		pstmt.setString(3, idNumber);
		pstmt.setString(4, phone);
		pstmt.setString(5, email);
		pstmt.setInt(6, familyMembersCount);
		pstmt.setString(7, paymentMethod);
		pstmt.setString(8, creditCardLast4);

		return pstmt.executeUpdate() > 0;
	}
	
	/**
	 * This method checks existing customer login details and returns the matching
	 * subscriber.
	 * 
	 * The method searches for a subscriber whose username and password match the
	 * values entered in the login screen.
	 * 
	 * @param username the username entered by the customer
	 * @param password the password entered by the customer
	 * @return a Subscriber object if login succeeds, otherwise null
	 * @throws SQLException if the select query fails
	 */
	public Subscriber loginSubscriber(String username, String password) throws SQLException {
		String query = selectByFields(
				new String[] {
						SUBSCRIBER_ID,
						SUBSCRIBER_NAME,
						SUBSCRIBER_EMAIL
				},
				new String[] {
						USERNAME,
						PASSWORD
				}
		);

		try (PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setString(1, username);
			pstmt.setString(2, password);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return new Subscriber(
							rs.getInt(SUBSCRIBER_ID),
							rs.getString(SUBSCRIBER_NAME),
							rs.getString(SUBSCRIBER_EMAIL)
					);
				}
			}
		}

		return null;
	}
	
	/**
	 * Checks whether a username already exists in the subscriber table.
	 * 
	 * @param username the username to check
	 * @return true if the username already exists, otherwise false
	 * @throws SQLException if the query fails
	 */
	public boolean isUsernameExists(String username) throws SQLException {
		String query = selectByFields(
				new String[] {
						USERNAME
				},
				new String[] {
						USERNAME
				}
		);

		try (PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setString(1, username);

			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}
	
	/**
	 * Checks whether an ID number already exists in the subscriber table.
	 * 
	 * @param idNumber the ID number to check
	 * @return true if the ID number already exists, otherwise false
	 * @throws SQLException if the query fails
	 */
	public boolean isIdNumberExists(String idNumber) throws SQLException {
		String query = selectByFields(
				new String[] {
						SUBSCRIBER_ID_NUMBER
				},
				new String[] {
						SUBSCRIBER_ID_NUMBER
				}
		);

		try (PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setString(1, idNumber);

			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}
	
	/**
	 * Registers a new subscriber in the subscriber table.
	 * 
	 * The method assumes that duplicate checks were already performed before
	 * calling it.
	 * 
	 * @param request the subscriber registration details
	 * @throws SQLException if the insert query fails
	 */
	public void registerSubscriber(RegisterSubscriberRequest request) throws SQLException {
		String sql = "INSERT INTO `" + getTableName() + "` "
				+ "("
				+ SUBSCRIBER_NAME + ", "
				+ SUBSCRIBER_ID_NUMBER + ", "
				+ SUBSCRIBER_PHONE + ", "
				+ SUBSCRIBER_EMAIL + ", "
				+ FAMILY_MEMBERS_COUNT + ", "
				+ PAYMENT_METHOD + ", "
				+ CREDIT_CARD_LAST4 + ", "
				+ USERNAME + ", "
				+ PASSWORD
				+ ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			String fullName = request.getFirstName() + " " + request.getLastName();

			pstmt.setString(1, fullName);
			pstmt.setString(2, request.getIdNumber());
			pstmt.setString(3, request.getPhone());
			pstmt.setString(4, request.getEmail());
			pstmt.setInt(5, request.getFamilyMembersCount());
			pstmt.setString(6, request.getPaymentMethod());

			if (request.getCreditCardLast4() == null || request.getCreditCardLast4().trim().isEmpty()) {
				pstmt.setNull(7, java.sql.Types.VARCHAR);
			} else {
				pstmt.setString(7, request.getCreditCardLast4());
			}

			pstmt.setString(8, request.getUsername());
			pstmt.setString(9, request.getPassword());

			pstmt.executeUpdate();
		}
	}
	
	
}