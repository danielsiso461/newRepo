package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * This class is the DB connector used when working with the visit table.
 * 
 * The class is implemented as a Singleton, so the server will use only one
 * database connection object for visits during runtime.
 * 
 * The visit table stores actual park visits after visitors enter the park. It
 * includes the related order, park, subscriber, visit type, actual number of
 * visitors, entry time, exit time, and the employees who handled the entry and
 * exit.
 */
public class VisitConnection extends AbstractDBConnection {

	/**
	 * The single instance of VisitConnection.
	 */
	private static VisitConnection instance;

	/**
	 * Private constructor for Singleton.
	 * 
	 * It creates the database connection once.
	 * 
	 * @throws SQLException if the connection to the database fails
	 */
	private VisitConnection() throws SQLException {
		connect();
	}

	/**
	 * Returns the single instance of VisitConnection.
	 * 
	 * If no instance exists, or if the existing database connection is closed, a new
	 * instance is created.
	 * 
	 * @return the only VisitConnection instance
	 * @throws SQLException if creating the database connection fails
	 */
	public static VisitConnection getInstance() throws SQLException {
		if (instance == null || instance.conn == null || instance.conn.isClosed()) {
			instance = new VisitConnection();
		}
		return instance;
	}

	/**
	 * Returns the table name used by this DB connector.
	 * 
	 * @return the visit table name
	 */
	@Override
	protected String getTableName() {
		return ConstantsDBTableNames.VISIT;
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
	 * The confirmation code is used as a QR code simulation. The method checks that
	 * the order exists, belongs to the selected park, is approved, and is valid for
	 * the current date and entrance time window.
	 *
	 * Return values:
	 * - positive number: created visit ID
	 * - -1: no valid order was found
	 * - -2: the order already has an open visit
	 * - -3: invalid actual number of visitors
	 *
	 * @param confirmationCode       the order confirmation code used as QR simulation
	 * @param parkId                 the park ID where the visitor entered
	 * @param actualNumberOfVisitors the actual number of visitors entering
	 * @param handledByEmployeeId    the employee ID that handled the entrance
	 * @param identificationMethod   the identification method used at the entrance
	 * @return the created visit ID, or a negative value if the visit cannot be created
	 * @throws SQLException if the select or insert query fails
	 */
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

				int orderNumber = orderRs.getInt("order_number");
				int orderParkId = orderRs.getInt("park_id");
				int subscriberId = orderRs.getInt("subscriber_id");
				int orderedVisitors = orderRs.getInt("number_of_visitors");
				String orderStatus = orderRs.getString("order_status");

				if (orderParkId != parkId) {
					return -4;
				}

				if ("completed".equalsIgnoreCase(orderStatus)) {
					return -6;
				}

				if (!"approved".equalsIgnoreCase(orderStatus)) {
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
					pstmt.setInt(3, subscriberId);
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

				int visitId = rs.getInt("visit_id");

				String updateSql = """
						UPDATE visit
						SET exit_time = NOW(),
						    exit_handled_by_employee_id = ?
						WHERE visit_id = ?;
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
	 * The visit is created with visit_type ordered, the current time as entry_time,
	 * the actual number of visitors that entered the park, the employee who handled
	 * the entrance, and the identification method used at the entrance.
	 * 
	 * @param orderNumber            the order number used to create the visit
	 * @param actualNumberOfVisitors the actual number of visitors that entered the
	 *                               park
	 * @param handledByEmployeeId    the employee ID of the worker who handled the
	 *                               entrance
	 * @param identificationMethod    the identification method used, such as
	 *                               confirmation_code or id_number
	 * @return the generated visit ID if the visit was created successfully, or -1
	 *         if the order was not found, was not approved, or the insert failed
	 * @throws SQLException if the select or insert query fails
	 */
	public int createVisitFromOrder(
			int orderNumber,
			int actualNumberOfVisitors,
			int handledByEmployeeId,
			String identificationMethod) throws SQLException {

		String orderSql = "SELECT order_number, park_id, subscriber_id "
				+ "FROM `order` "
				+ "WHERE order_number = ? AND order_status = 'approved';";

		PreparedStatement orderStmt = conn.prepareStatement(orderSql);
		orderStmt.setInt(1, orderNumber);

		ResultSet orderRs = orderStmt.executeQuery();

		if (!orderRs.next()) {
			return -1;
		}

		int parkId = orderRs.getInt("park_id");
		int subscriberId = orderRs.getInt("subscriber_id");

		String insertSql = "INSERT INTO visit "
				+ "(order_number, park_id, subscriber_id, visit_type, actual_number_of_visitors, "
				+ "entry_time, handled_by_employee_id, identification_method) "
				+ "VALUES (?, ?, ?, 'ordered', ?, NOW(), ?, ?);";

		PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);

		pstmt.setInt(1, orderNumber);
		pstmt.setInt(2, parkId);
		pstmt.setInt(3, subscriberId);
		pstmt.setInt(4, actualNumberOfVisitors);
		pstmt.setInt(5, handledByEmployeeId);
		pstmt.setString(6, identificationMethod);

		int rows = pstmt.executeUpdate();

		if (rows == 0) {
			return -1;
		}

		ResultSet keys = pstmt.getGeneratedKeys();

		if (keys.next()) {
			return keys.getInt(1);
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
			        int visitId = keys.getInt(1);

			        return visitId;
			    }
			}
		}

		return -1;
	}

	/**
	 * This method closes an existing visit.
	 * 
	 * Closing a visit means updating its exit_time to the current time and saving the
	 * employee who handled the exit. This allows the system to know that the visitors
	 * are no longer inside the park.
	 * 
	 * @param visitId                 the ID of the visit to close
	 * @param exitHandledByEmployeeId the employee ID of the worker who handled the
	 *                                exit
	 * @return true if the visit was closed successfully, false otherwise
	 * @throws SQLException if the update query fails
	 */
	public boolean closeVisit(int visitId, int exitHandledByEmployeeId) throws SQLException {
		ensureConnection();

		String sql = "UPDATE visit "
				+ "SET exit_time = NOW(), exit_handled_by_employee_id = ? "
				+ "WHERE visit_id = ? AND exit_time IS NULL;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, exitHandledByEmployeeId);
		pstmt.setInt(2, visitId);

		return pstmt.executeUpdate() > 0;
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
	 * This method returns a visit by its visit ID.
	 * 
	 * @param visitId the visit ID
	 * @return a ResultSet containing the visit data if the visit exists
	 * @throws SQLException if the select query fails
	 */
	public ResultSet getVisitById(int visitId) throws SQLException {
		String sql = "SELECT * FROM visit WHERE visit_id = ?;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, visitId);

		return pstmt.executeQuery();
	}
}