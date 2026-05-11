package common;

import java.io.Serializable;
import java.time.LocalDate;

public class OrderRow implements Serializable {
	private static final long serialVersionUID = 1L;
	private int orderNumber, orderId, visitorNumber, confCode, userId;
	private LocalDate orderDate, placementDate;
	
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
	
	public Integer getOrderNumber() {
		return orderNumber;
	}
	
	public Integer getOrderId() {
		return orderId;
	}
	
	public Integer getVisitorNumber() {
		return visitorNumber;
	}

	public Integer getConfCode() {
		return confCode;
	}

	public Integer getUserId() {
		return userId;
	}

	public LocalDate getOrderDate() {
		return orderDate;
	}

	public LocalDate getPlacementDate() {
		return placementDate;
	}
	
	public void setOrderDate(LocalDate orderDate) {
		this.orderDate = orderDate;
	}
	
	public void setNumberOfVisitors(int visitorNumber) {
		this.visitorNumber = visitorNumber;
	}
	
	public String toString() {
		return "Order Number: " + orderId + " " + 
				"# Visitors: " + visitorNumber + " " + 
				"Code: " + confCode + " " +
				"ID: " + userId + " " +
				"Order Date: " + orderDate.toString() + " " +
				"Placement Date: " + placementDate.toString();
	}

}
