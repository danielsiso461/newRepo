package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the DB connector used when working with the visit table.
 * 
 * The class is implemented as a Singleton, so the server will use only one
 * database connection object for visits during runtime.
 * 
 * The visit table stores actual park visits, including ordered visits and
 * unplanned visits. It is used when visitors enter and exit the park.
 */
public class VisitConnection extends AbstractDBConnection {

	/**
	 * The single instance of VisitConnection.
	 */
	private static VisitConnection instance;

	/**
	 * The visit ID column.
	 */
	private final String VISIT_ID = "visit_id";

	/**
	 * The order number column.
	 */
	private final String ORDER_NUMBER = "order_number";

	/**
	 * The park ID column.
	 */
	private final String PARK_ID = "park_id";

	/**
	 * The subscriber ID column.
	 */
	private final String SUBSCRIBER_ID = "subscriber_id";

	/**
	 * The visit type column.
	 */
	private final String VISIT_TYPE = "visit_type";

	/**
	 * The actual number of visitors column.
	 */
	private final String ACTUAL_NUMBER_OF_VISITORS = "actual_number_of_visitors";

	/**
	 * The entry time column.
	 */
	private final String ENTRY_TIME = "entry_time";

	/**
	 * The exit time column.
	 */
	private final String EXIT_TIME = "exit_time";

	/**
	 * The employee ID that handled the entry.
	 */
	private final String HANDLED_BY_EMPLOYEE_ID = "handled_by_employee_id";

	/**
	 * The employee ID that handled the exit.
	 */
	private final String EXIT_HANDLED_BY_EMPLOYEE_ID = "exit_handled_by_employee_id";

	/**
	 * The identification method column.
	 */
	private final String IDENTIFICATION_METHOD = "identification_method";

	/**
	 * Visit type for ordered visits.
	 */
	private final String ORDERED = "ordered";

	/**
	 * Visit type for unplanned visits.
	 */
	private final String UNPLANNED = "unplanned";

	/**
	 * Identification method by ID number.
	 */
	private final String ID_NUMBER = "id_number";

	/**
	 * Identification method by confirmation code.
	 */
	private final String CONFIRMATION_CODE = "confirmation_code";

	/**
	 * Private constructor for Singleton.
	 * 
	 * @throws SQLException if the database connection fails
	 */
	private VisitConnection() throws SQLException {
		connect();
	}

	/**
	 * Returns the single instance of VisitConnection.
	 * 
	 * @return the only VisitConnection instance
	 * @throws SQLException if creating the database connection fails
	 */
	public static VisitConnection getInstance() throws SQLException {
		if (instance == null || instance.conn == null || instance.conn.isClosed()) {
			instance = new VisitConnection();
		}

		return instance;
	}

	/**
	 * Returns the table name used by this DB connector.
	 * 
	 * @return the visit table name
	 */
	@Override
	protected String getTableName() {
		return ConstantsDBTableNames.VISIT;
	}

	/**
	 * Checks that the database connection is open.
	 * 
	 * @throws SQLException if reconnecting to the database fails
	 */
	private void ensureConnection() throws SQLException {
		if (conn == null || conn.isClosed()) {
			connect();
		}
	}

	/**
	 * Checks whether the visit type is valid.
	 * 
	 * @param visitType the visit type
	 * @return true if valid, false otherwise
	 */
	private boolean isValidVisitType(String visitType) {
		return ORDERED.equals(visitType) || UNPLANNED.equals(visitType);
	}

	/**
	 * Checks whether the identification method is valid.
	 * 
	 * @param identificationMethod the identification method
	 * @return true if valid, false otherwise
	 */
	private boolean isValidIdentificationMethod(String identificationMethod) {
		return ID_NUMBER.equals(identificationMethod) || CONFIRMATION_CODE.equals(identificationMethod);
	}

	/**
	 * Creates a visit record for visitors that arrived with an existing order.
	 * 
	 * The method checks that the park is active, that the employee can handle
	 * entrance for this park, that there is enough current capacity in the park, and
	 * that there is no other open visit for the same order.
	 * 
	 * The method uses insertFields from AbstractDBConnection.
	 * 
	 * @param orderNumber          the order number
	 * @param parkId               the park ID
	 * @param subscriberId         the subscriber ID
	 * @param actualNumberOfVisitors the actual number of visitors who entered
	 * @param handledByEmployeeId  the employee ID that handles the entrance
	 * @param identificationMethod the identification method
	 * @return the created visit ID, or -1 if the request is invalid
	 * @throws SQLException if the insert or select query fails
	 */
	public int createOrderedVisit(int orderNumber, int parkId, int subscriberId, int actualNumberOfVisitors,
			int handledByEmployeeId, String identificationMethod) throws SQLException {

		ensureConnection();

		if (orderNumber <= 0 || parkId <= 0 || subscriberId <= 0 || actualNumberOfVisitors <= 0
				|| handledByEmployeeId <= 0 || identificationMethod == null
				|| !isValidIdentificationMethod(identificationMethod)) {
			return -1;
		}

		if (!ParkConnection.getInstance().isActivePark(parkId)) {
			return -1;
		}

		if (!EmployeeConnection.getInstance().canHandleParkEntrance(handledByEmployeeId, parkId)) {
			return -1;
		}

		if (!ParkConnection.getInstance().hasAvailableCapacity(parkId, actualNumberOfVisitors)) {
			return -1;
		}

		if (!getOpenVisitByOrderNumber(orderNumber).isEmpty()) {
			return -1;
		}

		LocalDateTime entryTime = LocalDateTime.now();

		List<String> columnNames = new ArrayList<>();
		List<Object> values = new ArrayList<>();

		columnNames.add(ORDER_NUMBER);
		values.add(orderNumber);

		columnNames.add(PARK_ID);
		values.add(parkId);

		columnNames.add(SUBSCRIBER_ID);
		values.add(subscriberId);

		columnNames.add(VISIT_TYPE);
		values.add(ORDERED);

		columnNames.add(ACTUAL_NUMBER_OF_VISITORS);
		values.add(actualNumberOfVisitors);

		columnNames.add(ENTRY_TIME);
		values.add(Timestamp.valueOf(entryTime));

		columnNames.add(HANDLED_BY_EMPLOYEE_ID);
		values.add(handledByEmployeeId);

		columnNames.add(IDENTIFICATION_METHOD);
		values.add(identificationMethod);

		insertFields(columnNames.toArray(new String[columnNames.size()]), values);

		return getCreatedVisitId(orderNumber, parkId, subscriberId, ORDERED, actualNumberOfVisitors,
				handledByEmployeeId, identificationMethod, entryTime);
	}

	/**
	 * Creates a visit record for unplanned visitors.
	 * 
	 * The method checks that the park is active, that the employee can handle
	 * entrance for this park, and that there is enough current capacity in the park.
	 * 
	 * The method uses insertFields from AbstractDBConnection.
	 * 
	 * @param parkId                  the park ID
	 * @param subscriberId            the subscriber ID, or null if the visitors are not subscribers
	 * @param actualNumberOfVisitors  the actual number of visitors who entered
	 * @param handledByEmployeeId     the employee ID that handles the entrance
	 * @param identificationMethod    the identification method
	 * @return the created visit ID, or -1 if the request is invalid
	 * @throws SQLException if the insert or select query fails
	 */
	public int createUnplannedVisit(int parkId, Integer subscriberId, int actualNumberOfVisitors,
			int handledByEmployeeId, String identificationMethod) throws SQLException {

		ensureConnection();

		if (parkId <= 0 || actualNumberOfVisitors <= 0 || handledByEmployeeId <= 0 || identificationMethod == null
				|| !isValidIdentificationMethod(identificationMethod)
				|| (subscriberId != null && subscriberId <= 0)) {
			return -1;
		}

		if (!ParkConnection.getInstance().isActivePark(parkId)) {
			return -1;
		}

		if (!EmployeeConnection.getInstance().canHandleParkEntrance(handledByEmployeeId, parkId)) {
			return -1;
		}

		if (!ParkConnection.getInstance().hasAvailableCapacity(parkId, actualNumberOfVisitors)) {
			return -1;
		}

		LocalDateTime entryTime = LocalDateTime.now();

		List<String> columnNames = new ArrayList<>();
		List<Object> values = new ArrayList<>();

		columnNames.add(PARK_ID);
		values.add(parkId);

		if (subscriberId != null) {
			columnNames.add(SUBSCRIBER_ID);
			values.add(subscriberId);
		}

		columnNames.add(VISIT_TYPE);
		values.add(UNPLANNED);

		columnNames.add(ACTUAL_NUMBER_OF_VISITORS);
		values.add(actualNumberOfVisitors);

		columnNames.add(ENTRY_TIME);
		values.add(Timestamp.valueOf(entryTime));

		columnNames.add(HANDLED_BY_EMPLOYEE_ID);
		values.add(handledByEmployeeId);

		columnNames.add(IDENTIFICATION_METHOD);
		values.add(identificationMethod);

		insertFields(columnNames.toArray(new String[columnNames.size()]), values);

		return getCreatedVisitId(null, parkId, subscriberId, UNPLANNED, actualNumberOfVisitors, handledByEmployeeId,
				identificationMethod, entryTime);
	}

	/**
	 * Finds the visit ID that was created after inserting a new visit.
	 * 
	 * The method uses selectByFields from AbstractDBConnection. We use
	 * MAX(visit_id) because the new visit should be the latest matching visit.
	 * 
	 * @param orderNumber             the order number, or null for unplanned visits
	 * @param parkId                  the park ID
	 * @param subscriberId            the subscriber ID, or null if not relevant
	 * @param visitType               the visit type
	 * @param actualNumberOfVisitors  the actual number of visitors
	 * @param handledByEmployeeId     the entry employee ID
	 * @param identificationMethod    the identification method
	 * @param entryTime               the entry time
	 * @return the created visit ID, or -1 if not found
	 * @throws SQLException if the select query fails
	 */
	private int getCreatedVisitId(Integer orderNumber, int parkId, Integer subscriberId, String visitType,
			int actualNumberOfVisitors, int handledByEmployeeId, String identificationMethod,
			LocalDateTime entryTime) throws SQLException {

		ensureConnection();

		if (parkId <= 0 || visitType == null || !isValidVisitType(visitType) || actualNumberOfVisitors <= 0
				|| handledByEmployeeId <= 0 || identificationMethod == null
				|| !isValidIdentificationMethod(identificationMethod) || entryTime == null
				|| (orderNumber != null && orderNumber <= 0) || (subscriberId != null && subscriberId <= 0)) {
			return -1;
		}

		List<String> keyColumns = new ArrayList<>();
		List<Object> keyValues = new ArrayList<>();

		if (orderNumber != null) {
			keyColumns.add(ORDER_NUMBER);
			keyValues.add(orderNumber);
		}

		keyColumns.add(PARK_ID);
		keyValues.add(parkId);

		if (subscriberId != null) {
			keyColumns.add(SUBSCRIBER_ID);
			keyValues.add(subscriberId);
		}

		keyColumns.add(VISIT_TYPE);
		keyValues.add(visitType);

		keyColumns.add(ACTUAL_NUMBER_OF_VISITORS);
		keyValues.add(actualNumberOfVisitors);

		keyColumns.add(ENTRY_TIME);
		keyValues.add(Timestamp.valueOf(entryTime));

		keyColumns.add(HANDLED_BY_EMPLOYEE_ID);
		keyValues.add(handledByEmployeeId);

		keyColumns.add(IDENTIFICATION_METHOD);
		keyValues.add(identificationMethod);

		String sql = selectByFields(new String[] { "MAX(" + VISIT_ID + ") AS " + VISIT_ID },
				keyColumns.toArray(new String[keyColumns.size()]));

		PreparedStatement pstmt = conn.prepareStatement(sql);

		for (int i = 0; i < keyValues.size(); i++) {
			pstmt.setObject(i + 1, keyValues.get(i));
		}

		java.sql.ResultSet rs = pstmt.executeQuery();

		int visitId = -1;

		if (rs.next()) {
			visitId = rs.getInt(VISIT_ID);
		}

		rs.close();
		pstmt.close();

		return visitId;
	}

	/**
	 * Closes an open visit when visitors exit the park.
	 * 
	 * The method checks that the visit exists, that it is still open, and that the
	 * employee is allowed to handle exit for the visit park.
	 * 
	 * The method uses updateFields from AbstractDBConnection.
	 * 
	 * @param visitId                    the visit ID
	 * @param exitHandledByEmployeeId    the employee ID that handles the exit
	 * @return true if the visit was closed successfully, false otherwise
	 * @throws SQLException if the select or update query fails
	 */
	public boolean closeVisit(int visitId, int exitHandledByEmployeeId) throws SQLException {
		ensureConnection();

		if (visitId <= 0 || exitHandledByEmployeeId <= 0) {
			return false;
		}

		ArrayList<Object> visitData = getOpenVisitById(visitId);

		if (visitData.isEmpty()) {
			return false;
		}

		int parkId = ((Number) visitData.get(2)).intValue();

		if (!EmployeeConnection.getInstance().canHandleParkEntrance(exitHandledByEmployeeId, parkId)) {
			return false;
		}

		List<String> columnNames = new ArrayList<>();
		List<Object> newValues = new ArrayList<>();
		List<String> keyColumns = new ArrayList<>();
		List<Object> keyValues = new ArrayList<>();

		columnNames.add(EXIT_TIME);
		newValues.add(Timestamp.valueOf(LocalDateTime.now()));

		columnNames.add(EXIT_HANDLED_BY_EMPLOYEE_ID);
		newValues.add(exitHandledByEmployeeId);

		keyColumns.add(VISIT_ID);
		keyValues.add(visitId);

		updateFields(columnNames.toArray(new String[columnNames.size()]), newValues,
				keyColumns.toArray(new String[keyColumns.size()]), keyValues);

		return true;
	}

	/**
	 * Returns an open visit by visit ID.
	 * 
	 * An open visit is a visit with no exit_time.
	 * 
	 * The returned ArrayList contains:
	 * visit_id, order_number, park_id, subscriber_id, visit_type,
	 * actual_number_of_visitors, entry_time, handled_by_employee_id,
	 * identification_method.
	 * 
	 * @param visitId the visit ID
	 * @return visit data, or an empty ArrayList if no open visit was found
	 * @throws SQLException if the select query fails
	 */
	public ArrayList<Object> getOpenVisitById(int visitId) throws SQLException {
		ensureConnection();

		ArrayList<Object> visitData = new ArrayList<>();

		if (visitId <= 0) {
			return visitData;
		}

		String[] columnNames = {
				VISIT_ID,
				ORDER_NUMBER,
				PARK_ID,
				SUBSCRIBER_ID,
				VISIT_TYPE,
				ACTUAL_NUMBER_OF_VISITORS,
				ENTRY_TIME,
				HANDLED_BY_EMPLOYEE_ID,
				IDENTIFICATION_METHOD
		};

		String[] keyColumns = {
				VISIT_ID
		};

		String sql = selectByFields(columnNames, keyColumns);
		sql = sql.substring(0, sql.length() - 1) + " AND " + EXIT_TIME + " IS NULL;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, visitId);

		java.sql.ResultSet rs = pstmt.executeQuery();

		if (rs.next()) {
			visitData.add(rs.getInt(VISIT_ID));

			if (rs.getObject(ORDER_NUMBER) != null) {
				visitData.add(rs.getInt(ORDER_NUMBER));
			} else {
				visitData.add(null);
			}

			visitData.add(rs.getInt(PARK_ID));

			if (rs.getObject(SUBSCRIBER_ID) != null) {
				visitData.add(rs.getInt(SUBSCRIBER_ID));
			} else {
				visitData.add(null);
			}

			visitData.add(rs.getString(VISIT_TYPE));
			visitData.add(rs.getInt(ACTUAL_NUMBER_OF_VISITORS));
			visitData.add(rs.getTimestamp(ENTRY_TIME).toLocalDateTime());
			visitData.add(rs.getInt(HANDLED_BY_EMPLOYEE_ID));
			visitData.add(rs.getString(IDENTIFICATION_METHOD));
		}

		rs.close();
		pstmt.close();

		return visitData;
	}

	/**
	 * Returns an open visit by order number.
	 * 
	 * This is used to prevent opening two active visits for the same order.
	 * 
	 * @param orderNumber the order number
	 * @return visit data, or an empty ArrayList if no open visit was found
	 * @throws SQLException if the select query fails
	 */
	public ArrayList<Object> getOpenVisitByOrderNumber(int orderNumber) throws SQLException {
		ensureConnection();

		ArrayList<Object> visitData = new ArrayList<>();

		if (orderNumber <= 0) {
			return visitData;
		}

		String[] columnNames = {
				VISIT_ID,
				ORDER_NUMBER,
				PARK_ID,
				SUBSCRIBER_ID,
				VISIT_TYPE,
				ACTUAL_NUMBER_OF_VISITORS,
				ENTRY_TIME,
				HANDLED_BY_EMPLOYEE_ID,
				IDENTIFICATION_METHOD
		};

		String[] keyColumns = {
				ORDER_NUMBER
		};

		String sql = selectByFields(columnNames, keyColumns);
		sql = sql.substring(0, sql.length() - 1) + " AND " + EXIT_TIME + " IS NULL;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, orderNumber);

		java.sql.ResultSet rs = pstmt.executeQuery();

		if (rs.next()) {
			visitData.add(rs.getInt(VISIT_ID));
			visitData.add(rs.getInt(ORDER_NUMBER));
			visitData.add(rs.getInt(PARK_ID));

			if (rs.getObject(SUBSCRIBER_ID) != null) {
				visitData.add(rs.getInt(SUBSCRIBER_ID));
			} else {
				visitData.add(null);
			}

			visitData.add(rs.getString(VISIT_TYPE));
			visitData.add(rs.getInt(ACTUAL_NUMBER_OF_VISITORS));
			visitData.add(rs.getTimestamp(ENTRY_TIME).toLocalDateTime());
			visitData.add(rs.getInt(HANDLED_BY_EMPLOYEE_ID));
			visitData.add(rs.getString(IDENTIFICATION_METHOD));
		}

		rs.close();
		pstmt.close();

		return visitData;
	}
}