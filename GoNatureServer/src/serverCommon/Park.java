package serverCommon;

/**
 * This class represents a full park entity in the server side.
 * 
 * The class stores all park data that is needed by the server for management,
 * capacity checks, pricing, promotions, and internal park logic.
 * 
 * This class is not meant to be sent directly to the client, because it contains
 * internal management data such as maximum capacity and reserved places for
 * unplanned visitors.
 */
public class Park {

	/**
	 * The park ID.
	 */
	private int parkId;

	/**
	 * The park name.
	 */
	private String parkName;

	/**
	 * The maximum number of visitors allowed in the park.
	 */
	private int maxCapacity;

	/**
	 * The number of places reserved for unplanned visitors.
	 */
	private int placesForUnplannedVisitors;

	/**
	 * The estimated visit duration in hours.
	 */
	private double estimatedVisitDurationHours;

	/**
	 * The full entry price before discounts.
	 */
	private double fullEntryPrice;

	/**
	 * Indicates whether the park is active.
	 */
	private boolean active;

	/**
	 * Indicates whether the park currently has a promotion.
	 */
	private boolean promotions;

	/**
	 * Creates a full Park object.
	 * 
	 * @param parkId                      the park ID
	 * @param parkName                    the park name
	 * @param maxCapacity                 the maximum capacity of the park
	 * @param placesForUnplannedVisitors  the number of places reserved for
	 *                                    unplanned visitors
	 * @param estimatedVisitDurationHours the estimated visit duration in hours
	 * @param fullEntryPrice              the full entry price before discounts
	 * @param active                      true if the park is active, false otherwise
	 * @param promotions                  true if the park has an active promotion,
	 *                                    false otherwise
	 */
	public Park(int parkId, String parkName, int maxCapacity, int placesForUnplannedVisitors,
			double estimatedVisitDurationHours, double fullEntryPrice, boolean active, boolean promotions) {

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
	 * Returns the maximum capacity of the park.
	 * 
	 * @return the maximum capacity
	 */
	public int getMaxCapacity() {
		return maxCapacity;
	}

	/**
	 * Returns the number of places reserved for unplanned visitors.
	 * 
	 * @return the number of places reserved for unplanned visitors
	 */
	public int getPlacesForUnplannedVisitors() {
		return placesForUnplannedVisitors;
	}

	/**
	 * Returns the estimated visit duration in hours.
	 * 
	 * @return the estimated visit duration in hours
	 */
	public double getEstimatedVisitDurationHours() {
		return estimatedVisitDurationHours;
	}

	/**
	 * Returns the full entry price before discounts.
	 * 
	 * @return the full entry price
	 */
	public double getFullEntryPrice() {
		return fullEntryPrice;
	}

	/**
	 * Returns whether the park is active.
	 * 
	 * @return true if the park is active, false otherwise
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Returns whether the park has an active promotion.
	 * 
	 * @return true if the park has a promotion, false otherwise
	 */
	public boolean hasPromotions() {
		return promotions;
	}

	/**
	 * Updates the park name.
	 * 
	 * @param parkName the new park name
	 */
	public void setParkName(String parkName) {
		this.parkName = parkName;
	}

	/**
	 * Updates the maximum capacity.
	 * 
	 * @param maxCapacity the new maximum capacity
	 */
	public void setMaxCapacity(int maxCapacity) {
		this.maxCapacity = maxCapacity;
	}

	/**
	 * Updates the number of places reserved for unplanned visitors.
	 * 
	 * @param placesForUnplannedVisitors the new number of reserved places
	 */
	public void setPlacesForUnplannedVisitors(int placesForUnplannedVisitors) {
		this.placesForUnplannedVisitors = placesForUnplannedVisitors;
	}

	/**
	 * Updates the estimated visit duration in hours.
	 * 
	 * @param estimatedVisitDurationHours the new estimated visit duration
	 */
	public void setEstimatedVisitDurationHours(double estimatedVisitDurationHours) {
		this.estimatedVisitDurationHours = estimatedVisitDurationHours;
	}

	/**
	 * Updates the full entry price.
	 * 
	 * @param fullEntryPrice the new full entry price
	 */
	public void setFullEntryPrice(double fullEntryPrice) {
		this.fullEntryPrice = fullEntryPrice;
	}

	/**
	 * Updates the active status.
	 * 
	 * @param active true if the park is active, false otherwise
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Updates the promotion status.
	 * 
	 * @param promotions true if the park has a promotion, false otherwise
	 */
	public void setPromotions(boolean promotions) {
		this.promotions = promotions;
	}

	/**
	 * Returns the park data as public information that can be sent to the client.
	 * 
	 * This method hides internal management data and returns only the fields that
	 * the client needs for display and order creation.
	 * 
	 * @return a ParkInfo object with public park data
	 */
	public common.ParkInfo toParkInfo() {
		return new common.ParkInfo(parkId, parkName, estimatedVisitDurationHours, fullEntryPrice);
	}

	/**
	 * Returns the park information as a string.
	 * 
	 * @return a string representation of the park
	 */
	@Override
	public String toString() {
		return "Park ID: " + parkId + " " +
				"Name: " + parkName + " " +
				"Max Capacity: " + maxCapacity + " " +
				"Unplanned Places: " + placesForUnplannedVisitors + " " +
				"Estimated Duration: " + estimatedVisitDurationHours + " " +
				"Full Entry Price: " + fullEntryPrice + " " +
				"Active: " + active + " " +
				"Promotions: " + promotions;
	}
}