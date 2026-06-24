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
     */
    void onPendingParkParameterRequestsReceived(List<ParkParameterChangeRequest> requests);

    /**
     * Called when a create / approve / reject action receives a response.
     */
    void onParkParameterOperationResponse(OperationResponse response, Protocol responseType);
}