package databaseControllers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import common.EntryPriceReceipt;

/**
 * DB connector for the bill table.
 */
public class BillConnection extends AbstractDBConnection {

    private static BillConnection instance;

    /*
     * Table names.
     */
    private static final String VISIT_TABLE = "visit";
    private static final String PARK_TABLE = ConstantsDBTableNames.PARK;

    /*
     * Visit columns.
     */
    private static final String VISIT_ID = "visit_id";
    private static final String ORDER_NUMBER = "order_number";
    private static final String SUBSCRIBER_ID = "subscriber_id";
    private static final String PARK_ID = "park_id";
    private static final String ENTRY_TIME = "entry_time";

    /*
     * Bill columns.
     */
    private static final String BILL_TYPE = "bill_type";
    private static final String FULL_PRICE = "full_price";
    private static final String NUMBER_OF_PAID_VISITORS = "number_of_paid_visitors";
    private static final String DISCOUNT_PERCENT = "discount_percent";
    private static final String BASE_DISCOUNT_PERCENT = "base_discount_percent";
    private static final String PREPAID_DISCOUNT_PERCENT = "prepaid_discount_percent";
    private static final String SUBSCRIBER_EXTRA_DISCOUNT_PERCENT =
            "subscriber_extra_discount_percent";
    private static final String PROMOTION_DISCOUNT_PERCENT = "promotion_discount_percent";
    private static final String FINAL_PRICE = "final_price";
    private static final String BILL_DATE = "bill_date";

    /*
     * Park columns.
     */
    private static final String PARK_NAME = "park_name";

    private BillConnection() throws SQLException {
        connect();
    }

    public static BillConnection getInstance() throws SQLException {
        if (instance == null || instance.conn == null || instance.conn.isClosed()) {
            instance = new BillConnection();
        }

        return instance;
    }

    @Override
    public String getTableName() {
        return ConstantsDBTableNames.BILL;
    }

    /**
     * Creates a bill for an existing visit.
     *
     * This query is kept as a full SQL query because it inserts values using
     * SELECT from the visit_price_calculation view.
     */
    public boolean createBillFromVisit(int visitId) throws SQLException {
        ensureConnection();

        String sql = "INSERT INTO `" + getTableName() + "` "
                + "(visit_id, bill_type, full_price, number_of_paid_visitors, discount_percent, "
                + "base_discount_percent, prepaid_discount_percent, subscriber_extra_discount_percent, "
                + "promotion_discount_percent, final_price, bill_date) "
                + "SELECT visit_id, calculated_bill_type, full_price, number_of_paid_visitors, "
                + "base_discount_percent + prepaid_discount_percent "
                + "+ subscriber_extra_discount_percent + promotion_discount_percent, "
                + "base_discount_percent, prepaid_discount_percent, subscriber_extra_discount_percent, "
                + "promotion_discount_percent, final_price, NOW() "
                + "FROM visit_price_calculation "
                + "WHERE visit_id = ?;";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, visitId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Returns the bill records that belong to a specific visit.
     *
     * The method returns a list instead of exposing ResultSet outside the DB layer.
     */
    public List<Object[]> getBillByVisitId(int visitId) throws SQLException {
        ensureConnection();

        String sql = selectByFields(
                new String[] { "*" },
                new String[] { VISIT_ID }
        );

        List<Object[]> bills = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, visitId);

            try (ResultSet rs = pstmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    Object[] row = new Object[columnCount];

                    for (int i = 1; i <= columnCount; i++) {
                        row[i - 1] = rs.getObject(i);
                    }

                    bills.add(row);
                }
            }
        }

        return bills;
    }

    /**
     * Calculates or loads an entry payment receipt by order number.
     *
     * Flow:
     * 1. Find the visit by order_number.
     * 2. Create a bill if it does not exist.
     * 3. Load the bill and return it as a receipt.
     *
     * @param orderNumber the order number
     * @return entry price receipt
     * @throws SQLException if visit or bill cannot be found
     */
    public EntryPriceReceipt calculateReceiptByOrderNumber(int orderNumber)
            throws SQLException {

        ensureConnection();

        VisitData visitData = findVisitDataByOrderNumber(orderNumber);

        if (visitData == null) {
            throw new SQLException("No visit was found for order number: " + orderNumber);
        }

        createBillIfMissing(visitData.visitId);

        return loadReceiptByVisitId(
                visitData.subscriberId,
                visitData.orderNumber,
                visitData.visitId
        );
    }

    /**
     * Finds the visit data by order number.
     */
    private VisitData findVisitDataByOrderNumber(int orderNumber) throws SQLException {
        ensureConnection();

        String sql = "SELECT "
                + VISIT_ID + ", "
                + ORDER_NUMBER + ", "
                + SUBSCRIBER_ID + " "
                + "FROM `" + VISIT_TABLE + "` "
                + "WHERE " + ORDER_NUMBER + " = ? "
                + "ORDER BY " + ENTRY_TIME + " DESC, " + VISIT_ID + " DESC "
                + "LIMIT 1;";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    VisitData data = new VisitData();

                    data.visitId = rs.getInt(VISIT_ID);
                    data.orderNumber = rs.getInt(ORDER_NUMBER);
                    data.subscriberId = rs.getInt(SUBSCRIBER_ID);

                    return data;
                }
            }
        }

        return null;
    }

    /**
     * Creates a bill only if no bill exists yet for this visit.
     */
    private void createBillIfMissing(int visitId) throws SQLException {
        if (billExists(visitId)) {
            return;
        }

        boolean created = createBillFromVisit(visitId);

        if (!created) {
            throw new SQLException("Failed to create bill for visit ID: " + visitId);
        }
    }

    /**
     * Checks whether a bill already exists for a visit.
     */
    private boolean billExists(int visitId) throws SQLException {
        ensureConnection();

        String sql = "SELECT COUNT(*) AS bill_count "
                + "FROM `" + getTableName() + "` "
                + "WHERE " + VISIT_ID + " = ?;";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, visitId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("bill_count") > 0;
                }
            }
        }

        return false;
    }

    /**
     * Loads the bill and builds a receipt object.
     */
    private EntryPriceReceipt loadReceiptByVisitId(int customerId,
            int orderNumber, int visitId) throws SQLException {

        ensureConnection();

        String sql = "SELECT "
                + "b." + VISIT_ID + ", "
                + "b." + BILL_TYPE + ", "
                + "b." + FULL_PRICE + ", "
                + "b." + NUMBER_OF_PAID_VISITORS + ", "
                + "b." + DISCOUNT_PERCENT + ", "
                + "b." + BASE_DISCOUNT_PERCENT + ", "
                + "b." + PREPAID_DISCOUNT_PERCENT + ", "
                + "b." + SUBSCRIBER_EXTRA_DISCOUNT_PERCENT + ", "
                + "b." + PROMOTION_DISCOUNT_PERCENT + ", "
                + "b." + FINAL_PRICE + ", "
                + "b." + BILL_DATE + ", "
                + "p." + PARK_NAME + " "
                + "FROM `" + getTableName() + "` b "
                + "JOIN `" + VISIT_TABLE + "` v "
                + "ON b." + VISIT_ID + " = v." + VISIT_ID + " "
                + "JOIN `" + PARK_TABLE + "` p "
                + "ON v." + PARK_ID + " = p." + PARK_ID + " "
                + "WHERE b." + VISIT_ID + " = ? "
                + "ORDER BY b." + BILL_DATE + " DESC "
                + "LIMIT 1;";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, visitId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Bill was not found for visit ID: " + visitId);
                }

                EntryPriceReceipt receipt = new EntryPriceReceipt(customerId, visitId);

                BigDecimal finalPrice = getMoney(rs, FINAL_PRICE);
                receipt.setFinalPrice(finalPrice);

                receipt.addLine("Order Number", String.valueOf(orderNumber));
                receipt.addLine("Customer ID", String.valueOf(customerId));
                receipt.addLine("Visit ID", String.valueOf(visitId));
                receipt.addLine("Park", rs.getString(PARK_NAME));
                receipt.addLine("Bill type", rs.getString(BILL_TYPE));
                receipt.addLine("Bill date", String.valueOf(rs.getTimestamp(BILL_DATE)));

                receipt.addLine("Full price", formatMoney(getMoney(rs, FULL_PRICE)));

                receipt.addLine("Number of paid visitors",
                        String.valueOf(rs.getInt(NUMBER_OF_PAID_VISITORS)));

                receipt.addLine("Base discount",
                        formatPercent(getPercent(rs, BASE_DISCOUNT_PERCENT)));

                receipt.addLine("Prepaid discount",
                        formatPercent(getPercent(rs, PREPAID_DISCOUNT_PERCENT)));

                receipt.addLine("Subscriber extra discount",
                        formatPercent(getPercent(rs, SUBSCRIBER_EXTRA_DISCOUNT_PERCENT)));

                receipt.addLine("Park promotion discount",
                        formatPercent(getPercent(rs, PROMOTION_DISCOUNT_PERCENT)));

                receipt.addLine("Total discount",
                        formatPercent(getPercent(rs, DISCOUNT_PERCENT)));

                receipt.addLine("Final price", formatMoney(finalPrice));

                return receipt;
            }
        }
    }

    /**
     * Reads money safely from ResultSet.
     */
    private BigDecimal getMoney(ResultSet rs, String columnName) throws SQLException {
        BigDecimal value = rs.getBigDecimal(columnName);

        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return value.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Reads percent safely from ResultSet.
     */
    private BigDecimal getPercent(ResultSet rs, String columnName) throws SQLException {
        BigDecimal value = rs.getBigDecimal(columnName);

        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String formatMoney(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String formatPercent(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString() + "%";
    }

    private static class VisitData {
        private int visitId;
        private int orderNumber;
        private int subscriberId;
    }
}