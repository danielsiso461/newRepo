package common;

import java.io.Serializable;

/**
 * Represents a park in the system.
 * 
 * The same class is used both for server-side park logic and for sending park
 * data to the client.
 */
public class Park implements Serializable {

    private static final long serialVersionUID = 1L;

    private int parkId;
    private String parkName;
    private Integer maxCapacity;
    private Integer placesForUnplannedVisitors;
    private Double estimatedVisitDurationHours;
    private Double fullEntryPrice;
    private Boolean active;
    private boolean promotions;

    /**
     * Creates a full Park object.
     */
    public Park(int parkId, String parkName, int maxCapacity, int placesForUnplannedVisitors,
                double estimatedVisitDurationHours, double fullEntryPrice,
                boolean active, boolean promotions) {

        this.parkId = parkId;
        this.parkName = parkName;
        this.maxCapacity = maxCapacity;
        this.placesForUnplannedVisitors = placesForUnplannedVisitors;
        this.estimatedVisitDurationHours = estimatedVisitDurationHours;
        this.fullEntryPrice = fullEntryPrice;
        this.active = active;
        this.promotions = promotions;
    }

    /**
     * Creates a Park object with public display data only.
     */
    public Park(int parkId, String parkName,
                double estimatedVisitDurationHours, double fullEntryPrice) {

        this(parkId, parkName, 0, 0,
                estimatedVisitDurationHours, fullEntryPrice,
                true, false);
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

    public int getPlacesForUnplannedVisitors() {
        return placesForUnplannedVisitors;
    }

    public double getEstimatedVisitDurationHours() {
        return estimatedVisitDurationHours;
    }

    public double getFullEntryPrice() {
        return fullEntryPrice;
    }

    public boolean isActive() {
        return active;
    }

    public boolean hasPromotions() {
        return promotions;
    }

    public void setParkName(String parkName) {
        this.parkName = parkName;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public void setPlacesForUnplannedVisitors(int placesForUnplannedVisitors) {
        this.placesForUnplannedVisitors = placesForUnplannedVisitors;
    }

    public void setEstimatedVisitDurationHours(double estimatedVisitDurationHours) {
        this.estimatedVisitDurationHours = estimatedVisitDurationHours;
    }

    public void setFullEntryPrice(double fullEntryPrice) {
        this.fullEntryPrice = fullEntryPrice;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setPromotions(boolean promotions) {
        this.promotions = promotions;
    }

    /**
     * Used by ComboBox and ListView to display only the park name.
     */
    @Override
    public String toString() {
        return parkName;
    }
}