package common;

import java.time.LocalDate;

public class OrderRow {
	private int orderNum, numberOfVisitors, confirmationCode, id;
	private LocalDate orderDate, dateOfPlacingOrder;

	public OrderRow(int orderNum, LocalDate orderDate, int numberOfVisitors, 
			int confirmationCode, int id, LocalDate dateOfPlacingOrder) {
		this.orderNum = orderNum;
		this.orderDate = orderDate;
		this.numberOfVisitors = numberOfVisitors;
		this.confirmationCode = confirmationCode;
		this.id = id;
		this.dateOfPlacingOrder = dateOfPlacingOrder;
	}
	
	public int getOrderNum() {
		return orderNum;
	}
	
	public int getNumberOfVisitors() {
		return numberOfVisitors;
	}

	public int getConfirmationCode() {
		return confirmationCode;
	}

	public int getId() {
		return id;
	}

	public LocalDate getOrderDate() {
		return orderDate;
	}

	public LocalDate getDateOfPlacingOrder() {
		return dateOfPlacingOrder;
	}

}
