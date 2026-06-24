package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import common.Message;
import common.Order;
import common.UpdateMessage;

/**
 * This class is the DB connector used when working with the orders table.
 * 
 * The class is implemented as a Singleton, so the server will use only one
 * database connection object for orders during runtime.
 */
public final class OrderConnection extends AbstractDBConnection {

	/**
	 * The single instance of OrderConnection.
	 */
	private static final OrderConnection INSTANCE = new OrderConnection();

	// table columns
	private final String ORDER_NUMBER = "order_number";
	private final String ORDER_DATE = "order_date";
	private final String VISITOR_NUMBER = "number_of_visitors";
	private final String CONF_CODE = "confirmation_code";
	private final String SUBSCRIBER_ID = "subscriber_id";
	private final String PLACEMENT_DATE = "date_of_placing_order";
	private final String ORDER_HOUR = "order_hour";
	private final String ORDER_CUSTOMER_ID = "customer_id";
	private final String EMAIL = "email";
	private final String PARK_ID = "park_id";
	private final String GUIDE_ID = "guide_id";
	private final String ORDER_STATUS = "order_status";
	private final String ORDER_TYPE = "order_type";

	/* this is used to generate confirmation codes */
	private final int CONF_CODE_OFFSET = 100000;

	/**
	 * Private constructor for Singleton.
	 * 
	 * It creates the database connection once.
	 */
	private OrderConnection() {
		super();
		try {
			this.connect();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the single instance of OrderConnection.
	 * 
	 * @return the only OrderConnection instance
	 */
	public static OrderConnection getInstance() {
		return INSTANCE;
	}

	/**
	 * Returns the table name used by this DB connector.
	 * 
	 * @return the orders table name
	 */
	@Override
	public String getTableName() {
		return ConstantsDBTableNames.ORDER;
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
	/**
	 * This method converts the current row of a ResultSet into an Order object.
	 * 
	 * @param index the row index used for display in the client table
	 * @param rs    the ResultSet positioned on the current order row
	 * @return an Order object that represents the current order
	 * @throws SQLException if reading data from the ResultSet fails
	 */
	private Order convertResultSetToOrder(int index, ResultSet rs) throws SQLException {
		Integer guideId = rs.getObject(GUIDE_ID) == null ? null : rs.getInt(GUIDE_ID);

		return new Order(
				index,
				rs.getInt(ORDER_NUMBER),
				rs.getDate(ORDER_DATE).toLocalDate(),
				rs.getInt(VISITOR_NUMBER),
				rs.getInt(CONF_CODE),
				rs.getInt(ORDER_CUSTOMER_ID),
				rs.getDate(PLACEMENT_DATE).toLocalDate(),
				rs.getInt(PARK_ID),
				guideId,
				rs.getString(ORDER_STATUS),
				rs.getString(ORDER_TYPE)
		);
	}

	/**
	 * This method updates an order in the DB.
	 * 
	 * @param um the UpdateMessage received from client
	 * @throws SQLException if the update query fails
	 */
	public void updateOrder(UpdateMessage um) throws SQLException {
		ensureConnection();

		List<Object> newValues = new ArrayList<>();
		List<Object> keyValues = new ArrayList<>();
		List<String> columnNames = new ArrayList<>();
		List<String> keyColumns = new ArrayList<>();

		if (um.getUpdateDate() != null) {
			columnNames.add(ORDER_DATE);
			newValues.add(java.sql.Date.valueOf(um.getUpdateDate()));
		}

		if (um.getNumberOfVisitors() > 0) {
			columnNames.add(VISITOR_NUMBER);
			newValues.add(um.getNumberOfVisitors());
		}

		if (columnNames.isEmpty()) {
			throw new SQLException("No order fields were selected for update.");
		}

		keyColumns.add(ORDER_NUMBER);
		keyValues.add(um.getOrderId());

		updateFields(columnNames.toArray(new String[columnNames.size()]), newValues,
				keyColumns.toArray(new String[keyColumns.size()]), keyValues);
	}

	/**
	 * This method returns all visible orders made by a client.
	 * 
	 * Cancelled orders are kept in the database for reports and history,
	 * but they are not displayed in the visitor's order table.
	 * 
	 * @param m the Message received from client
	 * @return a list of the user's visible orders
	 * @throws SQLException if the select query fails
	 */
	public List<Order> getUserOrders(Message m) throws SQLException {
		ensureConnection();

		String sql = """
				SELECT *
				FROM `order`
				WHERE subscriber_id = ?
				  AND order_status <> 'cancelled'
				ORDER BY order_date;
				""";

		List<Order> orders = new ArrayList<>();

		Object data = m.getData();
		int subscriberId;

		if (data instanceof Integer) {
			subscriberId = (Integer) data;
		} else {
			subscriberId = Integer.parseInt(data.toString());
		}

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, subscriberId);

			try (ResultSet rs = pstmt.executeQuery()) {
				int index = 1;

				while (rs.next()) {
					orders.add(convertResultSetToOrder(index++, rs));
				}
			}
		}

		return orders;
	}

	/**
	 * This method creates a new order in the database.
	 * 
	 * @param orderDate        the requested visit date
	 * @param numberOfVisitors the requested number of visitors
	 * @param confirmationCode the confirmation code of the order
	 * @param subscriberId     the subscriber id that created the order
	 * @param parkId           the requested park id
	 * @param guideId          the guide id, or null for a private visit
	 * @param orderType        the order type: private or organized_group
	 * @return the generated order number, or -1 if the insert failed
	 * @throws SQLException if the insert query fails
	 */
	public int createOrder(java.time.LocalDate orderDate, int numberOfVisitors, int confirmationCode, int subscriberId,
			int parkId, Integer guideId, String orderType) throws SQLException {
		ensureConnection();

		String sql = """
				INSERT INTO `order`
				(
					order_date,
					order_hour,
					number_of_visitors,
					confirmation_code,
					subscriber_id,
					customer_id,
					email,
					park_id,
					guide_id,
					date_of_placing_order,
					order_status,
					order_type
				)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURDATE(), 'pending', ?);
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setDate(1, java.sql.Date.valueOf(orderDate));
			pstmt.setInt(2, 0);
			pstmt.setInt(3, numberOfVisitors);
			pstmt.setInt(4, confirmationCode);
			pstmt.setInt(5, subscriberId);
			pstmt.setInt(6, subscriberId);
			pstmt.setString(7, "");
			pstmt.setInt(8, parkId);

			if (guideId == null) {
				pstmt.setNull(9, java.sql.Types.INTEGER);
			} else {
				pstmt.setInt(9, guideId);
			}

			pstmt.setString(10, orderType);

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
	 * This method returns one order by its order number.
	 * 
	 * @param orderNumber the order number
	 * @return an Order object if the order exists, otherwise null
	 * @throws SQLException if the select query fails
	 */
	public Order getOrderByNumber(int orderNumber) throws SQLException {
		ensureConnection();

		String sql = """
				SELECT *
				FROM `order`
				WHERE order_number = ?;
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, orderNumber);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return convertResultSetToOrder(1, rs);
				}
			}
		}

		return null;
	}

	/**
	 * This method returns all orders of a specific park.
	 * 
	 * @param parkId the park id
	 * @return a list of orders that belong to the given park
	 * @throws SQLException if the select query fails
	 */
	public List<Order> getOrdersByPark(int parkId) throws SQLException {
		ensureConnection();

		String sql = """
				SELECT *
				FROM `order`
				WHERE park_id = ?
				ORDER BY order_date;
				""";

		List<Order> orders = new ArrayList<>();

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, parkId);

			try (ResultSet rs = pstmt.executeQuery()) {
				int index = 1;

				while (rs.next()) {
					orders.add(convertResultSetToOrder(index++, rs));
				}
			}
		}

		return orders;
	}

	/**
	 * This method returns all orders with a specific status.
	 * 
	 * @param orderStatus the requested order status
	 * @return a list of orders with the given status
	 * @throws SQLException if the select query fails
	 */
	public List<Order> getOrdersByStatus(String orderStatus) throws SQLException {
		ensureConnection();

		String sql = """
				SELECT *
				FROM `order`
				WHERE order_status = ?
				ORDER BY order_date;
				""";

		List<Order> orders = new ArrayList<>();

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, orderStatus);

			try (ResultSet rs = pstmt.executeQuery()) {
				int index = 1;

				while (rs.next()) {
					orders.add(convertResultSetToOrder(index++, rs));
				}
			}
		}

		return orders;
	}

	/**
	 * This method returns all orders with a specific order type.
	 * 
	 * @param orderType the requested order type
	 * @return a list of orders with the given type
	 * @throws SQLException if the select query fails
	 */
	public List<Order> getOrdersByType(String orderType) throws SQLException {
		ensureConnection();

		String sql = """
				SELECT *
				FROM `order`
				WHERE order_type = ?
				ORDER BY order_date;
				""";

		List<Order> orders = new ArrayList<>();

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, orderType);

			try (ResultSet rs = pstmt.executeQuery()) {
				int index = 1;

				while (rs.next()) {
					orders.add(convertResultSetToOrder(index++, rs));
				}
			}
		}

		return orders;
	}

	/**
	 * This method checks if an order exists in the database.
	 * 
	 * @param orderNumber the order number
	 * @return true if the order exists, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean orderExists(int orderNumber) throws SQLException {
		ensureConnection();

		String sql = """
				SELECT order_number
				FROM `order`
				WHERE order_number = ?;
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, orderNumber);

			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	/**
	 * This method updates the status of an order and saves the change in
	 * order_status_history.
	 * 
	 * @param orderNumber         the order number
	 * @param newStatus           the new order status
	 * @param changedByEmployeeId the employee id that changed the status
	 * @param reason              the reason for the status change
	 * @return true if the status was updated, false otherwise
	 * @throws SQLException if the update query fails
	 */
	public boolean updateOrderStatus(int orderNumber, String newStatus, int changedByEmployeeId, String reason)
			throws SQLException {
		ensureConnection();

		String oldStatusSql = """
				SELECT order_status
				FROM `order`
				WHERE order_number = ?;
				""";

		String oldStatus;

		try (PreparedStatement oldStatusStmt = conn.prepareStatement(oldStatusSql)) {
			oldStatusStmt.setInt(1, orderNumber);

			try (ResultSet rs = oldStatusStmt.executeQuery()) {
				if (!rs.next()) {
					return false;
				}

				oldStatus = rs.getString(ORDER_STATUS);
			}
		}

		String updateSql = """
				UPDATE `order`
				SET order_status = ?
				WHERE order_number = ?;
				""";

		boolean updated;

		try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
			updateStmt.setString(1, newStatus);
			updateStmt.setInt(2, orderNumber);

			updated = updateStmt.executeUpdate() > 0;
		}

		if (updated) {
			OrderStatusHistoryConnection.getInstance().addHistory(orderNumber, oldStatus, newStatus,
					changedByEmployeeId, reason);
		}

		return updated;
	}

	/**
	 * This method approves an order and saves the approval in the order status
	 * history.
	 * 
	 * @param orderNumber         the order number
	 * @param changedByEmployeeId the employee id that approved the order
	 * @return true if the order was approved, false otherwise
	 * @throws SQLException if the update query fails
	 */
	public boolean approveOrder(int orderNumber, int changedByEmployeeId) throws SQLException {
		return updateOrderStatus(orderNumber, "approved", changedByEmployeeId,
				"Order approved after availability check");
	}

	/**
	 * This method cancels an order and saves the cancellation in the order status
	 * history.
	 * 
	 * @param orderNumber         the order number
	 * @param changedByEmployeeId the employee id that cancelled the order
	 * @param reason              the cancellation reason
	 * @return true if the order was cancelled, false otherwise
	 * @throws SQLException if the update query fails
	 */
	public boolean cancelOrder(int orderNumber, int changedByEmployeeId, String reason) throws SQLException {
		return updateOrderStatus(orderNumber, "cancelled", changedByEmployeeId, reason);
	}

	/**
	 * This method marks an order as expired and saves the change in the order status
	 * history.
	 * 
	 * @param orderNumber         the order number
	 * @param changedByEmployeeId the employee id that changed the status
	 * @return true if the order was expired, false otherwise
	 * @throws SQLException if the update query fails
	 */
	public boolean expireOrder(int orderNumber, int changedByEmployeeId) throws SQLException {
		return updateOrderStatus(orderNumber, "expired", changedByEmployeeId,
				"Order expired because the visitor did not confirm in time");
	}

	/**
	 * This method marks an order as completed and saves the change in the order status
	 * history.
	 * 
	 * @param orderNumber         the order number
	 * @param changedByEmployeeId the employee id that completed the order
	 * @return true if the order was completed, false otherwise
	 * @throws SQLException if the update query fails
	 */
	public boolean completeOrder(int orderNumber, int changedByEmployeeId) throws SQLException {
		return updateOrderStatus(orderNumber, "completed", changedByEmployeeId,
				"Order completed after visit");
	}

	/**
	 * This method marks an order as no show and saves the change in the order status
	 * history.
	 * 
	 * @param orderNumber         the order number
	 * @param changedByEmployeeId the employee id that changed the status
	 * @return true if the order was marked as no show, false otherwise
	 * @throws SQLException if the update query fails
	 */
	public boolean markOrderAsNoShow(int orderNumber, int changedByEmployeeId) throws SQLException {
		return updateOrderStatus(orderNumber, "no_show", changedByEmployeeId,
				"Visitor did not arrive to the park");
	}

	/**
	 * This method updates the park of an order.
	 * 
	 * @param orderNumber the order number
	 * @param parkId      the new park id
	 * @throws SQLException if the update query fails
	 */
	public void updateOrderPark(int orderNumber, int parkId) throws SQLException {
		ensureConnection();

		List<Object> newValues = new ArrayList<>();
		List<Object> keyValues = new ArrayList<>();

		newValues.add(parkId);
		keyValues.add(orderNumber);

		updateFields(new String[] { PARK_ID }, newValues, new String[] { ORDER_NUMBER }, keyValues);
	}

	/**
	 * This method updates the guide of an order.
	 * 
	 * @param orderNumber the order number
	 * @param guideId     the new guide id, or null for a private order
	 * @throws SQLException if the update query fails
	 */
	public void updateOrderGuide(int orderNumber, Integer guideId) throws SQLException {
		ensureConnection();

		String sql = """
				UPDATE `order`
				SET guide_id = ?
				WHERE order_number = ?;
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			if (guideId == null) {
				pstmt.setNull(1, java.sql.Types.INTEGER);
			} else {
				pstmt.setInt(1, guideId);
			}

			pstmt.setInt(2, orderNumber);
			pstmt.executeUpdate();
		}
	}

	/**
	 * This method updates the type of an order.
	 * 
	 * @param orderNumber the order number
	 * @param orderType   the new order type
	 * @throws SQLException if the update query fails
	 */
	public void updateOrderType(int orderNumber, String orderType) throws SQLException {
		ensureConnection();

		List<Object> newValues = new ArrayList<>();
		List<Object> keyValues = new ArrayList<>();

		newValues.add(orderType);
		keyValues.add(orderNumber);

		updateFields(new String[] { ORDER_TYPE }, newValues, new String[] { ORDER_NUMBER }, keyValues);
	}

	/**
	 * This method returns all approved orders for a specific park and date.
	 * 
	 * @param parkId    the park id
	 * @param orderDate the requested visit date
	 * @return a list of approved orders for the given park and date
	 * @throws SQLException if the select query fails
	 */
	public List<Order> getApprovedOrdersByParkAndDate(int parkId, java.time.LocalDate orderDate)
			throws SQLException {
		ensureConnection();

		String sql = """
				SELECT *
				FROM `order`
				WHERE park_id = ?
				  AND order_date = ?
				  AND order_status = 'approved'
				ORDER BY order_number;
				""";

		List<Order> orders = new ArrayList<>();

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, parkId);
			pstmt.setDate(2, java.sql.Date.valueOf(orderDate));

			try (ResultSet rs = pstmt.executeQuery()) {
				int index = 1;

				while (rs.next()) {
					orders.add(convertResultSetToOrder(index++, rs));
				}
			}
		}

		return orders;
	}

	/**
	 * This method returns the total number of ordered visitors for a specific park and
	 * date.
	 * 
	 * @param parkId    the park id
	 * @param orderDate the requested visit date
	 * @return the total number of ordered visitors
	 * @throws SQLException if the select query fails
	 */
	public int getTotalApprovedVisitorsByParkAndDate(int parkId, java.time.LocalDate orderDate) throws SQLException {
		ensureConnection();

		String sql = """
				SELECT COALESCE(SUM(number_of_visitors), 0) AS total_visitors
				FROM `order`
				WHERE park_id = ?
				  AND order_date = ?
				  AND order_status = 'approved';
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, parkId);
			pstmt.setDate(2, java.sql.Date.valueOf(orderDate));

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("total_visitors");
				}
			}
		}

		return 0;
	}

	/**
	 * This method returns all orders in the system.
	 * 
	 * This method is used by the service representative order table.
	 * 
	 * @return a list of all orders
	 * @throws SQLException if the select query fails
	 */
	public List<Order> getAllOrders() throws SQLException {
		ensureConnection();

		String sql = """
				SELECT *
				FROM `order`
				ORDER BY order_date DESC, order_number;
				""";

		List<Order> orders = new ArrayList<>();

		try (PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			int index = 1;

			while (rs.next()) {
				orders.add(convertResultSetToOrder(index++, rs));
			}
		}

		return orders;
	}

	/*
	 * this method returns the next order number for a new order.
	 * 
	 * The order_number column is not AUTO_INCREMENT in the database, so the value
	 * is calculated manually.
	 * 
	 * @return the next available order number
	 * @throws SQLException if the query failed
	 */
	private int getNextOrderNumber() throws SQLException {
		ensureConnection();

		String sql = "SELECT COALESCE(MAX(order_number), 0) + 1 AS next_order_number FROM `order`;";

		try (PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			if (rs.next()) {
				return rs.getInt("next_order_number");
			}
		}

		return 1;
	}

	/*
	 * this method adds an order to the order table
	 * 
	 * @param o the order to add
	 * @return the complete order including order ID and confirmation code
	 * @throws SQLException if the process fails
	 */
	public Order bookOrder(Order o) throws SQLException {
		ensureConnection();

		if (o == null) {
			throw new SQLException("Order is missing.");
		}

		if (o.getOrderDate() == null) {
			throw new SQLException("Order date is missing.");
		}

		if (o.getVisitorNumber() == null) {
			throw new SQLException("Number of visitors is missing.");
		}

		if (o.getUserId() == null) {
			throw new SQLException("User ID is missing.");
		}

		if (o.getParkId() == null) {
			throw new SQLException("Park ID is missing.");
		}

		if (o.getPlacementDate() == null) {
			o.setPlacementDate(java.time.LocalDate.now());
		}

		if (o.getOrderStatus() == null || o.getOrderStatus().isBlank()) {
			o.setOrderStatus(Order.ORDER_STATUS_PENDING);
		}

		if (o.getOrderType() == null || o.getOrderType().isBlank()) {
			if (o.getGuideId() == null) {
				o.setOrderType(Order.ORDER_TYPE_PRIVATE);
			} else {
				o.setOrderType(Order.ORDER_TYPE_ORGANIZED);
			}
		}

		int newOrderNumber = getNextOrderNumber();

		// create confirmation code
		int code = newOrderNumber % CONF_CODE_OFFSET + CONF_CODE_OFFSET;

		String sql = "INSERT INTO `order` ("
				+ ORDER_NUMBER + ", "
				+ ORDER_DATE + ", "
				+ VISITOR_NUMBER + ", "
				+ CONF_CODE + ", "
				+ SUBSCRIBER_ID + ", "
				+ PLACEMENT_DATE + ", "
				+ PARK_ID + ", "
				+ GUIDE_ID + ", "
				+ ORDER_STATUS + ", "
				+ ORDER_TYPE + ", "
				+ ORDER_HOUR + ", "
				+ ORDER_CUSTOMER_ID + ", "
				+ EMAIL
				+ ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, newOrderNumber);
			ps.setDate(2, java.sql.Date.valueOf(o.getOrderDate()));
			ps.setInt(3, o.getVisitorNumber());
			ps.setInt(4, code);

			if (o.getIsSubscribed()) {
				ps.setInt(5, o.getUserId());
			} else {
				ps.setNull(5, java.sql.Types.INTEGER);
			}

			ps.setDate(6, java.sql.Date.valueOf(o.getPlacementDate()));
			ps.setInt(7, o.getParkId());

			if (o.getGuideId() == null) {
				ps.setNull(8, java.sql.Types.INTEGER);
			} else {
				ps.setInt(8, o.getGuideId());
			}

			ps.setString(9, o.getOrderStatus());
			ps.setString(10, o.getOrderType());
			ps.setInt(11, o.getOrderHour());
			ps.setInt(12, o.getUserId());
			ps.setString(13, o.getEmail() == null ? "" : o.getEmail());

			ps.executeUpdate();

			o.setOrderNumber(newOrderNumber);
			o.setOrderId(newOrderNumber);
			o.setConfirmationCode(code);
		}

		return o;
	}

	/**
	 * This method returns all orders that belong to a specific customer id number.
	 * 
	 * The method receives an ID number and returns the matching orders.
	 * It supports both subscribers and occasional customers.
	 * 
	 * @param customerIdNumber the ID number entered by the customer
	 * @return a list of Order objects that belong to the given ID number
	 * @throws SQLException if the select query fails
	 */
	public ArrayList<Order> getOrdersByCustomerIdNumber(String customerIdNumber) throws SQLException {
		ensureConnection();

		ArrayList<Order> orders = new ArrayList<>();

		String sql = """
				SELECT
				    o.order_number,
				    o.order_date,
				    o.number_of_visitors,
				    o.confirmation_code,
				    o.subscriber_id,
				    o.date_of_placing_order,
				    o.park_id,
				    o.guide_id,
				    o.order_status,
				    o.order_type,
				    o.order_hour,
				    o.customer_id,
				    o.email
				FROM `order` o
				LEFT JOIN subscriber s
				    ON o.subscriber_id = s.subscriber_id
				WHERE (CAST(o.customer_id AS CHAR) = ?
				       OR s.subscriber_id_number = ?)
				  AND o.order_status <> 'cancelled'
				ORDER BY o.order_date;
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, customerIdNumber);
			pstmt.setString(2, customerIdNumber);

			try (ResultSet rs = pstmt.executeQuery()) {
				int index = 1;

				while (rs.next()) {
					orders.add(convertResultSetToOrder(index++, rs));
				}
			}
		}

		return orders;
	}

	/**
	 * Prevents cloning of the Singleton instance.
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}