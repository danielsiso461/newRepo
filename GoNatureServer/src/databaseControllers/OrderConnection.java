package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import common.Message;
import common.OrderRow;
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

	/**
	 * The order number column in the order table.
	 */
	private final String ORDER_NUMBER = "order_number";

	/**
	 * The order date column in the order table.
	 */
	private final String ORDER_DATE = "order_date";

	/**
	 * The number of visitors column in the order table.
	 */
	private final String VISITOR_NUMBER = "number_of_visitors";

	/**
	 * The confirmation code column in the order table.
	 */
	private final String CONF_CODE = "confirmation_code";

	/**
	 * The subscriber ID column in the order table.
	 */
	private final String USER_ID = "subscriber_id";

	/**
	 * The date of placing order column in the order table.
	 */
	private final String PLACEMENT_DATE = "date_of_placing_order";

	/**
	 * The park ID column in the order table.
	 */
	private final String PARK_ID = "park_id";

	/**
	 * The guide ID column in the order table.
	 * 
	 * This value can be null for private orders.
	 */
	private final String GUIDE_ID = "guide_id";

	/**
	 * The order status column in the order table.
	 */
	private final String ORDER_STATUS = "order_status";

	/**
	 * The order type column in the order table.
	 */
	private final String ORDER_TYPE = "order_type";

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
	protected String getTableName() {
		return ConstantsDBTableNames.ORDER;
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

		keyColumns.add(ORDER_NUMBER);
		keyValues.add(um.getOrderId());

		updateFields(columnNames.toArray(new String[columnNames.size()]), newValues,
				keyColumns.toArray(new String[keyColumns.size()]), keyValues);
	}

	/**
	 * This method creates a new order in the database.
	 * 
	 * The method validates the order data before inserting it.
	 * A private order must not have a guide, so guideId must be null.
	 * An organized group order must have a guide, so guideId must not be null.
	 * 
	 * The method uses insertFields from AbstractDBConnection to insert the order.
	 * After the insert, it uses getCreatedOrderNumber to find and return the
	 * generated order number.
	 * 
	 * @param orderDate        the requested visit date
	 * @param numberOfVisitors the requested number of visitors
	 * @param confirmationCode the confirmation code of the order
	 * @param subscriberId     the subscriber ID that created the order
	 * @param parkId           the requested park ID
	 * @param guideId          the guide ID, or null for a private order
	 * @param orderType        the order type: private or organized_group
	 * @return the created order number, or -1 if the order data is invalid or the
	 *         created order was not found after insert
	 * @throws SQLException if the insert or select query fails
	 */
	public int createOrder(LocalDate orderDate, int numberOfVisitors, int confirmationCode, int subscriberId,
			int parkId, Integer guideId, String orderType) throws SQLException {

		ensureConnection();

		if (orderDate == null || numberOfVisitors <= 0 || confirmationCode <= 0 || subscriberId <= 0
				|| parkId <= 0 || orderType == null || orderType.isBlank()
				|| (!orderType.equals("private") && !orderType.equals("organized_group"))
				|| (orderType.equals("private") && guideId != null)
				|| (orderType.equals("organized_group") && guideId == null)
				|| (guideId != null && guideId <= 0)) {
			return -1;
		}

		LocalDate placementDate = LocalDate.now();

		List<String> columnNames = new ArrayList<>();
		List<Object> values = new ArrayList<>();

		columnNames.add(ORDER_DATE);
		values.add(java.sql.Date.valueOf(orderDate));

		columnNames.add(VISITOR_NUMBER);
		values.add(numberOfVisitors);

		columnNames.add(CONF_CODE);
		values.add(confirmationCode);

		columnNames.add(USER_ID);
		values.add(subscriberId);

		columnNames.add(PLACEMENT_DATE);
		values.add(java.sql.Date.valueOf(placementDate));

		columnNames.add(PARK_ID);
		values.add(parkId);

		/*
		 * guide_id is inserted only for organized group orders.
		 * For private orders, guide_id remains null in the database.
		 */
		if (guideId != null) {
			columnNames.add(GUIDE_ID);
			values.add(guideId);
		}

		columnNames.add(ORDER_STATUS);
		values.add("pending");

		columnNames.add(ORDER_TYPE);
		values.add(orderType);

		insertFields(columnNames.toArray(new String[columnNames.size()]), values);

		return getCreatedOrderNumber(orderDate, numberOfVisitors, confirmationCode, subscriberId, placementDate,
				parkId, guideId, "pending", orderType);
	}
	
	
	/**
	 * This method finds the order number that was created after inserting a new
	 * order.
	 * 
	 * The method validates the identifying values before running the select query.
	 * It uses selectByFields from AbstractDBConnection to build the SELECT query.
	 * 
	 * We use MAX(order_number) because the new order should be the latest matching
	 * order.
	 * 
	 * If guideId is null, the guide_id column is not added to the WHERE clause,
	 * because SQL comparison using guide_id = NULL does not work as expected.
	 * This fits private orders, where guide_id is intentionally null.
	 * 
	 * @param orderDate        the requested visit date
	 * @param numberOfVisitors the number of visitors
	 * @param confirmationCode the confirmation code
	 * @param subscriberId     the subscriber ID
	 * @param placementDate    the date the order was placed
	 * @param parkId           the park ID
	 * @param guideId          the guide ID, or null for a private order
	 * @param orderStatus      the order status
	 * @param orderType        the order type
	 * @return the created order number, or -1 if the data is invalid or no matching
	 *         order was found
	 * @throws SQLException if the select query fails
	 */
	private int getCreatedOrderNumber(LocalDate orderDate, int numberOfVisitors, int confirmationCode,
			int subscriberId, LocalDate placementDate, int parkId, Integer guideId, String orderStatus,
			String orderType) throws SQLException {

		ensureConnection();

		if (orderDate == null || numberOfVisitors <= 0 || confirmationCode <= 0 || subscriberId <= 0
				|| placementDate == null || parkId <= 0
				|| orderStatus == null || orderStatus.isBlank()
				|| (!orderStatus.equals("pending") && !orderStatus.equals("approved")
						&& !orderStatus.equals("cancelled") && !orderStatus.equals("expired")
						&& !orderStatus.equals("completed") && !orderStatus.equals("no_show"))
				|| orderType == null || orderType.isBlank()
				|| (!orderType.equals("private") && !orderType.equals("organized_group"))
				|| (orderType.equals("private") && guideId != null)
				|| (orderType.equals("organized_group") && guideId == null)
				|| (guideId != null && guideId <= 0)) {
			return -1;
		}

		List<String> keyColumns = new ArrayList<>();
		List<Object> keyValues = new ArrayList<>();

		keyColumns.add(ORDER_DATE);
		keyValues.add(java.sql.Date.valueOf(orderDate));

		keyColumns.add(VISITOR_NUMBER);
		keyValues.add(numberOfVisitors);

		keyColumns.add(CONF_CODE);
		keyValues.add(confirmationCode);

		keyColumns.add(USER_ID);
		keyValues.add(subscriberId);

		keyColumns.add(PLACEMENT_DATE);
		keyValues.add(java.sql.Date.valueOf(placementDate));

		keyColumns.add(PARK_ID);
		keyValues.add(parkId);

		if (guideId != null) {
			keyColumns.add(GUIDE_ID);
			keyValues.add(guideId);
		}

		keyColumns.add(ORDER_STATUS);
		keyValues.add(orderStatus);

		keyColumns.add(ORDER_TYPE);
		keyValues.add(orderType);

		String sql = selectByFields(new String[] { "MAX(" + ORDER_NUMBER + ") AS " + ORDER_NUMBER },
				keyColumns.toArray(new String[keyColumns.size()]));

		PreparedStatement pstmt = conn.prepareStatement(sql);

		for (int i = 0; i < keyValues.size(); i++) {
			pstmt.setObject(i + 1, keyValues.get(i));
		}

		ResultSet rs = pstmt.executeQuery();

		int createdOrderNumber = -1;

		if (rs.next()) {
			createdOrderNumber = rs.getInt(ORDER_NUMBER);
		}

		rs.close();
		pstmt.close();

		return createdOrderNumber;
	}
	

	/**
	 * This method returns all the orders made by a client.
	 * 
	 * @param m the Message received from client
	 * @return a list of the user's orders
	 * @throws SQLException if the select query fails
	 */
	public List<OrderRow> getUserOrders(Message m) throws SQLException {
		ensureConnection();

		String sql = selectByFields(new String[] { "*" }, new String[] { USER_ID });

		if (sql == null) {
			return null;
		}

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, Integer.parseInt((String) m.getData()));

		ResultSet rs = pstmt.executeQuery();

		List<OrderRow> orders = new ArrayList<>();
		int index = 1;

		while (rs.next()) {
			Integer guideId = null;

			if (rs.getObject(GUIDE_ID) != null) {
				guideId = rs.getInt(GUIDE_ID);
			}

			orders.add(new OrderRow(index++, rs.getInt(ORDER_NUMBER), rs.getDate(ORDER_DATE).toLocalDate(),
					rs.getInt(VISITOR_NUMBER), rs.getInt(CONF_CODE), rs.getInt(USER_ID),
					rs.getDate(PLACEMENT_DATE).toLocalDate(), rs.getInt(PARK_ID), guideId,
					rs.getString(ORDER_STATUS), rs.getString(ORDER_TYPE)));
		}

		rs.close();
		pstmt.close();

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