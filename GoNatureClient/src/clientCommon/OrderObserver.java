
package clientCommon;

import java.util.List;

import common.CancelOrderMessage;
import common.Order;
import common.UpdateMessage;

/**
 * Interface for UI components that receive order-related updates from the server.
 */
public interface OrderObserver {
	/**
	 * This method is called when the user's orders are received from the server.
	 * 
	 * @param rows the list of orders
	 */
	void onOrdersReceived(List<Order> rows);

	/**
	 * This method is called when the server returns an update result for an order.
	 * 
	 * @param success whether the update was successful
	 * @param updateMessage the update data
	 */
	void onUpdateResult(boolean success, UpdateMessage updateMessage);

	/**
	 * This method is called when the server returns a cancellation result for an order.
	 * 
	 * @param success whether the cancellation was successful
	 * @param cancelOrderMessage the cancellation request data
	 */
	void onCancelResult(boolean success, CancelOrderMessage cancelOrderMessage);

	/**
	 * Adds an order to the order table.
	 * 
	 * @param o the order to add
	 */
	void addOrder(Order o);

	/**
	 * This method handles the server shutting down the client.
	 */
	void handleExit();
	
	/**
	 * This method notifies order observers that an order was canceled via reminder.
	 * 
	 * @param o the order
	 */
    void reminderDeclined(Order o);
}

