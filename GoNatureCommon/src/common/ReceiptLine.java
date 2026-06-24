package common;

import java.io.Serializable;

/**
 * Represents one line in a price receipt.
 */
public class ReceiptLine implements Serializable {

    private static final long serialVersionUID = 1L;

    private String label;
    private String value;

    public ReceiptLine(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }
}