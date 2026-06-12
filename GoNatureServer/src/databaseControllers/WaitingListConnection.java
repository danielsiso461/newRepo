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
	 * Offers the newly available place to the first matching visitor in the waiting list.
	 *
	 * The method searches for the first visitor waiting for the same park and date.
	 * The visitor must request a number of visitors that can fit in the available
	 * places that were freed by a cancelled order.
	 *
	 * If a matching visitor is found, the waiting_status is changed from "waiting"
	 * to "offered", and the offer time fields are updated.
	 *
	 * @param parkId          the park ID of the cancelled order
	 * @param orderDate       the visit date of the cancelled order
	 * @param availablePlaces the number of places freed by the cancellation
	 * @return true if a waiting list request was updated to offered, false otherwise
	 * @throws SQLException if the select or update query fails
	 */
	public boolean offerFirstMatchingWaitingRequest(int parkId, java.time.LocalDate orderDate,
			int availablePlaces) throws SQLException {
		ensureConnection();

		String selectSql = """
				SELECT waiting_id
				FROM waiting_list
				WHERE park_id = ?
				  AND DATE(requested_order_date) = ?
				  AND waiting_status = 'waiting'
				  AND number_of_visitors <= ?
				ORDER BY queue_position ASC, created_at ASC
				LIMIT 1;
				""";

		Integer waitingId = null;

		try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
			pstmt.setInt(1, parkId);
			pstmt.setDate(2, java.sql.Date.valueOf(orderDate));
			pstmt.setInt(3, availablePlaces);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					waitingId = rs.getInt("waiting_id");
				}
			}
		}

		if (waitingId == null) {
			return false;
		}

		String updateSql = """
				UPDATE waiting_list
				SET waiting_status = 'offered',
				    offered_at = NOW(),
				    offer_expires_at = DATE_ADD(NOW(), INTERVAL 1 DAY)
				WHERE waiting_id = ?;
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
			pstmt.setInt(1, waitingId);

			return pstmt.executeUpdate() > 0;
		}
	}
	/**
	 * Prevents cloning of the Singleton instance.
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}