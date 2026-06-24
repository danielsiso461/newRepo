package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import common.Order;

/**
 * Checks whether an order can be booked without exceeding the park capacity.
 */
public class OrderExceedsParkCapacityCheck {

	private static OrderExceedsParkCapacityCheck instance;

	/**
	 * The park DB connector used for the capacity query.
	 */
	private final ParkConnection pc;

	/**
	 * The status value of approved orders.
	 */
	private static final String ORDER_STATUS_APPROVED = "approved";

	/**
	 * The alias returned by the capacity query.
	 */
	private static final String RETURN_COLUMN = "exceeds_capacity";

	/**
	 * The number of hours in one day.
	 */
	private static final int NUMBER_OF_HOURS_IN_A_DAY = 24;

	/**
	 * Private constructor for Singleton.
	 * 
	 * @param pc the park table connection
	 */
	private OrderExceedsParkCapacityCheck(ParkConnection pc) {
		this.pc = pc;
	}

	/**
	 * Returns the single instance of OrderExceedsParkCapacityCheck.
	 * 
	 * The OrderConnection parameter is kept for compatibility with existing calls,
	 * but the current capacity query uses one connection and joins the park and
	 * order tables in the same SQL query.
	 * 
	 * @param pc the park table connection
	 * @param oc the order table connection
	 * @return the only OrderExceedsParkCapacityCheck instance
	 */
	public static OrderExceedsParkCapacityCheck getInstance(ParkConnection pc, OrderConnection oc) {
		if (instance == null) {
			instance = new OrderExceedsParkCapacityCheck(pc);
		}

		return instance;
	}

	/**
	 * Checks if the requested order exceeds the park capacity.
	 * 
	 * Returns:
	 * -1 if there is a problem,
	 *  0 if the order can be booked,
	 *  1 if the order exceeds capacity.
	 * 
	 * @param order the order to check
	 * @return the capacity check result
	 * @throws SQLException if the query fails
	 */
	public int check(Order order) throws SQLException {
		if (pc == null || order == null) {
			return -1;
		}

		Integer numberOfVisitors = order.getVisitorNumber();
		LocalDate orderDate = order.getOrderDate();
		Integer parkId = order.getParkId();
		int hour = order.getOrderHour();

		if (numberOfVisitors == null || orderDate == null || parkId == null) {
			return -1;
		}

		pc.ensureConnection();

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