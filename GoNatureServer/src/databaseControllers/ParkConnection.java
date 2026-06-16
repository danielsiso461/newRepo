package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import common.Order;
import common.Park;
import common.ParkInfo;

/**
 * This class is the DB connector used when working with the park table.
 * 
 * The class is implemented as a Singleton, so the server will use only one
 * database connection object for parks during runtime.
 * 
 * The park table stores park information such as park name, maximum capacity,
 * number of reserved places for unplanned visitors, estimated visit duration,
 * full entry price, active status, and promotions.
 */
public class ParkConnection extends AbstractDBConnection {
	/* park table columns */
	private final String
					PARK_NAME_COLUMN = "park_name",
					PARK_ID_COLUMN = "park_id",
					PARK_IS_ACTIVE_COLUMN = "is_active";
	/* indicator that a park is active */
	private final int
					PARK_IS_ACTIVE_TRUE = 1;
	
	/**
	 * The single instance of ParkConnection.
	 */
	private static ParkConnection instance;

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
	 * This method converts the current row of a ResultSet into a full Park object.
	 * 
	 * @param rs the ResultSet positioned on the current park row
	 * @return a Park object that represents the current park
	 * @throws SQLException if reading data from the ResultSet fails
	 */
	private Park convertResultSetToPark(ResultSet rs) throws SQLException {
		return new Park(
				rs.getInt("park_id"),
				rs.getString("park_name"),
				rs.getInt("max_capacity"),
				rs.getInt("places_for_unplanned_visitors"),
				rs.getDouble("estimated_visit_duration_hours"),
				rs.getDouble("full_entry_price"),
				rs.getInt("is_active") == 1,
				rs.getInt("promotions") == 1
		);
	}

	/**
	 * This method returns all active parks as full Park objects.
	 * 
	 * The returned objects are intended for server-side use only, because they
	 * include internal management data such as capacity and reserved places for
	 * unplanned visitors.
	 * 
	 * @return a list of active full Park objects
	 * @throws SQLException if the select query fails
	 */
	public List<Park> getAllActiveParks() throws SQLException {
		ensureConnection();

		String sql = """
				SELECT *
				FROM park
				WHERE is_active = 1
				ORDER BY park_name;
				""";

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
	 * This method returns all parks as full Park objects.
	 * 
	 * The returned objects are intended for server-side use, such as management
	 * screens, capacity checks, and internal calculations.
	 * 
	 * @return a list of all full Park objects
	 * @throws SQLException if the select query fails
	 */
	public List<Park> getAllFullParks() throws SQLException {
		ensureConnection();

		String sql = """
				SELECT *
				FROM park
				ORDER BY park_id;
				""";

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
	 * This method returns a full park object by park ID.
	 * 
	 * The returned object is intended for server-side logic only, because it contains
	 * internal management data.
	 * 
	 * @param parkId the park ID
	 * @return a full Park object if the park exists, otherwise null
	 * @throws SQLException if the select query fails
	 */
	public Park getFullParkById(int parkId) throws SQLException {
		ensureConnection();

		String sql = """
				SELECT *
				FROM park
				WHERE park_id = ?;
				""";

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
	 * This method returns public information about all active parks.
	 * 
	 * The method converts full Park objects into ParkInfo objects before sending
	 * them to the client, so internal management data is not exposed.
	 * 
	 * @return a list of public park information objects
	 * @throws SQLException if the select query fails
	 */
	public List<ParkInfo> getAllActiveParksInfo() throws SQLException {
		List<ParkInfo> parkInfoList = new ArrayList<>();

		for (Park park : getAllActiveParks()) {
			parkInfoList.add(new ParkInfo(
					park.getParkId(),
					park.getParkName(),
					park.getEstimatedVisitDurationHours(),
					park.getFullEntryPrice()
			));
		}

		return parkInfoList;
	}
	

	/**
	 * This method returns public park information by park ID.
	 * 
	 * The returned object is intended to be sent to the client, so it does not
	 * include internal management data.
	 * 
	 * @param parkId the park ID
	 * @return a Park object if the park exists, otherwise null
	 * @throws SQLException if the select query fails
	 */
	public Park getParkById(int parkId) throws SQLException {
		Park park = getFullParkById(parkId);

		if (park == null) {
			return null;
		}

		return park;
	}

	/**
	 * This method returns the full entry price of a specific park.
	 * 
	 * The full entry price is the base price before applying discounts such as
	 * ordered visit discounts, group discounts, subscriber discounts, or promotion
	 * discounts.
	 * 
	 * @param parkId the park ID
	 * @return the full entry price of the park, or -1 if the park was not found
	 * @throws SQLException if the select query fails
	 */
	public double getFullEntryPrice(int parkId) throws SQLException {
		ensureConnection();

		String sql = """
				SELECT full_entry_price
				FROM park
				WHERE park_id = ?;
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, parkId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getDouble("full_entry_price");
				}
			}
		}

		return -1;
	}

	/**
	 * This method checks whether a specific park currently has a promotion.
	 * 
	 * @param parkId the park ID
	 * @return true if the park has an active promotion, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean hasPromotion(int parkId) throws SQLException {
		ensureConnection();

		String sql = """
				SELECT promotions
				FROM park
				WHERE park_id = ?;
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, parkId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("promotions") == 1;
				}
			}
		}

		return false;
	}

	/**
	 * This method checks whether a park has enough available capacity for the
	 * requested number of visitors at the current time.
	 * 
	 * This method is used for actual entrance control. It checks the number of
	 * visitors currently inside the park, according to visits that have not been
	 * closed yet. A visit is considered open if its exit_time is null.
	 * 
	 * @param parkId            the park ID
	 * @param requestedVisitors the number of visitors that want to enter the park
	 * @return true if the park has enough available capacity, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean hasAvailableCapacity(int parkId, int requestedVisitors) throws SQLException {
		ensureConnection();

		String sql = """
				SELECT
					p.max_capacity,
					COALESCE(SUM(v.actual_number_of_visitors), 0) AS current_visitors
				FROM park p
				LEFT JOIN visit v
					ON p.park_id = v.park_id
					AND v.exit_time IS NULL
				WHERE p.park_id = ?
				GROUP BY p.max_capacity;
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, parkId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (!rs.next()) {
					return false;
				}

				int maxCapacity = rs.getInt("max_capacity");
				int currentVisitors = rs.getInt("current_visitors");

				return currentVisitors + requestedVisitors <= maxCapacity;
			}
		}
	}

	/**
	 * This method checks whether a park has enough available places for ordered
	 * visitors on a specific date, while keeping reserved places for unplanned
	 * visitors.
	 * 
	 * This method is useful when creating an order in advance, because the system
	 * should not use all park capacity for orders if some places are reserved for
	 * unplanned visitors.
	 * 
	 * The check is done for a specific park and a specific order date, because
	 * orders from different dates should not affect each other.
	 * 
	 * @param parkId            the park ID
	 * @param orderDate         the requested order date
	 * @param requestedVisitors the number of visitors requested in the order
	 * @return true if there is enough order capacity for the given date, false
	 *         otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean hasAvailableOrderCapacity(int parkId, java.time.LocalDate orderDate, int requestedVisitors)
			throws SQLException {
		ensureConnection();

		String sql = """
				SELECT
					p.max_capacity,
					p.places_for_unplanned_visitors,
					COALESCE(SUM(o.number_of_visitors), 0) AS ordered_visitors
				FROM park p
				LEFT JOIN `order` o
					ON p.park_id = o.park_id
					AND o.order_status = 'approved'
					AND o.order_date = ?
				WHERE p.park_id = ?
				GROUP BY p.max_capacity, p.places_for_unplanned_visitors;
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setDate(1, java.sql.Date.valueOf(orderDate));
			pstmt.setInt(2, parkId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (!rs.next()) {
					return false;
				}

				int maxCapacity = rs.getInt("max_capacity");
				int reservedForUnplanned = rs.getInt("places_for_unplanned_visitors");
				int orderedVisitors = rs.getInt("ordered_visitors");

				int allowedOrderedVisitors = maxCapacity - reservedForUnplanned;

				return orderedVisitors + requestedVisitors <= allowedOrderedVisitors;
			}
		}
	}

	/**
	 * This method checks whether a park is active.
	 * 
	 * @param parkId the park ID
	 * @return true if the park is active, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean isParkActive(int parkId) throws SQLException {
		Park park = getFullParkById(parkId);

		return park != null && park.isActive();
	}
	
	/*
	 * this method returns a list of names of all active parks
	 * 
	 * @return list of names of all active parks
	 * @throws SQLException if the query failed
	 */
	public List<String> getActiveParksNames() throws SQLException {
		ensureConnection();
		
		String sql = selectByFields(new String[] {PARK_NAME_COLUMN}, new String[] {PARK_IS_ACTIVE_COLUMN});
		
		List<String> activeParkNames = new ArrayList<>();

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, PARK_IS_ACTIVE_TRUE);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					activeParkNames.add(rs.getString(PARK_NAME_COLUMN));
				}
			}
		}

		return activeParkNames;
	}
	
	/*
	 * this method returns the id of the park corresponding to the given name
	 * 
	 * @param name of relevant park
	 * @return id of relevant park
	 * @throws SQLException if the query failed
	 */
	public int getParkIdByName(String parkName) throws SQLException {
		ensureConnection();
		
		String sql = selectByFields(new String[] {PARK_ID_COLUMN}, new String[] {PARK_NAME_COLUMN});
		int parkId = -1;
		
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, parkName);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					parkId = rs.getInt(PARK_ID_COLUMN);
				}
			}
		}
		
		return parkId;
	}
}