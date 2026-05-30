package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
	 * This method returns all active parks.
	 * 
	 * Active parks are parks whose is_active value is 1.
	 * 
	 * @return a ResultSet containing all active parks
	 * @throws SQLException if the select query fails
	 */
	public ResultSet getAllActiveParks() throws SQLException {
		String sql = "SELECT * FROM park WHERE is_active = 1;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		return pstmt.executeQuery();
	}

	/**
	 * This method returns a park by its park ID.
	 * 
	 * @param parkId the park ID
	 * @return a ResultSet containing the park data if the park exists
	 * @throws SQLException if the select query fails
	 */
	public ResultSet getParkById(int parkId) throws SQLException {
		String sql = "SELECT * FROM park WHERE park_id = ?;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, parkId);

		return pstmt.executeQuery();
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
		String sql = "SELECT full_entry_price FROM park WHERE park_id = ?;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, parkId);

		ResultSet rs = pstmt.executeQuery();

		if (rs.next()) {
			return rs.getDouble("full_entry_price");
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
		String sql = "SELECT promotions FROM park WHERE park_id = ?;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, parkId);

		ResultSet rs = pstmt.executeQuery();

		if (rs.next()) {
			return rs.getInt("promotions") == 1;
		}

		return false;
	}

	/**
	 * This method checks whether a park has enough available capacity for the
	 * requested number of visitors.
	 * 
	 * The method compares the park maximum capacity with the number of visitors that
	 * are currently inside the park, according to visits that have not been closed
	 * yet. A visit is considered open if its exit_time is null.
	 * 
	 * @param parkId            the park ID
	 * @param requestedVisitors the number of visitors that want to enter the park
	 * @return true if the park has enough available capacity, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean hasAvailableCapacity(int parkId, int requestedVisitors) throws SQLException {
		String sql = "SELECT max_capacity FROM park WHERE park_id = ?;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, parkId);

		ResultSet rs = pstmt.executeQuery();

		if (!rs.next()) {
			return false;
		}

		int maxCapacity = rs.getInt("max_capacity");

		String currentSql = "SELECT COALESCE(SUM(actual_number_of_visitors), 0) AS current_visitors "
				+ "FROM visit "
				+ "WHERE park_id = ? AND exit_time IS NULL;";

		PreparedStatement currentStmt = conn.prepareStatement(currentSql);
		currentStmt.setInt(1, parkId);

		ResultSet currentRs = currentStmt.executeQuery();

		int currentVisitors = 0;

		if (currentRs.next()) {
			currentVisitors = currentRs.getInt("current_visitors");
		}

		return currentVisitors + requestedVisitors <= maxCapacity;
	}
}