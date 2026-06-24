package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import common.WaitingListMessage;

/**
 * This class handles all database operations related to the waiting_list table.
 *
 * The waiting list is used when a visitor cannot immediately create an order
 * because the requested park/date does not have enough available capacity.
 */
public class WaitingListConnection extends AbstractDBConnection {
	/**
	 * the connection's instance
	 */
	private static WaitingListConnection instance = null;
	/**
	 * the table's name
	 */
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
	 * Checks whether the subscriber already has an active waiting list request for
	 * the same park, date, time and number of visitors.
	 *
	 * Active requests are requests that are still waiting or already offered.
	 *
	 * @param subscriberId       the subscriber ID of the visitor
	 * @param parkId             the requested park ID
	 * @param requestedOrderDate the requested visit date and time
	 * @param numberOfVisitors   the requested number of visitors
	 * @return true if an active matching request already exists, false otherwise
	 * @throws SQLException if the select query fails
	 */
	private boolean activeWaitingRequestExists(int subscriberId, int parkId,
			java.time.LocalDateTime requestedOrderDate, int numberOfVisitors) throws SQLException {
		ensureConnection();

		String sql = """
				SELECT waiting_id
				FROM waiting_list
				WHERE subscriber_id = ?
				  AND park_id = ?
				  AND requested_order_date = ?
				  AND number_of_visitors = ?
				  AND waiting_status IN ('waiting', 'offered')
				LIMIT 1;
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, subscriberId);
			pstmt.setInt(2, parkId);
			pstmt.setTimestamp(3, java.sql.Timestamp.valueOf(requestedOrderDate));
			pstmt.setInt(4, numberOfVisitors);

			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}
	/**
	 * Adds a visitor request to the waiting list.
	 *
	 * The request is inserted with waiting_status = "waiting".
	 * The queue position is calculated automatically according to the same park
	 * and requested order date.
	 *
	 * If the subscriber already has an active waiting list request for the same
	 * park, date, time and number of visitors, the method does not insert another
	 * row and returns -1.
	 *
	 * @param subscriberId       the subscriber ID of the visitor
	 * @param parkId             the requested park ID
	 * @param requestedOrderDate the requested visit date and time
	 * @param numberOfVisitors   the requested number of visitors
	 * @return the queue position assigned to the visitor, or -1 if a duplicate active request exists
	 * @throws SQLException if the insert query fails
	 */
	public int addToWaitingList(int subscriberId, int parkId, java.time.LocalDateTime requestedOrderDate,
			int numberOfVisitors) throws SQLException {
		ensureConnection();

		// Do not allow the same subscriber to join the waiting list more than once
		// for the exact same park, date, time and number of visitors.
		if (activeWaitingRequestExists(subscriberId, parkId, requestedOrderDate, numberOfVisitors)) {
			return -1;
		}

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
	 * Rejects an offered waiting list request and offers the available place to the
	 * next matching visitor in the waiting list.
	 *
	 * The method first loads the offered waiting list request, because its park,
	 * requested date and number of visitors are needed in order to continue to the
	 * next visitor in the same waiting list.
	 *
	 * After the current request is changed to "rejected", the method searches for
	 * the next matching request and changes it to "offered".
	 *
	 * @param waitingId the waiting list request ID that should be rejected
	 * @return true if the offered request was rejected, false otherwise
	 * @throws SQLException if the select or update query fails
	 */
	public boolean rejectWaitingOfferAndOfferNext(int waitingId) throws SQLException {
		ensureConnection();

		Integer parkId = null;
		java.time.LocalDate orderDate = null;
		Integer availablePlaces = null;

		String selectSql = """
				SELECT
				    park_id,
				    DATE(requested_order_date) AS order_date,
				    number_of_visitors
				FROM waiting_list
				WHERE waiting_id = ?
				  AND waiting_status = 'offered';
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
			pstmt.setInt(1, waitingId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					parkId = rs.getInt("park_id");
					orderDate = rs.getDate("order_date").toLocalDate();
					availablePlaces = rs.getInt("number_of_visitors");
				}
			}
		}

		if (parkId == null || orderDate == null || availablePlaces == null) {
			return false;
		}

		String updateSql = """
				UPDATE waiting_list
				SET waiting_status = 'cancelled'
				WHERE waiting_id = ?
				  AND waiting_status = 'offered';
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
			pstmt.setInt(1, waitingId);

			if (pstmt.executeUpdate() == 0) {
				return false;
			}
		}

		/*
		 * After the current offer is rejected, the same available place can be offered
		 * to the next matching visitor in the waiting list.
		 */
		offerFirstMatchingWaitingRequest(parkId, orderDate, availablePlaces);

		return true;
	}
	/**
	 * Expires all waiting list offers whose expiration time has passed and offers
	 * the available places to the next matching visitors in the waiting list.
	 *
	 * The method finds every request with waiting_status = "offered" and
	 * offer_expires_at that is earlier than the current time.
	 *
	 * Each expired request is changed to "expired", and then the same available
	 * place is offered to the next matching visitor in the waiting list.
	 *
	 * @return the number of offers that were changed to expired
	 * @throws SQLException if the select or update query fails
	 */
	public int expireOldOffersAndOfferNext() throws SQLException {
		ensureConnection();

		int expiredCount = 0;

		String selectSql = """
				SELECT
				    waiting_id,
				    park_id,
				    DATE(requested_order_date) AS order_date,
				    number_of_visitors
				FROM waiting_list
				WHERE waiting_status = 'offered'
				  AND offer_expires_at IS NOT NULL
				  AND offer_expires_at < NOW();
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(selectSql);
				ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				int waitingId = rs.getInt("waiting_id");
				int parkId = rs.getInt("park_id");
				java.time.LocalDate orderDate = rs.getDate("order_date").toLocalDate();
				int availablePlaces = rs.getInt("number_of_visitors");

				String updateSql = """
						UPDATE waiting_list
						SET waiting_status = 'expired'
						WHERE waiting_id = ?
						  AND waiting_status = 'offered';
						""";

				try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
					updateStmt.setInt(1, waitingId);

					if (updateStmt.executeUpdate() > 0) {
						expiredCount++;

						/*
						 * After the current offer expires, the same available place can be offered to
						 * the next matching visitor in the waiting list.
						 */
						offerFirstMatchingWaitingRequest(parkId, orderDate, availablePlaces);
					}
				}
			}
		}

		return expiredCount;
	}
	/**
	 * Accepts an offered waiting list request.
	 *
	 * The method changes the waiting_status from "offered" to "accepted".
	 * It only updates requests that are currently offered, so an already rejected,
	 * expired or accepted request cannot be accepted again.
	 *
	 * @param waitingId the waiting list request ID that should be accepted
	 * @return true if the offered request was accepted, false otherwise
	 * @throws SQLException if the update query fails
	 */
	public boolean acceptWaitingOffer(int waitingId) throws SQLException {
		ensureConnection();

		String sql = """
				UPDATE waiting_list
				SET waiting_status = 'confirmed'
				WHERE waiting_id = ?
				  AND waiting_status = 'offered';
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, waitingId);

			return pstmt.executeUpdate() > 0;
		}
	}
	
	/**
	 * Returns all active waiting list requests for a specific subscriber.
	 *
	 * The method returns requests with status "waiting" and "offered".
	 * Requests with status "offered" are returned only if the offer did not expire.
	 *
	 * @param subscriberId the subscriber ID
	 * @return a list of active waiting list requests
	 * @throws SQLException if the select query fails
	 */
	public List<WaitingListMessage> getOfferedRequestsForSubscriber(int subscriberId) throws SQLException {
		ensureConnection();

		List<WaitingListMessage> offers = new ArrayList<>();

		String sql = """
				SELECT
				    wl.waiting_id,
				    wl.subscriber_id,
				    wl.park_id,
				    wl.requested_order_date,
				    wl.number_of_visitors,
				    wl.queue_position,
				    wl.waiting_status,
				    s.subscriber_email,
				    s.subscriber_phone
				FROM waiting_list wl
				LEFT JOIN subscriber s
				    ON wl.subscriber_id = s.subscriber_id
				WHERE wl.subscriber_id = ?
				  AND (
				        wl.waiting_status = 'waiting'
				        OR (
				            wl.waiting_status = 'offered'
				            AND (wl.offer_expires_at IS NULL OR wl.offer_expires_at >= NOW())
				        )
				  )
				ORDER BY wl.requested_order_date ASC, wl.queue_position ASC;
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, subscriberId);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					WaitingListMessage waitingListMessage = new WaitingListMessage(
							rs.getInt("subscriber_id"),
							rs.getInt("park_id"),
							rs.getTimestamp("requested_order_date").toLocalDateTime(),
							rs.getInt("number_of_visitors")
					);

					waitingListMessage.setWaitingId(rs.getInt("waiting_id"));
					waitingListMessage.setQueuePosition(rs.getInt("queue_position"));
					waitingListMessage.setWaitingStatus(rs.getString("waiting_status"));
					waitingListMessage.setSubscriberEmail(rs.getString("subscriber_email"));
					waitingListMessage.setSubscriberPhone(rs.getString("subscriber_phone"));

					offers.add(waitingListMessage);
				}
			}
		}

		return offers;
	}
	/**
	 * Prevents cloning of the Singleton instance.
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}