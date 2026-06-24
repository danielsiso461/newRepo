package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import common.Order;

/**
 * Checks whether an order exceeds the park capacity.
 */
public class OrderExceedsParkCapacityCheck {

	private static OrderExceedsParkCapacityCheck instance;

	private ParkConnection pc;
	private OrderConnection oc;

	private final String ORDER_STATUS_APPROVED = "approved";
	private final String RETURN_COLUMN = "exceeds_capacity";

	private final int NUMBER_OF_HOURS_IN_A_DAY = 24;

	/*
	 * The constructor of OrderExceedsParkCapacityCheck receives connections for
	 * ParkConnection and OrderConnection.
	 *
	 * @param pc the ParkConnection
	 * @param oc the OrderConnection
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
	 * @param pc the ParkConnection
	 * @param oc the OrderConnection
	 * @return the only OrderExceedsParkCapacityCheck instance
	 */
	public static OrderExceedsParkCapacityCheck getInstance(ParkConnection pc, OrderConnection oc) {
		if (instance == null) {
			instance = new OrderExceedsParkCapacityCheck(pc, oc);
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