
package clientCommon;

import common.OperationResponse;

/**
 * Interface for screens that need to receive the result of an existing customer login request.
 */
public interface ExistingCustomerLoginObserver {

    /**
     * This method is called when the server returns the existing customer login result.
     *
     * @param response the response received from the server
     */
    void onExistingCustomerLoginResult(OperationResponse response);
}

