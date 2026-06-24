package clientCommon;

import common.OperationResponse;

/**
 * This interface is used by screens that need to receive the result of an
 * employee login request.
 */
public interface EmployeeLoginObserver {

	/**
	 * This method is called when the server returns the employee login result.
	 * 
	 * @param response the response received from the server
	 */
	void onEmployeeLoginResult(OperationResponse response);
}
