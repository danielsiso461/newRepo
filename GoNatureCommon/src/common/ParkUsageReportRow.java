package common;

import java.io.Serializable;

public class ParkUsageReportRow implements Serializable {

    private static final long serialVersionUID = 1L;

    private String parkName;
    private int numberOfVisits;
    private double averageOccupancyPercent;
    private double maxOccupancyPercent;

    public ParkUsageReportRow(String parkName, int numberOfVisits,
                              double averageOccupancyPercent, double maxOccupancyPercent) {
        this.parkName = parkName;
        this.numberOfVisits = numberOfVisits;
        this.averageOccupancyPercent = averageOccupancyPercent;
        this.maxOccupancyPercent = maxOccupancyPercent;
    }

    public String getParkName() {
        return parkName;
    }

    public int getNumberOfVisits() {
        return numberOfVisits;
    }

    public double getAverageOccupancyPercent() {
        return averageOccupancyPercent;
    }

    public double getMaxOccupancyPercent() {
        return maxOccupancyPercent;
    }
}