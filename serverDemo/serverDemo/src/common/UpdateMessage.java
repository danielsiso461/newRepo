package common;

import java.io.Serializable;
import java.time.LocalDate;

public class UpdateMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	private int orderNumber;
	private LocalDate updateDate = null;
	private Integer numberOfVisitors = 0;
	
	public UpdateMessage(LocalDate updateDate, Integer numberOfVisitors, int orderNumber) {
		this.orderNumber = orderNumber;
		if(updateDate != null)
			this.updateDate = updateDate;
		if(numberOfVisitors != 0)
			this.numberOfVisitors = numberOfVisitors;
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
}
