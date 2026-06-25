
package databaseControllers;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import common.ParkParameterChangeRequest;

/**
 * Handles database operations related to park parameter change requests.
 * 
 * This connector creates change requests, retrieves pending requests, approves
 * or rejects requests, and applies approved changes to the park table.
 */
public class ParkParameterChangeRequestConnection extends AbstractDBConnection {

	/**
	 * The single instance of ParkParameterChangeRequestConnection.
	 */
	private static ParkParameterChangeRequestConnection instance;

	/*
	 * Park parameter change request table column names.
	 */
	private final String REQUEST_ID = "request_id";
	private final String PARK_ID = "park_id";
	private final String PARAMETER_NAME = "parameter_name";
	private final String OLD_VALUE = "old_value";
	private final String NEW_VALUE = "new_value";
	private final String REQUEST_STATUS = "request_status";

	/*
	 * Supported request status values.
	 */
	private final String STATUS_PENDING = "pending";
	private final String STATUS_APPROVED = "approved";
	private final String STATUS_REJECTED = "rejected";

	/**
	 * The park table name used when applying approved parameter changes.
	 */
	private final String PARK_TABLE = ConstantsDBTableNames.PARK;

	/*
	 * Supported park parameter names.
	 */
	private final String PARAMETER_MAX_CAPACITY = "max_capacity";

	private final String PARAMETER_PLACES_FOR_UNPLANNED_VISITORS =
			"places_for_unplanned_visitors";

	private final String PARAMETER_ESTIMATED_VISIT_DURATION_HOURS =
			"estimated_visit_duration_hours";

	private final String PARAMETER_PROMOTIONS = "promotions";

	/**
	 * Creates a new ParkParameterChangeRequestConnection instance.
	 * 
	 * The constructor is private because this class is implemented as a singleton.
	 * 
	 * @throws SQLException if connecting to the database fails
	 */
	private ParkParameterChangeRequestConnection() throws SQLException {
		connect();
	}

	/**
	 * Returns the single instance of ParkParameterChangeRequestConnection.
	 * 
	 * If no instance exists, or if the current database connection is closed, a new
	 * instance is created.
	 * 
	 * @return the active ParkParameterChangeRequestConnection instance
	 * @throws SQLException if creating the database connection fails
	 */
	public static ParkParameterChangeRequestConnection getInstance() throws SQLException {
		if (instance == null || instance.conn == null || instance.conn.isClosed()) {
			instance = new ParkParameterChangeRequestConnection();
		}

		return instance;
	}

	/**
	 * Returns the database table name used by this connector.
	 * 
	 * @return the park parameter change request table name
	 */
	@Override
	public String getTableName() {
		return ConstantsDBTableNames.PARK_PARAMETER_CHANGE_REQUEST;
	}

	/**
	 * Creates a new park parameter change request.
	 *
	 * The request is saved with pending status. The old value represents the value
	 * that existed when the request was created.
	 * 
	 * @param parkId the park ID related to the requested change
	 * @param requestedByEmployeeId the employee ID that created the request
	 * @param parameterName the park parameter that should be changed
	 * @param oldValue the current value before the requested change
	 * @param newValue the requested new value
	 * @return true if the request was created successfully, otherwise false
	 * @throws SQLException if the insert query fails
	 */
	public boolean createChangeRequest(int parkId, int requestedByEmployeeId,
			String parameterName, String oldValue, String newValue) throws SQLException {

		ensureConnection();

		if (parkId <= 0
				|| parameterName == null || parameterName.isBlank()
				|| oldValue == null
				|| newValue == null || newValue.isBlank()) {
			return false;
		}

		if (!isSupportedParameter(parameterName)) {
			return false;
		}

		insertFields(
				new String[] {
						PARK_ID,
						PARAMETER_NAME,
						OLD_VALUE,
						NEW_VALUE,
						REQUEST_STATUS
				},
				List.of(
						parkId,
						parameterName,
						oldValue,
						newValue,
						STATUS_PENDING
				)
		);

		return true;
	}

	/**
	 * Approves a pending park parameter change request.
	 * 
	 * The method loads the pending request, applies the requested value to the park
	 * table, and then updates the request status to approved inside one transaction.
	 * 
	 * @param requestId the ID of the request to approve
	 * @param approvedByEmployeeId the employee ID that approves the request
	 * @param reviewNote the review note attached to the approval action
	 * @return true if the request was approved and applied successfully, otherwise
	 *         false
	 * @throws SQLException if the transaction or update query fails
	 */
	public boolean approveRequest(int requestId, int approvedByEmployeeId,
			String reviewNote) throws SQLException {

		ensureConnection();

		if (requestId <= 0 || approvedByEmployeeId <= 0) {
			return false;
		}

		boolean oldAutoCommit = conn.getAutoCommit();

		try {
			conn.setAutoCommit(false);

			RequestData requestData = loadPendingRequestData(requestId);

			if (requestData == null) {
				conn.rollback();
				return false;
			}

			boolean parkUpdated = updateParkParameter(
					requestData.parkId,
					requestData.parameterName,
					requestData.newValue
			);

			if (!parkUpdated) {
				conn.rollback();
				return false;
			}

			boolean requestUpdated = updateRequestStatusOnly(
					requestId,
					STATUS_APPROVED
			);

			if (!requestUpdated) {
				conn.rollback();
				return false;
			}

			conn.commit();
			return true;

		} catch (SQLException e) {
			conn.rollback();
			throw e;

		} finally {
			conn.setAutoCommit(oldAutoCommit);
		}
	}

	/**
	 * Rejects a pending park parameter change request.
	 * 
	 * The method changes only the request status and does not update the park table.
	 * 
	 * @param requestId the ID of the request to reject
	 * @param approvedByEmployeeId the employee ID that rejects the request
	 * @param reviewNote the review note attached to the rejection action
	 * @return true if the request was rejected successfully, otherwise false
	 * @throws SQLException if the update query fails
	 */
	public boolean rejectRequest(int requestId, int approvedByEmployeeId,
			String reviewNote) throws SQLException {

		ensureConnection();

		if (requestId <= 0 || approvedByEmployeeId <= 0) {
			return false;
		}

		return updateRequestStatusOnly(
				requestId,
				STATUS_REJECTED
		);
	}

	/**
	 * Updates the status of a request only if it is still pending.
	 * 
	 * @param requestId the ID of the request to update
	 * @param newStatus the new status to assign to the request
	 * @return true if the request status was updated successfully, otherwise false
	 * @throws SQLException if the update query fails
	 */
	private boolean updateRequestStatusOnly(int requestId, String newStatus)
			throws SQLException {

		return updateFields(
				new String[] {
						REQUEST_STATUS
				},
				List.of(
						newStatus
				),
				new String[] {
						REQUEST_ID,
						REQUEST_STATUS
				},
				List.of(
						requestId,
						STATUS_PENDING
				)
		);
	}

	/**
	 * Loads the data of a pending request.
	 * 
	 * @param requestId the ID of the pending request to load
	 * @return the request data needed for approval, or null if no pending request
	 *         was found
	 * @throws SQLException if the select query fails
	 */
	private RequestData loadPendingRequestData(int requestId) throws SQLException {
		String sql = selectByFields(
				new String[] {
						PARK_ID,
						PARAMETER_NAME,
						NEW_VALUE
				},
				new String[] {
						REQUEST_ID,
						REQUEST_STATUS
				}
		);

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, requestId);
			pstmt.setString(2, STATUS_PENDING);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					RequestData data = new RequestData();
					data.parkId = rs.getInt(PARK_ID);
					data.parameterName = rs.getString(PARAMETER_NAME);
					data.newValue = rs.getString(NEW_VALUE);

					return data;
				}
			}
		}

		return null;
	}

	/**
	 * Applies a supported park parameter value to the park table.
	 * 
	 * @param parkId the park ID to update
	 * @param parameterName the supported park parameter name
	 * @param newValue the new value as a string
	 * @return true if the park parameter was updated successfully, otherwise false
	 * @throws SQLException if the parameter value is invalid or the update query
	 *         fails
	 */
	private boolean updateParkParameter(int parkId, String parameterName,
			String newValue) throws SQLException {

		if (parkId <= 0 || !isSupportedParameter(parameterName)
				|| newValue == null || newValue.isBlank()) {
			return false;
		}

		Object convertedValue = convertParkParameterValue(parameterName, newValue);

		return updateFieldsInTable(
				PARK_TABLE,
				new String[] {
						parameterName
				},
				List.of(
						convertedValue
				),
				new String[] {
						PARK_ID
				},
				List.of(
						parkId
				)
		);
	}

	/**
	 * Converts a park parameter value from string to the required database type.
	 * 
	 * @param parameterName the park parameter being converted
	 * @param newValue the new value as a string
	 * @return the converted value in the correct type
	 * @throws SQLException if the value is invalid or the parameter is unsupported
	 */
	private Object convertParkParameterValue(String parameterName, String newValue)
			throws SQLException {

		try {
			switch (parameterName) {

			case PARAMETER_MAX_CAPACITY:
				int maxCapacity = Integer.parseInt(newValue);

				if (maxCapacity <= 0) {
					throw new SQLException("Max capacity must be positive.");
				}

				return maxCapacity;

			case PARAMETER_PLACES_FOR_UNPLANNED_VISITORS:
				int placesForUnplannedVisitors = Integer.parseInt(newValue);

				if (placesForUnplannedVisitors < 0) {
					throw new SQLException("Places for unplanned visitors cannot be negative.");
				}

				return placesForUnplannedVisitors;

			case PARAMETER_ESTIMATED_VISIT_DURATION_HOURS:
				int estimatedVisitDurationHours = Integer.parseInt(newValue);

				if (estimatedVisitDurationHours <= 0) {
					throw new SQLException("Estimated visit duration must be positive.");
				}

				return estimatedVisitDurationHours;

			case PARAMETER_PROMOTIONS:
				BigDecimal promotions = new BigDecimal(newValue);

				if (promotions.compareTo(BigDecimal.ZERO) < 0
						|| promotions.compareTo(BigDecimal.valueOf(100)) > 0) {
					throw new SQLException("Promotions must be between 0 and 100.");
				}

				return promotions;

			default:
				throw new SQLException("Unsupported park parameter: " + parameterName);
			}

		} catch (NumberFormatException e) {
			throw new SQLException("Invalid numeric value for " + parameterName + ": " + newValue);
		}
	}

	/**
	 * Checks whether a park parameter is supported for change requests.
	 * 
	 * @param parameterName the parameter name to check
	 * @return true if the parameter is supported, otherwise false
	 */
	private boolean isSupportedParameter(String parameterName) {
		return PARAMETER_MAX_CAPACITY.equals(parameterName)
				|| PARAMETER_PLACES_FOR_UNPLANNED_VISITORS.equals(parameterName)
				|| PARAMETER_ESTIMATED_VISIT_DURATION_HOURS.equals(parameterName)
				|| PARAMETER_PROMOTIONS.equals(parameterName);
	}

	/**
	 * Retrieves a park parameter change request by its ID.
	 * 
	 * @param requestId the request ID to search for
	 * @return the matching ParkParameterChangeRequest object, or null if no request
	 *         was found
	 * @throws SQLException if the select query fails
	 */
	public ParkParameterChangeRequest getRequestById(int requestId) throws SQLException {
		ensureConnection();

		String sql = selectByFields(
				new String[] {
						"*"
				},
				new String[] {
						REQUEST_ID
				}
		);

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, requestId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return convertResultSetToRequest(rs);
				}
			}
		}

		return null;
	}

	/**
	 * Retrieves all pending park parameter change requests.
	 * 
	 * @return a list of pending park parameter change requests
	 * @throws SQLException if the select query fails
	 */
	public List<ParkParameterChangeRequest> getPendingRequests() throws SQLException {
		ensureConnection();

		String sql = """
				SELECT *
				FROM `park_parameter_change_request`
				WHERE request_status = ?
				ORDER BY request_id;
				""";

		List<ParkParameterChangeRequest> pendingRequests =
				new java.util.ArrayList<>();

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, STATUS_PENDING);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					pendingRequests.add(convertResultSetToRequest(rs));
				}
			}
		}

		return pendingRequests;
	}

	/**
	 * Converts the current ResultSet row into a ParkParameterChangeRequest object.
	 * 
	 * The method also loads the current value of the requested parameter for
	 * display in the approval screen.
	 * 
	 * @param rs the ResultSet positioned on a request row
	 * @return a ParkParameterChangeRequest object containing the row data
	 * @throws SQLException if reading data from the ResultSet fails
	 */
	private ParkParameterChangeRequest convertResultSetToRequest(ResultSet rs)
			throws SQLException {

		int requestId = rs.getInt(REQUEST_ID);
		int parkId = rs.getInt(PARK_ID);
		String parameterName = rs.getString(PARAMETER_NAME);
		String oldValue = rs.getString(OLD_VALUE);
		String newValue = rs.getString(NEW_VALUE);
		String requestStatus = rs.getString(REQUEST_STATUS);

		ParkParameterChangeRequest request = new ParkParameterChangeRequest(
				requestId,
				parkId,
				parameterName,
				oldValue,
				newValue,
				requestStatus
		);

		String currentValue = getCurrentParkParameterValue(parkId, parameterName);
		request.setCurrentValue(currentValue);

		return request;
	}

	/**
	 * Gets the current value of the requested parameter from the park table.
	 *
	 * This value is used only for display in the approval screen and does not
	 * replace old_value.
	 * 
	 * @param parkId the park ID whose current parameter value should be loaded
	 * @param parameterName the supported parameter name
	 * @return the current parameter value, or "-" if it cannot be found
	 * @throws SQLException if the select query fails
	 */
	private String getCurrentParkParameterValue(int parkId, String parameterName)
			throws SQLException {

		if (parkId <= 0 || parameterName == null || !isSupportedParameter(parameterName)) {
			return "-";
		}

		String sql = "SELECT `" + parameterName + "` AS current_value "
				+ "FROM `" + PARK_TABLE + "` "
				+ "WHERE `" + PARK_ID + "` = ?";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, parkId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					Object currentValue = rs.getObject("current_value");

					if (currentValue == null) {
						return "-";
					}

					return currentValue.toString();
				}
			}
		}

		return "-";
	}

	/**
	 * Holds the minimal data required to apply a pending change request.
	 */
	private static class RequestData {
		private int parkId;
		private String parameterName;
		private String newValue;
	}
}
