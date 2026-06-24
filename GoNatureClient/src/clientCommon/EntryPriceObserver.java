package clientCommon;

import common.OperationResponse;

/**
 * Observer for entry price calculation responses.
 */
public interface EntryPriceObserver {

    void onEntryPriceCalculated(OperationResponse response);
}