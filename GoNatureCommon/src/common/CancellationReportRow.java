package common;

import java.io.Serializable;

public class CancellationReportRow implements Serializable {

    private static final long serialVersionUID = 1L;

    private int parkId;
    private String parkName;
    private String status;
    private String reason;
    private int totalCancellations;
    private double averageDaysBeforeVisit;

    public CancellationReportRow(int parkId, String parkName, String status,
                                 String reason, int totalCancellations,
                                 double averageDaysBeforeVisit) {
        this.parkId = parkId;
        this.parkName = parkName;
        this.status = status;
        this.reason = reason;
        this.totalCancellations = totalCancellations;
        this.averageDaysBeforeVisit = averageDaysBeforeVisit;
    }

    public int getParkId() {
        return parkId;
    }

    public String getParkName() {
        return parkName;
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public int getTotalCancellations() {
        return totalCancellations;
    }

    public double getAverageDaysBeforeVisit() {
        return averageDaysBeforeVisit;
    }
}