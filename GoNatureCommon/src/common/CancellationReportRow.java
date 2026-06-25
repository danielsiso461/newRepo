package common;

import java.io.Serializable;
/**
 * this class represents a cancellation report row in the DB
 */
public class CancellationReportRow implements Serializable {
	/**
	 * serial version UID for serialization
	 */
    private static final long serialVersionUID = 1L;
    /**
     * the id of the park of the report
     */
    private int parkId;
    /**
     * the name of the park of the report
     */
    private String parkName;
    /**
     * the status of the report
     */
    private String status;
    /**
     * the reason for the request
     */
    private String reason;
    /**
     * count of total cancellations
     */
    private int totalCancellations;
    /**
     * the average of days before visit
     */
    private double averageDaysBeforeVisit;
    /**
     * constructor for a cancellation report row
     * @param parkId the id of the park
     * @param parkName the name of the park
     * @param status the status of the report
     * @param reason the reason for the report
     * @param totalCancellations the count of total cancellations
     * @param averageDaysBeforeVisit the average of days before visit
     */
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
    /**
     * getter for park id
     * @return the park id
     */
    public int getParkId() {
        return parkId;
    }
    /**
     * getter for park name
     * @return the park name
     */
    public String getParkName() {
        return parkName;
    }
    /**
     * the getter for report status
     * @return the report status
     */
    public String getStatus() {
        return status;
    }
    /**
     * the getter for report reason
     * @return the report reason
     */
    public String getReason() {
        return reason;
    }
    /**
     * the getter for total cancellations count
     * @return total cancellations count
     */
    public int getTotalCancellations() {
        return totalCancellations;
    }
    /**
     * the getter for average days before visit
     * @return the average of days before visit
     */
    public double getAverageDaysBeforeVisit() {
        return averageDaysBeforeVisit;
    }
}