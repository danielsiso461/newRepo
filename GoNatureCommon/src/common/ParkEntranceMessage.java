package common;

import java.io.Serializable;

/*
 * This class represents a park entrance control request or response.
 *
 * It is used when an employee checks visitors into the park, checks visitors out
 * of the park, or handles an occasional visit.
 */
public class ParkEntranceMessage implements Serializable {
	private static final long serialVersionUID = 1L;

	/*
	 * The numeric code used as QR / confirmation code simulation.
	 */
	private int confirmationCode;

	/*
	 * The park ID where the entrance or exit action is performed.
	 */
	private int parkId;

	/*
	 * The employee ID that handles the entrance or exit.
	 */
	private int employeeId;

	/*
	 * The actual number of visitors that entered or exited the park.
	 */
	private int actualNumberOfVisitors;

	/*
	 * The visit ID created or closed by the server.
	 */
	private int visitId;

	/*
	 * The order number connected to the visit, if the visit is based on an order.
	 */
	private int orderNumber;

	/*
	 * The subscriber ID connected to the order or visit.
	 */
	private int subscriberId;

	/*
	 * The identification method used at the entrance, for example confirmation_code
	 * or id_number.
	 */
	private String identificationMethod;

	/*
	 * A server response message that can be displayed to the employee.
	 */
	private String responseMessage;

	/*
	 * The number of visitors currently inside the park.
	 */
	private int currentVisitors;

	/*
	 * Creates an empty park entrance message.
	 */
	public ParkEntranceMessage() {
	}

	/*
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

	public int getConfirmationCode() {
		return confirmationCode;
	}

	public void setConfirmationCode(int confirmationCode) {
		this.confirmationCode = confirmationCode;
	}

	public int getParkId() {
		return parkId;
	}

	public void setParkId(int parkId) {
		this.parkId = parkId;
	}

	public int getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(int employeeId) {
		this.employeeId = employeeId;
	}

	public int getActualNumberOfVisitors() {
		return actualNumberOfVisitors;
	}

	public void setActualNumberOfVisitors(int actualNumberOfVisitors) {
		this.actualNumberOfVisitors = actualNumberOfVisitors;
	}

	public int getVisitId() {
		return visitId;
	}

	public void setVisitId(int visitId) {
		this.visitId = visitId;
	}

	public int getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}

	public int getSubscriberId() {
		return subscriberId;
	}

	public void setSubscriberId(int subscriberId) {
		this.subscriberId = subscriberId;
	}

	public String getIdentificationMethod() {
		return identificationMethod;
	}

	public void setIdentificationMethod(String identificationMethod) {
		this.identificationMethod = identificationMethod;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public int getCurrentVisitors() {
		return currentVisitors;
	}

	public void setCurrentVisitors(int currentVisitors) {
		this.currentVisitors = currentVisitors;
	}
}