package clientCommon;

import common.WaitingListMessage;

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
	 * Handles the server response after a visitor requests to join the waiting list.
	 *
	 * @param success            true if the visitor was added to the waiting list
	 *                           successfully, false otherwise
	 * @param waitingListMessage the waiting list request data returned by the server
	 */
	void onJoinWaitingListResult(boolean success, WaitingListMessage waitingListMessage);

	/**
	 * Handles a server shutdown/disconnect event.
	 */
	void handleExit();
}