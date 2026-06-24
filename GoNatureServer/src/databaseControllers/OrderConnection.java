package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

	private static final OrderConnection INSTANCE = new OrderConnection();

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

	private final int CONF_CODE_OFFSET = 100000;

	private OrderConnection() {
		super();

		try {
			this.connect();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static OrderConnection getInstance() {
		return INSTANCE;
	}

	@Override
	public String getTableName() {
		return ConstantsDBTableNames.ORDER;
	}

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

	public int createOrder(java.time.LocalDate orderDate, int numberOfVisitors,
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
		values.add(java.sql.Date.valueOf(java.time.LocalDate.now()));
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

	public boolean approveOrder(int orderNumber, int changedByEmployeeId) throws SQLException {
		return updateOrderStatus(
				orderNumber,
				Order.ORDER_STATUS_APPROVED,
				changedByEmployeeId,
				"Order approved after availability check"
		);
	}

	public boolean cancelOrder(int orderNumber, int changedByEmployeeId,
			String reason) throws SQLException {

		return updateOrderStatus(
				orderNumber,
				"cancelled",
				changedByEmployeeId,
				reason
		);
	}

	public boolean expireOrder(int orderNumber, int changedByEmployeeId) throws SQLException {
		return updateOrderStatus(
				orderNumber,
				"expired",
				changedByEmployeeId,
				"Order expired because the visitor did not confirm in time"
		);
	}

	public boolean completeOrder(int orderNumber, int changedByEmployeeId) throws SQLException {
		return updateOrderStatus(
				orderNumber,
				"completed",
				changedByEmployeeId,
				"Order completed after visit"
		);
	}

	public boolean markOrderAsNoShow(int orderNumber, int changedByEmployeeId)
			throws SQLException {

		return updateOrderStatus(
				orderNumber,
				"no_show",
				changedByEmployeeId,
				"Visitor did not arrive to the park"
		);
	}

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

	public List<Order> getApprovedOrdersByParkAndDate(int parkId,
			java.time.LocalDate orderDate) throws SQLException {

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

	public int getTotalApprovedVisitorsByParkAndDate(int parkId,
			java.time.LocalDate orderDate) throws SQLException {

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
			order.setPlacementDate(java.time.LocalDate.now());
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

	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}