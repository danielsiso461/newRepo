package clientCommon;

import common.OperationResponse;

/*
 * This interface is used by screens that need to receive the result of a
 * subscriber search request.
 */
public interface SearchSubscriberObserver {

	/*
	 * This method is called when the server returns the subscriber search result.
	 * 
	 * @param response the response received from the server
	 */
	void onSearchSubscriberResult(OperationResponse response);
}
