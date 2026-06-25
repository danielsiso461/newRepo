package common;

import java.io.Serializable;
/**
 * this class represents a report request object
 */
public class ReportRequest implements Serializable {
	/**
	 * serial version UID for serialization
	 */
    private static final long serialVersionUID = 1L;
    /**
     * the type of report
     */
    private String reportType;
    /**
     * the park id
     */
    private int parkId;
    /**
     * the requested month
     */
    private int month;
    /**
     * the requested year
     */
    private int year;
    /**
     * the requesting employee's id
     */
    private int employeeId;
    /**
     * constructor of the report request object
     * @param reportType type of report
     * @param parkId id of the park
     * @param month report's requested month
     * @param year report's requested year
     * @param employeeId the requesting employee's id
     */
    public ReportRequest(String reportType, int parkId, int month, int year, int employeeId) {
        this.reportType = reportType;
        this.parkId = parkId;
        this.month = month;
        this.year = year;
        this.employeeId = employeeId;
    }
    /**
     * getter for the report's type
     * @return the report's type
     */
    public String getReportType() {
        return reportType;
    }
    /**
     * getter for the park id
     * @return the park id
     */
    public int getParkId() {
        return parkId;
    }
    /**
     * getter for the report requests's month
     * @return the report requests's month
     */
    public int getMonth() {
        return month;
    }
    /**
     * getter for the report requests's year
     * @return the report requests's year
     */
    public int getYear() {
        return year;
    }
    /**
     * getter for the requesting employee's id
     * @return the requesting employee's id
     */
    public int getEmployeeId() {
        return employeeId;
    }
}