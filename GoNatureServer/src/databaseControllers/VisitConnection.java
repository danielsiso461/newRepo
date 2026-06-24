package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import common.Visit;

/**
 * DB connector for the visit table.
 */
public class VisitConnection extends AbstractDBConnection {

	private static VisitConnection instance;

	private final String VISIT_ID = "visit_id";
	private final String ORDER_NUMBER = "order_number";
	private final String PARK_ID = "park_id";
	private final String SUBSCRIBER_ID = "subscriber_id";
	private final String VISIT_TYPE = "visit_type";
	private final String ACTUAL_NUMBER_OF_VISITORS = "actual_number_of_visitors";
	private final String ENTRY_TIME = "entry_time";
	private final String EXIT_TIME = "exit_time";
	private final String HANDLED_BY_EMPLOYEE_ID = "handled_by_employee_id";
	private final String EXIT_HANDLED_BY_EMPLOYEE_ID = "exit_handled_by_employee_id";
	private final String IDENTIFICATION_METHOD = "identification_method";

	private final String ORDER_STATUS_APPROVED = "approved";
	private final String VISIT_TYPE_ORDERED = "ordered";

	private VisitConnection() throws SQLException {
		connect();
	}

	public static VisitConnection getInstance() throws SQLException {
		if (instance == null || instance.conn == null || instance.conn.isClosed()) {
			instance = new VisitConnection();
		}

		return instance;
	}

	@Override
	public String getTableName() {
		return ConstantsDBTableNames.VISIT;
	}

	/**
	 * This method checks that the database connection is open.
	 * 
	 * @throws SQLException if reconnecting to the database fails
	 */
	public void ensureConnection() throws SQLException {
		if (conn == null || conn.isClosed()) {
			connect();
		}
	}

	private Integer getNullableInt(ResultSet rs, String columnName) throws SQLException {
		Object value = rs.getObject(columnName);

		if (value == null) {
			return null;
		}

		return rs.getInt(columnName);
	}

	private LocalDateTime getNullableDateTime(ResultSet rs, String columnName) throws SQLException {
		Timestamp timestamp = rs.getTimestamp(columnName);

		if (timestamp == null) {
			return null;
		}

		return timestamp.toLocalDateTime();
	}

	private Visit convertResultSetToVisit(ResultSet rs) throws SQLException {
		return new Visit(
				rs.getInt(VISIT_ID),
				getNullableInt(rs, ORDER_NUMBER),
				rs.getInt(PARK_ID),
				getNullableInt(rs, SUBSCRIBER_ID),
				rs.getString(VISIT_TYPE),
				rs.getInt(ACTUAL_NUMBER_OF_VISITORS),
				getNullableDateTime(rs, ENTRY_TIME),
				getNullableDateTime(rs, EXIT_TIME),
				getNullableInt(rs, HANDLED_BY_EMPLOYEE_ID),
				getNullableInt(rs, EXIT_HANDLED_BY_EMPLOYEE_ID),
				rs.getString(IDENTIFICATION_METHOD)
		);
	}

	/**
	 * Returns the current number of visitors inside a specific park.
	 *
	 * Current visitors are visits that have an entry time but no exit time yet.
	 *
	 * @param parkId the park ID
	 * @return the number of visitors currently inside the park
	 * @throws SQLException if the select query fails
	 */
	public int getCurrentVisitorsInPark(int parkId) throws SQLException {
		ensureConnection();

		String sql = """
				SELECT COALESCE(SUM(actual_number_of_visitors), 0) AS current_visitors
				FROM visit
				WHERE park_id = ?
				  AND exit_time IS NULL;
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, parkId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("current_visitors");
				}
			}
		}

		return 0;
	}

	/**
	 * Creates a new park visit using an order confirmation code.
	 *
	 * The confirmation code is used as a QR code simulation. The method checks the
	 * order step by step so the server can return a specific failure message.
	 *
	 * Return values:
	 * - positive number: created visit ID
	 * - -1: no order was found for this confirmation code
	 * - -2: the order already has an open visit
	 * - -3: invalid actual number of visitors
	 * - -4: the order belongs to another park
	 * - -5: the order is not approved
	 * - -6: the order has already been completed
	 * - -7: the order is not valid for the current date and time
	 *
	 * @param confirmationCode       the order confirmation code used as QR simulation
	 * @param parkId                 the park ID where the visitor entered
	 * @param actualNumberOfVisitors the actual number of visitors entering
	 * @param handledByEmployeeId    the employee ID that handled the entrance
	 * @param identificationMethod   the identification method used at the entrance
	 * @return the created visit ID, or a negative value if the visit cannot be created
	 * @throws SQLException if the select or insert query fails
	 */
	public int createVisitFromConfirmationCode(
			int confirmationCode,
			int parkId,
			int actualNumberOfVisitors,
			int handledByEmployeeId,
			String identificationMethod) throws SQLException {
		ensureConnection();

		String orderSql = """
				SELECT
				    o.order_number,
				    o.park_id,
				    o.subscriber_id,
				    o.number_of_visitors,
				    o.order_status,
				    o.order_date,
				    o.order_hour,
				    p.estimated_visit_duration_hours
				FROM `order` o
				JOIN park p
				    ON o.park_id = p.park_id
				WHERE o.confirmation_code = ?
				LIMIT 1;
				""";

		try (PreparedStatement orderStmt = conn.prepareStatement(orderSql)) {
			orderStmt.setInt(1, confirmationCode);

			try (ResultSet orderRs = orderStmt.executeQuery()) {
				if (!orderRs.next()) {
					return -1;
				}

				int orderNumber = orderRs.getInt(ORDER_NUMBER);
				int orderParkId = orderRs.getInt(PARK_ID);
				Integer subscriberId = getNullableInt(orderRs, SUBSCRIBER_ID);
				int orderedVisitors = orderRs.getInt("number_of_visitors");
				String orderStatus = orderRs.getString("order_status");

				if (orderParkId != parkId) {
					return -4;
				}

				if ("completed".equalsIgnoreCase(orderStatus)) {
					return -6;
				}

				if (!ORDER_STATUS_APPROVED.equalsIgnoreCase(orderStatus)) {
					return -5;
				}

				if (actualNumberOfVisitors <= 0 || actualNumberOfVisitors > orderedVisitors) {
					return -3;
				}

				if (hasOpenVisitForOrder(orderNumber)) {
					return -2;
				}

				if (orderRs.getDate("order_date") == null) {
					return -7;
				}

				LocalDate orderDate = orderRs.getDate("order_date").toLocalDate();
				LocalDate currentDate = LocalDate.now();

				if (!orderDate.equals(currentDate)) {
					return -7;
				}

				int orderHour = orderRs.getInt("order_hour");

				if (orderRs.wasNull()) {
					return -7;
				}

				double estimatedVisitDuration = orderRs.getDouble("estimated_visit_duration_hours");

				if (orderRs.wasNull() || estimatedVisitDuration <= 0) {
					estimatedVisitDuration = 4.0;
				}

				LocalTime currentTime = LocalTime.now();

				double currentHour =
						currentTime.getHour()
						+ (currentTime.getMinute() / 60.0)
						+ (currentTime.getSecond() / 3600.0);

				double visitStartHour = orderHour;
				double visitEndHour = orderHour + estimatedVisitDuration;

				if (currentHour < visitStartHour || currentHour > visitEndHour) {
					return -7;
				}

				String insertSql = """
						INSERT INTO visit
						(
							order_number,
							park_id,
							subscriber_id,
							visit_type,
							actual_number_of_visitors,
							entry_time,
							handled_by_employee_id,
							identification_method
						)
						VALUES (?, ?, ?, 'ordered', ?, NOW(), ?, ?);
						""";

				try (PreparedStatement pstmt =
						conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
					pstmt.setInt(1, orderNumber);
					pstmt.setInt(2, parkId);

					if (subscriberId == null) {
						pstmt.setNull(3, java.sql.Types.INTEGER);
					} else {
						pstmt.setInt(3, subscriberId);
					}

					pstmt.setInt(4, actualNumberOfVisitors);
					pstmt.setInt(5, handledByEmployeeId);
					pstmt.setString(6, identificationMethod);

					int rows = pstmt.executeUpdate();

					if (rows == 0) {
						return -1;
					}

					try (ResultSet keys = pstmt.getGeneratedKeys()) {
						if (keys.next()) {
							int visitId = keys.getInt(1);

							/*
							 * After a successful entrance, the order is considered fulfilled.
							 */
							OrderConnection.getInstance().completeOrder(orderNumber, handledByEmployeeId);

							return visitId;
						}
					}
				}
			}
		}

		return -1;
	}

	/**
	 * Checks whether an order already has an open visit.
	 *
	 * An open visit is a visit with no exit time yet.
	 *
	 * @param orderNumber the order number
	 * @return true if the order already has an open visit, false otherwise
	 * @throws SQLException if the select query fails
	 */
	private boolean hasOpenVisitForOrder(int orderNumber) throws SQLException {
		ensureConnection();

		String sql = """
				SELECT visit_id
				FROM visit
				WHERE order_number = ?
				  AND exit_time IS NULL
				LIMIT 1;
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, orderNumber);

			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	/**
	 * Closes an open visit using the order confirmation code.
	 *
	 * The confirmation code is used as a QR code simulation at the park exit.
	 *
	 * @param confirmationCode          the order confirmation code
	 * @param parkId                    the park ID
	 * @param exitHandledByEmployeeId   the employee ID that handled the exit
	 * @return the closed visit ID, or -1 if no open visit was found
	 * @throws SQLException if the select or update query fails
	 */
	public int closeVisitByConfirmationCode(
			int confirmationCode,
			int parkId,
			int exitHandledByEmployeeId) throws SQLException {
		ensureConnection();

		String findSql = """
				SELECT v.visit_id
				FROM visit v
				JOIN `order` o
				    ON v.order_number = o.order_number
				WHERE o.confirmation_code = ?
				  AND v.park_id = ?
				  AND v.exit_time IS NULL
				LIMIT 1;
				""";

		try (PreparedStatement findStmt = conn.prepareStatement(findSql)) {
			findStmt.setInt(1, confirmationCode);
			findStmt.setInt(2, parkId);

			try (ResultSet rs = findStmt.executeQuery()) {
				if (!rs.next()) {
					return -1;
				}

				int visitId = rs.getInt(VISIT_ID);

				String updateSql = """
						UPDATE visit
						SET exit_time = NOW(),
						    exit_handled_by_employee_id = ?
						WHERE visit_id = ?
						  AND exit_time IS NULL;
						""";

				try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
					updateStmt.setInt(1, exitHandledByEmployeeId);
					updateStmt.setInt(2, visitId);

					if (updateStmt.executeUpdate() > 0) {
						return visitId;
					}
				}
			}
		}

		return -1;
	}

	/**
	 * This method creates a new visit from an approved order.
	 * 
	 * First, the method checks that the given order exists and that its status is
	 * approved. Then, it takes the park ID and subscriber ID from the order and
	 * inserts a new visit record into the visit table.
	 * 
	 * @param orderNumber            the order number used to create the visit
	 * @param actualNumberOfVisitors the actual number of visitors that entered the park
	 * @param handledByEmployeeId    the employee ID of the worker who handled the entrance
	 * @param identificationMethod    the identification method used at the entrance
	 * @return the generated visit ID if the visit was created successfully, or -1 otherwise
	 * @throws SQLException if the select or insert query fails
	 */
	public int createVisitFromOrder(
			int orderNumber,
			int actualNumberOfVisitors,
			int handledByEmployeeId,
			String identificationMethod) throws SQLException {
		ensureConnection();

		if (actualNumberOfVisitors <= 0) {
			return -1;
		}

		String orderSql = """
				SELECT order_number, park_id, subscriber_id
				FROM `order`
				WHERE order_number = ?
				  AND order_status = ?;
				""";

		try (PreparedStatement orderStmt = conn.prepareStatement(orderSql)) {
			orderStmt.setInt(1, orderNumber);
			orderStmt.setString(2, ORDER_STATUS_APPROVED);

			try (ResultSet orderRs = orderStmt.executeQuery()) {
				if (!orderRs.next()) {
					return -1;
				}

				int parkId = orderRs.getInt(PARK_ID);
				Integer subscriberId = getNullableInt(orderRs, SUBSCRIBER_ID);

				String insertSql = """
						INSERT INTO visit
						(
							order_number,
							park_id,
							subscriber_id,
							visit_type,
							actual_number_of_visitors,
							entry_time,
							handled_by_employee_id,
							identification_method
						)
						VALUES (?, ?, ?, 'ordered', ?, NOW(), ?, ?);
						""";

				try (PreparedStatement pstmt =
						conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
					pstmt.setInt(1, orderNumber);
					pstmt.setInt(2, parkId);

					if (subscriberId == null) {
						pstmt.setNull(3, java.sql.Types.INTEGER);
					} else {
						pstmt.setInt(3, subscriberId);
					}

					pstmt.setInt(4, actualNumberOfVisitors);
					pstmt.setInt(5, handledByEmployeeId);
					pstmt.setString(6, identificationMethod);

					int rows = pstmt.executeUpdate();

					if (rows == 0) {
						return -1;
					}

					try (ResultSet keys = pstmt.getGeneratedKeys()) {
						if (keys.next()) {
							return keys.getInt(1);
						}
					}
				}
			}
		}

		return -1;
	}

	/**
	 * Creates an occasional visit without an existing order.
	 *
	 * This method is used for visitors who arrive at the park without a reservation.
	 * The server should check park capacity before calling this method.
	 *
	 * @param parkId                 the park ID
	 * @param actualNumberOfVisitors the actual number of visitors entering
	 * @param handledByEmployeeId    the employee ID that handled the entrance
	 * @param identificationMethod    the identification method used at the entrance
	 * @return the generated visit ID if the visit was created successfully, or -1 otherwise
	 * @throws SQLException if the insert query fails
	 */
	public int createOccasionalVisit(
			int parkId,
			int actualNumberOfVisitors,
			int handledByEmployeeId,
			String identificationMethod) throws SQLException {
		ensureConnection();

		if (actualNumberOfVisitors <= 0) {
			return -1;
		}

		String insertSql = """
				INSERT INTO visit
				(
					park_id,
					visit_type,
					actual_number_of_visitors,
					entry_time,
					handled_by_employee_id,
					identification_method
				)
				VALUES (?, 'unplanned', ?, NOW(), ?, ?);
				""";

		try (PreparedStatement pstmt =
				conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setInt(1, parkId);
			pstmt.setInt(2, actualNumberOfVisitors);
			pstmt.setInt(3, handledByEmployeeId);
			pstmt.setString(4, identificationMethod);

			int rows = pstmt.executeUpdate();

			if (rows == 0) {
				return -1;
			}

			try (ResultSet keys = pstmt.getGeneratedKeys()) {
				if (keys.next()) {
					return keys.getInt(1);
				}
			}
		}

		return -1;
	}

	/**
	 * This method closes an existing visit.
	 * 
	 * Closing a visit means updating its exit_time to the current time and saving the
	 * employee who handled the exit.
	 * 
	 * @param visitId                 the ID of the visit to close
	 * @param exitHandledByEmployeeId the employee ID of the worker who handled the exit
	 * @return true if the visit was closed successfully, false otherwise
	 * @throws SQLException if the update query fails
	 */
	public boolean closeVisit(int visitId, int exitHandledByEmployeeId) throws SQLException {
		ensureConnection();

		String sql = """
				UPDATE visit
				SET exit_time = NOW(),
				    exit_handled_by_employee_id = ?
				WHERE visit_id = ?
				  AND exit_time IS NULL;
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, exitHandledByEmployeeId);
			pstmt.setInt(2, visitId);

			return pstmt.executeUpdate() > 0;
		}
	}

	/**
	 * Closes an open visit using the visit ID.
	 *
	 * This method is mainly used for occasional visitors, because they do not have an
	 * order confirmation code. The generated visit ID is given to the visitor at
	 * entrance and is used again when the visitor leaves the park.
	 *
	 * @param visitId                 the visit ID that was created at entrance
	 * @param parkId                  the park ID where the exit is performed
	 * @param exitHandledByEmployeeId the employee ID that handled the exit
	 * @return the closed visit ID, or -1 if no open visit was found
	 * @throws SQLException if the update query fails
	 */
	public int closeVisitByVisitId(int visitId, int parkId, int exitHandledByEmployeeId) throws SQLException {
		ensureConnection();

		String sql = """
				UPDATE visit
				SET exit_time = NOW(),
				    exit_handled_by_employee_id = ?
				WHERE visit_id = ?
				  AND park_id = ?
				  AND exit_time IS NULL;
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, exitHandledByEmployeeId);
			pstmt.setInt(2, visitId);
			pstmt.setInt(3, parkId);

			if (pstmt.executeUpdate() > 0) {
				return visitId;
			}
		}

		return -1;
	}

	/**
	 * Returns a visit by id.
	 * 
	 * @param visitId the visit ID
	 * @return a Visit object if the visit exists, otherwise null
	 * @throws SQLException if the select query fails
	 */
	public Visit getVisitById(int visitId) throws SQLException {
		ensureConnection();

		String sql = """
				SELECT *
				FROM visit
				WHERE visit_id = ?;
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, visitId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return convertResultSetToVisit(rs);
				}
			}
		}

		return null;
	}
}
