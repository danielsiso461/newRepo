package common;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a full calculated receipt for park entry payment.
 */
public class EntryPriceReceipt implements Serializable {
	/**
	 * serial version UID for serialization
	 */
    private static final long serialVersionUID = 1L;
    /**
     * the customer id
     */
    private int customerId;
    /**
     * the visit id
     */
    private int visitId;
    /**
     * the price of the visit
     */
    private BigDecimal finalPrice;
    /**
     * the lines that appear in the receipt
     */
    private List<ReceiptLine> receiptLines = new ArrayList<>();
    /**
     * constructor for the receipt
     * @param customerId the customer id
     * @param visitId the visit id
     */
    public EntryPriceReceipt(int customerId, int visitId) {
        this.customerId = customerId;
        this.visitId = visitId;
    }
    /**
     * getter for customer id
     * @return the customer id 
     */
    public int getCustomerId() {
        return customerId;
    }
    /**
     * getter for visit id
     * @return the visit id 
     */
    public int getVisitId() {
        return visitId;
    }
    /**
     * getter for receipt price
     * @return the receipt price
     */
    public BigDecimal getFinalPrice() {
        return finalPrice;
    }
    /**
     * setter for the receipt price
     * @param finalPrice the price to set
     */
    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }
    /**
     * getter for receipt lines
     * @return the receipt lines
     */
    public List<ReceiptLine> getReceiptLines() {
        return receiptLines;
    }
    /**
     * this method adds lines to the receipt lines list
     * @param label the line's label
     * @param value the line's value
     */
    public void addLine(String label, String value) {
        receiptLines.add(new ReceiptLine(label, value));
    }
    /**
     * this method creates the receipt as text
     * @return the string representing the receipt
     */
    public String toReceiptText() {
        StringBuilder sb = new StringBuilder();

        sb.append("========== Entry Payment Receipt ==========\n\n");

        for (ReceiptLine line : receiptLines) {
            sb.append(line.getLabel())
                    .append(": ")
                    .append(line.getValue())
                    .append("\n");
        }

        sb.append("\n===========================================\n");

        if (finalPrice != null) {
            sb.append("Final price: ")
                    .append(finalPrice)
                    .append("\n");
        }

        return sb.toString();
    }
}