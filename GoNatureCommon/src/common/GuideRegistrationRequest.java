package common;

import java.io.Serializable;

/**
 * This class represents a request to register a subscriber as a guide.
 */
public class GuideRegistrationRequest implements Serializable {
	/**
	 * serial version UID for serialization
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * the subscriber's id
	 */
	private int subscriberId;
	/**
	 * the authorizing employee's id
	 */
	private int authorizedByEmployeeId;
	/**
	 * the organization name of the guide
	 */
	private String organizationName;
	/**
	 * the activity status of the guide
	 */
	private String guideStatus;
	/**
	 * constructor for a guide registration request object
	 * @param subscriberId the subscriber's id
	 * @param organizationName the organization name of the guide
	 * @param guideStatus the activity status of the guide
	 * @param authorizedByEmployeeId the authorizing employee's id
	 */
	public GuideRegistrationRequest(int subscriberId, String organizationName,
			String guideStatus, int authorizedByEmployeeId) {
		this.subscriberId = subscriberId;
		this.organizationName = organizationName;
		this.guideStatus = guideStatus;
		this.authorizedByEmployeeId = authorizedByEmployeeId;
	}
	/**
	 * getter for the subscriber's id
	 * @return the subscriber's id
	 */
	public int getSubscriberId() {
		return subscriberId;
	}
	/**
	 * getter for the guide's organization's name
	 * @return the guide's organization's name
	 */
	public String getOrganizationName() {
		return organizationName;
	}
	/**
	 * getter for the authorizing employee's id
	 * @return the authorizing employee's id
	 */
	public int getAuthorizedByEmployeeId() {
		return authorizedByEmployeeId;
	}
	/**
	 * getter for the guide's status
	 * @return the guide's status
	 */
	public String getGuideStatus() {
		return guideStatus;
	}
}
