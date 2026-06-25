package common;

import java.io.Serializable;

/**
 * This class represents a park entrance control request or response.
 *
 * It is used when an employee checks visitors into the park, checks visitors out
 * of the park, or handles an occasional visit.
 */
public class ParkEntranceMessage implements Serializable {
	/**
	 * serial version UID for serialization
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The numeric code used as QR / confirmation code simulation.
	 */
	private int confirmationCode;

	/**
	 * The park ID where the entrance or exit action is performed.
	 */
	private int parkId;

	/**
	 * The employee ID that handles the entrance or exit.
	 */
	private int employeeId;

	/**
	 * The actual number of visitors that entered or exited the park.
	 */
	private int actualNumberOfVisitors;

	/**
	 * The visit ID created or closed by the server.
	 */
	private int visitId;

	/**
	 * The order number connected to the visit, if the visit is based on an order.
	 */
	private int orderNumber;

	/**
	 * The subscriber ID connected to the order or visit.
	 */
	private int subscriberId;

	/**
	 * The identification method used at the entrance, for example confirmation_code
	 * or id_number.
	 */
	private String identificationMethod;

	/**
	 * A server response message that can be displayed to the employee.
	 */
	private String responseMessage;

	/**
	 * The number of visitors currently inside the park.
	 */
	private int currentVisitors;

	/**
	 * Creates an empty park entrance message.
	 */
	public ParkEntranceMessage() {
	}

	/**
	 * Creates a park entrance message for check-in using a confirmation code.
	 *
	 * @param confirmationCode       the QR / confirmation code simulation
	 * @param parkId                 the park ID
	 * @param employeeId             the employee ID
	 * @param actualNumberOfVisitors the actual number of visitors entering
	 */
	public ParkEntranceMessage(int confirmationCode, int parkId, int employeeId, int actualNumberOfVisitors) {
		this.confirmationCode = confirmationCode;
		this.parkId = parkId;
		this.employeeId = employeeId;
		this.actualNumberOfVisitors = actualNumberOfVisitors;
		this.identificationMethod = "confirmation_code";
	}
	/**
	 * Returns the confirmation code.
	 *
	 * @return the confirmation code
	 */
	public int getConfirmationCode() {
		return confirmationCode;
	}
	/**
	 * Sets the confirmation code.
	 *
	 * @param confirmationCode the confirmation code
	 */
	public void setConfirmationCode(int confirmationCode) {
		this.confirmationCode = confirmationCode;
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
	 * Returns the employee ID.
	 *
	 * @return the employee ID
	 */
	public int getEmployeeId() {
		return employeeId;
	}
	/**
	 * Sets the employee ID.
	 *
	 * @param employeeId the employee ID
	 */
	public void setEmployeeId(int employeeId) {
		this.employeeId = employeeId;
	}
	/**
	 * Returns the actual number of visitors.
	 *
	 * @return the number of visitors
	 */
	public int getActualNumberOfVisitors() {
		return actualNumberOfVisitors;
	}
	/**
	 * Sets the actual number of visitors.
	 *
	 * @param actualNumberOfVisitors the number of visitors
	 */
	public void setActualNumberOfVisitors(int actualNumberOfVisitors) {
		this.actualNumberOfVisitors = actualNumberOfVisitors;
	}
	/**
	 * Returns the visit ID.
	 *
	 * @return the visit ID
	 */
	public int getVisitId() {
		return visitId;
	}
	/**
	 * Sets the visit ID.
	 *
	 * @param visitId the visit ID
	 */
	public void setVisitId(int visitId) {
		this.visitId = visitId;
	}
	/**
	 * Returns the order number.
	 *
	 * @return the order number
	 */
	public int getOrderNumber() {
		return orderNumber;
	}
	/**
	 * Sets the order number.
	 *
	 * @param orderNumber the order number
	 */
	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}
	/**
	 * Returns the subscriber ID.
	 *
	 * @return the subscriber ID
	 */
	public int getSubscriberId() {
		return subscriberId;
	}
	/**
	 * Sets the subscriber ID.
	 *
	 * @param subscriberId the subscriber ID
	 */
	public void setSubscriberId(int subscriberId) {
		this.subscriberId = subscriberId;
	}
	/**
	 * Returns the identification method.
	 *
	 * @return the identification method
	 */
	public String getIdentificationMethod() {
		return identificationMethod;
	}
	/**
	 * Sets the identification method.
	 *
	 * @param identificationMethod the identification method
	 */
	public void setIdentificationMethod(String identificationMethod) {
		this.identificationMethod = identificationMethod;
	}
	/**
	 * Returns the response message.
	 *
	 * @return the response message
	 */
	public String getResponseMessage() {
		return responseMessage;
	}
	/**
	 * Sets the response message.
	 *
	 * @param responseMessage the response message
	 */
	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}
	/**
	 * Returns the current number of visitors in the park.
	 *
	 * @return the current visitor count
	 */
	public int getCurrentVisitors() {
		return currentVisitors;
	}
	/**
	 * Sets the current number of visitors in the park.
	 *
	 * @param currentVisitors the current visitor count
	 */
	public void setCurrentVisitors(int currentVisitors) {
		this.currentVisitors = currentVisitors;
	}
}