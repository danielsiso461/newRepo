package client;

import java.util.List;

import common.OrderRow;
import common.UpdateMessage;
/*
 * this interface represents all UI items waiting on updates from the server
 */
public interface OrderObserver {
	/*
	 * this function handles receiving all user orders from the server
	 * 
	 * @param rows 	the orders
	 */
    void onOrdersReceived(List<OrderRow> rows);
    
    /*
	 * this function handles receiving an update for an order from the server
	 * 
	 * @param success 			whether the update was successful
	 * @param updateMessage 	the Data of the update
	 */
    void onUpdateResult(boolean success, UpdateMessage updateMessage);
    
    /*
	 * this function handles the server shutting the client down
	 */
    void handleExit();
}