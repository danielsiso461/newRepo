package clientCommon;

import java.util.List;

import common.Message;

public interface MakeOrderObserver {
	void onParkNamesReceived(List<String> parkNames);
	
	void onMakeOrderServerResponse(Message m);
}
