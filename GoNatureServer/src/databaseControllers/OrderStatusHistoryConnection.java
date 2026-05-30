package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class is the DB connector used when working with the
 * order_status_history table.
 * 
 * The class is implemented as a Singleton, so the server will use only one
 * database connection object for order status history during runtime.
 * 
 * The order_status_history table stores changes in order statuses, including
 * the previous status, the new status, the employee who changed it, the change
 * date, and the reason for the change.
 */
public class OrderStatusHistoryConnection extends AbstractDBConnection {

	/**
	 * The single instance of OrderStatusHistoryConnection.
	 */
	private static OrderStatusHistoryConnection instance;

	/**
	 * Private constructor for Singleton.
	 * 
	 * It creates the database connection once.
	 * 
	 * @throws SQLException if the connection to the database fails
	 */
	private OrderStatusHistoryConnection() throws SQLException {
		connect();
	}

	/**
	 * Returns the single instance of OrderStatusHistoryConnection.
	 * 
	 * If no instance exists, or if the existing database connection is closed, a new
	 * instance is created.
	 * 
	 * @return the only OrderStatusHistoryConnection instance
	 * @throws SQLException if creating the database connection fails
	 */
	public static OrderStatusHistoryConnection getInstance() throws SQLException {
		if (instance == null || instance.conn == null || instance.conn.isClosed()) {
			instance = new OrderStatusHistoryConnection();
		}
		return instance;
	}

	/**
	 * Returns the table name used by this DB connector.
	 * 
	 * @return the order_status_history table name
	 */
	@Override
	protected String getTableName() {
		return ConstantsDBTableNames.ORDER_STATUS_HISTORY;
	}

	/**
	 * This method adds a new status change record to the order status history table.
	 * 
	 * It should be called whenever an order status changes, for example from
	 * pending to approved, approved to cancelled, approved to completed, or approved
	 * to no_show.
	 * 
	 * @param orderNumber         the order number whose status was changed
	 * @param oldStatus           the previous status of the order
	 * @param newStatus           the new status of the order
	 * @param changedByEmployeeId the employee ID of the worker who changed the
	 *                            status
	 * @param changeReason        the reason for the status change
	 * @return true if the history record was added successfully, false otherwise
	 * @throws SQLException if the insert query fails
	 */
	public boolean addHistory(
			int orderNumber,
			String oldStatus,
			String newStatus,
			int changedByEmployeeId,
			String changeReason) throws SQLException {

		String sql = "INSERT INTO order_status_history "
				+ "(order_number, old_status, new_status, changed_by_employee_id, change_reason) "
				+ "VALUES (?, ?, ?, ?, ?);";

		PreparedStatement pstmt = conn.prepareStatement(sql);

		pstmt.setInt(1, orderNumber);
		pstmt.setString(2, oldStatus);
		pstmt.setString(3, newStatus);
		pstmt.setInt(4, changedByEmployeeId);
		pstmt.setString(5, changeReason);

		return pstmt.executeUpdate() > 0;
	}

	/**
	 * This method returns the cancellation report.
	 * 
	 * The cancellation report is based on the cancellation_report view and includes
	 * orders whose status changed to cancelled, expired, or no_show.
	 * 
	 * @return a ResultSet containing the cancellation report data
	 * @throws SQLException if the select query fails
	 */
	public ResultSet getCancellationReport() throws SQLException {
		String sql = "SELECT * FROM cancellation_report;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		return pstmt.executeQuery();
	}
}