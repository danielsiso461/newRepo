package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the DB connector used when working with the
 * park_parameter_change_request table.
 * 
 * The class is implemented as a Singleton, so the server will use only one
 * database connection object for park parameter change requests during runtime.
 * 
 * According to the system story, a park manager can request changes to park
 * parameters, and a department manager can approve or reject those requests.
 */
public class ParkParameterChangeRequestConnection extends AbstractDBConnection {

	/**
	 * The single instance of ParkParameterChangeRequestConnection.
	 */
	private static ParkParameterChangeRequestConnection instance;

	/**
	 * The request ID column.
	 */
	private final String REQUEST_ID = "request_id";

	/**
	 * The park ID column.
	 */
	private final String PARK_ID = "park_id";

	/**
	 * The employee ID of the park manager who requested the change.
	 */
	private final String REQUESTED_BY_EMPLOYEE_ID = "requested_by_employee_id";

	/**
	 * The employee ID of the department manager who reviewed the request.
	 */
	private final String APPROVED_BY_EMPLOYEE_ID = "approved_by_employee_id";

	/**
	 * The parameter name column.
	 */
	private final String PARAMETER_NAME = "parameter_name";

	/**
	 * The old value column.
	 */
	private final String OLD_VALUE = "old_value";

	/**
	 * The new value column.
	 */
	private final String NEW_VALUE = "new_value";

	/**
	 * The request status column.
	 */
	private final String REQUEST_STATUS = "request_status";

	/**
	 * The requested date and time column.
	 */
	private final String REQUESTED_AT = "requested_at";

	/**
	 * The reviewed date and time column.
	 */
	private final String REVIEWED_AT = "reviewed_at";

	/**
	 * The review note column.
	 */
	private final String REVIEW_NOTE = "review_note";

	/**
	 * Parameter value for maximum capacity.
	 */
	private final String MAX_CAPACITY = "max_capacity";

	/**
	 * Parameter value for places reserved for unplanned visitors.
	 */
	private final String PLACES_FOR_UNPLANNED_VISITORS = "places_for_unplanned_visitors";

	/**
	 * Parameter value for estimated visit duration.
	 */
	private final String ESTIMATED_VISIT_DURATION_HOURS = "estimated_visit_duration_hours";

	/**
	 * Parameter value for park promotions.
	 */
	private final String PROMOTIONS = "promotions";

	/**
	 * Request status value for pending requests.
	 */
	private final String PENDING = "pending";

	/**
	 * Request status value for approved requests.
	 */
	private final String APPROVED = "approved";

	/**
	 * Request status value for rejected requests.
	 */
	private final String REJECTED = "rejected";

	/**
	 * Private constructor for Singleton.
	 * 
	 * It creates the database connection once.
	 * 
	 * @throws SQLException if the connection to the database fails
	 */
	private ParkParameterChangeRequestConnection() throws SQLException {
		connect();
	}

	/**
	 * Returns the single instance of ParkParameterChangeRequestConnection.
	 * 
	 * If no instance exists, or if the existing database connection is closed, a new
	 * instance is created.
	 * 
	 * @return the only ParkParameterChangeRequestConnection instance
	 * @throws SQLException if creating the database connection fails
	 */
	public static ParkParameterChangeRequestConnection getInstance() throws SQLException {
		if (instance == null || instance.conn == null || instance.conn.isClosed()) {
			instance = new ParkParameterChangeRequestConnection();
		}

		return instance;
	}

	/**
	 * Returns the table name used by this DB connector.
	 * 
	 * @return the park_parameter_change_request table name
	 */
	@Override
	protected String getTableName() {
		return ConstantsDBTableNames.PARK_PARAMETER_CHANGE_REQUEST;
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
	 * This method checks whether the requested parameter name is valid.
	 * 
	 * @param parameterName the parameter name
	 * @return true if the parameter name is valid, false otherwise
	 */
	private boolean isValidParameterName(String parameterName) {
		return MAX_CAPACITY.equals(parameterName)
				|| PLACES_FOR_UNPLANNED_VISITORS.equals(parameterName)
				|| ESTIMATED_VISIT_DURATION_HOURS.equals(parameterName)
				|| PROMOTIONS.equals(parameterName);
	}

	/**
	 * This method checks whether the request status is valid.
	 * 
	 * @param requestStatus the request status
	 * @return true if the request status is valid, false otherwise
	 */
	private boolean isValidRequestStatus(String requestStatus) {
		return PENDING.equals(requestStatus) || APPROVED.equals(requestStatus) || REJECTED.equals(requestStatus);
	}

	/**
	 * This method creates a new park parameter change request.
	 * 
	 * According to the system story, only a park manager can request changes, and
	 * only for the park connected to him.
	 * 
	 * The request is created with pending status. The park table itself is not
	 * updated here. The actual park parameter is updated only after a department
	 * manager approves the request.
	 * 
	 * The method uses insertFields from AbstractDBConnection.
	 * 
	 * @param parkId                the park ID
	 * @param requestedByEmployeeId the park manager employee ID
	 * @param parameterName         the requested parameter name
	 * @param oldValue              the current value of the parameter
	 * @param newValue              the requested new value
	 * @return the created request ID, or -1 if the request is invalid
	 * @throws SQLException if the insert or select query fails
	 */
	public int createChangeRequest(int parkId, int requestedByEmployeeId, String parameterName, String oldValue,
			String newValue) throws SQLException {

		ensureConnection();

		if (parkId <= 0 || requestedByEmployeeId <= 0 || parameterName == null || !isValidParameterName(parameterName)
				|| oldValue == null || oldValue.isBlank() || newValue == null || newValue.isBlank()) {
			return -1;
		}

		if (!EmployeeConnection.getInstance().canRequestParkParameterChange(requestedByEmployeeId, parkId)) {
			return -1;
		}

		LocalDateTime requestedAt = LocalDateTime.now();

		List<String> columnNames = new ArrayList<>();
		List<Object> values = new ArrayList<>();

		columnNames.add(PARK_ID);
		values.add(parkId);

		columnNames.add(REQUESTED_BY_EMPLOYEE_ID);
		values.add(requestedByEmployeeId);

		columnNames.add(PARAMETER_NAME);
		values.add(parameterName);

		columnNames.add(OLD_VALUE);
		values.add(oldValue);

		columnNames.add(NEW_VALUE);
		values.add(newValue);

		columnNames.add(REQUEST_STATUS);
		values.add(PENDING);

		columnNames.add(REQUESTED_AT);
		values.add(Timestamp.valueOf(requestedAt));

		insertFields(columnNames.toArray(new String[columnNames.size()]), values);

		return getCreatedRequestId(parkId, requestedByEmployeeId, parameterName, oldValue, newValue, requestedAt);
	}

	/**
	 * This method finds the request ID that was created after inserting a new
	 * request.
	 * 
	 * The method uses selectByFields from AbstractDBConnection. We use
	 * MAX(request_id) because the new request should be the latest matching request.
	 * 
	 * @param parkId                the park ID
	 * @param requestedByEmployeeId the employee ID that created the request
	 * @param parameterName         the requested parameter name
	 * @param oldValue              the old value
	 * @param newValue              the new value
	 * @param requestedAt           the requested time
	 * @return the created request ID, or -1 if no matching request was found
	 * @throws SQLException if the select query fails
	 */
	private int getCreatedRequestId(int parkId, int requestedByEmployeeId, String parameterName, String oldValue,
			String newValue, LocalDateTime requestedAt) throws SQLException {

		ensureConnection();

		if (parkId <= 0 || requestedByEmployeeId <= 0 || parameterName == null || !isValidParameterName(parameterName)
				|| oldValue == null || oldValue.isBlank() || newValue == null || newValue.isBlank()
				|| requestedAt == null) {
			return -1;
		}

		List<String> keyColumns = new ArrayList<>();
		List<Object> keyValues = new ArrayList<>();

		keyColumns.add(PARK_ID);
		keyValues.add(parkId);

		keyColumns.add(REQUESTED_BY_EMPLOYEE_ID);
		keyValues.add(requestedByEmployeeId);

		keyColumns.add(PARAMETER_NAME);
		keyValues.add(parameterName);

		keyColumns.add(OLD_VALUE);
		keyValues.add(oldValue);

		keyColumns.add(NEW_VALUE);
		keyValues.add(newValue);

		keyColumns.add(REQUEST_STATUS);
		keyValues.add(PENDING);

		keyColumns.add(REQUESTED_AT);
		keyValues.add(Timestamp.valueOf(requestedAt));

		String sql = selectByFields(new String[] { "MAX(" + REQUEST_ID + ") AS " + REQUEST_ID },
				keyColumns.toArray(new String[keyColumns.size()]));

		PreparedStatement pstmt = conn.prepareStatement(sql);

		for (int i = 0; i < keyValues.size(); i++) {
			pstmt.setObject(i + 1, keyValues.get(i));
		}

		java.sql.ResultSet rs = pstmt.executeQuery();

		int requestId = -1;

		if (rs.next()) {
			requestId = rs.getInt(REQUEST_ID);
		}

		rs.close();
		pstmt.close();

		return requestId;
	}

	/**
	 * This method returns a specific change request by request ID.
	 * 
	 * The returned ArrayList contains:
	 * request_id, park_id, requested_by_employee_id, approved_by_employee_id,
	 * parameter_name, old_value, new_value, request_status, requested_at,
	 * reviewed_at, review_note.
	 * 
	 * @param requestId the request ID
	 * @return an ArrayList containing the request data, or an empty ArrayList if the
	 *         request was not found
	 * @throws SQLException if the select query fails
	 */
	public ArrayList<Object> getRequestById(int requestId) throws SQLException {
		ensureConnection();

		ArrayList<Object> requestData = new ArrayList<>();

		if (requestId <= 0) {
			return requestData;
		}

		String[] columnNames = {
				REQUEST_ID,
				PARK_ID,
				REQUESTED_BY_EMPLOYEE_ID,
				APPROVED_BY_EMPLOYEE_ID,
				PARAMETER_NAME,
				OLD_VALUE,
				NEW_VALUE,
				REQUEST_STATUS,
				REQUESTED_AT,
				REVIEWED_AT,
				REVIEW_NOTE
		};

		String[] keyColumns = {
				REQUEST_ID
		};

		String sql = selectByFields(columnNames, keyColumns);

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, requestId);

		java.sql.ResultSet rs = pstmt.executeQuery();

		if (rs.next()) {
			requestData.add(rs.getInt(REQUEST_ID));
			requestData.add(rs.getInt(PARK_ID));
			requestData.add(rs.getInt(REQUESTED_BY_EMPLOYEE_ID));

			if (rs.getObject(APPROVED_BY_EMPLOYEE_ID) != null) {
				requestData.add(rs.getInt(APPROVED_BY_EMPLOYEE_ID));
			} else {
				requestData.add(null);
			}

			requestData.add(rs.getString(PARAMETER_NAME));
			requestData.add(rs.getString(OLD_VALUE));
			requestData.add(rs.getString(NEW_VALUE));
			requestData.add(rs.getString(REQUEST_STATUS));
			requestData.add(rs.getTimestamp(REQUESTED_AT).toLocalDateTime());

			if (rs.getObject(REVIEWED_AT) != null) {
				requestData.add(rs.getTimestamp(REVIEWED_AT).toLocalDateTime());
			} else {
				requestData.add(null);
			}

			requestData.add(rs.getString(REVIEW_NOTE));
		}

		rs.close();
		pstmt.close();

		return requestData;
	}

	/**
	 * This method returns all pending parameter change requests.
	 * 
	 * This is used by a department manager in order to review requests.
	 * 
	 * Each inner ArrayList contains:
	 * request_id, park_id, requested_by_employee_id, parameter_name, old_value,
	 * new_value, requested_at.
	 * 
	 * @return an ArrayList of pending requests
	 * @throws SQLException if the select query fails
	 */
	public ArrayList<ArrayList<Object>> getPendingRequests() throws SQLException {
		ensureConnection();

		ArrayList<ArrayList<Object>> requests = new ArrayList<>();

		String[] columnNames = {
				REQUEST_ID,
				PARK_ID,
				REQUESTED_BY_EMPLOYEE_ID,
				PARAMETER_NAME,
				OLD_VALUE,
				NEW_VALUE,
				REQUESTED_AT
		};

		String[] keyColumns = {
				REQUEST_STATUS
		};

		String sql = selectByFields(columnNames, keyColumns);
		sql = sql.substring(0, sql.length() - 1) + " ORDER BY " + REQUESTED_AT + ";";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, PENDING);

		java.sql.ResultSet rs = pstmt.executeQuery();

		while (rs.next()) {
			ArrayList<Object> row = new ArrayList<>();

			row.add(rs.getInt(REQUEST_ID));
			row.add(rs.getInt(PARK_ID));
			row.add(rs.getInt(REQUESTED_BY_EMPLOYEE_ID));
			row.add(rs.getString(PARAMETER_NAME));
			row.add(rs.getString(OLD_VALUE));
			row.add(rs.getString(NEW_VALUE));
			row.add(rs.getTimestamp(REQUESTED_AT).toLocalDateTime());

			requests.add(row);
		}

		rs.close();
		pstmt.close();

		return requests;
	}

	/**
	 * This method approves a pending park parameter change request.
	 * 
	 * Only a department manager can approve requests.
	 * 
	 * When the request is approved:
	 * 1. The request status is updated to approved.
	 * 2. The review details are saved.
	 * 3. The actual park parameter is updated in the park table.
	 * 
	 * @param requestId            the request ID
	 * @param approvedByEmployeeId the department manager employee ID
	 * @param reviewNote           the review note, or null if there is no note
	 * @return true if the request was approved successfully, false otherwise
	 * @throws SQLException if the select or update query fails
	 */
	public boolean approveRequest(int requestId, int approvedByEmployeeId, String reviewNote) throws SQLException {
		ensureConnection();

		if (requestId <= 0 || !EmployeeConnection.getInstance().canApproveParkParameterChange(approvedByEmployeeId)) {
			return false;
		}

		ArrayList<Object> requestData = getRequestById(requestId);

		if (requestData.isEmpty()) {
			return false;
		}

		String currentStatus = (String) requestData.get(7);

		if (!PENDING.equals(currentStatus)) {
			return false;
		}

		int parkId = ((Number) requestData.get(1)).intValue();
		String parameterName = (String) requestData.get(4);
		String newValue = (String) requestData.get(6);

		updateRequestReviewStatus(requestId, approvedByEmployeeId, APPROVED, reviewNote);

		updateParkParameter(parkId, parameterName, newValue);

		return true;
	}

	/**
	 * This method rejects a pending park parameter change request.
	 * 
	 * Only a department manager can reject requests.
	 * 
	 * When the request is rejected, the actual park table is not updated.
	 * 
	 * @param requestId            the request ID
	 * @param approvedByEmployeeId the department manager employee ID
	 * @param reviewNote           the review note
	 * @return true if the request was rejected successfully, false otherwise
	 * @throws SQLException if the select or update query fails
	 */
	public boolean rejectRequest(int requestId, int approvedByEmployeeId, String reviewNote) throws SQLException {
		ensureConnection();

		if (requestId <= 0 || !EmployeeConnection.getInstance().canApproveParkParameterChange(approvedByEmployeeId)) {
			return false;
		}

		ArrayList<Object> requestData = getRequestById(requestId);

		if (requestData.isEmpty()) {
			return false;
		}

		String currentStatus = (String) requestData.get(7);

		if (!PENDING.equals(currentStatus)) {
			return false;
		}

		updateRequestReviewStatus(requestId, approvedByEmployeeId, REJECTED, reviewNote);

		return true;
	}

	/**
	 * This method updates the review status of a request.
	 * 
	 * The method uses updateFields from AbstractDBConnection.
	 * 
	 * @param requestId            the request ID
	 * @param approvedByEmployeeId the employee ID that reviewed the request
	 * @param requestStatus        the new request status
	 * @param reviewNote           the review note, or null if there is no note
	 * @throws SQLException if the update query fails
	 */
	private void updateRequestReviewStatus(int requestId, int approvedByEmployeeId, String requestStatus,
			String reviewNote) throws SQLException {

		if (requestId <= 0 || approvedByEmployeeId <= 0 || requestStatus == null
				|| !isValidRequestStatus(requestStatus)) {
			return;
		}

		List<String> columnNames = new ArrayList<>();
		List<Object> newValues = new ArrayList<>();
		List<String> keyColumns = new ArrayList<>();
		List<Object> keyValues = new ArrayList<>();

		columnNames.add(APPROVED_BY_EMPLOYEE_ID);
		newValues.add(approvedByEmployeeId);

		columnNames.add(REQUEST_STATUS);
		newValues.add(requestStatus);

		columnNames.add(REVIEWED_AT);
		newValues.add(Timestamp.valueOf(LocalDateTime.now()));

		if (reviewNote != null && !reviewNote.isBlank()) {
			columnNames.add(REVIEW_NOTE);
			newValues.add(reviewNote);
		}

		keyColumns.add(REQUEST_ID);
		keyValues.add(requestId);

		updateFields(columnNames.toArray(new String[columnNames.size()]), newValues,
				keyColumns.toArray(new String[keyColumns.size()]), keyValues);
	}

	/**
	 * This method updates the actual parameter in the park table after a request is
	 * approved.
	 * 
	 * This method delegates the actual park update to AbstractDBConnection through
	 * the ParkConnection table connector.
	 * 
	 * @param parkId        the park ID
	 * @param parameterName the parameter name
	 * @param newValue      the new value
	 * @throws SQLException if the update query fails
	 */
	private void updateParkParameter(int parkId, String parameterName, String newValue) throws SQLException {
		if (parkId <= 0 || parameterName == null || !isValidParameterName(parameterName) || newValue == null
				|| newValue.isBlank()) {
			return;
		}

		List<Object> newValues = new ArrayList<>();
		List<Object> keyValues = new ArrayList<>();

		newValues.add(newValue);
		keyValues.add(parkId);

		ParkConnection.getInstance().updateFields(new String[] { parameterName }, newValues,
				new String[] { PARK_ID }, keyValues);
	}
}