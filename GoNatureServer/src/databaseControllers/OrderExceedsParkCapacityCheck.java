package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import common.Order;
/**
 * this class is in charge of calculating whether an order can be booked at the given details
 */
public class OrderExceedsParkCapacityCheck {

	private static OrderExceedsParkCapacityCheck instance;
	/** holds the park connection */
	private ParkConnection pc;
	/** holds the order connection */
	private OrderConnection oc;
	
	/** holds the order status of approved */
	private final String ORDER_STATUS_APPROVED = "'approved'";
	/** holds which return column we want */
	private final String returnColumn = "exceeds_capacity";
	/** holds the number of hours in a day*/
	private final int NUMBER_OF_HOURS_IN_A_DAY = 24;

	/**
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
	 * @param pc the park table connection
	 * @param oc the order table connection
	 * @return the only OrderExceedsParkCapacityCheck instance
	 */
	public static OrderExceedsParkCapacityCheck getInstance(ParkConnection pc, OrderConnection oc) {
		if (instance == null) {
			instance = new OrderExceedsParkCapacityCheck(pc, oc);
		}

		return instance;
	}
	/**
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
	public int check(Order order) throws SQLException {
		if (pc == null || oc == null || order == null) {
			return -1;
		}

		Integer numberOfVisitors = order.getVisitorNumber();
		LocalDate orderDate = order.getOrderDate();
		Integer parkId = order.getParkId();
		int hour = order.getOrderHour();

		if (numberOfVisitors == null || orderDate == null || parkId == null) {
			return -1;
		}

		String sql = """
				SELECT
				    (COALESCE(SUM(o.number_of_visitors), 0) + ?
				        > p.max_capacity - p.places_for_unplanned_visitors) AS exceeds_capacity
				FROM park p
				LEFT JOIN `order` o
				    ON o.park_id = p.park_id
				    AND o.order_date = ?
				    AND o.order_status = ?
				    AND LEAST(
				        ABS(o.order_hour - ?),
				        ? - ABS(o.order_hour - ?)
				    ) <= p.estimated_visit_duration_hours
				WHERE p.park_id = ?
				GROUP BY p.park_id, p.max_capacity, p.places_for_unplanned_visitors;
				""";

		try (PreparedStatement pstmt = pc.conn.prepareStatement(sql)) {
			pstmt.setInt(1, numberOfVisitors);
			pstmt.setDate(2, java.sql.Date.valueOf(orderDate));
			pstmt.setString(3, ORDER_STATUS_APPROVED);
			pstmt.setInt(4, hour);
			pstmt.setInt(5, NUMBER_OF_HOURS_IN_A_DAY);
			pstmt.setInt(6, hour);
			pstmt.setInt(7, parkId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getBoolean(RETURN_COLUMN) ? 1 : 0;
				}
			}
		}

		return -1;
	}
}