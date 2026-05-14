package common;

import java.io.Serializable;
import java.time.LocalDate;
/*
 * this class represents the data in a <Message> class when the
 * requests and replies are regarding updating an order
 * we also hold the orderNumber relevant to the user
 */
public class UpdateMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	private String ordererId;
	private int orderNumber, orderId;
	private LocalDate updateDate = null;
	private Integer numberOfVisitors = 0;
	
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
	
	public int getOrderId() {
		return orderId;
	}
	
	public int getOrderNumber() {
		return orderNumber;
	}
	
	public LocalDate getUpdateDate() {
		return updateDate;
	}
	
	public int getNumberOfVisitors() {
		return numberOfVisitors.intValue();
	}
	
	public String getOrdererId() {
		return ordererId;
	}
}
