
package clientCommon;

import common.OperationResponse;

/**
 * Observer for receiving user information search results.
 */
public interface UserInformationObserver {

	/**
	 * This method is called when the server returns the user information result.
	 *
	 * @param response the response received from the server
	 */
	void onUserInformationResult(OperationResponse response);
}

