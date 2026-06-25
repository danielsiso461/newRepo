package common;

import java.io.Serializable;

/**
 * Represents the data sent between the client and the server
 * when an order cancellation is requested.
 *
 * The order is not deleted from the database.
 * Instead, the server updates the order_status field to "cancelled",
 * so the cancellation can still be used in reports and order history.
 */
public class CancelOrderMessage implements Serializable {
	/**
	 * serial version UID for serialization
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The real order ID from the database.
	 * In the database, this value is stored in the order_number column.
	 */
	private int orderId;

	/**
	 * The order number as displayed in the user's table.
	 * This helps update the correct row in the TableView after the server responds.
	 */
	private int orderNumber;

	/**
	 * The ID of the user who owns the order.
	 */
	private String ordererId;

	/**
	 * The reason for cancelling the order.
	 */
	private String reason;

	/**
	 * Creates a new cancellation message for an order.
	 *
	 * @param orderId     the real order ID from the database
	 * @param orderNumber the order number in the displayed table
	 * @param ordererId   the ID of the user who owns the order
	 * @param reason      the reason for cancelling the order
	 */
	public CancelOrderMessage(int orderId, int orderNumber, String ordererId, String reason) {
		this.orderId = orderId;
		this.orderNumber = orderNumber;
		this.ordererId = ordererId;

		if (reason == null || reason.isBlank()) {
			this.reason = "Visitor cancelled the order";
		} else {
			this.reason = reason;
		}
	}

	/**
	 * Returns the real order ID from the database.
	 *
	 * @return the real order ID
	 */
	public int getOrderId() {
		return orderId;
	}

	/**
	 * Returns the order number in the displayed table.
	 *
	 * @return the displayed order number
	 */
	public int getOrderNumber() {
		return orderNumber;
	}

	/**
	 * Returns the ID of the user who owns the order.
	 *
	 * @return the user ID
	 */
	public String getOrdererId() {
		return ordererId;
	}

	/**
	 * Returns the cancellation reason.
	 *
	 * @return the cancellation reason
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * Returns the cancellation message data as a string.
	 *
	 * @return a string representation of the cancellation request
	 */
	@Override
	public String toString() {
		return "CancelOrderMessage [orderId=" + orderId +
				", orderNumber=" + orderNumber +
				", ordererId=" + ordererId +
				", reason=" + reason + "]";
	}
}