package common;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a request to change one of the park parameters.
 */
public class ParkParameterChangeRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private int requestId;
	private int parkId;

	private int requestedByEmployeeId;
	private Integer approvedByEmployeeId;

	private String parameterName;
	private String oldValue;
	private String newValue;
	private String requestStatus;
	private String reviewNote;

	private LocalDateTime requestedAt;
	private LocalDateTime reviewedAt;

	public ParkParameterChangeRequest(int requestId, int parkId,
			String parameterName, String oldValue, String newValue,
			String requestStatus) {

		this.requestId = requestId;
		this.parkId = parkId;
		this.parameterName = parameterName;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.requestStatus = requestStatus;

		this.requestedByEmployeeId = 0;
		this.approvedByEmployeeId = null;
		this.requestedAt = null;
		this.reviewedAt = null;
		this.reviewNote = "";
	}

	public ParkParameterChangeRequest(int requestId, int parkId,
			int requestedByEmployeeId, Integer approvedByEmployeeId,
			String parameterName, String oldValue, String newValue,
			String requestStatus, LocalDateTime requestedAt,
			LocalDateTime reviewedAt, String reviewNote) {

		this.requestId = requestId;
		this.parkId = parkId;
		this.requestedByEmployeeId = requestedByEmployeeId;
		this.approvedByEmployeeId = approvedByEmployeeId;
		this.parameterName = parameterName;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.requestStatus = requestStatus;
		this.requestedAt = requestedAt;
		this.reviewedAt = reviewedAt;
		this.reviewNote = reviewNote;
	}

	public int getRequestId() {
		return requestId;
	}

	public int getParkId() {
		return parkId;
	}

	public int getRequestedByEmployeeId() {
		return requestedByEmployeeId;
	}

	public Integer getApprovedByEmployeeId() {
		return approvedByEmployeeId;
	}

	public String getParameterName() {
		return parameterName;
	}

	public String getOldValue() {
		return oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public String getRequestStatus() {
		return requestStatus;
	}

	public LocalDateTime getRequestedAt() {
		return requestedAt;
	}

	public LocalDateTime getReviewedAt() {
		return reviewedAt;
	}

	public String getReviewNote() {
		return reviewNote;
	}

	@Override
	public String toString() {
		return "Request #" + requestId + " - park " + parkId + " - "
				+ parameterName + ": " + oldValue + " -> " + newValue;
	}
}