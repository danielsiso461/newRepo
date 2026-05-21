package common;

import java.io.Serializable;
import java.time.LocalDate;

/*
 * this class represents a row from the orders database
 * 
 * the class stores all information related to an order
 * including the order details and the number of the order
 * in the user's query result
 */
public class OrderRow implements Serializable {

	/*
	 * serial version UID for serialization
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * stores the order information
	 */
	private int orderNumber, orderId, visitorNumber, confCode, userId;

	/*
	 * stores the order date and placement date
	 */
	private LocalDate orderDate, placementDate;

	/*
	 * constructor that creates a new order row
	 * 
	 * @param orderNumber		the number of the order in the query
	 * @param orderId			the order ID
	 * @param orderDate			the date of the order
	 * @param visitorNumber		the number of visitors
	 * @param confCode			the confirmation code
	 * @param userId			the ID of the user
	 * @param placementDate		the date the order was placed
	 */
	public OrderRow(int orderNumber, int orderId, LocalDate orderDate, int visitorNumber,
			int confCode, int userId, LocalDate placementDate) {

		this.orderNumber = orderNumber;
		this.orderId = orderId;
		this.orderDate = orderDate;
		this.visitorNumber = visitorNumber;
		this.confCode = confCode;
		this.userId = userId;
		this.placementDate = placementDate;
	}

	/*
	 * getter that returns the order number
	 * 
	 * @return the order number
	 */
	public Integer getOrderNumber() {
		return orderNumber;
	}

	/*
	 * getter that returns the order ID
	 * 
	 * @return the order ID
	 */
	public Integer getOrderId() {
		return orderId;
	}

	/*
	 * getter that returns the number of visitors
	 * 
	 * @return the number of visitors
	 */
	public Integer getVisitorNumber() {
		return visitorNumber;
	}

	/*
	 * getter that returns the confirmation code
	 * 
	 * @return the confirmation code
	 */
	public Integer getConfCode() {
		return confCode;
	}

	/*
	 * getter that returns the user ID
	 * 
	 * @return the user ID
	 */
	public Integer getUserId() {
		return userId;
	}

	/*
	 * getter that returns the order date
	 * 
	 * @return the order date
	 */
	public LocalDate getOrderDate() {
		return orderDate;
	}

	/*
	 * getter that returns the placement date
	 * 
	 * @return the placement date
	 */
	public LocalDate getPlacementDate() {
		return placementDate;
	}

	/*
	 * setter that updates the order date
	 * 
	 * this function is used in case of updates
	 * 
	 * @param orderDate the new order date
	 */
	public void setOrderDate(LocalDate orderDate) {
		this.orderDate = orderDate;
	}

	/*
	 * setter that updates the number of visitors
	 * 
	 * this function is used in case of updates
	 * 
	 * @param visitorNumber the new number of visitors
	 */
	public void setNumberOfVisitors(int visitorNumber) {
		this.visitorNumber = visitorNumber;
	}

	/*
	 * returns the order information as a string
	 * 
	 * @return a string representation of the order
	 */
	public String toString() {
		return "Order Number: " + orderId + " " +
				"# Visitors: " + visitorNumber + " " +
				"Code: " + confCode + " " +
				"ID: " + userId + " " +
				"Order Date: " + orderDate.toString() + " " +
				"Placement Date: " + placementDate.toString();
	}
}