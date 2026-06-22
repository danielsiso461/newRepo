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

	/**
	 * The single instance of OrderConnection.
	 */
	private static final OrderConnection INSTANCE = new OrderConnection();

	// table columns
	private final String ORDER_NUMBER = "order_number";
	private final String ORDER_DATE = "order_date";
	private final String VISITOR_NUMBER = "number_of_visitors";
	private final String CONF_CODE = "confirmation_code";
	private final String USER_ID = "subscriber_id";
	private final String PLACEMENT_DATE = "date_of_placing_order";

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
	 * This method updates an order in the DB.
	 * 
	 * @param um the UpdateMessage received from client
	 * @throws SQLException if the update query fails
	 */
	public void updateOrder(UpdateMessage um) throws SQLException {
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
	 * This method returns all the orders made by a client.
	 * 
	 * @param m the Message received from client
	 * @return a list of the user's orders
	 * @throws SQLException if the select query fails
	 */
	public List<Order> getUserOrders(Message m) throws SQLException {
		String s = selectByFields(new String[] { "*" }, new String[] { USER_ID });
		if (s == null) {
			return null;
		}

		PreparedStatement pstmt = conn.prepareStatement(s);
		pstmt.setInt(1, Integer.parseInt((String) m.getData()));

		ResultSet rs = pstmt.executeQuery();

		List<Order> l = new ArrayList<>();
		int i = 1;

		while (rs.next()) {
			l.add(new Order(i++, rs.getInt(ORDER_NUMBER), rs.getDate(ORDER_DATE).toLocalDate(),
					rs.getInt(VISITOR_NUMBER), rs.getInt(CONF_CODE), rs.getInt(USER_ID),
					rs.getDate(PLACEMENT_DATE).toLocalDate()));
		}

		rs.close();
		pstmt.close();

		return l;
	}
	
	/**
	 * Prevents cloning of the Singleton instance.
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}