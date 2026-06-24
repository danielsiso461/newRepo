package common;

import java.io.Serializable;
import java.time.LocalDate;

/*
 * this class represents the data inside a <Message> object
 * when requests and replies are related to updating an order
 * 
 * the class also stores the relevant order number for the user
 */
public class UpdateMessage implements Serializable {

	/*
	 * serial version UID for serialization
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * stores the ID of the user who ordered
	 */
	private String ordererId;

	/*
	 * stores the order number and order ID
	 */
	private int orderNumber, orderId;

	/*
	 * stores the updated order date
	 */
	private LocalDate updateDate = null;

	/*
	 * stores the updated number of visitors
	 */
	private Integer numberOfVisitors = 0;

	/*
	 * constructor that creates a new update message
	 * 
	 * @param updateDate			the updated order date
	 * @param numberOfVisitors		the updated number of visitors
	 * @param orderId				the order ID
	 * @param orderNumber			the order number for the user
	 * @param ordererId				the ID of the user
	 */
	public UpdateMessage(LocalDate updateDate, Integer numberOfVisitors,
			int orderId, int orderNumber, String ordererId) {

		this.orderId = orderId;

		if(updateDate != null)
			this.updateDate = updateDate;

		if(numberOfVisitors != 0)
			this.numberOfVisitors = numberOfVisitors;

		this.orderNumber = orderNumber;
		this.ordererId = ordererId;
	}

	/*
	 * getter that returns the order ID
	 * 
	 * @return the order ID
	 */
	public int getOrderId() {
		return orderId;
	}

	/*
	 * getter that returns the order number
	 * 
	 * @return the order number
	 */
	public int getOrderNumber() {
		return orderNumber;
	}

	/*
	 * getter that returns the updated order date
	 * 
	 * @return the updated order date
	 */
	public LocalDate getUpdateDate() {
		return updateDate;
	}

	/*
	 * getter that returns the updated number of visitors
	 * 
	 * @return the updated number of visitors
	 */
	public int getNumberOfVisitors() {
		return numberOfVisitors.intValue();
	}

	/*
	 * getter that returns the ID of the user
	 * 
	 * @return the user ID
	 */
	public String getOrdererId() {
		return ordererId;
	}
}