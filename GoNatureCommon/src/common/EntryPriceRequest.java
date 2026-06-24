package common;

import java.io.Serializable;

/**
 * Request for calculating the entry price of an order.
 */
public class EntryPriceRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private int orderNumber;

    public EntryPriceRequest(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    /*
     * Kept temporarily for backward compatibility with old code.
     * Prefer using getOrderNumber().
     */
    @Deprecated
    public int getCustomerId() {
        return orderNumber;
    }

    @Override
    public String toString() {
        return "EntryPriceRequest{orderNumber=" + orderNumber + "}";
    }
}