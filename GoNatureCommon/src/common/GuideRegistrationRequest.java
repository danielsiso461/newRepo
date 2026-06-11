package common;

import java.io.Serializable;

public class GuideRegistrationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private int subscriberId;
    private int authorizedByEmployeeId;
    private String organizationName;
    private String guideStatus;

    public GuideRegistrationRequest(int subscriberId, int authorizedByEmployeeId, String organizationName, String guideStatus) {
        this.subscriberId = subscriberId;
        this.authorizedByEmployeeId = authorizedByEmployeeId;
        this.organizationName = organizationName;
        this.guideStatus = guideStatus;
    }

    public int getSubscriberId() {
        return subscriberId;
    }

    public int getAuthorizedByEmployeeId() {
        return authorizedByEmployeeId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public String getGuideStatus() {
        return guideStatus;
    }

    @Override
    public String toString() {
        return "GuideRegistrationRequest{" +
                "subscriberId=" + subscriberId +
                ", authorizedByEmployeeId=" + authorizedByEmployeeId +
                ", organizationName='" + organizationName + '\'' +
                ", guideStatus='" + guideStatus + '\'' +
                '}';
    }
}
