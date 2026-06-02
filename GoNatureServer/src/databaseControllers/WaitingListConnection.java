package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the DB connector used when working with the waiting_list table.
 * 
 * The class is implemented as a Singleton, so the server will use only one
 * database connection object for the waiting list during runtime.
 * 
 * According to the system story, when there is no available place for a requested
 * order, the visitor can be added to a waiting list. When a place becomes
 * available, the first waiting request in the queue receives an offer that must
 * be confirmed within a limited time.
 */
public class WaitingListConnection extends AbstractDBConnection {

	/**
	 * The single instance of WaitingListConnection.
	 */
	private static WaitingListConnection instance;

	/**
	 * The waiting ID column.
	 */
	private final String WAITING_ID = "waiting_id";

	/**
	 * The subscriber ID column.
	 */
	private final String SUBSCRIBER_ID = "subscriber_id";

	/**
	 * The park ID column.
	 */
	private final String PARK_ID = "park_id";

	/**
	 * The requested order date column.
	 */
	private final String REQUESTED_ORDER_DATE = "requested_order_date";

	/**
	 * The number of visitors column.
	 */
	private final String NUMBER_OF_VISITORS = "number_of_visitors";

	/**
	 * The queue position column.
	 */
	private final String QUEUE_POSITION = "queue_position";

	/**
	 * The waiting status column.
	 */
	private final String WAITING_STATUS = "waiting_status";

	/**
	 * The offered time column.
	 */
	private final String OFFERED_AT = "offered_at";

	/**
	 * The offer expiration time column.
	 */
	private final String OFFER_EXPIRES_AT = "offer_expires_at";

	/**
	 * The created time column.
	 */
	private final String CREATED_AT = "created_at";

	/**
	 * Waiting status value.
	 */
	private final String WAITING = "waiting";

	/**
	 * Offered status value.
	 */
	private final String OFFERED = "offered";

	/**
	 * Confirmed status value.
	 */
	private final String CONFIRMED = "confirmed";

	/**
	 * Expired status value.
	 */
	private final String EXPIRED = "expired";

	/**
	 * Cancelled status value.
	 */
	private final String CANCELLED = "cancelled";

	/**
	 * Private constructor for Singleton.
	 * 
	 * @throws SQLException if the database connection fails
	 */
	private WaitingListConnection() throws SQLException {
		connect();
	}

	/**
	 * Returns the single instance of WaitingListConnection.
	 * 
	 * @return the only WaitingListConnection instance
	 * @throws SQLException if creating the database connection fails
	 */
	public static WaitingListConnection getInstance() throws SQLException {
		if (instance == null || instance.conn == null || instance.conn.isClosed()) {
			instance = new WaitingListConnection();
		}

		return instance;
	}

	/**
	 * Returns the table name used by this DB connector.
	 * 
	 * @return the waiting_list table name
	 */
	@Override
	protected String getTableName() {
		return ConstantsDBTableNames.WAITING_LIST;
	}

	/**
	 * Checks that the database connection is open.
	 * 
	 * @throws SQLException if reconnecting to the database fails
	 */
	private void ensureConnection() throws SQLException {
		if (conn == null || conn.isClosed()) {
			connect();
		}
	}

	/**
	 * Checks whether the waiting status is valid.
	 * 
	 * @param waitingStatus the waiting status
	 * @return true if valid, false otherwise
	 */
	private boolean isValidWaitingStatus(String waitingStatus) {
		return WAITING.equals(waitingStatus) || OFFERED.equals(waitingStatus) || CONFIRMED.equals(waitingStatus)
				|| EXPIRED.equals(waitingStatus) || CANCELLED.equals(waitingStatus);
	}

	/**
	 * Adds a subscriber request to the waiting list.
	 * 
	 * The method calculates the next queue position for the same park and requested
	 * date, then inserts a new waiting request with waiting status.
	 * 
	 * The method uses insertFields from AbstractDBConnection.
	 * 
	 * @param subscriberId       the subscriber ID
	 * @param parkId             the park ID
	 * @param requestedOrderDate the requested visit date and time
	 * @param numberOfVisitors   the number of visitors requested
	 * @return the created waiting ID, or -1 if the request is invalid
	 * @throws SQLException if the insert or select query fails
	 */
	public int addToWaitingList(int subscriberId, int parkId, LocalDateTime requestedOrderDate, int numberOfVisitors)
			throws SQLException {

		ensureConnection();

		if (subscriberId <= 0 || parkId <= 0 || requestedOrderDate == null || numberOfVisitors <= 0) {
			return -1;
		}

		if (!SubscriberConnection.getInstance().subscriberExists(subscriberId)) {
			return -1;
		}

		if (!ParkConnection.getInstance().isActivePark(parkId)) {
			return -1;
		}

		if (hasActiveWaitingRequest(subscriberId, parkId, requestedOrderDate)) {
			return -1;
		}

		int queuePosition = getNextQueuePosition(parkId, requestedOrderDate);
		LocalDateTime createdAt = LocalDateTime.now();

		List<String> columnNames = new ArrayList<>();
		List<Object> values = new ArrayList<>();

		columnNames.add(SUBSCRIBER_ID);
		values.add(subscriberId);

		columnNames.add(PARK_ID);
		values.add(parkId);

		columnNames.add(REQUESTED_ORDER_DATE);
		values.add(Timestamp.valueOf(requestedOrderDate));

		columnNames.add(NUMBER_OF_VISITORS);
		values.add(numberOfVisitors);

		columnNames.add(QUEUE_POSITION);
		values.add(queuePosition);

		columnNames.add(WAITING_STATUS);
		values.add(WAITING);

		columnNames.add(CREATED_AT);
		values.add(Timestamp.valueOf(createdAt));

		insertFields(columnNames.toArray(new String[columnNames.size()]), values);

		return getCreatedWaitingId(subscriberId, parkId, requestedOrderDate, queuePosition, createdAt);
	}

	/**
	 * Returns the next queue position for a specific park and requested date.
	 * 
	 * Waiting and offered requests are still considered active in the queue.
	 * 
	 * @param parkId             the park ID
	 * @param requestedOrderDate the requested visit date and time
	 * @return the next queue position
	 * @throws SQLException if the select query fails
	 */
	private int getNextQueuePosition(int parkId, LocalDateTime requestedOrderDate) throws SQLException {
		ensureConnection();

		if (parkId <= 0 || requestedOrderDate == null) {
			return -1;
		}

		String sql = """
				SELECT COALESCE(MAX(queue_position), 0) + 1 AS next_position
				FROM waiting_list
				WHERE park_id = ?
					AND requested_order_date = ?
					AND waiting_status IN ('waiting', 'offered');
				""";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, parkId);
		pstmt.setTimestamp(2, Timestamp.valueOf(requestedOrderDate));

		java.sql.ResultSet rs = pstmt.executeQuery();

		int nextPosition = 1;

		if (rs.next()) {
			nextPosition = rs.getInt("next_position");
		}

		rs.close();
		pstmt.close();

		return nextPosition;
	}

	/**
	 * Finds the waiting ID that was created after inserting a new waiting request.
	 * 
	 * The method uses selectByFields from AbstractDBConnection. MAX(waiting_id) is
	 * used because the new waiting request should be the latest matching request.
	 * 
	 * @param subscriberId       the subscriber ID
	 * @param parkId             the park ID
	 * @param requestedOrderDate the requested visit date and time
	 * @param queuePosition      the queue position
	 * @param createdAt          the created time
	 * @return the created waiting ID, or -1 if no matching request was found
	 * @throws SQLException if the select query fails
	 */
	private int getCreatedWaitingId(int subscriberId, int parkId, LocalDateTime requestedOrderDate, int queuePosition,
			LocalDateTime createdAt) throws SQLException {

		ensureConnection();

		if (subscriberId <= 0 || parkId <= 0 || requestedOrderDate == null || queuePosition <= 0
				|| createdAt == null) {
			return -1;
		}

		List<String> keyColumns = new ArrayList<>();
		List<Object> keyValues = new ArrayList<>();

		keyColumns.add(SUBSCRIBER_ID);
		keyValues.add(subscriberId);

		keyColumns.add(PARK_ID);
		keyValues.add(parkId);

		keyColumns.add(REQUESTED_ORDER_DATE);
		keyValues.add(Timestamp.valueOf(requestedOrderDate));

		keyColumns.add(QUEUE_POSITION);
		keyValues.add(queuePosition);

		keyColumns.add(WAITING_STATUS);
		keyValues.add(WAITING);

		keyColumns.add(CREATED_AT);
		keyValues.add(Timestamp.valueOf(createdAt));

		String sql = selectByFields(new String[] { "MAX(" + WAITING_ID + ") AS " + WAITING_ID },
				keyColumns.toArray(new String[keyColumns.size()]));

		PreparedStatement pstmt = conn.prepareStatement(sql);

		for (int i = 0; i < keyValues.size(); i++) {
			pstmt.setObject(i + 1, keyValues.get(i));
		}

		java.sql.ResultSet rs = pstmt.executeQuery();

		int waitingId = -1;

		if (rs.next()) {
			waitingId = rs.getInt(WAITING_ID);
		}

		rs.close();
		pstmt.close();

		return waitingId;
	}

	/**
	 * Checks whether the subscriber already has an active waiting request for the
	 * same park and requested date.
	 * 
	 * Active means waiting or offered.
	 * 
	 * @param subscriberId       the subscriber ID
	 * @param parkId             the park ID
	 * @param requestedOrderDate the requested visit date and time
	 * @return true if an active waiting request exists, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean hasActiveWaitingRequest(int subscriberId, int parkId, LocalDateTime requestedOrderDate)
			throws SQLException {

		ensureConnection();

		if (subscriberId <= 0 || parkId <= 0 || requestedOrderDate == null) {
			return false;
		}

		String sql = """
				SELECT waiting_id
				FROM waiting_list
				WHERE subscriber_id = ?
					AND park_id = ?
					AND requested_order_date = ?
					AND waiting_status IN ('waiting', 'offered')
				LIMIT 1;
				""";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, subscriberId);
		pstmt.setInt(2, parkId);
		pstmt.setTimestamp(3, Timestamp.valueOf(requestedOrderDate));

		java.sql.ResultSet rs = pstmt.executeQuery();

		boolean exists = rs.next();

		rs.close();
		pstmt.close();

		return exists;
	}

	/**
	 * Returns waiting request data by waiting ID.
	 * 
	 * The returned ArrayList contains:
	 * waiting_id, subscriber_id, park_id, requested_order_date, number_of_visitors,
	 * queue_position, waiting_status, offered_at, offer_expires_at, created_at.
	 * 
	 * @param waitingId the waiting ID
	 * @return waiting request data, or an empty ArrayList if not found
	 * @throws SQLException if the select query fails
	 */
	public ArrayList<Object> getWaitingRequestById(int waitingId) throws SQLException {
		ensureConnection();

		ArrayList<Object> waitingData = new ArrayList<>();

		if (waitingId <= 0) {
			return waitingData;
		}

		String[] columnNames = {
				WAITING_ID,
				SUBSCRIBER_ID,
				PARK_ID,
				REQUESTED_ORDER_DATE,
				NUMBER_OF_VISITORS,
				QUEUE_POSITION,
				WAITING_STATUS,
				OFFERED_AT,
				OFFER_EXPIRES_AT,
				CREATED_AT
		};

		String[] keyColumns = {
				WAITING_ID
		};

		String sql = selectByFields(columnNames, keyColumns);

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, waitingId);

		java.sql.ResultSet rs = pstmt.executeQuery();

		if (rs.next()) {
			waitingData.add(rs.getInt(WAITING_ID));
			waitingData.add(rs.getInt(SUBSCRIBER_ID));
			waitingData.add(rs.getInt(PARK_ID));
			waitingData.add(rs.getTimestamp(REQUESTED_ORDER_DATE).toLocalDateTime());
			waitingData.add(rs.getInt(NUMBER_OF_VISITORS));
			waitingData.add(rs.getInt(QUEUE_POSITION));
			waitingData.add(rs.getString(WAITING_STATUS));

			if (rs.getObject(OFFERED_AT) != null) {
				waitingData.add(rs.getTimestamp(OFFERED_AT).toLocalDateTime());
			} else {
				waitingData.add(null);
			}

			if (rs.getObject(OFFER_EXPIRES_AT) != null) {
				waitingData.add(rs.getTimestamp(OFFER_EXPIRES_AT).toLocalDateTime());
			} else {
				waitingData.add(null);
			}

			waitingData.add(rs.getTimestamp(CREATED_AT).toLocalDateTime());
		}

		rs.close();
		pstmt.close();

		return waitingData;
	}

	/**
	 * Returns the first waiting request in the queue for a specific park and
	 * requested date.
	 * 
	 * This is used when a place becomes available and the system needs to offer it
	 * to the next subscriber in the queue.
	 * 
	 * @param parkId             the park ID
	 * @param requestedOrderDate the requested visit date and time
	 * @return waiting request data, or an empty ArrayList if no waiting request exists
	 * @throws SQLException if the select query fails
	 */
	public ArrayList<Object> getNextWaitingRequest(int parkId, LocalDateTime requestedOrderDate) throws SQLException {
		ensureConnection();

		ArrayList<Object> waitingData = new ArrayList<>();

		if (parkId <= 0 || requestedOrderDate == null) {
			return waitingData;
		}

		String[] columnNames = {
				WAITING_ID,
				SUBSCRIBER_ID,
				PARK_ID,
				REQUESTED_ORDER_DATE,
				NUMBER_OF_VISITORS,
				QUEUE_POSITION,
				WAITING_STATUS,
				OFFERED_AT,
				OFFER_EXPIRES_AT,
				CREATED_AT
		};

		String[] keyColumns = {
				PARK_ID,
				REQUESTED_ORDER_DATE,
				WAITING_STATUS
		};

		String sql = selectByFields(columnNames, keyColumns);
		sql = sql.substring(0, sql.length() - 1) + " ORDER BY " + QUEUE_POSITION + " LIMIT 1;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, parkId);
		pstmt.setTimestamp(2, Timestamp.valueOf(requestedOrderDate));
		pstmt.setString(3, WAITING);

		java.sql.ResultSet rs = pstmt.executeQuery();

		if (rs.next()) {
			waitingData.add(rs.getInt(WAITING_ID));
			waitingData.add(rs.getInt(SUBSCRIBER_ID));
			waitingData.add(rs.getInt(PARK_ID));
			waitingData.add(rs.getTimestamp(REQUESTED_ORDER_DATE).toLocalDateTime());
			waitingData.add(rs.getInt(NUMBER_OF_VISITORS));
			waitingData.add(rs.getInt(QUEUE_POSITION));
			waitingData.add(rs.getString(WAITING_STATUS));

			if (rs.getObject(OFFERED_AT) != null) {
				waitingData.add(rs.getTimestamp(OFFERED_AT).toLocalDateTime());
			} else {
				waitingData.add(null);
			}

			if (rs.getObject(OFFER_EXPIRES_AT) != null) {
				waitingData.add(rs.getTimestamp(OFFER_EXPIRES_AT).toLocalDateTime());
			} else {
				waitingData.add(null);
			}

			waitingData.add(rs.getTimestamp(CREATED_AT).toLocalDateTime());
		}

		rs.close();
		pstmt.close();

		return waitingData;
	}

	/**
	 * Creates an offer for a waiting request.
	 * 
	 * The waiting status changes from waiting to offered, offered_at is set to now,
	 * and offer_expires_at is set to one hour from now.
	 * 
	 * @param waitingId the waiting ID
	 * @return true if the offer was created successfully, false otherwise
	 * @throws SQLException if the select or update query fails
	 */
	public boolean offerPlace(int waitingId) throws SQLException {
		ensureConnection();

		if (waitingId <= 0) {
			return false;
		}

		ArrayList<Object> waitingData = getWaitingRequestById(waitingId);

		if (waitingData.isEmpty()) {
			return false;
		}

		if (!WAITING.equals(waitingData.get(6))) {
			return false;
		}

		LocalDateTime offeredAt = LocalDateTime.now();
		LocalDateTime offerExpiresAt = offeredAt.plusHours(1);

		List<String> columnNames = new ArrayList<>();
		List<Object> newValues = new ArrayList<>();
		List<String> keyColumns = new ArrayList<>();
		List<Object> keyValues = new ArrayList<>();

		columnNames.add(WAITING_STATUS);
		newValues.add(OFFERED);

		columnNames.add(OFFERED_AT);
		newValues.add(Timestamp.valueOf(offeredAt));

		columnNames.add(OFFER_EXPIRES_AT);
		newValues.add(Timestamp.valueOf(offerExpiresAt));

		keyColumns.add(WAITING_ID);
		keyValues.add(waitingId);

		updateFields(columnNames.toArray(new String[columnNames.size()]), newValues,
				keyColumns.toArray(new String[keyColumns.size()]), keyValues);

		return true;
	}

	/**
	 * Confirms an offered waiting request.
	 * 
	 * The offer can be confirmed only if it is still in offered status and the offer
	 * expiration time has not passed.
	 * 
	 * @param waitingId the waiting ID
	 * @return true if the offer was confirmed successfully, false otherwise
	 * @throws SQLException if the select or update query fails
	 */
	public boolean confirmOffer(int waitingId) throws SQLException {
		ensureConnection();

		if (waitingId <= 0) {
			return false;
		}

		ArrayList<Object> waitingData = getWaitingRequestById(waitingId);

		if (waitingData.isEmpty()) {
			return false;
		}

		if (!OFFERED.equals(waitingData.get(6))) {
			return false;
		}

		LocalDateTime offerExpiresAt = (LocalDateTime) waitingData.get(8);

		if (offerExpiresAt == null || LocalDateTime.now().isAfter(offerExpiresAt)) {
			expireOffer(waitingId);
			return false;
		}

		return updateWaitingStatus(waitingId, CONFIRMED);
	}

	/**
	 * Expires an offered waiting request.
	 * 
	 * @param waitingId the waiting ID
	 * @return true if the offer was expired successfully, false otherwise
	 * @throws SQLException if the update query fails
	 */
	public boolean expireOffer(int waitingId) throws SQLException {
		return updateWaitingStatus(waitingId, EXPIRED);
	}

	/**
	 * Cancels a waiting request.
	 * 
	 * @param waitingId the waiting ID
	 * @return true if the waiting request was cancelled successfully, false otherwise
	 * @throws SQLException if the update query fails
	 */
	public boolean cancelWaitingRequest(int waitingId) throws SQLException {
		return updateWaitingStatus(waitingId, CANCELLED);
	}

	/**
	 * Updates waiting status.
	 * 
	 * The method uses updateFields from AbstractDBConnection.
	 * 
	 * @param waitingId     the waiting ID
	 * @param waitingStatus the new waiting status
	 * @return true if the update request was valid, false otherwise
	 * @throws SQLException if the update query fails
	 */
	private boolean updateWaitingStatus(int waitingId, String waitingStatus) throws SQLException {
		ensureConnection();

		if (waitingId <= 0 || waitingStatus == null || !isValidWaitingStatus(waitingStatus)) {
			return false;
		}

		List<String> columnNames = new ArrayList<>();
		List<Object> newValues = new ArrayList<>();
		List<String> keyColumns = new ArrayList<>();
		List<Object> keyValues = new ArrayList<>();

		columnNames.add(WAITING_STATUS);
		newValues.add(waitingStatus);

		keyColumns.add(WAITING_ID);
		keyValues.add(waitingId);

		updateFields(columnNames.toArray(new String[columnNames.size()]), newValues,
				keyColumns.toArray(new String[keyColumns.size()]), keyValues);

		return true;
	}
}