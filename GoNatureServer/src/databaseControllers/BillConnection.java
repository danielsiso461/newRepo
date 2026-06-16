package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DB connector for the bill table.
 */
public class BillConnection extends AbstractDBConnection {

    private static BillConnection instance;

    private final String VISIT_ID = "visit_id";

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
    protected String getTableName() {
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
                + "base_discount_percent + prepaid_discount_percent, "
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
}