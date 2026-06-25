package databaseControllers;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import common.ParkParameterChangeRequest;

/**
 * DB connector for the park_parameter_change_request table.
 */
public class ParkParameterChangeRequestConnection extends AbstractDBConnection {

	private static ParkParameterChangeRequestConnection instance;

	private final String REQUEST_ID = "request_id";
	private final String PARK_ID = "park_id";
	private final String PARAMETER_NAME = "parameter_name";
	private final String OLD_VALUE = "old_value";
	private final String NEW_VALUE = "new_value";
	private final String REQUEST_STATUS = "request_status";

	private final String STATUS_PENDING = "pending";
	private final String STATUS_APPROVED = "approved";
	private final String STATUS_REJECTED = "rejected";

	private final String PARK_TABLE = ConstantsDBTableNames.PARK;

	private final String PARAMETER_MAX_CAPACITY = "max_capacity";

	private final String PARAMETER_PLACES_FOR_UNPLANNED_VISITORS =
			"places_for_unplanned_visitors";

	private final String PARAMETER_ESTIMATED_VISIT_DURATION_HOURS =
			"estimated_visit_duration_hours";

	private final String PARAMETER_PROMOTIONS = "promotions";

	private ParkParameterChangeRequestConnection() throws SQLException {
		connect();
	}

	public static ParkParameterChangeRequestConnection getInstance() throws SQLException {
		if (instance == null || instance.conn == null || instance.conn.isClosed()) {
			instance = new ParkParameterChangeRequestConnection();
		}

		return instance;
	}

	@Override
	public String getTableName() {
		return ConstantsDBTableNames.PARK_PARAMETER_CHANGE_REQUEST;
	}

	/**
	 * Creates a new park parameter change request.
	 *
	 * requestedByEmployeeId is used by the server for permission checks.
	 * The oldValue is the value that existed when the request was created.
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
	 * Approves a pending request.
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
	 * Rejects a pending request.
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

	private boolean isSupportedParameter(String parameterName) {
		return PARAMETER_MAX_CAPACITY.equals(parameterName)
				|| PARAMETER_PLACES_FOR_UNPLANNED_VISITORS.equals(parameterName)
				|| PARAMETER_ESTIMATED_VISIT_DURATION_HOURS.equals(parameterName)
				|| PARAMETER_PROMOTIONS.equals(parameterName);
	}

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
	 * This is used only for display in the approval screen.
	 * It does not replace old_value.
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

	private static class RequestData {
		private int parkId;
		private String parameterName;
		private String newValue;
	}
}