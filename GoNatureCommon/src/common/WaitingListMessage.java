package common;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents the data sent between the client and the server
 * when a visitor asks to join the waiting list.
 *
 * This message is used when the requested park/date does not have enough
 * available capacity for a regular order.
 */
public class WaitingListMessage implements Serializable {
	/**
	 * serial version UID for serialization
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The subscriber who wants to join the waiting list.
	 */
	private int subscriberId;

	/**
	 * The requested park ID.
	 */
	private int parkId;
	/**
	 * The requested park name.
	 * 
	 * This value is used when the client screen has the park name but not the park ID.
	 */
	private String parkName;

	/**
	 * The requested visit date and time.
	 */
	private LocalDateTime requestedOrderDate;

	/**
	 * The number of visitors requested by the subscriber.
	 */
	private int numberOfVisitors;

	/**
	 * The subscriber's position in the waiting queue.
	 * This value is filled after the server inserts the request into the DB.
	 */
	private int queuePosition;

	/**
	 * The current waiting list status.
	 * Possible values include: waiting, offered, confirmed, expired, cancelled.
	 */
	private String waitingStatus;
	/**
	 * The waiting list request ID.
	 * 
	 * This value is used when the client wants to perform an action on an existing
	 * waiting list request, for example rejecting an offered request.
	 */
	private int waitingId;
	/**
	 * the subscriber's email
	 */
	private String subscriberEmail;
	/**
	 * the subscriber's phone
	 */
	private String subscriberPhone;

	/**
	 * Creates a new waiting list request message.
	 *
	 * @param subscriberId       the subscriber ID
	 * @param parkId             the requested park ID
	 * @param requestedOrderDate the requested visit date and time
	 * @param numberOfVisitors   the requested number of visitors
	 */
	public WaitingListMessage(int subscriberId, int parkId, LocalDateTime requestedOrderDate,
			int numberOfVisitors) {
		this.subscriberId = subscriberId;
		this.parkId = parkId;
		this.parkName = null;
		this.requestedOrderDate = requestedOrderDate;
		this.numberOfVisitors = numberOfVisitors;
		this.queuePosition = -1;
		this.waitingStatus = "waiting";
		this.waitingId = -1;
	}
	/**
	 * Creates a new waiting list request message using the park name.
	 *
	 * This constructor is used by client screens that display park names to the user
	 * and do not hold the park ID directly.
	 *
	 * @param subscriberId       the subscriber ID
	 * @param parkName           the requested park name
	 * @param requestedOrderDate the requested visit date and time
	 * @param numberOfVisitors   the requested number of visitors
	 */
	public WaitingListMessage(int subscriberId, String parkName, LocalDateTime requestedOrderDate,
			int numberOfVisitors) {
		this.subscriberId = subscriberId;
		this.parkId = -1;
		this.parkName = parkName;
		this.requestedOrderDate = requestedOrderDate;
		this.numberOfVisitors = numberOfVisitors;
		this.queuePosition = -1;
		this.waitingStatus = "waiting";
		this.waitingId = -1;
	}
	
	/**
	 * Creates a waiting list message for actions that use an existing waiting list
	 * request.
	 *
	 * This constructor is used when the client wants to perform an action on an
	 * existing waiting list row, for example rejecting an offered request.
	 *
	 * @param waitingId the waiting list request ID
	 */
	public WaitingListMessage(int waitingId) {
		this.waitingId = waitingId;
		this.subscriberId = -1;
		this.parkId = -1;
		this.parkName = null;
		this.requestedOrderDate = null;
		this.numberOfVisitors = -1;
		this.queuePosition = -1;
		this.waitingStatus = null;
	}
	
	/**
	 * Returns the waiting list request ID.
	 *
	 * @return the waiting list request ID
	 */
	public int getWaitingId() {
		return waitingId;
	}

	/**
	 * Updates the waiting list request ID.
	 *
	 * @param waitingId the waiting list request ID
	 */
	public void setWaitingId(int waitingId) {
		this.waitingId = waitingId;
	}
	/**
	 * getter for subscriber id
	 * @return subscriber id
	 */
	public int getSubscriberId() {
		return subscriberId;
	}
	/**
	 * getter for park id
	 * @return park id
	 */
	public int getParkId() {
		return parkId;
	}
	/**
	 * Updates the park ID after the server resolves the park name.
	 *
	 * @param parkId the resolved park ID
	 */
	public void setParkId(int parkId) {
		this.parkId = parkId;
	}

	/**
	 * Returns the requested park name.
	 *
	 * @return the requested park name
	 */
	public String getParkName() {
		return parkName;
	}
	/**
	 * getter for the requested order date
	 * @return the requested order date
	 */
	public LocalDateTime getRequestedOrderDate() {
		return requestedOrderDate;
	}
	/**
	 * getter for the number of visitors
	 * @return the number of visitors
	 */
	public int getNumberOfVisitors() {
		return numberOfVisitors;
	}
	/**
	 * getter for the queue position
	 * @return the queue position
	 */
	public int getQueuePosition() {
		return queuePosition;
	}
	/**
	 * getter for the waiting status
	 * @return the waiting status
	 */
	public String getWaitingStatus() {
		return waitingStatus;
	}

	/**
	 * Updates the queue position after the server inserts the request into the DB.
	 *
	 * @param queuePosition the assigned queue position
	 */
	public void setQueuePosition(int queuePosition) {
		this.queuePosition = queuePosition;
	}

	/**
	 * Updates the waiting status after the server handles the request.
	 *
	 * @param waitingStatus the new waiting status
	 */
	public void setWaitingStatus(String waitingStatus) {
		this.waitingStatus = waitingStatus;
	}
	/**
	 * standard toString method
	 */
	@Override
	public String toString() {
		return "WaitingListMessage [subscriberId=" + subscriberId +
				", parkId=" + parkId +
				", parkName=" + parkName +
				", requestedOrderDate=" + requestedOrderDate +
				", numberOfVisitors=" + numberOfVisitors +
				", queuePosition=" + queuePosition +
				", waitingStatus=" + waitingStatus + "]";
	}
	/**
	 * Returns the subscriber email used for notification simulation.
	 *
	 * @return the subscriber email
	 */
	public String getSubscriberEmail() {
		return subscriberEmail;
	}

	/**
	 * Sets the subscriber email used for notification simulation.
	 *
	 * @param subscriberEmail the subscriber email
	 */
	public void setSubscriberEmail(String subscriberEmail) {
		this.subscriberEmail = subscriberEmail;
	}

	/**
	 * Returns the subscriber phone used for notification simulation.
	 *
	 * @return the subscriber phone
	 */
	public String getSubscriberPhone() {
		return subscriberPhone;
	}

	/**
	 * Sets the subscriber phone used for notification simulation.
	 *
	 * @param subscriberPhone the subscriber phone
	 */
	public void setSubscriberPhone(String subscriberPhone) {
		this.subscriberPhone = subscriberPhone;
	}
}