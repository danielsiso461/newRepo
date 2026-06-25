
package clientCommon;

import common.OperationResponse;

/**
 * Interface for screens that need to receive the result of a register guide request.
 */
public interface RegisterGuideObserver {
	/**
	 * This method is called when the server returns the register guide result.
	 *
	 * @param response the response received from the server
	 */
	void onRegisterGuideResult(OperationResponse response);
}

