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
public class Order implements Serializable {
	public static final String 
		ORDER_TYPE_ORGANIZED = "organized_group",
		ORDER_TYPE_PRIVATE = "private";
	/*
	 * serial version UID for serialization
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * stores the order information
	 */
	private Integer orderNumber, orderId, visitorNumber, confCode, userId;

	/*
	 * stores the park id of the order
	 */
	private Integer parkId;
	private String parkName;

	/*
	 * stores the guide id of the order
	 * 
	 * this value can be null for private orders
	 */
	private Integer guideId;

	/*
	 * stores the order date and placement date
	 */
	private LocalDate orderDate, placementDate;

	/*
	 * stores the status of the order
	 * 
	 * possible values are: pending, approved, cancelled, expired, completed, no_show
	 */
	private String orderStatus;

	/*
	 * stores the type of the order
	 * 
	 * possible values are: private, organized_group
	 */
	private String orderType;

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
	public Order(int orderNumber, int orderId, LocalDate orderDate, int visitorNumber,
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
	 * constructor that creates a new order row with the extended order information
	 * 
	 * @param orderNumber		the number of the order in the query
	 * @param orderId			the order ID
	 * @param orderDate			the date of the order
	 * @param visitorNumber		the number of visitors
	 * @param confCode			the confirmation code
	 * @param userId			the ID of the user
	 * @param placementDate		the date the order was placed
	 * @param parkId			the park ID of the order
	 * @param guideId			the guide ID, or null for private orders
	 * @param orderStatus		the status of the order
	 * @param orderType			the type of the order
	 */
	public Order(int orderNumber, int orderId, LocalDate orderDate, int visitorNumber,
			int confCode, int userId, LocalDate placementDate, int parkId, Integer guideId,
			String orderStatus, String orderType) {

		this.orderNumber = orderNumber;
		this.orderId = orderId;
		this.orderDate = orderDate;
		this.visitorNumber = visitorNumber;
		this.confCode = confCode;
		this.userId = userId;
		this.placementDate = placementDate;
		this.parkId = parkId;
		this.guideId = guideId;
		this.orderStatus = orderStatus;
		this.orderType = orderType;
	}
	
	/*
	 * used for making new orders
	 * */	
	public Order(LocalDate orderDate, int visitorNumber,int userId, 
			String parkName) {

		this.orderDate = orderDate;
		this.visitorNumber = visitorNumber;
		this.userId = userId;
		this.parkName = parkName;
		this.guideId = userId;
		orderStatus = "pending";
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
	 * getter that returns the park ID
	 * 
	 * @return the park ID
	 */
	public Integer getParkId() {
		return parkId;
	}

	/*
	 * getter that returns the guide ID
	 * 
	 * @return the guide ID, or null if the order is private
	 */
	public Integer getGuideId() {
		return guideId;
	}

	/*
	 * getter that returns the order status
	 * 
	 * @return the order status
	 */
	public String getOrderStatus() {
		return orderStatus;
	}

	/*
	 * getter that returns the order type
	 * 
	 * @return the order type
	 */
	public String getOrderType() {
		return orderType;
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
	 * setter that updates the park ID
	 * 
	 * this function is used in case of updates
	 * 
	 * @param parkId the new park ID
	 */
	public void setParkId(int parkId) {
		this.parkId = parkId;
	}

	/*
	 * setter that updates the guide ID
	 * 
	 * this function is used in case of updates
	 * 
	 * @param guideId the new guide ID, or null for private orders
	 */
	public void setGuideId(Integer guideId) {
		this.guideId = guideId;
	}

	/*
	 * setter that updates the order status
	 * 
	 * this function is used in case of updates
	 * 
	 * @param orderStatus the new order status
	 */
	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}

	/*
	 * setter that updates the order type
	 * 
	 * this function is used in case of updates
	 * 
	 * @param orderType the new order type
	 */
	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	/*
	 * returns the order information as a string
	 * 
	 * @return a string representation of the order
	 */
	@Override
	public String toString() {
		return "Order Number: " + orderId + " " +
				"# Visitors: " + visitorNumber + " " +
				"Code: " + confCode + " " +
				"ID: " + userId + " " +
				"Park ID: " + parkId + " " +
				"Guide ID: " + guideId + " " +
				"Status: " + orderStatus + " " +
				"Type: " + orderType + " " +
				"Order Date: " + orderDate.toString() + " " +
				"Placement Date: " + placementDate.toString();
	}
}