package common;

import java.io.Serializable;

public class VisitDurationReportRow implements Serializable {

    private static final long serialVersionUID = 1L;

    private String parkName;
    private String visitorType;
    private int numberOfVisits;
    private double averageDurationMinutes;

    public VisitDurationReportRow(String parkName, String visitorType,
                                  int numberOfVisits, double averageDurationMinutes) {
        this.parkName = parkName;
        this.visitorType = visitorType;
        this.numberOfVisits = numberOfVisits;
        this.averageDurationMinutes = averageDurationMinutes;
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

    public double getAverageDurationMinutes() {
        return averageDurationMinutes;
    }
}