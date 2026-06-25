package common;

import java.io.Serializable;

/**
 * Represents the current visitor counter status of a park.
 */
public class ParkVisitorCounterSnapshot implements Serializable {
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
     * the max capacity of the park
     */
    private int maxCapacity;
    /**
     * the current visitor in the park
     */
    private int currentVisitors;
    /**
     * constructor of visitor counter object
     * @param parkId the park's id
     * @param parkName the park's name
     * @param maxCapacity the max capacity of the park
     * @param currentVisitors the count of visitors in the park
     */
    public ParkVisitorCounterSnapshot(int parkId, String parkName,
            int maxCapacity, int currentVisitors) {

        this.parkId = parkId;
        this.parkName = parkName;
        this.maxCapacity = maxCapacity;
        this.currentVisitors = currentVisitors;
    }
    /**
     * getter for park id
     * @return park id
     */
    public int getParkId() {
        return parkId;
    }
    /**
     * getter for park name
     * @return park name
     */
    public String getParkName() {
        return parkName;
    }
    /**
     * getter for max capacity in the park
     * @return max capacity in the park
     */
    public int getMaxCapacity() {
        return maxCapacity;
    }
    /**
     * getter for count of current visitors in the park
     * @return count of current visitors in the park
     */
    public int getCurrentVisitors() {
        return currentVisitors;
    }
    /**
     * getter for count of available places in the park
     * @return count of available places in the park
     */
    public int getAvailablePlaces() {
        return maxCapacity - currentVisitors;
    }
    /**
     * standard toString method
     */
    @Override
    public String toString() {
        return "ParkVisitorCounterSnapshot{"
                + "parkId=" + parkId
                + ", parkName='" + parkName + '\''
                + ", maxCapacity=" + maxCapacity
                + ", currentVisitors=" + currentVisitors
                + '}';
    }
}