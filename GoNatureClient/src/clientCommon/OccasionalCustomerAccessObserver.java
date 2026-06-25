
package clientCommon;

import common.OperationResponse;

/**
 * Interface for screens that need to receive the result of an occasional customer access request.
 */
public interface OccasionalCustomerAccessObserver {

	/**
	 * This method is called when the server returns the result of the occasional customer access request.
	 * 
	 * @param response the response received from the server
	 */
	void onOccasionalCustomerAccessResult(OperationResponse response);
}

