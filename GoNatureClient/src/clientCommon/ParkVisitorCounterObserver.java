package clientCommon;

import java.util.List;

import common.OperationResponse;
import common.ParkVisitorCounterSnapshot;
import common.Protocol;

/**
 * Observer for park visitor counter screens.
 */
public interface ParkVisitorCounterObserver {

    void onParkVisitorCountersReceived(List<ParkVisitorCounterSnapshot> counters);

    void onParkVisitorCounterOperationResponse(
            OperationResponse response,
            Protocol responseType
    );

    void onParkVisitorCountersUpdated();
}