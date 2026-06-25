package common;

import java.io.Serializable;
/**
 * this class represents a report object of visit duration
 */
public class VisitDurationReportRow implements Serializable {
	/**
	 * serial version UID for serialization
	 */
    private static final long serialVersionUID = 1L;
    /**
     * the park name
     */
    private String parkName;
    /**
     * the type of visitor
     */
    private String visitorType;
    /**
     * the number of visits
     */
    private int numberOfVisits;
    /**
     * the average duration of visit in minutes
     */
    private double averageDurationMinutes;
    /**
     * constructor for a visit duration report (as a row in the DB)
     * @param parkName the park's name
     * @param visitorType the visitor type of the row
     * @param numberOfVisits the number of visits
     * @param averageDurationMinutes the average duration of visit in minutes
     */
    public VisitDurationReportRow(String parkName, String visitorType,
                                  int numberOfVisits, double averageDurationMinutes) {
        this.parkName = parkName;
        this.visitorType = visitorType;
        this.numberOfVisits = numberOfVisits;
        this.averageDurationMinutes = averageDurationMinutes;
    }
    /**
     * getter for the park name
     * @return the park name
     */
    public String getParkName() {
        return parkName;
    }
    /**
     * getter for the visitor type of the row
     * @return the visitor type of the row
     */
    public String getVisitorType() {
        return visitorType;
    }
    /**
     * getter for the number of visits
     * @return the number of visits
     */
    public int getNumberOfVisits() {
        return numberOfVisits;
    }
    /**
     * getter for the average duration of visit in minutes
     * @return the average duration of visit in minutes
     */
    public double getAverageDurationMinutes() {
        return averageDurationMinutes;
    }
}