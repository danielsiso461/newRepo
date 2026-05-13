package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import common.*;

// this class is the DB connector used when working with the orders table
public class OrderConnection extends AbstractDBConnection {
	// table columns
	private final String ORDER_NUMBER = "order_number", ORDER_DATE = "order_date",
			VISITOR_NUMBER = "number_of_visitors", CONF_CODE = "confirmation_code", USER_ID = "subscriber_id",
			PLACEMENT_DATE = "date_of_placing_order";

	// constructor taking care of the connection
	public OrderConnection() {
		super();
		try {
			this.connect();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getTableName() {
		return ConstantsDBTableNames.ORDER;
	}

	/*
	 * this method updates an order in the DB
	 * 
	 * @param um the UpdateMessage received from client
	 */
	public void updateOrder(UpdateMessage um) throws SQLException {
		List<Object> newValues = new ArrayList<>(), keyValues = new ArrayList<>();
		List<String> columnNames = new ArrayList<>(), keyColumns = new ArrayList<>();

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

	/*
	 * this method returns all the orders made by client
	 * 
	 * @param m the Message received from client
	 */
	public List<OrderRow> getUserOrders(Message m) throws SQLException {
		String s = selectByFields(new String[] { "*" }, new String[] { USER_ID });
		if (s == null)
			return null;

		PreparedStatement pstmt = conn.prepareStatement(s);

		pstmt.setInt(1, Integer.parseInt((String) m.getData()));

		// Execute update and get number of affected rows
		ResultSet rs = pstmt.executeQuery();

		List<OrderRow> l = new ArrayList<>();
		int i = 1;
		while (rs.next()) {
			l.add(new OrderRow(i++, rs.getInt(ORDER_NUMBER), rs.getDate(ORDER_DATE).toLocalDate(),
					rs.getInt(VISITOR_NUMBER), rs.getInt(CONF_CODE), rs.getInt(USER_ID),
					rs.getDate(PLACEMENT_DATE).toLocalDate()));
		}

		rs.close();
		pstmt.close();

		return l;
	}
}
