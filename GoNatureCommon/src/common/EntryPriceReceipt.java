package common;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a full calculated receipt for park entry payment.
 */
public class EntryPriceReceipt implements Serializable {

    private static final long serialVersionUID = 1L;

    private int customerId;
    private int visitId;
    private BigDecimal finalPrice;
    private List<ReceiptLine> receiptLines = new ArrayList<>();

    public EntryPriceReceipt(int customerId, int visitId) {
        this.customerId = customerId;
        this.visitId = visitId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getVisitId() {
        return visitId;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }

    public List<ReceiptLine> getReceiptLines() {
        return receiptLines;
    }

    public void addLine(String label, String value) {
        receiptLines.add(new ReceiptLine(label, value));
    }

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