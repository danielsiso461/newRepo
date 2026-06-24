package clientCommon;

import java.util.List;
import common.Park;

/**
 * This interface is used by GUI controllers that want to receive park data
 * updates from the client controller.
 */
public interface ParkObserver {

	/**
	 * This method is called when the active parks list is received or updated.
	 * 
	 * @param parks the active parks received from the server
	 */
	void onParksReceived(List<Park> parks);
}