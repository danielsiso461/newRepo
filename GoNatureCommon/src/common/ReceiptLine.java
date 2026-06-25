package common;

import java.io.Serializable;

/**
 * Represents one line in a price receipt.
 */
public class ReceiptLine implements Serializable {
	/**
	 * serial version UID for serialization
	 */
    private static final long serialVersionUID = 1L;
    /**
     * the receipt's line label
     */
    private String label;
    /**
     * the receipt's line price
     */
    private String value;
    /**
     * constructor for a receipt line
     * @param label the label of the line
     * @param value the price of the line
     */
    public ReceiptLine(String label, String value) {
        this.label = label;
        this.value = value;
    }
    /**
     * getter for the receipt's line label
     * @return the receipt's line label
     */
    public String getLabel() {
        return label;
    }
    /**
     * getter for the receipt's line price
     * @return the receipt's line price
     */
    public String getValue() {
        return value;
    }
}