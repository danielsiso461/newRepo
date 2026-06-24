package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DB connector for the order_status_history table.
 */
public class OrderStatusHistoryConnection extends AbstractDBConnection {

    private static OrderStatusHistoryConnection instance;

    private final String ORDER_NUMBER = "order_number";
    private final String OLD_STATUS = "old_status";
    private final String NEW_STATUS = "new_status";
    private final String CHANGED_BY_EMPLOYEE_ID = "changed_by_employee_id";
    private final String CHANGE_REASON = "change_reason";

    private OrderStatusHistoryConnection() throws SQLException {
        connect();
    }

    public static OrderStatusHistoryConnection getInstance() throws SQLException {
        if (instance == null || instance.conn == null || instance.conn.isClosed()) {
            instance = new OrderStatusHistoryConnection();
        }

        return instance;
    }

   
    @Override
    public String getTableName() {
        return ConstantsDBTableNames.ORDER_STATUS_HISTORY;
    }

    /**
     * Adds a new status change record.
     */
    public boolean addHistory(int orderNumber, String oldStatus, String newStatus,
            int changedByEmployeeId, String changeReason) throws SQLException {

        ensureConnection();

        List<Object> values = new ArrayList<>();

        values.add(orderNumber);
        values.add(oldStatus);
        values.add(newStatus);
        values.add(changedByEmployeeId);
        values.add(changeReason);

        insertFields(
                new String[] {
                        ORDER_NUMBER,
                        OLD_STATUS,
                        NEW_STATUS,
                        CHANGED_BY_EMPLOYEE_ID,
                        CHANGE_REASON
                },
                values
        );

        return true;
    }

    /**
     * Returns the cancellation report from the cancellation_report view.
     */
    public List<Object[]> getCancellationReport() throws SQLException {
        ensureConnection();

        String sql = "SELECT * FROM cancellation_report;";

        List<Object[]> reportRows = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Object[] row = new Object[columnCount];

                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }

                reportRows.add(row);
            }
        }

        return reportRows;
    }
}