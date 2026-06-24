package clientCommon;

import common.ParkEntranceMessage;

/**
 * This observer is used by screens that handle park entrance control.
 *
 * It receives the server responses for check-in, check-out, occasional visits,
 * and current visitors count requests.
 */
public interface ParkEntranceObserver {
	/**
	 * Handles the result of checking in visitors using an existing order.
	 *
	 * @param success true if the check-in was completed successfully
	 * @param parkEntranceMessage the response data returned from the server
	 */
	void onCheckInOrderResult(boolean success, ParkEntranceMessage parkEntranceMessage);

	/**
	 * Handles the result of checking out visitors from the park.
	 *
	 * @param success true if the check-out was completed successfully
	 * @param parkEntranceMessage the response data returned from the server
	 */
	void onCheckOutVisitResult(boolean success, ParkEntranceMessage parkEntranceMessage);

	/**
	 * Handles the result of creating an occasional visit.
	 *
	 * @param success true if the occasional visit was created successfully
	 * @param parkEntranceMessage the response data returned from the server
	 */
	void onOccasionalVisitResult(boolean success, ParkEntranceMessage parkEntranceMessage);

	/**
	 * Handles the result of loading the current number of visitors in a park.
	 *
	 * @param success true if the current visitors count was loaded successfully
	 * @param parkEntranceMessage the response data returned from the server
	 */
	void onCurrentVisitorsReceived(boolean success, ParkEntranceMessage parkEntranceMessage);

	/**
	 * Handles server shutdown/disconnect.
	 */
	void handleExit();
}