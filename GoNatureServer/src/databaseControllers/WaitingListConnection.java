package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class handles all database operations related to the waiting_list table.
 *
 * The waiting list is used when a visitor cannot immediately create an order
 * because the requested park/date does not have enough available capacity.
 */
public class WaitingListConnection extends AbstractDBConnection {

	private static WaitingListConnection instance = null;

	private static final String TABLE_NAME = "waiting_list";

	/**
	 * Private constructor for Singleton usage.
	 *
	 * @throws SQLException if the database connection cannot be created
	 */
	private WaitingListConnection() throws SQLException {
		connect();
	}

	/**
	 * Returns the single instance of WaitingListConnection.
	 *
	 * @return the WaitingListConnection instance
	 * @throws SQLException if the database connection cannot be created
	 */
	public static WaitingListConnection getInstance() throws SQLException {
		if (instance == null) {
			instance = new WaitingListConnection();
		}

		return instance;
	}

	/**
	 * Returns the table name used by this database connector.
	 *
	 * @return the waiting_list table name
	 */
	@Override
	protected String getTableName() {
		return TABLE_NAME;
	}

	/**
	 * Makes sure the database connection is open before running a query.
	 *
	 * @throws SQLException if the connection cannot be opened
	 */
	private void ensureConnection() throws SQLException {
		if (conn == null || conn.isClosed()) {
			connect();
		}
	}

	/**
	 * Calculates the next queue position for a specific park and requested date.
	 *
	 * The next position is calculated by taking the current maximum queue_position
	 * for the same park and requested date and adding 1.
	 *
	 * @param parkId             the requested park ID
	 * @param requestedOrderDate the requested visit date and time
	 * @return the next available queue position
	 * @throws SQLException if the select query fails
	 */
	public int getNextQueuePosition(int parkId, java.time.LocalDateTime requestedOrderDate) throws SQLException {
		ensureConnection();

		String sql = """
				SELECT COALESCE(MAX(queue_position), 0) + 1 AS next_position
				FROM waiting_list
				WHERE park_id = ?
				  AND requested_order_date = ?
				  AND waiting_status IN ('waiting', 'offered');
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, parkId);
			pstmt.setTimestamp(2, java.sql.Timestamp.valueOf(requestedOrderDate));

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("next_position");
				}
			}
		}

		return 1;
	}

	/**
	 * Adds a visitor request to the waiting list.
	 *
	 * The request is inserted with waiting_status = "waiting".
	 * The queue position is calculated automatically according to the same park
	 * and requested order date.
	 *
	 * @param subscriberId       the subscriber ID of the visitor
	 * @param parkId             the requested park ID
	 * @param requestedOrderDate the requested visit date and time
	 * @param numberOfVisitors   the requested number of visitors
	 * @return the queue position assigned to the visitor
	 * @throws SQLException if the insert query fails
	 */
	public int addToWaitingList(int subscriberId, int parkId, java.time.LocalDateTime requestedOrderDate,
			int numberOfVisitors) throws SQLException {
		ensureConnection();

		int queuePosition = getNextQueuePosition(parkId, requestedOrderDate);

		String sql = """
				INSERT INTO waiting_list
				(
					subscriber_id,
					park_id,
					requested_order_date,
					number_of_visitors,
					queue_position,
					waiting_status,
					created_at
				)
				VALUES (?, ?, ?, ?, ?, 'waiting', NOW());
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, subscriberId);
			pstmt.setInt(2, parkId);
			pstmt.setTimestamp(3, java.sql.Timestamp.valueOf(requestedOrderDate));
			pstmt.setInt(4, numberOfVisitors);
			pstmt.setInt(5, queuePosition);

			pstmt.executeUpdate();
		}

		return queuePosition;
	}

	/**
	 * Prevents cloning of the Singleton instance.
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}