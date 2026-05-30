package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class is the DB connector used when working with the
 * park_parameter_change_request table.
 * 
 * The class is implemented as a Singleton, so the server will use only one
 * database connection object for park parameter change requests during runtime.
 * 
 * The park_parameter_change_request table stores requests made by park managers
 * to change park parameters such as maximum capacity, places for unplanned
 * visitors, estimated visit duration, and promotions.
 * 
 * According to the system logic, a park manager creates a request and a
 * department manager approves or rejects it.
 */
public class ParkParameterChangeRequestConnection extends AbstractDBConnection {

	/**
	 * The single instance of ParkParameterChangeRequestConnection.
	 */
	private static ParkParameterChangeRequestConnection instance;

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
	 * This method creates a new park parameter change request.
	 * 
	 * The request is created by a park manager and remains pending until a
	 * department manager approves or rejects it.
	 * 
	 * The actual permission checks are handled by the database trigger and should
	 * also be validated in the server logic before calling this method.
	 * 
	 * @param parkId                the park ID whose parameter should be changed
	 * @param requestedByEmployeeId the employee ID of the park manager who created
	 *                              the request
	 * @param parameterName         the name of the parameter to change, such as
	 *                              max_capacity, places_for_unplanned_visitors,
	 *                              estimated_visit_duration_hours, or promotions
	 * @param oldValue              the current value of the parameter before the
	 *                              requested change
	 * @param newValue              the requested new value of the parameter
	 * @return true if the request was created successfully, false otherwise
	 * @throws SQLException if the insert query fails
	 */
	public boolean createChangeRequest(
			int parkId,
			int requestedByEmployeeId,
			String parameterName,
			String oldValue,
			String newValue) throws SQLException {

		String sql = "INSERT INTO park_parameter_change_request "
				+ "(park_id, requested_by_employee_id, parameter_name, old_value, new_value) "
				+ "VALUES (?, ?, ?, ?, ?);";

		PreparedStatement pstmt = conn.prepareStatement(sql);

		pstmt.setInt(1, parkId);
		pstmt.setInt(2, requestedByEmployeeId);
		pstmt.setString(3, parameterName);
		pstmt.setString(4, oldValue);
		pstmt.setString(5, newValue);

		return pstmt.executeUpdate() > 0;
	}

	/**
	 * This method approves a pending park parameter change request.
	 * 
	 * When the request is approved, the database trigger updates the matching value
	 * in the park table according to the requested parameter and new value.
	 * 
	 * Only a department manager should approve a request. This rule is enforced by a
	 * database trigger and should also be checked in the server logic.
	 * 
	 * @param requestId            the ID of the request to approve
	 * @param approvedByEmployeeId the employee ID of the department manager who
	 *                             approved the request
	 * @param reviewNote           a note explaining the approval decision
	 * @return true if the request was approved successfully, false otherwise
	 * @throws SQLException if the update query fails
	 */
	public boolean approveRequest(
			int requestId,
			int approvedByEmployeeId,
			String reviewNote) throws SQLException {

		String sql = "UPDATE park_parameter_change_request "
				+ "SET request_status = 'approved', approved_by_employee_id = ?, review_note = ? "
				+ "WHERE request_id = ? AND request_status = 'pending';";

		PreparedStatement pstmt = conn.prepareStatement(sql);

		pstmt.setInt(1, approvedByEmployeeId);
		pstmt.setString(2, reviewNote);
		pstmt.setInt(3, requestId);

		return pstmt.executeUpdate() > 0;
	}

	/**
	 * This method rejects a pending park parameter change request.
	 * 
	 * Rejecting a request does not update the park table. It only changes the
	 * request status to rejected and stores the department manager that reviewed it.
	 * 
	 * Only a department manager should reject a request. This rule is enforced by a
	 * database trigger and should also be checked in the server logic.
	 * 
	 * @param requestId            the ID of the request to reject
	 * @param approvedByEmployeeId the employee ID of the department manager who
	 *                             rejected the request
	 * @param reviewNote           a note explaining the rejection decision
	 * @return true if the request was rejected successfully, false otherwise
	 * @throws SQLException if the update query fails
	 */
	public boolean rejectRequest(
			int requestId,
			int approvedByEmployeeId,
			String reviewNote) throws SQLException {

		String sql = "UPDATE park_parameter_change_request "
				+ "SET request_status = 'rejected', approved_by_employee_id = ?, review_note = ? "
				+ "WHERE request_id = ? AND request_status = 'pending';";

		PreparedStatement pstmt = conn.prepareStatement(sql);

		pstmt.setInt(1, approvedByEmployeeId);
		pstmt.setString(2, reviewNote);
		pstmt.setInt(3, requestId);

		return pstmt.executeUpdate() > 0;
	}

	/**
	 * This method returns all pending park parameter change requests.
	 * 
	 * Pending requests are requests that have not yet been approved or rejected by a
	 * department manager.
	 * 
	 * @return a ResultSet containing all pending requests ordered by request date
	 * @throws SQLException if the select query fails
	 */
	public ResultSet getPendingRequests() throws SQLException {
		String sql = "SELECT * FROM park_parameter_change_request "
				+ "WHERE request_status = 'pending' "
				+ "ORDER BY requested_at;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		return pstmt.executeQuery();
	}
}