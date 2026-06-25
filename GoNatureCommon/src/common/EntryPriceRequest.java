package common;

import java.io.Serializable;

/**
 * Request for calculating the entry price of an order.
 */
public class EntryPriceRequest implements Serializable {
	/**
	 * serial version UID for serialization
	 */
    private static final long serialVersionUID = 1L;
    /**
     * the order's number
     */
    private int orderNumber;
    /**
     * constructor that makes an entry price request
     * @param orderNumber the order's number
     */
    public EntryPriceRequest(int orderNumber) {
        this.orderNumber = orderNumber;
    }
    /**
     * getter for the order's number
     * @return the order's number
     */
    public int getOrderNumber() {
        return orderNumber;
    }

    /**
     * Kept temporarily for backward compatibility with old code.
     * Prefer using getOrderNumber().
     * returns the order's number
     * @return the order's number
     */
    @Deprecated
    public int getCustomerId() {
        return orderNumber;
    }
    /**
     * standard toString method
     */
    @Override
    public String toString() {
        return "EntryPriceRequest{orderNumber=" + orderNumber + "}";
    }
}