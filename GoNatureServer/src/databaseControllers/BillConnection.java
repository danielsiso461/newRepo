
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
 * Handles database operations related to bills and entry payment receipts.
 * 
 * This connector creates bills from visit records, checks whether a bill already
 * exists, retrieves bill data, and builds receipt objects for park entry
 * payments according to the visit_price_calculation view.
 * 
 * The class is implemented as a singleton so the server uses one shared bill
 * database connector during runtime.
 */
public class BillConnection extends AbstractDBConnection {

    /**
     * The single instance of BillConnection.
     */
    private static BillConnection instance;

    /*
     * Table names.
     */
    private static final String VISIT_TABLE = "visit";
    private static final String PARK_TABLE = ConstantsDBTableNames.PARK;

    /*
     * Visit table column names.
     */
    private static final String VISIT_ID = "visit_id";
    private static final String ORDER_NUMBER = "order_number";
    private static final String SUBSCRIBER_ID = "subscriber_id";
    private static final String PARK_ID = "park_id";
    private static final String ENTRY_TIME = "entry_time";

    /*
     * Bill table column names.
     */
    private static final String BILL_TYPE = "bill_type";
    private static final String FULL_PRICE = "full_price";
    private static final String NUMBER_OF_PAID_VISITORS = "number_of_paid_visitors";
    private static final String DISCOUNT_PERCENT = "discount_percent";
    private static final String BASE_DISCOUNT_PERCENT = "base_discount_percent";
    private static final String PREPAID_DISCOUNT_PERCENT = "prepaid_discount_percent";
    private static final String SUBSCRIBER_EXTRA_DISCOUNT_PERCENT = "subscriber_extra_discount_percent";
    private static final String PROMOTION_DISCOUNT_PERCENT = "promotion_discount_percent";
    private static final String FINAL_PRICE = "final_price";
    private static final String BILL_DATE = "bill_date";

    /*
     * Park table column names.
     */
    private static final String PARK_NAME = "park_name";

    /**
     * Creates a new bill database connection.
     * 
     * The constructor is private because this class is implemented as a singleton.
     * 
     * @throws SQLException if connecting to the database fails
     */
    private BillConnection() throws SQLException {
        connect();
    }

    /**
     * Returns the single instance of BillConnection.
     * 
     * If no instance exists, or if the current database connection is closed, a new
     * instance is created.
     * 
     * @return the active BillConnection instance
     * @throws SQLException if creating the database connection fails
     */
    public static BillConnection getInstance() throws SQLException {
        if (instance == null || instance.conn == null || instance.conn.isClosed()) {
            instance = new BillConnection();
        }

        return instance;
    }

    /**
     * Returns the database table name used by this connector.
     * 
     * @return the bill table name
     */
    @Override
    public String getTableName() {
        return ConstantsDBTableNames.BILL;
    }

    /**
     * Creates a bill record for an existing visit.
     * 
     * This query is kept as a full SQL query because it inserts values using a
     * SELECT query from the visit_price_calculation view.
     * 
     * The bill values are calculated by selecting data from the
     * visit_price_calculation view and inserting the result into the bill table.
     * 
     * @param visitId the ID of the visit for which the bill should be created
     * @return true if the bill was created successfully, otherwise false
     * @throws SQLException if the insert operation fails
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
     * Retrieves all bill records that belong to a specific visit.
     * 
     * The method returns the data as a list of object arrays instead of exposing a
     * ResultSet outside the database layer.
     * 
     * @param visitId the ID of the visit whose bills should be retrieved
     * @return a list of bill records that match the given visit ID
     * @throws SQLException if the select operation fails
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
     * The method finds the latest visit related to the given order number, creates a
     * bill if one does not already exist, and returns the bill data as an
     * EntryPriceReceipt object.
     * 
     * @param orderNumber the order number used to locate the visit
     * @return the entry price receipt for the matching visit
     * @throws SQLException if no visit is found, if bill creation fails, or if the
     *         receipt cannot be loaded
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
     * Finds the latest visit data related to a specific order number.
     * 
     * @param orderNumber the order number used to search for the visit
     * @return the matching VisitData object, or null if no visit was found
     * @throws SQLException if the select operation fails
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
     * Creates a bill for the given visit only if one does not already exist.
     * 
     * @param visitId the ID of the visit that should have a bill
     * @throws SQLException if checking or creating the bill fails
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
     * Checks whether a bill already exists for the given visit.
     * 
     * @param visitId the ID of the visit to check
     * @return true if a bill exists for the visit, otherwise false
     * @throws SQLException if the select operation fails
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
     * Loads bill data for a visit and converts it into an EntryPriceReceipt object.
     * 
     * The receipt includes order details, customer details, visit details, park
     * information, discount breakdown, and the final payment amount.
     * 
     * @param customerId the ID of the customer related to the visit
     * @param orderNumber the order number related to the visit
     * @param visitId the ID of the visit whose bill should be loaded
     * @return an EntryPriceReceipt object containing the bill details
     * @throws SQLException if the bill cannot be found or loaded
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
     * Reads a monetary value from the ResultSet safely.
     * 
     * If the database value is null, zero is returned. The result is always rounded
     * to two decimal places.
     * 
     * @param rs the ResultSet containing the value
     * @param columnName the name of the money column
     * @return the monetary value as BigDecimal with two decimal places
     * @throws SQLException if reading from the ResultSet fails
     */
    private BigDecimal getMoney(ResultSet rs, String columnName) throws SQLException {
        BigDecimal value = rs.getBigDecimal(columnName);

        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return value.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Reads a percentage value from the ResultSet safely.
     * 
     * If the database value is null, zero is returned. The result is always rounded
     * to two decimal places.
     * 
     * @param rs the ResultSet containing the value
     * @param columnName the name of the percentage column
     * @return the percentage value as BigDecimal with two decimal places
     * @throws SQLException if reading from the ResultSet fails
     */
    private BigDecimal getPercent(ResultSet rs, String columnName) throws SQLException {
        BigDecimal value = rs.getBigDecimal(columnName);

        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return value.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Formats a monetary value as a plain string with two decimal places.
     * 
     * @param value the monetary value to format
     * @return the formatted money string
     */
    private String formatMoney(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * Formats a percentage value as a plain string with two decimal places and a
     * percent sign.
     * 
     * @param value the percentage value to format
     * @return the formatted percentage string
     */
    private String formatPercent(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString() + "%";
    }

    /**
     * Holds basic visit data required for creating or loading a bill.
     */
    private static class VisitData {
        /**
         * The visit ID.
         */
        private int visitId;

        /**
         * The order number related to the visit.
         */
        private int orderNumber;

        /**
         * The subscriber ID related to the visit.
         */
        private int subscriberId;
    }
}
