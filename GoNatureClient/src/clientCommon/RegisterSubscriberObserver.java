
package clientCommon;

import common.OperationResponse;

/**
 * Interface for screens that need to receive the result of a register subscriber request.
 */
public interface RegisterSubscriberObserver {

	/**
	 * This method is called when the server returns the register subscriber result.
	 * 
	 * @param response the response received from the server
	 */
	void onRegisterSubscriberResult(OperationResponse response);
}


