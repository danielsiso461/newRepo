package common;

import java.io.Serializable;

/**
 * Represents a park in the GoNature system.
 */
public class Park implements Serializable {
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
     * the park's max capacity
     */
    private int maxCapacity;
    /**
     * the current visitors in the park
     */
    private int currentVisitors;
    /**
     * the places for unplanned visitors in the park
     */
    private int placesForUnplannedVisitors;
    /**
     * the estimated visit duration in hours of the park
     */
    private double estimatedVisitDurationHours;
    /**
     * the park's full entry price
     */
    private double fullEntryPrice;
    /**
     * the park's activity status
     */
    private boolean active;

    /**
     * Stores the park promotion discount percent.
     * Example:
     * 0.00  means no discount.
     * 10.00 means 10% discount.
     */
    private double promotions;
    /**
     * Creates a new park.
     *
     * @param parkId the park ID
     * @param parkName the park name
     * @param maxCapacity the maximum number of visitors allowed
     * @param currentVisitors the current number of visitors
     * @param placesForUnplannedVisitors the number of places reserved for unplanned visitors
     * @param estimatedVisitDurationHours the estimated visit duration in hours
     * @param fullEntryPrice the full entry price
     * @param active whether the park is active
     * @param promotions the promotion discount percentage
     */
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

    /**
     * Backward-compatible constructor for older code that does not send
     * currentVisitors yet.
     * @param parkId the id of the park
     * @param parkName the name of the park
     * @param maxCapacity the max capacity of the park
     * @param placesForUnplannedVisitors places for unplanned visitors in the park
     * @param estimatedVisitDurationHours the estimated visit duration in hours of the park
     * @param fullEntryPrice the full entry price to the park
     * @param active activity status of the park
     * @param promotions sale promotions of the park
     */
    @Deprecated
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

    /**
     * Backward-compatible constructor.
     * If some older code still sends boolean promotions, true is treated as 1%.
     * Prefer using the double promotions constructor.
     * @param parkId the id of the park
     * @param parkName the name of the park
     * @param maxCapacity the max capacity of the park
     * @param placesForUnplannedVisitors places for unplanned visitors in the park
     * @param estimatedVisitDurationHours the estimated visit duration in hours of the park
     * @param fullEntryPrice the full entry price to the park
     * @param active activity status of the park
     * @param hasPromotions whether the park has promotions
     */
    @Deprecated
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
    /**
     * Returns the park ID.
     *
     * @return the park ID
     */
    public int getParkId() {
        return parkId;
    }
    /**
     * Returns the park name.
     *
     * @return the park name
     */
    public String getParkName() {
        return parkName;
    }
    /**
     * Returns the maximum capacity.
     *
     * @return the maximum capacity
     */
    public int getMaxCapacity() {
        return maxCapacity;
    }
    /**
     * Returns the current number of visitors.
     *
     * @return the current number of visitors
     */
    public int getCurrentVisitors() {
        return currentVisitors;
    }
    /**
     * Returns the number of available places.
     *
     * @return the number of available places
     */
    public int getAvailablePlaces() {
        return maxCapacity - currentVisitors;
    }
    /**
     * Returns the number of places reserved for unplanned visitors.
     *
     * @return the number of reserved places
     */
    public int getPlacesForUnplannedVisitors() {
        return placesForUnplannedVisitors;
    }
    /**
     * Returns the estimated visit duration.
     *
     * @return the estimated visit duration in hours
     */
    public double getEstimatedVisitDurationHours() {
        return estimatedVisitDurationHours;
    }
    /**
     * Returns the full entry price.
     *
     * @return the full entry price
     */
    public double getFullEntryPrice() {
        return fullEntryPrice;
    }
    /**
     * Returns whether the park is active.
     *
     * @return true if the park is active
     */
    public boolean isActive() {
        return active;
    }
    /**
     * Returns the promotion discount percentage.
     *
     * @return the promotion discount percentage
     */
    public double getPromotions() {
        return promotions;
    }
    /**
     * Returns the promotion discount percentage.
     *
     * @return the promotion discount percentage
     */
    public double getPromotionPercent() {
        return promotions;
    }
    /**
     * Returns whether the park has an active promotion.
     *
     * @return true if a promotion is available
     */
    public boolean hasPromotions() {
        return promotions > 0;
    }
    /**
     * Sets the park ID.
     *
     * @param parkId the park ID
     */
    public void setParkId(int parkId) {
        this.parkId = parkId;
    }
    /**
     * Sets the park name.
     *
     * @param parkName the park name
     */
    public void setParkName(String parkName) {
        this.parkName = parkName;
    }
    /**
     * Sets the maximum capacity.
     *
     * @param maxCapacity the maximum capacity
     */
    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
    /**
     * Sets the current number of visitors.
     *
     * @param currentVisitors the current number of visitors
     */
    public void setCurrentVisitors(int currentVisitors) {
        this.currentVisitors = currentVisitors;
    }
    /**
     * Sets the number of places reserved for unplanned visitors.
     *
     * @param placesForUnplannedVisitors the number of reserved places
     */
    public void setPlacesForUnplannedVisitors(int placesForUnplannedVisitors) {
        this.placesForUnplannedVisitors = placesForUnplannedVisitors;
    }
    /**
     * Sets the estimated visit duration.
     *
     * @param estimatedVisitDurationHours the estimated visit duration in hours
     */
    public void setEstimatedVisitDurationHours(double estimatedVisitDurationHours) {
        this.estimatedVisitDurationHours = estimatedVisitDurationHours;
    }
    /**
     * Sets the full entry price.
     *
     * @param fullEntryPrice the full entry price
     */
    public void setFullEntryPrice(double fullEntryPrice) {
        this.fullEntryPrice = fullEntryPrice;
    }
    /**
     * Sets whether the park is active.
     *
     * @param active whether the park is active
     */
    public void setActive(boolean active) {
        this.active = active;
    }
    /**
     * Sets the promotion discount percentage.
     *
     * @param promotions the promotion discount percentage
     */
    public void setPromotions(double promotions) {
        this.promotions = promotions;
    }
    /**
     * standard toString method
     */
    @Override
    public String toString() {
        return parkName;
    }
}