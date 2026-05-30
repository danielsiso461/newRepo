package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
		String sql = "UPDATE visit "
				+ "SET exit_time = NOW(), exit_handled_by_employee_id = ? "
				+ "WHERE visit_id = ?;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, exitHandledByEmployeeId);
		pstmt.setInt(2, visitId);

		return pstmt.executeUpdate() > 0;
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