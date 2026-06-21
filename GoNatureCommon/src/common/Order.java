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
		ORDER_TYPE_PRIVATE = "private",
		ORDER_STATUS_PENDING = "pending",
		ORDER_STATUS_APPROVED = "approved",
		ORDER_STATUS_CANCELLED = "cancelled",
		ORDER_STATUS_EXPIRED = "expired",
		ORDER_STATUS_COMPLETED = "completed",
		ORDER_STATUS_NO_SHOW = "no_show";
	/*
	 * serial version UID for serialization
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * stores the order information
	 */
	private Integer orderNumber, orderId, visitorNumber, confCode, userId;
	
	/*
	 * stores the phone number of the user 
	 */
	private String phoneNumber = "";

	/*
	 * stores the park id of the order
	 */
	private Integer parkId;
	// used to get parkId on the server (park name is unique)
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
	 * stores the booked hour of the given order
	 * */
	private int orderHour;
	
	/*
	 * this field holds the email of the booking user
	*/
	private String email;
	/*
	 * true if the user is subscribed and false otherwise
	 */
	private boolean isSubscribed = false;
	
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
	 * constructor used for making new orders
	 * 
	 * @param orderDate			the requested order date
	 * @param visitorNumber		the requested number of visitors
	 * @param userId			the Id of the booking user
	 * @param parkName			the name of the park to visit
	 * @param orderHour			the hour at which the user wishes to visit
	 * @param email 			holds the email of the booking user
	 * */	
	public Order(LocalDate orderDate, int visitorNumber,int userId, 
			String parkName, int orderHour, String email) {

		this.orderDate = orderDate;
		this.visitorNumber = visitorNumber;
		this.userId = userId;
		this.parkName = parkName;
		orderStatus = ORDER_STATUS_PENDING;
		this.orderHour = orderHour;
		this.email = email;
	}
	
	/**
	 * Constructs an order using the same structure that was previously used by Order.
	 * <p>
	 * This constructor is useful when loading existing orders from the database,
	 * where the order number also represents the order ID, and the subscriber ID
	 * represents the user ID related to the order.
	 *
	 * @param orderNumber        the order number
	 * @param orderDate          the date of the order
	 * @param numberOfVisitors   the number of visitors in the order
	 * @param confirmationCode   the confirmation code of the order
	 * @param subscriberId       the subscriber ID related to the order
	 * @param dateOfPlacingOrder the date on which the order was placed
	 * @param parkId             the park ID of the order
	 * @param guideId            the guide ID, or null for private orders
	 * @param orderStatus        the current status of the order
	 * @param orderType          the type of the order
	 */
	public Order(int orderNumber, LocalDate orderDate, int numberOfVisitors,
			int confirmationCode, int subscriberId, LocalDate dateOfPlacingOrder,
			int parkId, Integer guideId, String orderStatus, String orderType) {

		this.orderNumber = orderNumber;
		this.orderId = orderNumber;
		this.orderDate = orderDate;
		this.visitorNumber = numberOfVisitors;
		this.confCode = confirmationCode;
		this.userId = subscriberId;
		this.placementDate = dateOfPlacingOrder;
		this.parkId = parkId;
		this.guideId = guideId;
		this.orderStatus = orderStatus;
		this.orderType = orderType;
	}
	
	/**
	 * Returns the subscriber ID related to the order.
	 * <p>
	 * In the current Order class, the subscriber ID is represented by the userId field.
	 * This method is kept for compatibility with code that previously worked with Order.
	 *
	 * @return the subscriber ID
	 */
	public Integer getSubscriberId() {
		return userId;
	}

	/**
	 * Returns the number of visitors in the order.
	 * <p>
	 * This method is an alias for getVisitorNumber and is kept for clearer naming
	 * and compatibility with existing UI code.
	 *
	 * @return the number of visitors
	 */
	public Integer getNumberOfVisitors() {
		return visitorNumber;
	}

	/**
	 * Returns the confirmation code of the order.
	 * <p>
	 * This method is an alias for getConfCode and is kept for compatibility
	 * with existing code that uses the full field name.
	 *
	 * @return the confirmation code
	 */
	public Integer getConfirmationCode() {
		return confCode;
	}

	/**
	 * Returns the date on which the order was placed.
	 * <p>
	 * This method is an alias for getPlacementDate and is kept for compatibility
	 * with existing code that uses the database field name.
	 *
	 * @return the date of placing the order
	 */
	public LocalDate getDateOfPlacingOrder() {
		return placementDate;
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
	 * getter that returns the park name
	 * 
	 * @return the park name
	 */
	public String getParkName(){
		return parkName;
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
	 * getter that returns the order hour
	 * 
	 * @return the hour of the order
	 * */
	public int getOrderHour() {
		return orderHour;
	}
	
	/*
	 * getter that returns the order email
	 * 
	 * @return the email of the order
	 * */
	public String getEmail() {
		return email;
	}
	
	/*
	 * sets the order number field
	 * @param orderNumber the number to set order number to
	 */
	public void setOrderNumber(Integer orderNumber) {
		this.orderNumber = orderNumber;
	}
	
	/*
	 * setter that updates the placement date
	 * 
	 * this function is used by the SERVER to set the date of the order placement
	 * it is done by the server to ensure uniformity
	 * 
	 * @param placementDate the order placement date
	 */
	public void setPlacementDate(LocalDate placementDate) {
		this.placementDate = placementDate;
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
	 * setter that updates the order ID
	 * 
	 * this function is used only when adding a new update to the DB
	 * 
	 * @param orderId the new order ID
	 */
	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}
	
	/*
	 * setter that updates the order confirmation code
	 * 
	 * this function is used only when adding a new update to the DB
	 * 
	 * @param confCode the new confirmation code
	 */
	public void setConfirmationCode(Integer confCode) {
		this.confCode = confCode;
	}
	
	/* 
	 * function that sets the isSubscribed flag to true
	 */
	public void setIsSubscribedToTrue() {
		this.isSubscribed = true;
	}
	
	/*
	 * getter that returns if the user is subscribed
	 * 
	 * @return true if the user is subscribed
	 * */
	public boolean getIsSubscribed() {
		return isSubscribed;
	}
	
	/* 
	 * setter that sets the phone number corresponding to the order
	 * 
	 * this function is used only after adding a new update to the DB
	 * if the user is subscribed
	 * 
	 * @param phoneNumber the phone number
	 */
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	/* 
	 * getter that return the phone number corresponding to the order
	 * 
	 * @return the phone number
	 */
	public String getPhoneNumber() {
		return phoneNumber;
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