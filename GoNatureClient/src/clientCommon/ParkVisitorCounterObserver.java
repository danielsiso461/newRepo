
package clientCommon;

import java.util.List;

import common.OperationResponse;
import common.ParkVisitorCounterSnapshot;
import common.Protocol;

/**
 * Observer for park visitor counter screens.
 */
public interface ParkVisitorCounterObserver {

    /**
     * Called when park visitor counters are received from the server.
     *
     * @param counters the list of park visitor counter snapshots
     */
    void onParkVisitorCountersReceived(List<ParkVisitorCounterSnapshot> counters);

    /**
     * Called when a park visitor counter operation receives a response.
     *
     * @param response the operation response received from the server
     * @param responseType the protocol type of the response
     */
    void onParkVisitorCounterOperationResponse(
            OperationResponse response,
            Protocol responseType
    );

    /**
     * Called when the park visitor counters are updated.
     */
    void onParkVisitorCountersUpdated();
}

