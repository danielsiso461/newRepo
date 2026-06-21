package common;

import java.io.Serializable;

/**
 * Represents a park in the GoNature system.
 */
public class Park implements Serializable {

    private static final long serialVersionUID = 1L;

    private int parkId;
    private String parkName;
    private int maxCapacity;
    private int currentVisitors;
    private int placesForUnplannedVisitors;
    private double estimatedVisitDurationHours;
    private double fullEntryPrice;
    private boolean active;

    /*
     * Stores the park promotion discount percent.
     * Example:
     * 0.00  means no discount.
     * 10.00 means 10% discount.
     */
    private double promotions;

    public Park(int parkId, String parkName, int maxCapacity,
            int currentVisitors, int placesForUnplannedVisitors,
            double estimatedVisitDurationHours, double fullEntryPrice,
            boolean active, double promotions) {

        this.parkId = parkId;
        this.parkName = parkName;
        this.maxCapacity = maxCapacity;
        this.currentVisitors = currentVisitors;
        this.placesForUnplannedVisitors = placesForUnplannedVisitors;
        this.estimatedVisitDurationHours = estimatedVisitDurationHours;
        this.fullEntryPrice = fullEntryPrice;
        this.active = active;
        this.promotions = promotions;
    }

    /*
     * Backward-compatible constructor for older code that does not send
     * currentVisitors yet.
     */
    public Park(int parkId, String parkName, int maxCapacity,
            int placesForUnplannedVisitors, double estimatedVisitDurationHours,
            double fullEntryPrice, boolean active, double promotions) {

        this(
                parkId,
                parkName,
                maxCapacity,
                0,
                placesForUnplannedVisitors,
                estimatedVisitDurationHours,
                fullEntryPrice,
                active,
                promotions
        );
    }

    /*
     * Backward-compatible constructor.
     * If some older code still sends boolean promotions, true is treated as 1%.
     * Prefer using the double promotions constructor.
     */
    public Park(int parkId, String parkName, int maxCapacity,
            int placesForUnplannedVisitors, double estimatedVisitDurationHours,
            double fullEntryPrice, boolean active, boolean hasPromotions) {

        this(
                parkId,
                parkName,
                maxCapacity,
                0,
                placesForUnplannedVisitors,
                estimatedVisitDurationHours,
                fullEntryPrice,
                active,
                hasPromotions ? 1.00 : 0.00
        );
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

    public double getPromotions() {
        return promotions;
    }

    public double getPromotionPercent() {
        return promotions;
    }

    public boolean hasPromotions() {
        return promotions > 0;
    }

    public void setParkId(int parkId) {
        this.parkId = parkId;
    }

    public void setParkName(String parkName) {
        this.parkName = parkName;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public void setCurrentVisitors(int currentVisitors) {
        this.currentVisitors = currentVisitors;
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

    public void setPromotions(double promotions) {
        this.promotions = promotions;
    }

    @Override
    public String toString() {
        return parkName;
    }
}