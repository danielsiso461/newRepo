package clientCommon;

import java.util.List;

import common.WaitingListMessage;
import java.util.List;

/**
 * This interface represents UI screens that wait for waiting-list related
 * responses from the server.
 *
 * A screen that allows a visitor to join the waiting list should implement this
 * interface so ClientController can notify it when the server returns success
 * or failure.
 */


public interface WaitingListObserver {
	/**
	 * Handles the result of rejecting a waiting list offer.
	 *
	 * @param success            true if the waiting list offer was rejected successfully
	 * @param waitingListMessage the waiting list message returned from the server
	 */
	void onRejectWaitingOfferResult(boolean success, WaitingListMessage waitingListMessage);
	
	
	/**
	 * Handles the result of accepting a waiting list offer.
	 *
	 * @param success            true if the waiting list offer was accepted successfully
	 * @param waitingListMessage the waiting list message returned from the server
	 */
	void onAcceptWaitingOfferResult(boolean success, WaitingListMessage waitingListMessage);
	
	
	/**
	 * Handles the server response after a visitor requests to join the waiting list.
	 *
	 * @param success            true if the visitor was added to the waiting list
	 *                           successfully, false otherwise
	 * @param waitingListMessage the waiting list request data returned by the server
	 */
	void onJoinWaitingListResult(boolean success, WaitingListMessage waitingListMessage);

	/**
	 * Handles the result of requesting the offered waiting list requests.
	 *
	 * @param success true if the offers were loaded successfully
	 * @param offers  the offered waiting list requests returned from the server
	 */
	void onWaitingOffersReceived(boolean success, List<WaitingListMessage> offers);
	
	/**
	 * Handles a server shutdown/disconnect event.
	 */
	void handleExit();


	
}