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
		this.requestedOrderDate = requestedOrderDate;
		this.numberOfVisitors = numberOfVisitors;
		this.queuePosition = -1;
		this.waitingStatus = "waiting";
	}

	public int getSubscriberId() {
		return subscriberId;
	}

	public int getParkId() {
		return parkId;
	}

	public LocalDateTime getRequestedOrderDate() {
		return requestedOrderDate;
	}

	public int getNumberOfVisitors() {
		return numberOfVisitors;
	}

	public int getQueuePosition() {
		return queuePosition;
	}

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

	@Override
	public String toString() {
		return "WaitingListMessage [subscriberId=" + subscriberId +
				", parkId=" + parkId +
				", requestedOrderDate=" + requestedOrderDate +
				", numberOfVisitors=" + numberOfVisitors +
				", queuePosition=" + queuePosition +
				", waitingStatus=" + waitingStatus + "]";
	}
}