package clientCommon;

import common.OperationResponse;

/**
 * Observer for entry price calculation responses.
 */
public interface EntryPriceObserver {

    /**
     * This method is called when the server returns the entry price calculation result.
     *
     * @param response the response received from the server
     */
    void onEntryPriceCalculated(OperationResponse response);
}