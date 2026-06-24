package clientCommon;

import java.util.List;

import common.Message;
/**
 * This interface is used by screens that handle making orders
 */
public interface MakeOrderObserver {
	/**
	 * this method handles passing available park names to the make order page
	 * @param parkNames the list of park names
	 */
	void onParkNamesReceived(List<String> parkNames);
	
	/**
	 * this method handles sending the server response to the make order page
	 * @param m the message from the server
	 */
	void onMakeOrderServerResponse(Message m);
}
