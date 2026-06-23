package common;

import java.io.Serializable;

/*
 * This class represents a request to register a subscriber as a guide.
 */
public class GuideRegistrationRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private int subscriberId;
	private int authorizedByEmployeeId;
	private String organizationName;
	private String guideStatus;

	public GuideRegistrationRequest(int subscriberId, String organizationName,
			String guideStatus, int authorizedByEmployeeId) {
		this.subscriberId = subscriberId;
		this.organizationName = organizationName;
		this.guideStatus = guideStatus;
		this.authorizedByEmployeeId = authorizedByEmployeeId;
	}

	public int getSubscriberId() {
		return subscriberId;
	}

	public String getOrganizationName() {
		return organizationName;
	}
	
	public int getAuthorizedByEmployeeId() {
		return authorizedByEmployeeId;
	}

	public String getGuideStatus() {
		return guideStatus;
	}
}
