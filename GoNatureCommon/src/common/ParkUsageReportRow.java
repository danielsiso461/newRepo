package common;

import java.io.Serializable;
/**
 * this class represents a row of data in the park usage report
 */
public class ParkUsageReportRow implements Serializable {
	/**
	 * serial version UID for serialization
	 */
    private static final long serialVersionUID = 1L;
    /**
     * the name of the park
     */
    private String parkName;
    /**
     * the number of visits in the park
     */
    private int numberOfVisits;
    /**
     * the average percent of occupancy of the park
     */
    private double averageOccupancyPercent;
    /**
     * the max percent of occupancy of the park
     */
    private double maxOccupancyPercent;
    /**
     * constructor for the row of a park usage report
     * @param parkName the name of the park
     * @param numberOfVisits the number of visits in the report
     * @param averageOccupancyPercent the average percent of occupancy of the park
     * @param maxOccupancyPercent the max percent of occupancy of the park
     */
    public ParkUsageReportRow(String parkName, int numberOfVisits,
                              double averageOccupancyPercent, double maxOccupancyPercent) {
        this.parkName = parkName;
        this.numberOfVisits = numberOfVisits;
        this.averageOccupancyPercent = averageOccupancyPercent;
        this.maxOccupancyPercent = maxOccupancyPercent;
    }
    /**
     * getter for the park name
     * @return the park name
     */
    public String getParkName() {
        return parkName;
    }
    /**
     * getter for the number of visits
     * @return the number of visits
     */
    public int getNumberOfVisits() {
        return numberOfVisits;
    }
    /**
     * getter for the average percent of occupancy of the park
     * @return the average percent of occupancy of the park
     */
    public double getAverageOccupancyPercent() {
        return averageOccupancyPercent;
    }
    /**
     * getter for the max percent of occupancy of the park
     * @return the max percent of occupancy of the park
     */
    public double getMaxOccupancyPercent() {
        return maxOccupancyPercent;
    }
}