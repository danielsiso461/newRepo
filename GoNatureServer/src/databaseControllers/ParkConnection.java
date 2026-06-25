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
 * DB connector for the park table.
 */
public class ParkConnection extends AbstractDBConnection {

	private static final String PARK_ID = "park_id";
	private static final String PARK_NAME = "park_name";
	private static final String MAX_CAPACITY = "max_capacity";
	private static final String CURRENT_VISITORS = "current_visitors";
	private static final String PLACES_FOR_UNPLANNED_VISITORS = "places_for_unplanned_visitors";
	private static final String ESTIMATED_VISIT_DURATION_HOURS = "estimated_visit_duration_hours";
	private static final String FULL_ENTRY_PRICE = "full_entry_price";
	private static final String IS_ACTIVE = "is_active";
	private static final String PROMOTIONS = "promotions";

	private static final String PARK_VISITOR_COUNTER_LOG = "park_visitor_counter_log";
	private static final String EMPLOYEE_ID = "employee_id";
	private static final String ACTION_TYPE = "action_type";
	private static final String AMOUNT = "amount";
	private static final String VISITORS_BEFORE = "visitors_before";
	private static final String VISITORS_AFTER = "visitors_after";

	private static final String PARAMETER_MAX_CAPACITY = "max_capacity";
	private static final String PARAMETER_PLACES_FOR_UNPLANNED_VISITORS =
			"places_for_unplanned_visitors";
	private static final String PARAMETER_ESTIMATED_VISIT_DURATION_HOURS =
			"estimated_visit_duration_hours";
	private static final String PARAMETER_PROMOTIONS = "promotions";

	private static final int ACTIVE_TRUE = 1;

	private static final int MIN_COUNTER_UPDATE_AMOUNT = 1;
	private static final int MAX_COUNTER_UPDATE_AMOUNT = 15;

	private static ParkConnection instance;

	private ParkConnection() throws SQLException {
		connect();
	}

	public static ParkConnection getInstance() throws SQLException {
		if (instance == null || instance.conn == null || instance.conn.isClosed()) {
			instance = new ParkConnection();
		}

		return instance;
	}

	@Override
	public String getTableName() {
		return ConstantsDBTableNames.PARK;
	}

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

	private ParkVisitorCounterSnapshot convertResultSetToCounterSnapshot(ResultSet rs)
			throws SQLException {

		return new ParkVisitorCounterSnapshot(
				rs.getInt(PARK_ID),
				rs.getString(PARK_NAME),
				rs.getInt(MAX_CAPACITY),
				rs.getInt(CURRENT_VISITORS)
		);
	}

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

	public List<Park> getAllActiveParksInfo() throws SQLException {
		return getAllActiveParks();
	}

	public Park getParkById(int parkId) throws SQLException {
		return getFullParkById(parkId);
	}

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

	public boolean hasPromotion(int parkId) throws SQLException {
		return getPromotionPercent(parkId) > 0;
	}

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

	public boolean isParkActive(int parkId) throws SQLException {
		Park park = getFullParkById(parkId);

		return park != null && park.isActive();
	}

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

	private int convertPositiveInteger(String value, String parameterName)
			throws SQLException {

		int number = Integer.parseInt(value);

		if (number <= 0) {
			throw new SQLException("Parameter " + parameterName + " must be positive.");
		}

		return number;
	}

	private int convertNonNegativeInteger(String value, String parameterName)
			throws SQLException {

		int number = Integer.parseInt(value);

		if (number < 0) {
			throw new SQLException("Parameter " + parameterName + " cannot be negative.");
		}

		return number;
	}

	private BigDecimal convertPromotionPercent(String value) throws SQLException {
		BigDecimal percent = new BigDecimal(value);

		if (percent.compareTo(BigDecimal.ZERO) < 0
				|| percent.compareTo(new BigDecimal("100")) > 0) {
			throw new SQLException("Promotion percent must be between 0 and 100.");
		}

		return percent;
	}

	private static class CounterData {
		private int currentVisitors;
		private int maxCapacity;
	}
}