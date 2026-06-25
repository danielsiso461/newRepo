package clientCommon;

import common.OperationResponse;

/*
 * Observer for receiving user information search results.
 */
public interface UserInformationObserver {

	void onUserInformationResult(OperationResponse response);
}
