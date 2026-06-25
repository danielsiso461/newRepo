package common;

import java.io.Serializable;

/**
 * Represents a request to change one of the park parameters.
 *
 * oldValue is the value that existed when the request was created.
 * currentValue is the value that currently exists in the park table,
 * and is used only for display in the approval screen.
 */
public class ParkParameterChangeRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private int requestId;
	private int parkId;
	private String parameterName;
	private String oldValue;
	private String currentValue;
	private String newValue;
	private String requestStatus;

	public ParkParameterChangeRequest(int requestId, int parkId,
			String parameterName, String oldValue, String newValue,
			String requestStatus) {

		this.requestId = requestId;
		this.parkId = parkId;
		this.parameterName = parameterName;
		this.oldValue = oldValue;
		this.currentValue = oldValue;
		this.newValue = newValue;
		this.requestStatus = requestStatus;
	}

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public int getParkId() {
		return parkId;
	}

	public void setParkId(int parkId) {
		this.parkId = parkId;
	}

	public String getParameterName() {
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getCurrentValue() {
		return currentValue;
	}

	public void setCurrentValue(String currentValue) {
		this.currentValue = currentValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	public String getRequestStatus() {
		return requestStatus;
	}

	public void setRequestStatus(String requestStatus) {
		this.requestStatus = requestStatus;
	}

	@Override
	public String toString() {
		return "ParkParameterChangeRequest{" +
				"requestId=" + requestId +
				", parkId=" + parkId +
				", parameterName='" + parameterName + '\'' +
				", oldValue='" + oldValue + '\'' +
				", currentValue='" + currentValue + '\'' +
				", newValue='" + newValue + '\'' +
				", requestStatus='" + requestStatus + '\'' +
				'}';
	}
}