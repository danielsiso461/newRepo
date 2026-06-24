package common;

import java.io.Serializable;

public class VisitorReportRow implements Serializable {

    private static final long serialVersionUID = 1L;

    private int parkId;
    private String parkName;
    private String visitorType;
    private int numberOfVisits;
    private int totalVisitors;

    public VisitorReportRow(int parkId, String parkName, String visitorType,
                            int numberOfVisits, int totalVisitors) {
        this.parkId = parkId;
        this.parkName = parkName;
        this.visitorType = visitorType;
        this.numberOfVisits = numberOfVisits;
        this.totalVisitors = totalVisitors;
    }

    public int getParkId() {
        return parkId;
    }

    public String getParkName() {
        return parkName;
    }

    public String getVisitorType() {
        return visitorType;
    }

    public int getNumberOfVisits() {
        return numberOfVisits;
    }

    public int getTotalVisitors() {
        return totalVisitors;
    }
}