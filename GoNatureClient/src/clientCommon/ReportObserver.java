
package clientCommon;

import common.OperationResponse;

/**
 * Observer for report response screens.
 */
public interface ReportObserver {
    /**
     * This method is called when the server returns a report response.
     *
     * @param response the response received from the server
     */
    void onReportResponse(OperationResponse response);
}

