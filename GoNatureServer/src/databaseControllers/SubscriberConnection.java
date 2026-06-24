package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import common.RegisterSubscriberRequest;
import common.Subscriber;

/**
 * DB connector for the subscriber table.
 */
public class SubscriberConnection extends AbstractDBConnection {

	/**
	 * The single instance of SubscriberConnection.
	 */
	private static SubscriberConnection instance;

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
	public String getTableName() {
		return ConstantsDBTableNames.SUBSCRIBER;
	}

	/**
	 * Converts the current ResultSet row into a Subscriber object.
	 * 
	 * @param rs the ResultSet positioned on the current subscriber row
	 * @return a Subscriber object
	 * @throws SQLException if reading data from the ResultSet fails
	 */
	private Subscriber convertResultSetToSubscriber(ResultSet rs) throws SQLException {
		return new Subscriber(
				rs.getInt(SUBSCRIBER_ID),
				rs.getString(SUBSCRIBER_NAME),
				rs.getString(SUBSCRIBER_EMAIL)
		);
	}

	/**
	 * Returns a subscriber by subscriber ID.
	 * 
	 * @param subscriberId the subscriber ID to search for
	 * @return a Subscriber object if found, otherwise null
	 * @throws SQLException if the select query fails
	 */
	public Subscriber getSubscriberById(int subscriberId) throws SQLException {
		ensureConnection();

		String sql = selectByFields(
				new String[] {
						SUBSCRIBER_ID,
						SUBSCRIBER_NAME,
						SUBSCRIBER_EMAIL
				},
				new String[] {
						SUBSCRIBER_ID
				}
		);

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, subscriberId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return convertResultSetToSubscriber(rs);
				}
			}
		}

		return null;
	}

	/**
	 * This method searches for a subscriber by subscriber ID and returns it as a
	 * Subscriber object.
	 * 
	 * This method is kept because other parts of the project may already use this
	 * name.
	 * 
	 * @param subscriberId the subscriber ID to search for
	 * @return a Subscriber object if found, otherwise null
	 * @throws SQLException if the select query fails
	 */
	public Subscriber findSubscriberById(int subscriberId) throws SQLException {
		return getSubscriberById(subscriberId);
	}

	/**
	 * Checks whether a subscriber exists.
	 * 
	 * @param subscriberId the subscriber ID to check
	 * @return true if the subscriber exists, otherwise false
	 * @throws SQLException if the select query fails
	 */
	public boolean subscriberExists(int subscriberId) throws SQLException {
		ensureConnection();

		String sql = selectByFields(
				new String[] {
						SUBSCRIBER_ID
				},
				new String[] {
						SUBSCRIBER_ID
				}
		);

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, subscriberId);

			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
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
		ensureConnection();

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
					return convertResultSetToSubscriber(rs);
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
		ensureConnection();

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
		ensureConnection();

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
		ensureConnection();

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
	 * Adds a new subscriber.
	 * 
	 * This method is kept for older parts of the project that may still create
	 * subscribers without username and password.
	 * 
	 * @param subscriberId the subscriber ID
	 * @param subscriberName the subscriber full name
	 * @param idNumber the subscriber ID number
	 * @param phone the subscriber phone
	 * @param email the subscriber email
	 * @param familyMembersCount the number of family members
	 * @param paymentMethod the payment method
	 * @param creditCardLast4 last four digits of credit card
	 * @return true if the insert request was executed
	 * @throws SQLException if the insert query fails
	 */
	public boolean addSubscriber(int subscriberId, String subscriberName, String idNumber,
			String phone, String email, int familyMembersCount,
			String paymentMethod, String creditCardLast4) throws SQLException {

		ensureConnection();

		List<Object> values = new ArrayList<>();

		values.add(subscriberId);
		values.add(subscriberName);
		values.add(idNumber);
		values.add(phone);
		values.add(email);
		values.add(familyMembersCount);
		values.add(paymentMethod);
		values.add(creditCardLast4);

		insertFields(
				new String[] {
						SUBSCRIBER_ID,
						SUBSCRIBER_NAME,
						SUBSCRIBER_ID_NUMBER,
						SUBSCRIBER_PHONE,
						SUBSCRIBER_EMAIL,
						FAMILY_MEMBERS_COUNT,
						PAYMENT_METHOD,
						CREDIT_CARD_LAST4
				},
				values
		);

		return true;
	}

	/**
	 * Returns the subscriber phone number by subscriber ID.
	 * 
	 * @param subscriberId the subscriber ID
	 * @return the subscriber phone number, or null if not found
	 * @throws SQLException if the select query fails
	 */
	public String getPhoneNumberById(int subscriberId) throws SQLException {
		ensureConnection();

		String sql = selectByFields(
				new String[] {
						SUBSCRIBER_PHONE
				},
				new String[] {
						SUBSCRIBER_ID
				}
		);

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, subscriberId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getString(SUBSCRIBER_PHONE);
				}
			}
		}

		return null;
	}
}
