package common;

import java.io.Serializable;

public class ReportRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String reportType;
    private int parkId;
    private int month;
    private int year;
    private int employeeId;

    public ReportRequest(String reportType, int parkId, int month, int year, int employeeId) {
        this.reportType = reportType;
        this.parkId = parkId;
        this.month = month;
        this.year = year;
        this.employeeId = employeeId;
    }

    public String getReportType() {
        return reportType;
    }

    public int getParkId() {
        return parkId;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public int getEmployeeId() {
        return employeeId;
    }
}