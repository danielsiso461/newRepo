package common;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * This class represents a park in the system.
 * 
 * The class is placed in common because park data is used both by the server and
 * by the client.
 */
public class Park implements Serializable {

	private static final long serialVersionUID = 1L;

	private int parkId;
	private String parkName;
	private int maxCapacity;
	private int placesForUnplannedVisitors;
	private int estimatedVisitDurationHours;
	private BigDecimal fullEntryPrice;
	private boolean active;
	private String parkColor;
	private String promotions;

	public Park(int parkId, String parkName, int maxCapacity, int placesForUnplannedVisitors,
			int estimatedVisitDurationHours, BigDecimal fullEntryPrice, boolean active, String parkColor,
			String promotions) {

		this.parkId = parkId;
		this.parkName = parkName;
		this.maxCapacity = maxCapacity;
		this.placesForUnplannedVisitors = placesForUnplannedVisitors;
		this.estimatedVisitDurationHours = estimatedVisitDurationHours;
		this.fullEntryPrice = fullEntryPrice;
		this.active = active;
		this.parkColor = parkColor;
		this.promotions = promotions;
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

	public int getEstimatedVisitDurationHours() {
		return estimatedVisitDurationHours;
	}

	public BigDecimal getFullEntryPrice() {
		return fullEntryPrice;
	}

	public boolean isActive() {
		return active;
	}

	public String getParkColor() {
		return parkColor;
	}

	public String getPromotions() {
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

	public void setEstimatedVisitDurationHours(int estimatedVisitDurationHours) {
		this.estimatedVisitDurationHours = estimatedVisitDurationHours;
	}

	public void setFullEntryPrice(BigDecimal fullEntryPrice) {
		this.fullEntryPrice = fullEntryPrice;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setParkColor(String parkColor) {
		this.parkColor = parkColor;
	}

	public void setPromotions(String promotions) {
		this.promotions = promotions;
	}

	@Override
	public String toString() {
		return "Park ID: " + parkId + " " +
				"Name: " + parkName + " " +
				"Max Capacity: " + maxCapacity + " " +
				"Unplanned Places: " + placesForUnplannedVisitors + " " +
				"Estimated Duration: " + estimatedVisitDurationHours + " " +
				"Full Entry Price: " + fullEntryPrice + " " +
				"Active: " + active + " " +
				"Park Color: " + parkColor + " " +
				"Promotions: " + promotions;
	}
}