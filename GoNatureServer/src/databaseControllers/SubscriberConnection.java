package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import common.RegisterSubscriberRequest;
import common.Subscriber;

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
	 * the subsriber id column name
	 */
	private final String SUBSCRIBER_ID = "subscriber_id";
	/**
	 * the subsriber name column name
	 */
	private final String SUBSCRIBER_NAME = "subscriber_name";
	/**
	 * the subsriber id number column name
	 */
	private final String SUBSCRIBER_ID_NUMBER = "subscriber_id_number";
	/**
	 * the subsriber phone column name
	 */
	private final String SUBSCRIBER_PHONE = "subscriber_phone";
	/**
	 * the subsriber email column name
	 */
	private final String SUBSCRIBER_EMAIL = "subscriber_email";
	/**
	 * the subsriber family member count column name
	 */
	private final String FAMILY_MEMBERS_COUNT = "family_members_count";
	/**
	 * the subsriber payment method column name
	 */
	private final String PAYMENT_METHOD = "payment_method";
	/**
	 * the subsriber's credit card's 4 last digits column name
	 */
	private final String CREDIT_CARD_LAST4 = "credit_card_last4";
	/**
	 * the subsriber username column name
	 */
	private final String USERNAME = "username";
	/**
	 * the subsriber password column name
	 */
	private final String PASSWORD = "password";

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
	 * This method checks whether a subscriber exists in the database.
	 * 
	 * @param subscriberId the subscriber ID to check
	 * @return true if the subscriber exists, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean subscriberExists(int subscriberId) throws SQLException {
		String sql = "SELECT subscriber_id FROM subscriber WHERE subscriber_id = ?;";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, subscriberId);

			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
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
				new String[] {
						SUBSCRIBER_ID,
						SUBSCRIBER_NAME,
						SUBSCRIBER_EMAIL
				},
				new String[] {
						SUBSCRIBER_ID
				}
		);

		try (PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setInt(1, subscriberId);

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

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
				+ SUBSCRIBER_ID + ", "
				+ SUBSCRIBER_NAME + ", "
				+ SUBSCRIBER_ID_NUMBER + ", "
				+ SUBSCRIBER_PHONE + ", "
				+ SUBSCRIBER_EMAIL + ", "
				+ FAMILY_MEMBERS_COUNT + ", "
				+ PAYMENT_METHOD + ", "
				+ CREDIT_CARD_LAST4 + ", "
				+ USERNAME + ", "
				+ PASSWORD
				+ ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			String fullName = request.getFirstName() + " " + request.getLastName();

			int subscriberId = Integer.parseInt(request.getIdNumber());

			pstmt.setInt(1, subscriberId);
			pstmt.setString(2, fullName);
			pstmt.setString(3, request.getIdNumber());
			pstmt.setString(4, request.getPhone());
			pstmt.setString(5, request.getEmail());
			pstmt.setInt(6, request.getFamilyMembersCount());
			pstmt.setString(7, request.getPaymentMethod());

			if (request.getCreditCardLast4() == null || request.getCreditCardLast4().trim().isEmpty()) {
				pstmt.setNull(8, java.sql.Types.VARCHAR);
			} else {
				pstmt.setString(8, request.getCreditCardLast4());
			}

			pstmt.setString(9, request.getUsername());
			pstmt.setString(10, request.getPassword());

			pstmt.executeUpdate();
		}
	}

	/**
	 * this method gets the subscriber's phone by their ID
	 * 
	 * @param id the user's id
	 * @return the phone number
	 * @throws SQLException if the query failed
	 */
	public String getPhoneNumberById(int id) throws SQLException {
		String sql = selectByFields(new String[] { SUBSCRIBER_PHONE }, new String[] { SUBSCRIBER_ID });

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, id);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getString(SUBSCRIBER_PHONE);
				}
			}
		}

		return null;
	}
}
