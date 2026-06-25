package common;

import java.io.Serializable;
/**
 * this method represents a report row about visitors in the DB
 */
public class VisitorReportRow implements Serializable {
	/**
	 * serial version UID for serialization
	 */
    private static final long serialVersionUID = 1L;
    /**
     * the park's id
     */
    private int parkId;
    /**
     * the park's name
     */
    private String parkName;
    /**
     * the type of visitor the line represents
     */
    private String visitorType;
    /**
     * the number of visits
     */
    private int numberOfVisits;
    /**
     * the total number of visitors in the report
     */
    private int totalVisitors;
    /**
     * constructor for the report's row
     * @param parkId the park's id
     * @param parkName the park's name
     * @param visitorType the type of visitors of the row
     * @param numberOfVisits the number of visits
     * @param totalVisitors the total number of visitors
     */
    public VisitorReportRow(int parkId, String parkName, String visitorType,
                            int numberOfVisits, int totalVisitors) {
        this.parkId = parkId;
        this.parkName = parkName;
        this.visitorType = visitorType;
        this.numberOfVisits = numberOfVisits;
        this.totalVisitors = totalVisitors;
    }
    /**
     * getter for the park's id
     * @return the park's id
     */
    public int getParkId() {
        return parkId;
    }
    /**
     * getter for the park's name
     * @return the park's name
     */
    public String getParkName() {
        return parkName;
    }
    /**
     * getter for the visitor type
     * @return the visitor type
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
     * getter for the total number of visitors
     * @return the total number of visitors
     */
    public int getTotalVisitors() {
        return totalVisitors;
    }
}