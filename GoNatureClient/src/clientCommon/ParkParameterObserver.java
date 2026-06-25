
package clientCommon;

import java.util.List;

import common.OperationResponse;
import common.ParkParameterChangeRequest;
import common.Protocol;

/**
 * Observer for park parameter change request screens.
 */
public interface ParkParameterObserver {

    /**
     * Called when pending park parameter change requests are received.
     *
     * @param requests the list of pending park parameter change requests
     */
    void onPendingParkParameterRequestsReceived(List<ParkParameterChangeRequest> requests);

    /**
     * Called when a create, approve, or reject action receives a response.
     *
     * @param response the operation response received from the server
     * @param responseType the protocol type of the response
     */
    void onParkParameterOperationResponse(OperationResponse response, Protocol responseType);
}
