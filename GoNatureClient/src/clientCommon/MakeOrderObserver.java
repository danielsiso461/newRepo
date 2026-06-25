
package clientCommon;

import java.util.List;

import common.Message;

/**
 * Observer for make order screen responses.
 */
public interface MakeOrderObserver {
	/**
	 * This method is called when the park names are received from the server.
	 *
	 * @param parkNames the list of park names
	 */
	void onParkNamesReceived(List<String> parkNames);
	
	/**
	 * This method is called when the server returns a response for making an order.
	 *
	 * @param m the message received from the server
	 */
	void onMakeOrderServerResponse(Message m);
}

