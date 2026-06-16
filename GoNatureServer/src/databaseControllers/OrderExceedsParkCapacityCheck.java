package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import common.Order;

public class OrderExceedsParkCapacityCheck {
	/**
	 * The single instance of OrderExceedsParkCapacityCheck.
	 */
	private static OrderExceedsParkCapacityCheck instance;
	/* holds the park connection */
	private ParkConnection pc;
	/* holds the order connection */
	private OrderConnection oc;
	
	/* holds the order status of approved and which return column we want */
	private final String ORDER_STATUS_APPROVED = "'approved'",
							returnColumn = "exceeds_capacity";
	/* holds the number of hours in a day*/
	private final int NUMBER_OF_HOURS_IN_A_DAY = 24;

	/*
	 * the constructor of OrderExceedsParkCapacityCheck
	 * receives connections for ParkConnection, OrderConnection
	 * 
	 * @param pc 	the ParkConnection
	 * @param oc	the OrderConnection
	 */
	private OrderExceedsParkCapacityCheck(ParkConnection pc, OrderConnection oc) {
		this.pc = pc;
		this.oc = oc;
	}
	
	/**
	 * Returns the single instance of OrderExceedsParkCapacityCheck.
	 * 
	 * If no instance exists, a new instance is created.
	 * 
	 * @return the only OrderExceedsParkCapacityCheck instance
	 */
	public static OrderExceedsParkCapacityCheck getInstance(
			ParkConnection pc, OrderConnection oc) {
		if (instance == null) {
			instance = new OrderExceedsParkCapacityCheck(pc, oc);
		}
		return instance;
	}
	/*
	 * this method calculates if a given order can be booked
	 * based on the park and date.
	 * 
	 * Since the order table does not contain an order_hour column, the capacity
	 * check is done for the whole requested date.
	 * 
	 * @param o the order to check for
	 * @return -1 if there was a problem, 0 if the order can be booked, 1 if the order cannot be booked
	 * @throws SQLException if the query failed
	 */
	public int check(Order o) throws SQLException {
		if (pc == null || oc == null || o == null) {
			return -1;
		}

		Integer numberOfVisitors = o.getVisitorNumber();
		if (numberOfVisitors == null) {
			return -1;
		}

		LocalDate orderDate = o.getOrderDate();
		if (orderDate == null) {
			return -1;
		}

		Integer parkId = o.getParkId();
		if (parkId == null) {
			return -1;
		}

		String sql =
				"SELECT (COALESCE(SUM(o.number_of_visitors), 0) + ? "
				+ "> p.max_capacity - p.places_for_unplanned_visitors"
				+ ") AS " + returnColumn
				+ " FROM park p "
				+ "LEFT JOIN `order` o "
				+ "ON o.park_id = p.park_id "
				+ "AND o.order_date = ? "
				+ "AND o.order_status = " + ORDER_STATUS_APPROVED + " "
				+ "WHERE p.park_id = ? "
				+ "GROUP BY p.park_id;";

		PreparedStatement pstmt = pc.conn.prepareStatement(sql);

		pstmt.setInt(1, numberOfVisitors);
		pstmt.setObject(2, orderDate);
		pstmt.setInt(3, parkId);

		ResultSet rs = pstmt.executeQuery();

		int retval = -1;
		if (rs.next()) {
			retval = rs.getInt(returnColumn);
		}

		rs.close();
		pstmt.close();

		return retval;
	}

}
