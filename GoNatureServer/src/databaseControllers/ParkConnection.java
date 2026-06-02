package databaseControllers;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import common.Park;

/**
 * This class is the DB connector used when working with the park table.
 * 
 * The class is implemented as a Singleton, so the server will use only one
 * database connection object for parks during runtime.
 * 
 * The park table stores the park parameters, such as maximum capacity, reserved
 * places for unplanned visitors, estimated visit duration, entry price, active
 * status, display color and promotions.
 */
public class ParkConnection extends AbstractDBConnection {

	/**
	 * The single instance of ParkConnection.
	 */
	private static ParkConnection instance;

	private final String PARK_ID = "park_id";
	private final String PARK_NAME = "park_name";
	private final String MAX_CAPACITY = "max_capacity";
	private final String PLACES_FOR_UNPLANNED_VISITORS = "places_for_unplanned_visitors";
	private final String ESTIMATED_VISIT_DURATION_HOURS = "estimated_visit_duration_hours";
	private final String FULL_ENTRY_PRICE = "full_entry_price";
	private final String IS_ACTIVE = "is_active";
	private final String PARK_COLOR = "parkcol";
	private final String PROMOTIONS = "promotions";

	/**
	 * Private constructor for Singleton.
	 * 
	 * It creates the database connection once.
	 * 
	 * @throws SQLException if the connection to the database fails
	 */
	private ParkConnection() throws SQLException {
		connect();
	}

	/**
	 * Returns the single instance of ParkConnection.
	 * 
	 * If no instance exists, or if the existing database connection is closed, a new
	 * instance is created.
	 * 
	 * @return the only ParkConnection instance
	 * @throws SQLException if creating the database connection fails
	 */
	public static ParkConnection getInstance() throws SQLException {
		if (instance == null || instance.conn == null || instance.conn.isClosed()) {
			instance = new ParkConnection();
		}

		return instance;
	}

	/**
	 * Returns the table name used by this DB connector.
	 * 
	 * @return the park table name
	 */
	@Override
	protected String getTableName() {
		return ConstantsDBTableNames.PARK;
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
	 * This method returns park data by park ID.
	 * 
	 * The method returns a common.Park object because the Park class is shared
	 * between client and server.
	 * 
	 * @param parkId the park ID
	 * @return a Park object if the park exists, otherwise null
	 * @throws SQLException if the select query fails
	 */
	public Park getParkById(int parkId) throws SQLException {
		ensureConnection();

		if (parkId <= 0) {
			return null;
		}

		String[] columnNames = {
				PARK_ID,
				PARK_NAME,
				MAX_CAPACITY,
				PLACES_FOR_UNPLANNED_VISITORS,
				ESTIMATED_VISIT_DURATION_HOURS,
				FULL_ENTRY_PRICE,
				IS_ACTIVE,
				PARK_COLOR,
				PROMOTIONS
		};

		String[] keyColumns = {
				PARK_ID
		};

		String sql = selectByFields(columnNames, keyColumns);

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, parkId);

		java.sql.ResultSet rs = pstmt.executeQuery();

		Park park = null;

		if (rs.next()) {
			park = new Park(
					rs.getInt(PARK_ID),
					rs.getString(PARK_NAME),
					rs.getInt(MAX_CAPACITY),
					rs.getInt(PLACES_FOR_UNPLANNED_VISITORS),
					rs.getInt(ESTIMATED_VISIT_DURATION_HOURS),
					rs.getBigDecimal(FULL_ENTRY_PRICE),
					rs.getInt(IS_ACTIVE) == 1,
					rs.getString(PARK_COLOR),
					rs.getString(PROMOTIONS));
		}

		rs.close();
		pstmt.close();

		return park;
	}

	/**
	 * This method returns an active park by park ID.
	 * 
	 * @param parkId the park ID
	 * @return a Park object if the park exists and is active, otherwise null
	 * @throws SQLException if the select query fails
	 */
	public Park getActiveParkById(int parkId) throws SQLException {
		Park park = getParkById(parkId);

		if (park == null || !park.isActive()) {
			return null;
		}

		return park;
	}

	/**
	 * This method returns all active parks.
	 * 
	 * @return a list of active parks
	 * @throws SQLException if the select query fails
	 */
	public List<Park> getAllActiveParks() throws SQLException {
		ensureConnection();

		List<Park> parks = new ArrayList<>();

		String[] columnNames = {
				PARK_ID,
				PARK_NAME,
				MAX_CAPACITY,
				PLACES_FOR_UNPLANNED_VISITORS,
				ESTIMATED_VISIT_DURATION_HOURS,
				FULL_ENTRY_PRICE,
				IS_ACTIVE,
				PARK_COLOR,
				PROMOTIONS
		};

		String[] keyColumns = {
				IS_ACTIVE
		};

		String sql = selectByFields(columnNames, keyColumns);

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, 1);

		java.sql.ResultSet rs = pstmt.executeQuery();

		while (rs.next()) {
			Park park = new Park(
					rs.getInt(PARK_ID),
					rs.getString(PARK_NAME),
					rs.getInt(MAX_CAPACITY),
					rs.getInt(PLACES_FOR_UNPLANNED_VISITORS),
					rs.getInt(ESTIMATED_VISIT_DURATION_HOURS),
					rs.getBigDecimal(FULL_ENTRY_PRICE),
					rs.getInt(IS_ACTIVE) == 1,
					rs.getString(PARK_COLOR),
					rs.getString(PROMOTIONS));

			parks.add(park);
		}

		rs.close();
		pstmt.close();

		return parks;
	}

	/**
	 * This method checks whether a park exists and is active.
	 * 
	 * @param parkId the park ID
	 * @return true if the park exists and is active, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean isActivePark(int parkId) throws SQLException {
		return getActiveParkById(parkId) != null;
	}

	/**
	 * This method returns the full entry price of a park.
	 * 
	 * Since full_entry_price is DECIMAL(10,2) in the database, BigDecimal is used.
	 * 
	 * @param parkId the park ID
	 * @return the full entry price, or null if the park was not found
	 * @throws SQLException if the select query fails
	 */
	public BigDecimal getFullEntryPrice(int parkId) throws SQLException {
		ensureConnection();

		if (parkId <= 0) {
			return null;
		}

		String sql = selectByFields(new String[] { FULL_ENTRY_PRICE }, new String[] { PARK_ID });

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, parkId);

		java.sql.ResultSet rs = pstmt.executeQuery();

		BigDecimal price = null;

		if (rs.next()) {
			price = rs.getBigDecimal(FULL_ENTRY_PRICE);
		}

		rs.close();
		pstmt.close();

		return price;
	}

	/**
	 * This method checks whether a park has enough available capacity for immediate
	 * entrance.
	 * 
	 * The method checks current visitors who entered the park and have not exited
	 * yet. This is relevant for actual entrance handling.
	 * 
	 * @param parkId            the park ID
	 * @param requestedVisitors the number of visitors requesting entrance
	 * @return true if there is enough available capacity, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean hasAvailableCapacity(int parkId, int requestedVisitors) throws SQLException {
		ensureConnection();

		if (parkId <= 0 || requestedVisitors <= 0) {
			return false;
		}

		String sql = """
				SELECT
					p.max_capacity,
					COALESCE(SUM(v.actual_number_of_visitors), 0) AS current_visitors
				FROM park p
				LEFT JOIN visit v
					ON p.park_id = v.park_id
					AND v.exit_time IS NULL
				WHERE p.park_id = ?
					AND p.is_active = 1
				GROUP BY p.max_capacity;
				""";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, parkId);

		java.sql.ResultSet rs = pstmt.executeQuery();

		boolean hasCapacity = false;

		if (rs.next()) {
			int maxCapacity = rs.getInt("max_capacity");
			int currentVisitors = rs.getInt("current_visitors");

			hasCapacity = currentVisitors + requestedVisitors <= maxCapacity;
		}

		rs.close();
		pstmt.close();

		return hasCapacity;
	}

	/**
	 * This method checks whether a park has enough available order capacity for a
	 * specific date.
	 * 
	 * The method keeps reserved places for unplanned visitors, according to the park
	 * parameter places_for_unplanned_visitors.
	 * 
	 * This method is used when creating an order in advance.
	 * 
	 * @param parkId            the park ID
	 * @param orderDate         the requested visit date
	 * @param requestedVisitors the number of requested visitors
	 * @return true if there is enough capacity for an advance order, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean hasAvailableOrderCapacity(int parkId, LocalDate orderDate, int requestedVisitors)
			throws SQLException {

		ensureConnection();

		if (parkId <= 0 || orderDate == null || requestedVisitors <= 0) {
			return false;
		}

		String sql = """
				SELECT
					p.max_capacity,
					p.places_for_unplanned_visitors,
					COALESCE(SUM(o.number_of_visitors), 0) AS ordered_visitors
				FROM park p
				LEFT JOIN `order` o
					ON p.park_id = o.park_id
					AND o.order_date = ?
					AND o.order_status IN ('pending', 'approved')
				WHERE p.park_id = ?
					AND p.is_active = 1
				GROUP BY p.max_capacity, p.places_for_unplanned_visitors;
				""";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setDate(1, java.sql.Date.valueOf(orderDate));
		pstmt.setInt(2, parkId);

		java.sql.ResultSet rs = pstmt.executeQuery();

		boolean hasCapacity = false;

		if (rs.next()) {
			int maxCapacity = rs.getInt("max_capacity");
			int reservedForUnplanned = rs.getInt("places_for_unplanned_visitors");
			int orderedVisitors = rs.getInt("ordered_visitors");

			int allowedOrderedVisitors = maxCapacity - reservedForUnplanned;

			hasCapacity = orderedVisitors + requestedVisitors <= allowedOrderedVisitors;
		}

		rs.close();
		pstmt.close();

		return hasCapacity;
	}
}