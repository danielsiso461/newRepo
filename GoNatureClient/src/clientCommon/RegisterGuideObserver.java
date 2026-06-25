package clientCommon;

import common.OperationResponse;

/*
 * This interface is used by screens that need to receive the result of a
 * register guide request.
 */
public interface RegisterGuideObserver {
	void onRegisterGuideResult(OperationResponse response);
}
