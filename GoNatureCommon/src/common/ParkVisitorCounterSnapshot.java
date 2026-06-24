package common;

import java.io.Serializable;

/**
 * Represents the current visitor counter status of a park.
 */
public class ParkVisitorCounterSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    private int parkId;
    private String parkName;
    private int maxCapacity;
    private int currentVisitors;

    public ParkVisitorCounterSnapshot(int parkId, String parkName,
            int maxCapacity, int currentVisitors) {

        this.parkId = parkId;
        this.parkName = parkName;
        this.maxCapacity = maxCapacity;
        this.currentVisitors = currentVisitors;
    }

    public int getParkId() {
        return parkId;
    }

    public String getParkName() {
        return parkName;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public int getCurrentVisitors() {
        return currentVisitors;
    }

    public int getAvailablePlaces() {
        return maxCapacity - currentVisitors;
    }

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