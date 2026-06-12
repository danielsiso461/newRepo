package common;

import java.io.Serializable;

/**
 * This class represents public park information that can be sent to the client.
 * 
 * The class contains only the park details needed for display and order creation,
 * without exposing internal management data such as capacity limits, reserved
 * places for unplanned visitors, active status, or promotion settings.
 */
public class ParkInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The park ID.
	 */
	private int parkId;

	/**
	 * The park name.
	 */
	private String parkName;

	/**
	 * The estimated visit duration in hours.
	 */
	private double estimatedVisitDurationHours;

	/**
	 * The full entry price before discounts.
	 */
	private double fullEntryPrice;

	/**
	 * Creates a new ParkInfo object.
	 * 
	 * @param parkId                      the park ID
	 * @param parkName                    the park name
	 * @param estimatedVisitDurationHours the estimated visit duration in hours
	 * @param fullEntryPrice              the full entry price before discounts
	 */
	public ParkInfo(int parkId, String parkName, double estimatedVisitDurationHours, double fullEntryPrice) {
		this.parkId = parkId;
		this.parkName = parkName;
		this.estimatedVisitDurationHours = estimatedVisitDurationHours;
		this.fullEntryPrice = fullEntryPrice;
	}

	public int getParkId() {
		return parkId;
	}

	public String getParkName() {
		return parkName;
	}

	public double getEstimatedVisitDurationHours() {
		return estimatedVisitDurationHours;
	}

	public double getFullEntryPrice() {
		return fullEntryPrice;
	}

	@Override
	public String toString() {
		return parkName;
	}
}