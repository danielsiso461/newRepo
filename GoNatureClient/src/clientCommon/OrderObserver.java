package clientCommon;

import java.util.List;

import common.CancelOrderMessage;
import common.Order;
import common.UpdateMessage;

/*
 * this interface represents all UI items waiting on order-related updates from the server
 */
public interface OrderObserver {
	/*
	 * this function handles receiving all user orders from the server
	 * 
	 * @param rows 	the orders
	 */
	void onOrdersReceived(List<Order> rows);

	/*
	 * this function handles receiving an update result for an order from the server
	 * 
	 * @param success 			whether the update was successful
	 * @param updateMessage 	the data of the update
	 */
	void onUpdateResult(boolean success, UpdateMessage updateMessage);

	/*
	 * this function handles receiving a cancellation result for an order from the server
	 * 
	 * @param success				whether the cancellation was successful
	 * @param cancelOrderMessage	the data of the cancellation request
	 */
	void onCancelResult(boolean success, CancelOrderMessage cancelOrderMessage);

	/*
	 * this function adds an order to the order table
	 * 
	 * @param o the order to add to the order table
	 */
	void addOrder(Order o);

	/*
	 * this function handles the server shutting the client down
	 */
	void handleExit();
	
	/**
	 * this method notifies order observers that an order was canceled via reminder
	 * @param o the order
	 */
    void reminderDeclined(Order o);
}