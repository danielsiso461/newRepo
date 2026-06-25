
package databaseControllers;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import common.Park;
import common.ParkVisitorCounterSnapshot;
import common.ParkVisitorCounterUpdateRequest;

/**
 * Handles database operations related to parks.
 * 
 * This connector supports retrieving park information, checking capacity,
 * managing visitor counters, logging counter updates, and updating approved
 * park parameters.
 */
public class ParkConnection extends AbstractDBConnection {

	/*
	 * Park table column names.
	 */
	private static final String PARK_ID = "park_id";
	private static final String PARK_NAME = "park_name";
	private static final String MAX_CAPACITY = "max_capacity";
	private static final String CURRENT_VISITORS = "current_visitors";
	private static final String PLACES_FOR_UNPLANNED_VISITORS = "places_for_unplanned_visitors";
	private static final String ESTIMATED_VISIT_DURATION_HOURS = "estimated_visit_duration_hours";
	private static final String FULL_ENTRY_PRICE = "full_entry_price";
	private static final String IS_ACTIVE = "is_active";
	private static final String PROMOTIONS = "promotions";

	/*
	 * Visitor counter log table and column names.
	 */
	private static final String PARK_VISITOR_COUNTER_LOG = "park_visitor_counter_log";
	private static final String EMPLOYEE_ID = "employee_id";
	private static final String ACTION_TYPE = "action_type";
	private static final String AMOUNT = "amount";
	private static final String VISITORS_BEFORE = "visitors_before";
	private static final String VISITORS_AFTER = "visitors_after";

	/*
	 * Supported park parameter names.
	 */
	private static final String PARAMETER_MAX_CAPACITY = "max_capacity";
	private static final String PARAMETER_PLACES_FOR_UNPLANNED_VISITORS =
			"places_for_unplanned_visitors";
	private static final String PARAMETER_ESTIMATED_VISIT_DURATION_HOURS =
			"estimated_visit_duration_hours";
	private static final String PARAMETER_PROMOTIONS = "promotions";

	/**
	 * Integer value that represents an active park in the database.
	 */
	private static final int ACTIVE_TRUE = 1;

	/**
	 * Minimum allowed number of visitors in a counter update.
	 */
	private static final int MIN_COUNTER_UPDATE_AMOUNT = 1;

	/**
	 * Maximum allowed number of visitors in a counter update.
	 */
	private static final int MAX_COUNTER_UPDATE_AMOUNT = 15;

	/**
	 * The single instance of ParkConnection.
	 */
	private static ParkConnection instance;

	/**
	 * Creates a new ParkConnection instance.
	 * 
	 * The constructor is private because this class is implemented as a singleton.
	 * 
	 * @throws SQLException if connecting to the database fails
	 */
	private ParkConnection() throws SQLException {
		connect();
	}

	/**
	 * Returns the single instance of ParkConnection.
	 * 
	 * If no instance exists, or if the current database connection is closed, a new
	 * instance is created.
	 * 
	 * @return the active ParkConnection instance
	 * @throws SQLException if creating the database connection fails
	 */
	public static ParkConnection getInstance() throws SQLException {
		if (instance == null || instance.conn == null || instance.conn.isClosed()) {
			instance = new ParkConnection();
		}

		return instance;
	}

	/**
	 * Returns the database table name used by this connector.
	 * 
	 * @return the park table name
	 */
	@Override
	public String getTableName() {
		return ConstantsDBTableNames.PARK;
	}

	/**
	 * Converts the current ResultSet row into a Park object.
	 * 
	 * @param rs the ResultSet positioned on a park row
	 * @return a Park object containing the row data
	 * @throws SQLException if reading data from the ResultSet fails
	 */
	private Park convertResultSetToPark(ResultSet rs) throws SQLException {
		return new Park(
				rs.getInt(PARK_ID),
				rs.getString(PARK_NAME),
				rs.getInt(MAX_CAPACITY),
				rs.getInt(CURRENT_VISITORS),
				rs.getInt(PLACES_FOR_UNPLANNED_VISITORS),
				rs.getDouble(ESTIMATED_VISIT_DURATION_HOURS),
				rs.getDouble(FULL_ENTRY_PRICE),
				rs.getInt(IS_ACTIVE) == ACTIVE_TRUE,
				rs.getDouble(PROMOTIONS)
		);
	}

	/**
	 * Converts the current ResultSet row into a park visitor counter snapshot.
	 * 
	 * @param rs the ResultSet positioned on a park counter row
	 * @return a ParkVisitorCounterSnapshot object containing the counter data
	 * @throws SQLException if reading data from the ResultSet fails
	 */
	private ParkVisitorCounterSnapshot convertResultSetToCounterSnapshot(ResultSet rs)
			throws SQLException {

		return new ParkVisitorCounterSnapshot(
				rs.getInt(PARK_ID),
				rs.getString(PARK_NAME),
				rs.getInt(MAX_CAPACITY),
				rs.getInt(CURRENT_VISITORS)
		);
	}

	/**
	 * Retrieves all active parks from the database.
	 * 
	 * @return a list of active parks
	 * @throws SQLException if the select query fails
	 */
	public List<Park> getAllActiveParks() throws SQLException {
		ensureConnection();

		String sql = selectByFields(
				new String[] {
						"*"
				},
				new String[] {
						IS_ACTIVE
				}
		);

		List<Park> parks = new ArrayList<>();

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, ACTIVE_TRUE);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					parks.add(convertResultSetToPark(rs));
				}
			}
		}

		return parks;
	}

	/**
	 * Retrieves all parks from the database, including inactive parks.
	 * 
	 * @return a list of all parks
	 * @throws SQLException if the select query fails
	 */
	public List<Park> getAllFullParks() throws SQLException {
		ensureConnection();

		String sql = "SELECT * FROM `" + getTableName() + "`;";

		List<Park> parks = new ArrayList<>();

		try (PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				parks.add(convertResultSetToPark(rs));
			}
		}

		return parks;
	}

	/**
	 * Retrieves full park details by park ID.
	 * 
	 * @param parkId the park ID to search for
	 * @return the matching Park object, or null if no park was found
	 * @throws SQLException if the select query fails
	 */
	public Park getFullParkById(int parkId) throws SQLException {
		ensureConnection();

		String sql = selectByFields(
				new String[] {
						"*"
				},
				new String[] {
						PARK_ID
				}
		);

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, parkId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return convertResultSetToPark(rs);
				}
			}
		}

		return null;
	}

	/**
	 * Retrieves information for all active parks.
	 * 
	 * This method delegates to getAllActiveParks and is kept for compatibility with
	 * existing code.
	 * 
	 * @return a list of active parks
	 * @throws SQLException if the select query fails
	 */
	public List<Park> getAllActiveParksInfo() throws SQLException {
		return getAllActiveParks();
	}

	/**
	 * Retrieves park details by park ID.
	 * 
	 * This method delegates to getFullParkById.
	 * 
	 * @param parkId the park ID to search for
	 * @return the matching Park object, or null if no park was found
	 * @throws SQLException if the select query fails
	 */
	public Park getParkById(int parkId) throws SQLException {
		return getFullParkById(parkId);
	}

	/**
	 * Retrieves the full entry price of a specific park.
	 * 
	 * @param parkId the park ID to search for
	 * @return the full entry price, or -1 if the park was not found
	 * @throws SQLException if the select query fails
	 */
	public double getFullEntryPrice(int parkId) throws SQLException {
		ensureConnection();

		String sql = selectByFields(
				new String[] {
						FULL_ENTRY_PRICE
				},
				new String[] {
						PARK_ID
				}
		);

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, parkId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getDouble(FULL_ENTRY_PRICE);
				}
			}
		}

		return -1;
	}

	/**
	 * Retrieves the promotion discount percentage of a specific park.
	 * 
	 * @param parkId the park ID to search for
	 * @return the promotion percentage, or 0 if no promotion was found
	 * @throws SQLException if the select query fails
	 */
	public double getPromotionPercent(int parkId) throws SQLException {
		ensureConnection();

		String sql = selectByFields(
				new String[] {
						PROMOTIONS
				},
				new String[] {
						PARK_ID
				}
		);

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, parkId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getDouble(PROMOTIONS);
				}
			}
		}

		return 0;
	}

	/**
	 * Checks whether a park has an active promotion.
	 * 
	 * @param parkId the park ID to check
	 * @return true if the park has a promotion greater than zero, otherwise false
	 * @throws SQLException if the select query fails
	 */
	public boolean hasPromotion(int parkId) throws SQLException {
		return getPromotionPercent(parkId) > 0;
	}

	/**
	 * Checks whether the park currently has enough available capacity.
	 * 
	 * @param parkId the park ID to check
	 * @param requestedVisitors the number of visitors requested
	 * @return true if the park has enough available capacity, otherwise false
	 * @throws SQLException if the select query fails
	 */
	public boolean hasAvailableCapacity(int parkId, int requestedVisitors)
			throws SQLException {

		ensureConnection();

		String sql = selectByFields(
				new String[] {
						MAX_CAPACITY,
						CURRENT_VISITORS
				},
				new String[] {
						PARK_ID
				}
		);

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, parkId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (!rs.next()) {
					return false;
				}

				int maxCapacity = rs.getInt(MAX_CAPACITY);
				int currentVisitors = rs.getInt(CURRENT_VISITORS);

				return currentVisitors + requestedVisitors <= maxCapacity;
			}
		}
	}

	/**
	 * Checks whether there is enough capacity for an order on a specific date.
	 * 
	 * The calculation reserves part of the park capacity for unplanned visitors and
	 * compares the remaining capacity with already approved orders.
	 * 
	 * @param parkId the park ID to check
	 * @param orderDate the requested visit date
	 * @param requestedVisitors the number of visitors requested
	 * @return true if the order can be approved without exceeding capacity,
	 *         otherwise false
	 * @throws SQLException if the select query fails
	 */
	public boolean hasAvailableOrderCapacity(int parkId,
			java.time.LocalDate orderDate, int requestedVisitors)
			throws SQLException {

		ensureConnection();

		String sql = """
				SELECT
				    p.park_id,
				    p.max_capacity,
				    p.places_for_unplanned_visitors,
				    COALESCE(SUM(o.number_of_visitors), 0) AS ordered_visitors
				FROM park p
				LEFT JOIN `order` o
				    ON p.park_id = o.park_id
				    AND o.order_status = 'approved'
				    AND o.order_date = ?
				WHERE p.park_id = ?
				GROUP BY p.park_id, p.max_capacity, p.places_for_unplanned_visitors;
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setDate(1, java.sql.Date.valueOf(orderDate));
			pstmt.setInt(2, parkId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (!rs.next()) {
					return false;
				}

				int maxCapacity = rs.getInt(MAX_CAPACITY);
				int reservedForUnplanned = rs.getInt(PLACES_FOR_UNPLANNED_VISITORS);
				int orderedVisitors = rs.getInt("ordered_visitors");

				int allowedOrderedVisitors = maxCapacity - reservedForUnplanned;

				return orderedVisitors + requestedVisitors <= allowedOrderedVisitors;
			}
		}
	}

	/**
	 * Checks whether a park exists and is active.
	 * 
	 * @param parkId the park ID to check
	 * @return true if the park exists and is active, otherwise false
	 * @throws SQLException if the select query fails
	 */
	public boolean isParkActive(int parkId) throws SQLException {
		Park park = getFullParkById(parkId);

		return park != null && park.isActive();
	}

	/**
	 * Retrieves the names of all active parks.
	 * 
	 * @return a list of active park names
	 * @throws SQLException if the select query fails
	 */
	public List<String> getActiveParksNames() throws SQLException {
		ensureConnection();

		String sql = selectByFields(
				new String[] {
						PARK_NAME
				},
				new String[] {
						IS_ACTIVE
				}
		);

		List<String> activeParkNames = new ArrayList<>();

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, ACTIVE_TRUE);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					activeParkNames.add(rs.getString(PARK_NAME));
				}
			}
		}

		return activeParkNames;
	}

	/**
	 * Retrieves a park ID by its park name.
	 * 
	 * @param parkName the park name to search for
	 * @return the matching park ID, or -1 if no park was found
	 * @throws SQLException if the select query fails
	 */
	public int getParkIdByName(String parkName) throws SQLException {
		ensureConnection();

		String sql = selectByFields(
				new String[] {
						PARK_ID
				},
				new String[] {
						PARK_NAME
				}
		);

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, parkName);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(PARK_ID);
				}
			}
		}

		return -1;
	}

	/**
	 * Retrieves visitor counter snapshots for all active parks.
	 * 
	 * @return a list of visitor counter snapshots
	 * @throws SQLException if the select query fails
	 */
	public List<ParkVisitorCounterSnapshot> getAllParkVisitorCounters()
			throws SQLException {

		ensureConnection();

		String sql = "SELECT "
				+ PARK_ID + ", "
				+ PARK_NAME + ", "
				+ MAX_CAPACITY + ", "
				+ CURRENT_VISITORS + " "
				+ "FROM `" + getTableName() + "` "
				+ "WHERE " + IS_ACTIVE + " = ? "
				+ "ORDER BY " + PARK_NAME + ";";

		List<ParkVisitorCounterSnapshot> counters = new ArrayList<>();

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, ACTIVE_TRUE);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					counters.add(convertResultSetToCounterSnapshot(rs));
				}
			}
		}

		return counters;
	}

	/**
	 * Retrieves the visitor counter snapshot of a specific active park.
	 * 
	 * @param parkId the park ID to search for
	 * @return the matching counter snapshot, or null if no active park was found
	 * @throws SQLException if the select query fails
	 */
	public ParkVisitorCounterSnapshot getParkVisitorCounter(int parkId)
			throws SQLException {

		ensureConnection();

		String sql = "SELECT "
				+ PARK_ID + ", "
				+ PARK_NAME + ", "
				+ MAX_CAPACITY + ", "
				+ CURRENT_VISITORS + " "
				+ "FROM `" + getTableName() + "` "
				+ "WHERE " + PARK_ID + " = ? "
				+ "AND " + IS_ACTIVE + " = ?;";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, parkId);
			pstmt.setInt(2, ACTIVE_TRUE);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return convertResultSetToCounterSnapshot(rs);
				}
			}
		}

		return null;
	}

	/**
	 * Updates the current visitor counter of a park.
	 * 
	 * The update is performed inside a transaction. The method validates the
	 * request, locks the relevant park row, updates the visitor amount, writes a log
	 * record, and commits the transaction.
	 * 
	 * @param parkId the park ID to update
	 * @param employeeId the employee ID performing the update
	 * @param actionType the counter action type, either entry or exit
	 * @param amount the number of visitors to add or remove
	 * @return true if the visitor counter was updated successfully
	 * @throws SQLException if validation fails, capacity rules are violated, or the
	 *         update query fails
	 */
	public boolean updateCurrentVisitors(int parkId, int employeeId,
			String actionType, int amount) throws SQLException {

		ensureConnection();

		validateCounterUpdateRequest(actionType, amount);

		boolean oldAutoCommit = conn.getAutoCommit();

		try {
			conn.setAutoCommit(false);

			CounterData counterData = loadCounterDataForUpdate(parkId);

			if (counterData == null) {
				throw new SQLException("Park was not found or is not active.");
			}

			int visitorsBefore = counterData.currentVisitors;
			int visitorsAfter = calculateVisitorsAfterUpdate(
					visitorsBefore,
					counterData.maxCapacity,
					actionType,
					amount
			);

			updateCurrentVisitorsValue(parkId, visitorsAfter);

			insertCounterUpdateLog(
					parkId,
					employeeId,
					actionType,
					amount,
					visitorsBefore,
					visitorsAfter
			);

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
	 * Validates a visitor counter update request.
	 * 
	 * @param actionType the requested counter action type
	 * @param amount the number of visitors in the update
	 * @throws SQLException if the action type is invalid or the amount is outside
	 *         the allowed range
	 */
	private void validateCounterUpdateRequest(String actionType, int amount)
			throws SQLException {

		if (!ParkVisitorCounterUpdateRequest.ACTION_ENTRY.equals(actionType)
				&& !ParkVisitorCounterUpdateRequest.ACTION_EXIT.equals(actionType)) {
			throw new SQLException("Unknown visitor counter action: " + actionType);
		}

		if (amount < MIN_COUNTER_UPDATE_AMOUNT || amount > MAX_COUNTER_UPDATE_AMOUNT) {
			throw new SQLException("Visitors amount must be between 1 and 15.");
		}
	}

	/**
	 * Loads park counter data and locks the selected park row for update.
	 * 
	 * @param parkId the park ID to load
	 * @return the counter data for the park, or null if the park was not found or
	 *         is inactive
	 * @throws SQLException if the select query fails
	 */
	private CounterData loadCounterDataForUpdate(int parkId) throws SQLException {
		String sql = "SELECT "
				+ CURRENT_VISITORS + ", "
				+ MAX_CAPACITY + " "
				+ "FROM `" + getTableName() + "` "
				+ "WHERE " + PARK_ID + " = ? "
				+ "AND " + IS_ACTIVE + " = ? "
				+ "FOR UPDATE;";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, parkId);
			pstmt.setInt(2, ACTIVE_TRUE);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					CounterData data = new CounterData();

					data.currentVisitors = rs.getInt(CURRENT_VISITORS);
					data.maxCapacity = rs.getInt(MAX_CAPACITY);

					return data;
				}
			}
		}

		return null;
	}

	/**
	 * Calculates the visitor counter value after an entry or exit update.
	 * 
	 * @param visitorsBefore the number of visitors before the update
	 * @param maxCapacity the park maximum capacity
	 * @param actionType the counter action type
	 * @param amount the number of visitors to add or remove
	 * @return the number of visitors after the update
	 * @throws SQLException if the update would exceed capacity or create a negative
	 *         visitor count
	 */
	private int calculateVisitorsAfterUpdate(int visitorsBefore, int maxCapacity,
			String actionType, int amount) throws SQLException {

		if (ParkVisitorCounterUpdateRequest.ACTION_ENTRY.equals(actionType)) {
			int visitorsAfter = visitorsBefore + amount;

			if (visitorsAfter > maxCapacity) {
				throw new SQLException("Cannot enter visitors. Park capacity exceeded.");
			}

			return visitorsAfter;
		}

		int visitorsAfter = visitorsBefore - amount;

		if (visitorsAfter < 0) {
			throw new SQLException("Cannot exit more visitors than currently inside the park.");
		}

		return visitorsAfter;
	}

	/**
	 * Updates the current visitor counter value of a park.
	 * 
	 * @param parkId the park ID to update
	 * @param visitorsAfter the new visitor counter value
	 * @throws SQLException if the update does not affect any row or if the query
	 *         fails
	 */
	private void updateCurrentVisitorsValue(int parkId, int visitorsAfter)
			throws SQLException {

		String sql = "UPDATE `" + getTableName() + "` "
				+ "SET " + CURRENT_VISITORS + " = ? "
				+ "WHERE " + PARK_ID + " = ?;";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, visitorsAfter);
			pstmt.setInt(2, parkId);

			int rows = pstmt.executeUpdate();

			if (rows == 0) {
				throw new SQLException("Failed to update park visitor counter.");
			}
		}
	}

	/**
	 * Inserts a log record for a visitor counter update.
	 * 
	 * @param parkId the park ID whose counter was updated
	 * @param employeeId the employee ID that performed the update
	 * @param actionType the counter action type
	 * @param amount the number of visitors added or removed
	 * @param visitorsBefore the visitor counter value before the update
	 * @param visitorsAfter the visitor counter value after the update
	 * @throws SQLException if the insert query fails
	 */
	private void insertCounterUpdateLog(int parkId, int employeeId,
			String actionType, int amount, int visitorsBefore,
			int visitorsAfter) throws SQLException {

		String sql = "INSERT INTO `" + PARK_VISITOR_COUNTER_LOG + "` ("
				+ PARK_ID + ", "
				+ EMPLOYEE_ID + ", "
				+ ACTION_TYPE + ", "
				+ AMOUNT + ", "
				+ VISITORS_BEFORE + ", "
				+ VISITORS_AFTER
				+ ") VALUES (?, ?, ?, ?, ?, ?);";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, parkId);
			pstmt.setInt(2, employeeId);
			pstmt.setString(3, actionType);
			pstmt.setInt(4, amount);
			pstmt.setInt(5, visitorsBefore);
			pstmt.setInt(6, visitorsAfter);

			pstmt.executeUpdate();
		}
	}

	/**
	 * Updates a configurable park parameter.
	 * 
	 * The method validates the requested parameter, converts the new value to the
	 * correct type, and updates the matching column in the park table.
	 * 
	 * @param parkId the park ID to update
	 * @param parameterName the parameter name to update
	 * @param newValue the new value as received from the request
	 * @return true if the parameter was updated successfully, otherwise false
	 * @throws SQLException if the parameter name or value is invalid, or if the
	 *         update query fails
	 */
	public boolean updateParkParameter(int parkId, String parameterName,
			String newValue) throws SQLException {

		ensureConnection();

		if (parameterName == null || parameterName.isBlank()
				|| newValue == null || newValue.isBlank()) {
			throw new SQLException("Invalid park parameter update request.");
		}

		String columnName = getParkColumnByParameterName(parameterName);

		if (columnName == null) {
			throw new SQLException("Unknown park parameter: " + parameterName);
		}

		Object convertedValue = convertParkParameterValue(parameterName, newValue);

		return updateFields(
				new String[] {
						columnName
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
	 * Maps a park parameter name to the matching database column name.
	 * 
	 * @param parameterName the logical parameter name
	 * @return the matching database column name, or null if the parameter is
	 *         unknown
	 */
	private String getParkColumnByParameterName(String parameterName) {
		switch (parameterName) {

		case PARAMETER_MAX_CAPACITY:
			return MAX_CAPACITY;

		case PARAMETER_PLACES_FOR_UNPLANNED_VISITORS:
			return PLACES_FOR_UNPLANNED_VISITORS;

		case PARAMETER_ESTIMATED_VISIT_DURATION_HOURS:
			return ESTIMATED_VISIT_DURATION_HOURS;

		case PARAMETER_PROMOTIONS:
			return PROMOTIONS;

		default:
			return null;
		}
	}

	/**
	 * Converts a park parameter value from string to the required database type.
	 * 
	 * @param parameterName the parameter being converted
	 * @param newValue the new value as a string
	 * @return the converted value in the correct type
	 * @throws SQLException if the value is invalid for the requested parameter
	 */
	private Object convertParkParameterValue(String parameterName, String newValue)
			throws SQLException {

		String cleanValue = newValue.trim();

		try {
			switch (parameterName) {

			case PARAMETER_MAX_CAPACITY:
				return convertPositiveInteger(cleanValue, parameterName);

			case PARAMETER_PLACES_FOR_UNPLANNED_VISITORS:
				return convertNonNegativeInteger(cleanValue, parameterName);

			case PARAMETER_ESTIMATED_VISIT_DURATION_HOURS:
				return convertPositiveInteger(cleanValue, parameterName);

			case PARAMETER_PROMOTIONS:
				return convertPromotionPercent(cleanValue);

			default:
				throw new SQLException("Unknown park parameter: " + parameterName);
			}

		} catch (NumberFormatException e) {
			throw new SQLException("Invalid numeric value for parameter: " + parameterName, e);
		}
	}

	/**
	 * Converts a string value to a positive integer.
	 * 
	 * @param value the string value to convert
	 * @param parameterName the parameter name used for the error message
	 * @return the converted positive integer
	 * @throws SQLException if the value is not positive or cannot be converted
	 */
	private int convertPositiveInteger(String value, String parameterName)
			throws SQLException {

		int number = Integer.parseInt(value);

		if (number <= 0) {
			throw new SQLException("Parameter " + parameterName + " must be positive.");
		}

		return number;
	}

	/**
	 * Converts a string value to a non-negative integer.
	 * 
	 * @param value the string value to convert
	 * @param parameterName the parameter name used for the error message
	 * @return the converted non-negative integer
	 * @throws SQLException if the value is negative or cannot be converted
	 */
	private int convertNonNegativeInteger(String value, String parameterName)
			throws SQLException {

		int number = Integer.parseInt(value);

		if (number < 0) {
			throw new SQLException("Parameter " + parameterName + " cannot be negative.");
		}

		return number;
	}

	/**
	 * Converts and validates a promotion percentage value.
	 * 
	 * @param value the promotion percentage as a string
	 * @return the converted promotion percentage
	 * @throws SQLException if the percentage is outside the range 0 to 100
	 */
	private BigDecimal convertPromotionPercent(String value) throws SQLException {
		BigDecimal percent = new BigDecimal(value);

		if (percent.compareTo(BigDecimal.ZERO) < 0
				|| percent.compareTo(new BigDecimal("100")) > 0) {
			throw new SQLException("Promotion percent must be between 0 and 100.");
		}

		return percent;
	}

	/**
	 * Holds visitor counter values used during a transactional counter update.
	 */
	private static class CounterData {
		private int currentVisitors;
		private int maxCapacity;
	}
}

