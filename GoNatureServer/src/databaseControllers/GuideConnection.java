package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the DB connector used when working with the guide table.
 * 
 * The class is implemented as a Singleton, so the server will use only one
 * database connection object for guides during runtime.
 * 
 * According to the system story, an authorized guide is required for organized
 * group orders. A guide is based on an existing subscriber and must be registered
 * by a service representative.
 */
public class GuideConnection extends AbstractDBConnection {

	/**
	 * The single instance of GuideConnection.
	 */
	private static GuideConnection instance;

	/**
	 * The guide ID column in the guide table.
	 */
	private final String GUIDE_ID = "guide_id";

	/**
	 * The subscriber ID column in the guide table.
	 */
	private final String SUBSCRIBER_ID = "subscriber_id";

	/**
	 * The employee ID that authorized the guide.
	 */
	private final String AUTHORIZED_BY_EMPLOYEE_ID = "authorized_by_employee_id";

	/**
	 * The organization name column in the guide table.
	 */
	private final String ORGANIZATION_NAME = "organization_name";

	/**
	 * The guide status column in the guide table.
	 */
	private final String GUIDE_STATUS = "guide_status";

	/**
	 * The created time column in the guide table.
	 */
	private final String CREATED_AT = "created_at";

	/**
	 * Guide status value for active guides.
	 */
	private final String ACTIVE = "active";

	/**
	 * Private constructor for Singleton.
	 * 
	 * It creates the database connection once.
	 * 
	 * @throws SQLException if the connection to the database fails
	 */
	private GuideConnection() throws SQLException {
		connect();
	}

	/**
	 * Returns the single instance of GuideConnection.
	 * 
	 * If no instance exists, or if the existing database connection is closed, a new
	 * instance is created.
	 * 
	 * @return the only GuideConnection instance
	 * @throws SQLException if creating the database connection fails
	 */
	public static GuideConnection getInstance() throws SQLException {
		if (instance == null || instance.conn == null || instance.conn.isClosed()) {
			instance = new GuideConnection();
		}

		return instance;
	}

	/**
	 * Returns the table name used by this DB connector.
	 * 
	 * @return the guide table name
	 */
	@Override
	protected String getTableName() {
		return ConstantsDBTableNames.GUIDE;
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
	 * This method registers a new guide in the database.
	 * 
	 * According to the system story, guides are registered by a service
	 * representative. The method checks that the subscriber exists, that the
	 * authorizing employee is allowed to register guides, and that the subscriber is
	 * not already registered as an active guide.
	 * 
	 * The method uses insertFields from AbstractDBConnection.
	 * 
	 * @param subscriberId           the subscriber ID connected to the guide
	 * @param authorizedByEmployeeId the employee ID that authorizes the guide
	 * @param organizationName       the guide organization name
	 * @return the active guide ID after registration, or -1 if the request is invalid
	 * @throws SQLException if the insert or select query fails
	 */
	public int addGuide(int subscriberId, int authorizedByEmployeeId, String organizationName) throws SQLException {
		ensureConnection();

		if (subscriberId <= 0 || authorizedByEmployeeId <= 0 || organizationName == null
				|| organizationName.isBlank()) {
			return -1;
		}

		if (!SubscriberConnection.getInstance().subscriberExists(subscriberId)) {
			return -1;
		}

		if (!EmployeeConnection.getInstance().canRegisterGuide(authorizedByEmployeeId)) {
			return -1;
		}

		if (getActiveGuideIdBySubscriberId(subscriberId) != -1) {
			return -1;
		}

		List<String> columnNames = new ArrayList<>();
		List<Object> values = new ArrayList<>();

		columnNames.add(SUBSCRIBER_ID);
		values.add(subscriberId);

		columnNames.add(AUTHORIZED_BY_EMPLOYEE_ID);
		values.add(authorizedByEmployeeId);

		columnNames.add(ORGANIZATION_NAME);
		values.add(organizationName);

		columnNames.add(GUIDE_STATUS);
		values.add(ACTIVE);

		columnNames.add(CREATED_AT);
		values.add(Timestamp.valueOf(LocalDateTime.now()));

		insertFields(columnNames.toArray(new String[columnNames.size()]), values);

		return getActiveGuideIdBySubscriberId(subscriberId);
	}

	/**
	 * This method checks whether a guide is active.
	 * 
	 * Active guides are allowed to be connected to organized group orders.
	 * 
	 * @param guideId the guide ID
	 * @return true if the guide exists and his status is active, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean isActiveGuide(int guideId) throws SQLException {
		ensureConnection();

		if (guideId <= 0) {
			return false;
		}

		String[] columnNames = {
				GUIDE_STATUS
		};

		String[] keyColumns = {
				GUIDE_ID
		};

		String sql = selectByFields(columnNames, keyColumns);

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, guideId);

		java.sql.ResultSet rs = pstmt.executeQuery();

		boolean activeGuide = false;

		if (rs.next()) {
			activeGuide = ACTIVE.equals(rs.getString(GUIDE_STATUS));
		}

		rs.close();
		pstmt.close();

		return activeGuide;
	}

	/**
	 * This method returns the guide ID of an active guide by subscriber ID.
	 * 
	 * This is useful when the system knows the subscriber ID and needs to check
	 * whether this subscriber is also an active guide.
	 * 
	 * @param subscriberId the subscriber ID
	 * @return the active guide ID, or -1 if this subscriber is not an active guide
	 * @throws SQLException if the select query fails
	 */
	public int getActiveGuideIdBySubscriberId(int subscriberId) throws SQLException {
		ensureConnection();

		if (subscriberId <= 0) {
			return -1;
		}

		String[] columnNames = {
				GUIDE_ID
		};

		String[] keyColumns = {
				SUBSCRIBER_ID,
				GUIDE_STATUS
		};

		String sql = selectByFields(columnNames, keyColumns);

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, subscriberId);
		pstmt.setString(2, ACTIVE);

		java.sql.ResultSet rs = pstmt.executeQuery();

		int guideId = -1;

		if (rs.next()) {
			guideId = rs.getInt(GUIDE_ID);
		}

		rs.close();
		pstmt.close();

		return guideId;
	}

	/**
	 * This method checks whether an active guide exists for a subscriber.
	 * 
	 * @param subscriberId the subscriber ID
	 * @return true if the subscriber is an active guide, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean isActiveGuideBySubscriberId(int subscriberId) throws SQLException {
		return getActiveGuideIdBySubscriberId(subscriberId) != -1;
	}
}