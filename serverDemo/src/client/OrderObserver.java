package client;

import java.util.List;

import common.OrderRow;
import common.UpdateMessage;

public interface OrderObserver {
    void onOrdersReceived(List<OrderRow> rows);

    void onUpdateResult(boolean success, UpdateMessage updateMessage);
}