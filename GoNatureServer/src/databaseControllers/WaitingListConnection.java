package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import common.WaitingListMessage;

/*
 * This class handles all database operations related to the waiting_list table.
 *
 * The waiting list is used when a visitor cannot immediately create an order
 * because the requested park/date does not have enough available capacity.
 */
public class WaitingListConnection extends AbstractDBConnection {

	private static WaitingListConnection instance = null;

	private static final String TABLE_NAME = "waiting_list";

	private static final String WAITING_ID = "waiting_id";
	private static final String SUBSCRIBER_ID = "subscriber_id";
	private static final String PARK_ID = "park_id";
	private static final String REQUESTED_ORDER_DATE = "requested_order_date";
	private static final String NUMBER_OF_VISITORS = "number_of_visitors";
	private static final String QUEUE_POSITION = "queue_position";
	private static final String WAITING_STATUS = "waiting_status";
	private static final String CREATED_AT = "created_at";
	private static final String OFFERED_AT = "offered_at";
	private static final String OFFER_EXPIRES_AT = "offer_expires_at";

	private static final String STATUS_WAITING = "waiting";
	private static final String STATUS_OFFERED = "offered";
	private static final String STATUS_CANCELLED = "cancelled";
	private static final String STATUS_EXPIRED = "expired";
	private static final String STATUS_CONFIRMED = "confirmed";

	private WaitingListConnection() throws SQLException {
		connect();
	}

	public static WaitingListConnection getInstance() throws SQLException {
		if (instance == null || instance.conn == null || instance.conn.isClosed()) {
			instance = new WaitingListConnection();
		}

		return instance;
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
	}

	public int getNextQueuePosition(int parkId, LocalDateTime requestedOrderDate)
			throws SQLException {

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
			pstmt.setTimestamp(2, Timestamp.valueOf(requestedOrderDate));

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("next_position");
				}
			}
		}

		return 1;
	}

	private boolean waitingRequestExistsByStatus(int subscriberId, int parkId,
			LocalDateTime requestedOrderDate, int numberOfVisitors,
			String waitingStatus) throws SQLException {

		ensureConnection();

		String sql = selectByFields(
				new String[] {
						WAITING_ID
				},
				new String[] {
						SUBSCRIBER_ID,
						PARK_ID,
						REQUESTED_ORDER_DATE,
						NUMBER_OF_VISITORS,
						WAITING_STATUS
				}
		);

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, subscriberId);
			pstmt.setInt(2, parkId);
			pstmt.setTimestamp(3, Timestamp.valueOf(requestedOrderDate));
			pstmt.setInt(4, numberOfVisitors);
			pstmt.setString(5, waitingStatus);

			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	private boolean activeWaitingRequestExists(int subscriberId, int parkId,
			LocalDateTime requestedOrderDate, int numberOfVisitors)
			throws SQLException {

		return waitingRequestExistsByStatus(
				subscriberId,
				parkId,
				requestedOrderDate,
				numberOfVisitors,
				STATUS_WAITING
		) || waitingRequestExistsByStatus(
				subscriberId,
				parkId,
				requestedOrderDate,
				numberOfVisitors,
				STATUS_OFFERED
		);
	}

	public int addToWaitingList(int subscriberId, int parkId,
			LocalDateTime requestedOrderDate, int numberOfVisitors)
			throws SQLException {

		ensureConnection();

		if (activeWaitingRequestExists(subscriberId, parkId, requestedOrderDate,
				numberOfVisitors)) {
			return -1;
		}

		int queuePosition = getNextQueuePosition(parkId, requestedOrderDate);

		List<Object> values = new ArrayList<>();

		values.add(subscriberId);
		values.add(parkId);
		values.add(Timestamp.valueOf(requestedOrderDate));
		values.add(numberOfVisitors);
		values.add(queuePosition);
		values.add(STATUS_WAITING);
		values.add(Timestamp.valueOf(LocalDateTime.now()));

		insertFields(
				new String[] {
						SUBSCRIBER_ID,
						PARK_ID,
						REQUESTED_ORDER_DATE,
						NUMBER_OF_VISITORS,
						QUEUE_POSITION,
						WAITING_STATUS,
						CREATED_AT
				},
				values
		);

		return queuePosition;
	}

	public boolean offerFirstMatchingWaitingRequest(int parkId,
			LocalDate orderDate, int availablePlaces) throws SQLException {

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
					waitingId = rs.getInt(WAITING_ID);
				}
			}
		}

		if (waitingId == null) {
			return false;
		}

		LocalDateTime now = LocalDateTime.now();

		return updateFields(
				new String[] {
						WAITING_STATUS,
						OFFERED_AT,
						OFFER_EXPIRES_AT
				},
				List.of(
						STATUS_OFFERED,
						Timestamp.valueOf(now),
						Timestamp.valueOf(now.plusHours(1))
				),
				new String[] {
						WAITING_ID
				},
				List.of(
						waitingId
				)
		);
	}

	public boolean rejectWaitingOfferAndOfferNext(int waitingId) throws SQLException {
		ensureConnection();

		Integer parkId = null;
		LocalDate orderDate = null;
		Integer availablePlaces = null;

		String sql = selectByFields(
				new String[] {
						PARK_ID,
						REQUESTED_ORDER_DATE,
						NUMBER_OF_VISITORS
				},
				new String[] {
						WAITING_ID,
						WAITING_STATUS
				}
		);

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, waitingId);
			pstmt.setString(2, STATUS_OFFERED);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					parkId = rs.getInt(PARK_ID);
					orderDate = rs.getTimestamp(REQUESTED_ORDER_DATE)
							.toLocalDateTime()
							.toLocalDate();
					availablePlaces = rs.getInt(NUMBER_OF_VISITORS);
				}
			}
		}

		if (parkId == null || orderDate == null || availablePlaces == null) {
			return false;
		}

		boolean rejected = updateFields(
				new String[] {
						WAITING_STATUS
				},
				List.of(
						STATUS_CANCELLED
				),
				new String[] {
						WAITING_ID,
						WAITING_STATUS
				},
				List.of(
						waitingId,
						STATUS_OFFERED
				)
		);

		if (!rejected) {
			return false;
		}

		offerFirstMatchingWaitingRequest(parkId, orderDate, availablePlaces);

		return true;
	}

	public int expireOldOffersAndOfferNext() throws SQLException {
		ensureConnection();

		int expiredCount = 0;

		String selectSql = """
				SELECT
				    waiting_id,
				    park_id,
				    requested_order_date,
				    number_of_visitors
				FROM waiting_list
				WHERE waiting_status = 'offered'
				  AND offer_expires_at IS NOT NULL
				  AND offer_expires_at < NOW();
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(selectSql);
				ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				int waitingId = rs.getInt(WAITING_ID);
				int parkId = rs.getInt(PARK_ID);
				LocalDate orderDate = rs.getTimestamp(REQUESTED_ORDER_DATE)
						.toLocalDateTime()
						.toLocalDate();
				int availablePlaces = rs.getInt(NUMBER_OF_VISITORS);

				boolean expired = updateFields(
						new String[] {
								WAITING_STATUS
						},
						List.of(
								STATUS_EXPIRED
						),
						new String[] {
								WAITING_ID,
								WAITING_STATUS
						},
						List.of(
								waitingId,
								STATUS_OFFERED
						)
				);

				if (expired) {
					expiredCount++;
					offerFirstMatchingWaitingRequest(parkId, orderDate, availablePlaces);
				}
			}
		}

		return expiredCount;
	}

	public boolean acceptWaitingOffer(int waitingId) throws SQLException {
		ensureConnection();

		return updateFields(
				new String[] {
						WAITING_STATUS
				},
				List.of(
						STATUS_CONFIRMED
				),
				new String[] {
						WAITING_ID,
						WAITING_STATUS
				},
				List.of(
						waitingId,
						STATUS_OFFERED
				)
		);
	}

	public List<WaitingListMessage> getOfferedRequestsForSubscriber(int subscriberId)
			throws SQLException {

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
							rs.getInt(SUBSCRIBER_ID),
							rs.getInt(PARK_ID),
							rs.getTimestamp(REQUESTED_ORDER_DATE).toLocalDateTime(),
							rs.getInt(NUMBER_OF_VISITORS)
					);

					waitingListMessage.setWaitingId(rs.getInt(WAITING_ID));
					waitingListMessage.setQueuePosition(rs.getInt(QUEUE_POSITION));
					waitingListMessage.setWaitingStatus(rs.getString(WAITING_STATUS));
					waitingListMessage.setSubscriberEmail(rs.getString("subscriber_email"));
					waitingListMessage.setSubscriberPhone(rs.getString("subscriber_phone"));

					offers.add(waitingListMessage);
				}
			}
		}

		return offers;
	}
	
	
	public int acceptWaitingOfferAndCreateOrder(int waitingId) throws SQLException {
		ensureConnection();

		boolean previousAutoCommit = conn.getAutoCommit();

		try {
			conn.setAutoCommit(false);

			String selectSql = selectByFieldsForUpdate(
					new String[] {
							WAITING_ID,
							SUBSCRIBER_ID,
							PARK_ID,
							REQUESTED_ORDER_DATE,
							NUMBER_OF_VISITORS,
							OFFER_EXPIRES_AT
					},
					new String[] {
							WAITING_ID,
							WAITING_STATUS
					}
			);

			Integer subscriberId = null;
			Integer parkId = null;
			LocalDateTime requestedOrderDate = null;
			Integer numberOfVisitors = null;
			LocalDateTime offerExpiresAt = null;

			try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
				pstmt.setInt(1, waitingId);
				pstmt.setString(2, STATUS_OFFERED);

				try (ResultSet rs = pstmt.executeQuery()) {
					if (rs.next()) {
						subscriberId = rs.getInt(SUBSCRIBER_ID);
						parkId = rs.getInt(PARK_ID);
						requestedOrderDate =
								rs.getTimestamp(REQUESTED_ORDER_DATE).toLocalDateTime();
						numberOfVisitors = rs.getInt(NUMBER_OF_VISITORS);

						if (rs.getTimestamp(OFFER_EXPIRES_AT) != null) {
							offerExpiresAt =
									rs.getTimestamp(OFFER_EXPIRES_AT).toLocalDateTime();
						}
					}
				}
			}

			if (subscriberId == null
					|| parkId == null
					|| requestedOrderDate == null
					|| numberOfVisitors == null) {
				conn.rollback();
				return -1;
			}

			if (offerExpiresAt != null && offerExpiresAt.isBefore(LocalDateTime.now())) {
				conn.rollback();
				return -1;
			}

			if (numberOfVisitors < 1 || numberOfVisitors > 15) {
				conn.rollback();
				return -1;
			}

			int newOrderNumber = getNextOrderNumberForWaitingAcceptedOrder();
			int confirmationCode = newOrderNumber % 100000 + 100000;

			insertFieldsInTable(
					"order",
					new String[] {
							"order_number",
							"order_date",
							"number_of_visitors",
							"confirmation_code",
							"subscriber_id",
							"date_of_placing_order",
							"park_id",
							"guide_id",
							"order_status",
							"order_type",
							"order_hour",
							"customer_id",
							"email"
					},
					List.of(
							newOrderNumber,
							java.sql.Date.valueOf(requestedOrderDate.toLocalDate()),
							numberOfVisitors,
							confirmationCode,
							subscriberId,
							java.sql.Date.valueOf(LocalDate.now()),
							parkId,
							null,
							"approved",
							"private",
							requestedOrderDate.getHour(),
							subscriberId,
							""
					)
			);

			boolean updatedWaitingRequest = updateFields(
					new String[] {
							WAITING_STATUS
					},
					List.of(
							STATUS_CONFIRMED
					),
					new String[] {
							WAITING_ID,
							WAITING_STATUS
					},
					List.of(
							waitingId,
							STATUS_OFFERED
					)
			);

			if (!updatedWaitingRequest) {
				conn.rollback();
				return -1;
			}

			conn.commit();
			return newOrderNumber;

		} catch (SQLException e) {
			conn.rollback();
			throw e;

		} finally {
			conn.setAutoCommit(previousAutoCommit);
		}
	}

	private int getNextOrderNumberForWaitingAcceptedOrder() throws SQLException {
		ensureConnection();

		String sql = """
				SELECT COALESCE(MAX(order_number), 0) + 1 AS next_order_number
				FROM `order`;
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			if (rs.next()) {
				return rs.getInt("next_order_number");
			}
		}

		return 1;
	}
	

	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}