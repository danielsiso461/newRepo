
package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import common.Message;
import common.Order;
import common.UpdateMessage;

/**
 * Handles database operations related to park orders.
 * 
 * This connector manages order creation, updates, status changes, retrieval by
 * different filters, and automatic status updates such as expired orders and
 * no-show orders. The class is implemented as a singleton so the server uses one
 * shared order database connector during runtime.
 */
public final class OrderConnection extends AbstractDBConnection {

	/**
	 * The single instance of OrderConnection.
	 */
	private static final OrderConnection INSTANCE = new OrderConnection();

	/*
	 * Order table column names.
	 */
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

	/**
	 * Offset used for generating confirmation codes.
	 */
	private final int CONF_CODE_OFFSET = 100000;

	/**
	 * Creates the OrderConnection singleton instance.
	 * 
	 * The constructor is private to prevent external object creation and opens the
	 * database connection when the singleton is initialized.
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
	 * @return the singleton OrderConnection instance
	 */
	public static OrderConnection getInstance() {
		return INSTANCE;
	}

	/**
	 * Returns the database table name used by this connector.
	 * 
	 * @return the order table name
	 */
	@Override
	public String getTableName() {
		return ConstantsDBTableNames.ORDER;
	}

	/**
	 * Converts the current row of a ResultSet into an Order object.
	 * 
	 * @param index the display index assigned to the order
	 * @param rs the ResultSet positioned on the order row
	 * @return an Order object containing the row data
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
	 * Updates editable fields of an existing order.
	 * 
	 * The method updates only the fields that are included in the received
	 * UpdateMessage.
	 * 
	 * @param updateMessage the update request containing the order ID and new values
	 * @throws SQLException if no fields were selected or if the update query fails
	 */
	public void updateOrder(UpdateMessage updateMessage) throws SQLException {
		ensureConnection();

		List<Object> newValues = new ArrayList<>();
		List<Object> keyValues = new ArrayList<>();
		List<String> columnNames = new ArrayList<>();
		List<String> keyColumns = new ArrayList<>();

		if (updateMessage.getUpdateDate() != null) {
			columnNames.add(ORDER_DATE);
			newValues.add(java.sql.Date.valueOf(updateMessage.getUpdateDate()));
		}

		if (updateMessage.getNumberOfVisitors() > 0) {
			columnNames.add(VISITOR_NUMBER);
			newValues.add(updateMessage.getNumberOfVisitors());
		}

		if (columnNames.isEmpty()) {
			throw new SQLException("No order fields were selected for update.");
		}

		keyColumns.add(ORDER_NUMBER);
		keyValues.add(updateMessage.getOrderId());

		updateFields(
				columnNames.toArray(new String[columnNames.size()]),
				newValues,
				keyColumns.toArray(new String[keyColumns.size()]),
				keyValues
		);
	}

	/**
	 * Retrieves all active orders that belong to a specific subscriber.
	 * 
	 * Cancelled orders are excluded from the returned list.
	 * 
	 * @param message a message containing the subscriber ID as its data
	 * @return a list of the subscriber's non-cancelled orders
	 * @throws SQLException if the select query fails
	 */
	public List<Order> getUserOrders(Message message) throws SQLException {
		ensureConnection();

		String sql = """
				SELECT *
				FROM `order`
				WHERE subscriber_id = ?
				  AND order_status <> 'cancelled'
				ORDER BY order_date;
				""";

		List<Order> orders = new ArrayList<>();

		Object data = message.getData();
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
	 * Creates a new order record using the provided order details.
	 * 
	 * A new order number is generated before insertion, and the created order is
	 * inserted with pending status.
	 * 
	 * @param orderDate the requested visit date
	 * @param numberOfVisitors the number of visitors in the order
	 * @param confirmationCode the confirmation code assigned to the order
	 * @param subscriberId the subscriber ID related to the order
	 * @param parkId the selected park ID
	 * @param guideId the guide ID, or null for a private visit
	 * @param orderType the order type
	 * @return the generated order number
	 * @throws SQLException if the insert query fails
	 */
	public int createOrder(LocalDate orderDate, int numberOfVisitors,
			int confirmationCode, int subscriberId, int parkId, Integer guideId,
			String orderType) throws SQLException {

		ensureConnection();

		int newOrderNumber = getNextOrderNumber();

		List<Object> values = new ArrayList<>();

		values.add(newOrderNumber);
		values.add(java.sql.Date.valueOf(orderDate));
		values.add(0);
		values.add(numberOfVisitors);
		values.add(confirmationCode);
		values.add(subscriberId);
		values.add(subscriberId);
		values.add("");
		values.add(parkId);
		values.add(guideId);
		values.add(java.sql.Date.valueOf(LocalDate.now()));
		values.add(Order.ORDER_STATUS_PENDING);
		values.add(orderType);

		insertFields(
				new String[] {
						ORDER_NUMBER,
						ORDER_DATE,
						ORDER_HOUR,
						VISITOR_NUMBER,
						CONF_CODE,
						SUBSCRIBER_ID,
						ORDER_CUSTOMER_ID,
						EMAIL,
						PARK_ID,
						GUIDE_ID,
						PLACEMENT_DATE,
						ORDER_STATUS,
						ORDER_TYPE
				},
				values
		);

		return newOrderNumber;
	}

	/**
	 * Retrieves a single order by its order number.
	 * 
	 * @param orderNumber the order number to search for
	 * @return the matching Order object, or null if no order was found
	 * @throws SQLException if the select query fails
	 */
	public Order getOrderByNumber(int orderNumber) throws SQLException {
		ensureConnection();

		String sql = selectByFields(
				new String[] {
						"*"
				},
				new String[] {
						ORDER_NUMBER
				}
		);

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
	 * Retrieves all orders for a specific park.
	 * 
	 * @param parkId the park ID to filter by
	 * @return a list of orders for the given park
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
	 * Retrieves all orders with a specific status.
	 * 
	 * @param orderStatus the order status to filter by
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
	 * Retrieves all orders with a specific order type.
	 * 
	 * @param orderType the order type to filter by
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
	 * Checks whether an order exists in the database.
	 * 
	 * @param orderNumber the order number to check
	 * @return true if the order exists, otherwise false
	 * @throws SQLException if the select query fails
	 */
	public boolean orderExists(int orderNumber) throws SQLException {
		ensureConnection();

		String sql = selectByFields(
				new String[] {
						ORDER_NUMBER
				},
				new String[] {
						ORDER_NUMBER
				}
		);

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, orderNumber);

			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	/**
	 * Updates the status of an existing order.
	 * 
	 * The method first validates the input and checks that the order exists before
	 * applying the status update.
	 * 
	 * @param orderNumber the order number to update
	 * @param newStatus the new status to assign to the order
	 * @param changedByEmployeeId the employee ID responsible for the change
	 * @param reason the reason for the status change
	 * @return true if the status was updated successfully, otherwise false
	 * @throws SQLException if the update query fails
	 */
	public boolean updateOrderStatus(int orderNumber, String newStatus,
			int changedByEmployeeId, String reason) throws SQLException {

		ensureConnection();

		if (orderNumber <= 0 || newStatus == null || newStatus.isBlank()) {
			return false;
		}

		if (!orderExists(orderNumber)) {
			return false;
		}

		return updateFields(
				new String[] {
						ORDER_STATUS
				},
				List.of(
						newStatus
				),
				new String[] {
						ORDER_NUMBER
				},
				List.of(
						orderNumber
				)
		);
	}

	/**
	 * Approves an existing order.
	 * 
	 * @param orderNumber the order number to approve
	 * @param changedByEmployeeId the employee ID responsible for the approval
	 * @return true if the order was approved successfully, otherwise false
	 * @throws SQLException if the update query fails
	 */
	public boolean approveOrder(int orderNumber, int changedByEmployeeId) throws SQLException {
		return updateOrderStatus(
				orderNumber,
				Order.ORDER_STATUS_APPROVED,
				changedByEmployeeId,
				"Order approved after availability check"
		);
	}

	/**
	 * Cancels an existing order.
	 * 
	 * @param orderNumber the order number to cancel
	 * @param changedByEmployeeId the employee ID responsible for the cancellation
	 * @param reason the reason for cancelling the order
	 * @return true if the order was cancelled successfully, otherwise false
	 * @throws SQLException if the update query fails
	 */
	public boolean cancelOrder(int orderNumber, int changedByEmployeeId,
			String reason) throws SQLException {

		return updateOrderStatus(
				orderNumber,
				"cancelled",
				changedByEmployeeId,
				reason
		);
	}

	/**
	 * Marks an order as expired.
	 * 
	 * @param orderNumber the order number to expire
	 * @param changedByEmployeeId the employee ID responsible for the update
	 * @return true if the order was expired successfully, otherwise false
	 * @throws SQLException if the update query fails
	 */
	public boolean expireOrder(int orderNumber, int changedByEmployeeId) throws SQLException {
		return updateOrderStatus(
				orderNumber,
				"expired",
				changedByEmployeeId,
				"Order expired because the visitor did not confirm in time"
		);
	}

	/**
	 * Marks an order as completed.
	 * 
	 * @param orderNumber the order number to complete
	 * @param changedByEmployeeId the employee ID responsible for the update
	 * @return true if the order was completed successfully, otherwise false
	 * @throws SQLException if the update query fails
	 */
	public boolean completeOrder(int orderNumber, int changedByEmployeeId) throws SQLException {
		return updateOrderStatus(
				orderNumber,
				"completed",
				changedByEmployeeId,
				"Order completed after visit"
		);
	}

	/**
	 * Marks an order as no-show.
	 * 
	 * @param orderNumber the order number to update
	 * @param changedByEmployeeId the employee ID responsible for the update
	 * @return true if the order was marked as no-show successfully, otherwise false
	 * @throws SQLException if the update query fails
	 */
	public boolean markOrderAsNoShow(int orderNumber, int changedByEmployeeId)
			throws SQLException {

		return updateOrderStatus(
				orderNumber,
				"no_show",
				changedByEmployeeId,
				"Visitor did not arrive to the park"
		);
	}

	/**
	 * Updates the park assigned to an order.
	 * 
	 * @param orderNumber the order number to update
	 * @param parkId the new park ID
	 * @throws SQLException if the update query fails
	 */
	public void updateOrderPark(int orderNumber, int parkId) throws SQLException {
		ensureConnection();

		updateFields(
				new String[] {
						PARK_ID
				},
				List.of(
						parkId
				),
				new String[] {
						ORDER_NUMBER
				},
				List.of(
						orderNumber
				)
		);
	}

	/**
	 * Updates the guide assigned to an order.
	 * 
	 * @param orderNumber the order number to update
	 * @param guideId the new guide ID, or null if no guide is assigned
	 * @throws SQLException if the update query fails
	 */
	public void updateOrderGuide(int orderNumber, Integer guideId) throws SQLException {
		ensureConnection();

		List<Object> newValues = new ArrayList<>();
		List<Object> keyValues = new ArrayList<>();

		newValues.add(guideId);
		keyValues.add(orderNumber);

		updateFields(
				new String[] {
						GUIDE_ID
				},
				newValues,
				new String[] {
						ORDER_NUMBER
				},
				keyValues
		);
	}

	/**
	 * Updates the type of an existing order.
	 * 
	 * @param orderNumber the order number to update
	 * @param orderType the new order type
	 * @throws SQLException if the update query fails
	 */
	public void updateOrderType(int orderNumber, String orderType) throws SQLException {
		ensureConnection();

		updateFields(
				new String[] {
						ORDER_TYPE
				},
				List.of(
						orderType
				),
				new String[] {
						ORDER_NUMBER
				},
				List.of(
						orderNumber
				)
		);
	}

	/**
	 * Retrieves approved orders for a specific park and date.
	 * 
	 * @param parkId the park ID to filter by
	 * @param orderDate the visit date to filter by
	 * @return a list of approved orders for the given park and date
	 * @throws SQLException if the select query fails
	 */
	public List<Order> getApprovedOrdersByParkAndDate(int parkId,
			LocalDate orderDate) throws SQLException {

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
	 * Calculates the total number of approved visitors for a park on a specific
	 * date.
	 * 
	 * @param parkId the park ID to filter by
	 * @param orderDate the visit date to filter by
	 * @return the total number of approved visitors
	 * @throws SQLException if the select query fails
	 */
	public int getTotalApprovedVisitorsByParkAndDate(int parkId,
			LocalDate orderDate) throws SQLException {

		ensureConnection();

		String sql = selectByFields(
				new String[] {
						"COALESCE(SUM(" + VISITOR_NUMBER + "), 0) AS total_visitors"
				},
				new String[] {
						PARK_ID,
						ORDER_DATE,
						ORDER_STATUS
				}
		);

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, parkId);
			pstmt.setDate(2, java.sql.Date.valueOf(orderDate));
			pstmt.setString(3, Order.ORDER_STATUS_APPROVED);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("total_visitors");
				}
			}
		}

		return 0;
	}

	/**
	 * Retrieves all orders from the database.
	 * 
	 * @return a list of all orders ordered by date and order number
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

	/**
	 * Calculates the next available order number.
	 * 
	 * @return the next order number, or 1 if the table is empty
	 * @throws SQLException if the select query fails
	 */
	private int getNextOrderNumber() throws SQLException {
		ensureConnection();

		String sql = "SELECT COALESCE(MAX(order_number), 0) + 1 AS next_order_number FROM `order`;";

		try (PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			if (rs.next()) {
				return rs.getInt("next_order_number");
			}
		}

		return 1;
	}

	/**
	 * Books a new order and inserts it into the database.
	 * 
	 * The method validates required order fields, fills default values when needed,
	 * generates an order number and confirmation code, inserts the order, and
	 * updates the given Order object with the generated values.
	 * 
	 * @param order the order object to book
	 * @return the same Order object after the generated values are assigned
	 * @throws SQLException if required order data is missing or if the insert query
	 *         fails
	 */
	public Order bookOrder(Order order) throws SQLException {
		ensureConnection();

		if (order == null) {
			throw new SQLException("Order is missing.");
		}

		if (order.getOrderDate() == null) {
			throw new SQLException("Order date is missing.");
		}

		if (order.getVisitorNumber() == null) {
			throw new SQLException("Number of visitors is missing.");
		}

		if (order.getUserId() == null) {
			throw new SQLException("User ID is missing.");
		}

		if (order.getParkId() == null) {
			throw new SQLException("Park ID is missing.");
		}

		if (order.getPlacementDate() == null) {
			order.setPlacementDate(LocalDate.now());
		}

		if (order.getOrderStatus() == null || order.getOrderStatus().isBlank()) {
			order.setOrderStatus(Order.ORDER_STATUS_PENDING);
		}

		if (order.getOrderType() == null || order.getOrderType().isBlank()) {
			if (order.getGuideId() == null) {
				order.setOrderType(Order.ORDER_TYPE_PRIVATE);
			} else {
				order.setOrderType(Order.ORDER_TYPE_ORGANIZED);
			}
		}

		int newOrderNumber = getNextOrderNumber();

		int code = newOrderNumber % CONF_CODE_OFFSET + CONF_CODE_OFFSET;

		Integer subscriberId = null;

		if (order.getIsSubscribed()) {
			subscriberId = order.getUserId();
		}

		List<Object> values = new ArrayList<>();

		values.add(newOrderNumber);
		values.add(java.sql.Date.valueOf(order.getOrderDate()));
		values.add(order.getVisitorNumber());
		values.add(code);
		values.add(subscriberId);
		values.add(java.sql.Date.valueOf(order.getPlacementDate()));
		values.add(order.getParkId());
		values.add(order.getGuideId());
		values.add(order.getOrderStatus());
		values.add(order.getOrderType());
		values.add(order.getOrderHour());
		values.add(order.getUserId());
		values.add(order.getEmail() == null ? "" : order.getEmail());

		insertFields(
				new String[] {
						ORDER_NUMBER,
						ORDER_DATE,
						VISITOR_NUMBER,
						CONF_CODE,
						SUBSCRIBER_ID,
						PLACEMENT_DATE,
						PARK_ID,
						GUIDE_ID,
						ORDER_STATUS,
						ORDER_TYPE,
						ORDER_HOUR,
						ORDER_CUSTOMER_ID,
						EMAIL
				},
				values
		);

		order.setOrderNumber(newOrderNumber);
		order.setOrderId(newOrderNumber);
		order.setConfirmationCode(code);

		return order;
	}

	/**
	 * Retrieves active orders by a customer ID number.
	 * 
	 * The search checks both the order customer ID and the subscriber ID number.
	 * Cancelled orders are excluded.
	 * 
	 * @param customerIdNumber the customer ID number to search by
	 * @return a list of matching non-cancelled orders
	 * @throws SQLException if the select query fails
	 */
	public ArrayList<Order> getOrdersByCustomerIdNumber(String customerIdNumber)
			throws SQLException {

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
	 * Retrieves orders by date, hour, and status.
	 * 
	 * This method is mainly used for time-based checks such as reminders or
	 * automatic cancellation flows.
	 * 
	 * @param date the order date to filter by
	 * @param hour the order hour to filter by
	 * @param status the order status to filter by
	 * @return a list of orders that match the given date, hour, and status
	 * @throws SQLException if the select query fails
	 */
	public List<Order> getOrdersByDateAndHourAndStatus(LocalDate date, int hour, String status)
			throws SQLException {

		ensureConnection();

		String sql = """
				SELECT
				    o.order_number,
				    o.order_date,
				    o.order_hour,
				    o.number_of_visitors,
				    o.order_status,
				    o.customer_id,
				    o.email,
				    o.park_id,
				    s.subscriber_phone
				FROM `order` o
				LEFT JOIN subscriber s
				    ON o.subscriber_id = s.subscriber_id
				WHERE o.order_date = ?
				  AND o.order_hour = ?
				  AND o.order_status = ?;
				""";

		List<Order> orders = new ArrayList<>();

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setObject(1, date);
			pstmt.setInt(2, hour);
			pstmt.setString(3, status);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					Order order = new Order(
							rs.getInt(ORDER_NUMBER),
							rs.getObject(ORDER_DATE, LocalDate.class),
							rs.getInt(ORDER_HOUR),
							rs.getString(ORDER_STATUS),
							rs.getInt(ORDER_CUSTOMER_ID),
							rs.getInt(VISITOR_NUMBER),
							rs.getString(EMAIL),
							rs.getInt(PARK_ID),
							rs.getString("subscriber_phone")
					);

					orders.add(order);
				}
			}
		}

		return orders;
	}

	/**
	 * Automatically updates matching orders to expired status.
	 * 
	 * @param date the order date to match
	 * @param hour the order hour to match
	 * @param status the current status that should be updated
	 * @throws SQLException if the update query fails
	 */
	public void autoCancelOrderList(LocalDate date, int hour, String status) throws SQLException {
		ensureConnection();

		updateFields(
				new String[] {
						ORDER_STATUS
				},
				List.of(
						"expired"
				),
				new String[] {
						ORDER_DATE,
						ORDER_HOUR,
						ORDER_STATUS
				},
				List.of(
						date,
						hour,
						status
				)
		);
	}

	/**
	 * Updates old orders with the given status to no-show status.
	 * 
	 * An order is marked as no-show when its expected visit time, including the
	 * park's estimated visit duration, has already passed.
	 * 
	 * @param status the current status of orders that should be checked
	 * @return a message describing whether the update affected any records
	 * @throws SQLException if the update query fails
	 */
	public String updateOrdersToNoShowsAccordingToStatus(String status) throws SQLException {
		ensureConnection();

		String sql = """
				UPDATE `order` o
				JOIN park p ON o.park_id = p.park_id
				SET o.order_status = 'no_show'
				WHERE o.order_status = ?
				  AND DATE_ADD(
				      DATE_ADD(o.order_date, INTERVAL o.order_hour HOUR),
				      INTERVAL p.estimated_visit_duration_hours HOUR
				  ) < NOW();
				""";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, status);

			int rows = pstmt.executeUpdate();

			if (rows > 0) {
				return "No show update completed successfully!";
			}

			return "No show update failed: record not found.";
		}
	}

	/**
	 * Prevents cloning of the singleton instance.
	 * 
	 * @return never returns, because cloning is not supported
	 * @throws CloneNotSupportedException always thrown to prevent cloning
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}

