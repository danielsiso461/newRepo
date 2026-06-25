
package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import common.Order;

/**
 * Checks whether a new order can be booked without exceeding the selected
 * park's capacity.
 * 
 * The check compares the requested number of visitors with the park capacity,
 * while also considering existing approved orders around the requested visit
 * hour.
 */
public class OrderExceedsParkCapacityCheck {

	private static OrderExceedsParkCapacityCheck instance;

	/**
	 * Park database connector used to execute the capacity check query.
	 */
	private final ParkConnection pc;

	/**
	 * Status value used for orders that are already approved.
	 */
	private static final String ORDER_STATUS_APPROVED = "approved";

	/**
	 * Column alias returned by the capacity check query.
	 */
	private static final String RETURN_COLUMN = "exceeds_capacity";

	/**
	 * Number of hours in one full day.
	 */
	private static final int NUMBER_OF_HOURS_IN_A_DAY = 24;

	/**
	 * Creates an OrderExceedsParkCapacityCheck instance.
	 * 
	 * The constructor is private because this class is implemented as a singleton.
	 * 
	 * @param pc the park database connector used for the capacity query
	 */
	private OrderExceedsParkCapacityCheck(ParkConnection pc) {
		this.pc = pc;
	}

	/**
	 * Returns the single instance of OrderExceedsParkCapacityCheck.
	 * 
	 * The OrderConnection parameter is kept for compatibility with existing calls.
	 * The current capacity query uses the park connection and joins the park and
	 * order tables in the same SQL query.
	 * 
	 * @param pc the park database connector
	 * @param oc the order database connector, kept for compatibility
	 * @return the singleton OrderExceedsParkCapacityCheck instance
	 */
	public static OrderExceedsParkCapacityCheck getInstance(ParkConnection pc, OrderConnection oc) {
		if (instance == null) {
			instance = new OrderExceedsParkCapacityCheck(pc);
		}

		return instance;
	}

	/**
	 * Checks whether the requested order exceeds the park's available capacity.
	 * 
	 * The method returns -1 when the input is invalid or no matching park data is
	 * found, 0 when the order can be booked, and 1 when the order exceeds the
	 * allowed capacity.
	 * 
	 * @param order the order to check
	 * @return -1 for an error, 0 if the order can be booked, or 1 if it exceeds
	 *         capacity
	 * @throws SQLException if the database query fails
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
