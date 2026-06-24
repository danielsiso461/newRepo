package clientCommon;

import common.OperationResponse;

/**
 * This interface is used by screens that need to receive the result of a
 * register guide request.
 */
public interface RegisterGuideObserver {
	/**
	 * this method notifies the screen that handles guide registration of the server's response
	 * @param response the server's response
	 */
	void onRegisterGuideResult(OperationResponse response);
}
