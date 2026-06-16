package clientCommon;

import common.OperationResponse;

public interface ReportObserver {
    void onReportResponse(OperationResponse response);
}