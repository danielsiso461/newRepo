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
	/**
	 * serial version UID for serialization
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * the id of the request
	 */
	private int requestId;
	/**
	 * the park id 
	 */
	private int parkId;
	/**
	 * the name of the parameter to change
	 */
	private String parameterName;
	/**
	 * the old value of the parameter
	 */
	private String oldValue;
	/**
	 * the current value of the parameter
	 */
	private String currentValue;
	/**
	 * the new value to change to
	 */
	private String newValue;
	/**
	 * the status of the request
	 */
	private String requestStatus;
	/**
	 * Creates a new park parameter change request.
	 *
	 * @param requestId the request ID
	 * @param parkId the park ID
	 * @param parameterName the name of the parameter to change
	 * @param oldValue the original value of the parameter
	 * @param newValue the requested new value
	 * @param requestStatus the current status of the request
	 */
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
	/**
	 * Returns the request ID.
	 *
	 * @return the request ID
	 */
	public int getRequestId() {
		return requestId;
	}
	/**
	 * Sets the request ID.
	 *
	 * @param requestId the request ID
	 */
	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}
	/**
	 * Returns the park ID.
	 *
	 * @return the park ID
	 */
	public int getParkId() {
		return parkId;
	}
	/**
	 * Sets the park ID.
	 *
	 * @param parkId the park ID
	 */
	public void setParkId(int parkId) {
		this.parkId = parkId;
	}
	/**
	 * Returns the parameter name.
	 *
	 * @return the parameter name
	 */
	public String getParameterName() {
		return parameterName;
	}
	/**
	 * Sets the parameter name.
	 *
	 * @param parameterName the parameter name
	 */
	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}
	/**
	 * Returns the original value.
	 *
	 * @return the original value
	 */
	public String getOldValue() {
		return oldValue;
	}
	/**
	 * Sets the original value.
	 *
	 * @param oldValue the original value
	 */
	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}
	/**
	 * Returns the current value.
	 *
	 * @return the current value
	 */
	public String getCurrentValue() {
		return currentValue;
	}
	/**
	 * Sets the current value.
	 *
	 * @param currentValue the current value
	 */
	public void setCurrentValue(String currentValue) {
		this.currentValue = currentValue;
	}
	/**
	 * Returns the requested new value.
	 *
	 * @return the new value
	 */
	public String getNewValue() {
		return newValue;
	}
	/**
	 * Sets the requested new value.
	 *
	 * @param newValue the new value
	 */
	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}
	/**
	 * Returns the request status.
	 *
	 * @return the request status
	 */
	public String getRequestStatus() {
		return requestStatus;
	}
	/**
	 * Sets the request status.
	 *
	 * @param requestStatus the request status
	 */
	public void setRequestStatus(String requestStatus) {
		this.requestStatus = requestStatus;
	}
	/**
	 * standard toString method
	 */
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