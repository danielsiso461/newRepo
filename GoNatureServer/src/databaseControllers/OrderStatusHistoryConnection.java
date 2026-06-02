package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the DB connector used when working with the order_status_history
 * table.
 * 
 * The class is implemented as a Singleton, so the server will use only one
 * database connection object for order status history during runtime.
 * 
 * The order_status_history table stores every status change made to an order,
 * including the old status, the new status, the employee who changed it, the
 * change time, and the reason for the change.
 */
public class OrderStatusHistoryConnection extends AbstractDBConnection {

	/**
	 * The single instance of OrderStatusHistoryConnection.
	 */
	private static OrderStatusHistoryConnection instance;

	/**
	 * The history ID column in the order_status_history table.
	 */
	private final String HISTORY_ID = "history_id";

	/**
	 * The order number column in the order_status_history table.
	 */
	private final String ORDER_NUMBER = "order_number";

	/**
	 * The old order status column in the order_status_history table.
	 */
	private final String OLD_STATUS = "old_status";

	/**
	 * The new order status column in the order_status_history table.
	 */
	private final String NEW_STATUS = "new_status";

	/**
	 * The employee ID that changed the order status.
	 */
	private final String CHANGED_BY_EMPLOYEE_ID = "changed_by_employee_id";

	/**
	 * The change date and time column in the order_status_history table.
	 */
	private final String CHANGED_AT = "changed_at";

	/**
	 * The reason for the status change.
	 */
	private final String CHANGE_REASON = "change_reason";

	/**
	 * Order status value for pending orders.
	 */
	private final String PENDING = "pending";

	/**
	 * Order status value for approved orders.
	 */
	private final String APPROVED = "approved";

	/**
	 * Order status value for cancelled orders.
	 */
	private final String CANCELLED = "cancelled";

	/**
	 * Order status value for expired orders.
	 */
	private final String EXPIRED = "expired";

	/**
	 * Order status value for completed orders.
	 */
	private final String COMPLETED = "completed";

	/**
	 * Order status value for no-show orders.
	 */
	private final String NO_SHOW = "no_show";

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
	 * This method checks whether the given order status is valid.
	 * 
	 * @param status the order status
	 * @return true if the status is valid, false otherwise
	 */
	private boolean isValidOrderStatus(String status) {
		return PENDING.equals(status) || APPROVED.equals(status) || CANCELLED.equals(status)
				|| EXPIRED.equals(status) || COMPLETED.equals(status) || NO_SHOW.equals(status);
	}

	/**
	 * This method adds a new order status history record.
	 * 
	 * The method uses insertFields from AbstractDBConnection.
	 * 
	 * This method should be called whenever an order status changes, for example:
	 * pending to approved, pending to cancelled, approved to completed, or approved
	 * to no_show.
	 * 
	 * @param orderNumber         the order number whose status was changed
	 * @param oldStatus           the previous order status
	 * @param newStatus           the new order status
	 * @param changedByEmployeeId the employee ID that changed the status
	 * @param changeReason        the reason for the status change
	 * @return the created history ID, or -1 if the request is invalid or the record
	 *         was not found after insert
	 * @throws SQLException if the insert or select query fails
	 */
	public int addHistory(int orderNumber, String oldStatus, String newStatus, int changedByEmployeeId,
			String changeReason) throws SQLException {

		ensureConnection();

		if (orderNumber <= 0 || oldStatus == null || newStatus == null || !isValidOrderStatus(oldStatus)
				|| !isValidOrderStatus(newStatus) || oldStatus.equals(newStatus) || changedByEmployeeId <= 0
				|| changeReason == null || changeReason.isBlank()) {
			return -1;
		}

		LocalDateTime changedAt = LocalDateTime.now();

		List<String> columnNames = new ArrayList<>();
		List<Object> values = new ArrayList<>();

		columnNames.add(ORDER_NUMBER);
		values.add(orderNumber);

		columnNames.add(OLD_STATUS);
		values.add(oldStatus);

		columnNames.add(NEW_STATUS);
		values.add(newStatus);

		columnNames.add(CHANGED_BY_EMPLOYEE_ID);
		values.add(changedByEmployeeId);

		columnNames.add(CHANGED_AT);
		values.add(Timestamp.valueOf(changedAt));

		columnNames.add(CHANGE_REASON);
		values.add(changeReason);

		insertFields(columnNames.toArray(new String[columnNames.size()]), values);

		return getCreatedHistoryId(orderNumber, oldStatus, newStatus, changedByEmployeeId, changedAt, changeReason);
	}

	/**
	 * This method finds the history ID that was created after inserting a new
	 * history record.
	 * 
	 * The method uses selectByFields from AbstractDBConnection. We use
	 * MAX(history_id) because the new history record should be the latest matching
	 * record.
	 * 
	 * @param orderNumber         the order number
	 * @param oldStatus           the previous order status
	 * @param newStatus           the new order status
	 * @param changedByEmployeeId the employee ID that changed the status
	 * @param changedAt           the change date and time
	 * @param changeReason        the reason for the status change
	 * @return the created history ID, or -1 if no matching record was found
	 * @throws SQLException if the select query fails
	 */
	private int getCreatedHistoryId(int orderNumber, String oldStatus, String newStatus, int changedByEmployeeId,
			LocalDateTime changedAt, String changeReason) throws SQLException {

		ensureConnection();

		if (orderNumber <= 0 || oldStatus == null || newStatus == null || !isValidOrderStatus(oldStatus)
				|| !isValidOrderStatus(newStatus) || changedByEmployeeId <= 0 || changedAt == null
				|| changeReason == null || changeReason.isBlank()) {
			return -1;
		}

		List<String> keyColumns = new ArrayList<>();
		List<Object> keyValues = new ArrayList<>();

		keyColumns.add(ORDER_NUMBER);
		keyValues.add(orderNumber);

		keyColumns.add(OLD_STATUS);
		keyValues.add(oldStatus);

		keyColumns.add(NEW_STATUS);
		keyValues.add(newStatus);

		keyColumns.add(CHANGED_BY_EMPLOYEE_ID);
		keyValues.add(changedByEmployeeId);

		keyColumns.add(CHANGED_AT);
		keyValues.add(Timestamp.valueOf(changedAt));

		keyColumns.add(CHANGE_REASON);
		keyValues.add(changeReason);

		String sql = selectByFields(new String[] { "MAX(" + HISTORY_ID + ") AS " + HISTORY_ID },
				keyColumns.toArray(new String[keyColumns.size()]));

		PreparedStatement pstmt = conn.prepareStatement(sql);

		for (int i = 0; i < keyValues.size(); i++) {
			pstmt.setObject(i + 1, keyValues.get(i));
		}

		java.sql.ResultSet rs = pstmt.executeQuery();

		int historyId = -1;

		if (rs.next()) {
			historyId = rs.getInt(HISTORY_ID);
		}

		rs.close();
		pstmt.close();

		return historyId;
	}

	/**
	 * This method returns the status history records of a specific order.
	 * 
	 * The method uses selectByFields from AbstractDBConnection and adds ORDER BY
	 * manually because the abstract method builds only the basic SELECT and WHERE
	 * parts.
	 * 
	 * Each inner ArrayList contains:
	 * history_id, order_number, old_status, new_status, changed_by_employee_id,
	 * changed_at, change_reason.
	 * 
	 * @param orderNumber the order number
	 * @return an ArrayList of history records, or an empty ArrayList if no records
	 *         were found
	 * @throws SQLException if the select query fails
	 */
	public ArrayList<ArrayList<Object>> getHistoryByOrderNumber(int orderNumber) throws SQLException {
		ensureConnection();

		ArrayList<ArrayList<Object>> historyList = new ArrayList<>();

		if (orderNumber <= 0) {
			return historyList;
		}

		String[] columnNames = {
				HISTORY_ID,
				ORDER_NUMBER,
				OLD_STATUS,
				NEW_STATUS,
				CHANGED_BY_EMPLOYEE_ID,
				CHANGED_AT,
				CHANGE_REASON
		};

		String[] keyColumns = {
				ORDER_NUMBER
		};

		String sql = selectByFields(columnNames, keyColumns);

		sql = sql.substring(0, sql.length() - 1) + " ORDER BY " + CHANGED_AT + ";";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, orderNumber);

		java.sql.ResultSet rs = pstmt.executeQuery();

		while (rs.next()) {
			ArrayList<Object> historyRow = new ArrayList<>();

			historyRow.add(rs.getInt(HISTORY_ID));
			historyRow.add(rs.getInt(ORDER_NUMBER));
			historyRow.add(rs.getString(OLD_STATUS));
			historyRow.add(rs.getString(NEW_STATUS));
			historyRow.add(rs.getInt(CHANGED_BY_EMPLOYEE_ID));
			historyRow.add(rs.getTimestamp(CHANGED_AT).toLocalDateTime());
			historyRow.add(rs.getString(CHANGE_REASON));

			historyList.add(historyRow);
		}

		rs.close();
		pstmt.close();

		return historyList;
	}

	/**
	 * This method returns the latest status change record of a specific order.
	 * 
	 * Each returned ArrayList contains:
	 * history_id, order_number, old_status, new_status, changed_by_employee_id,
	 * changed_at, change_reason.
	 * 
	 * @param orderNumber the order number
	 * @return an ArrayList containing the latest history record, or an empty
	 *         ArrayList if no record was found
	 * @throws SQLException if the select query fails
	 */
	public ArrayList<Object> getLatestHistoryByOrderNumber(int orderNumber) throws SQLException {
		ensureConnection();

		ArrayList<Object> historyRow = new ArrayList<>();

		if (orderNumber <= 0) {
			return historyRow;
		}

		String[] columnNames = {
				HISTORY_ID,
				ORDER_NUMBER,
				OLD_STATUS,
				NEW_STATUS,
				CHANGED_BY_EMPLOYEE_ID,
				CHANGED_AT,
				CHANGE_REASON
		};

		String[] keyColumns = {
				ORDER_NUMBER
		};

		String sql = selectByFields(columnNames, keyColumns);

		sql = sql.substring(0, sql.length() - 1) + " ORDER BY " + CHANGED_AT + " DESC LIMIT 1;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, orderNumber);

		java.sql.ResultSet rs = pstmt.executeQuery();

		if (rs.next()) {
			historyRow.add(rs.getInt(HISTORY_ID));
			historyRow.add(rs.getInt(ORDER_NUMBER));
			historyRow.add(rs.getString(OLD_STATUS));
			historyRow.add(rs.getString(NEW_STATUS));
			historyRow.add(rs.getInt(CHANGED_BY_EMPLOYEE_ID));
			historyRow.add(rs.getTimestamp(CHANGED_AT).toLocalDateTime());
			historyRow.add(rs.getString(CHANGE_REASON));
		}

		rs.close();
		pstmt.close();

		return historyRow;
	}
}