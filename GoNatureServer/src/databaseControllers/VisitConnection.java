package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import common.Visit;

/**
 * DB connector for the visit table.
 */
public class VisitConnection extends AbstractDBConnection {

    private static VisitConnection instance;

    private final String VISIT_ID = "visit_id";
    private final String ORDER_NUMBER = "order_number";
    private final String PARK_ID = "park_id";
    private final String SUBSCRIBER_ID = "subscriber_id";
    private final String VISIT_TYPE = "visit_type";
    private final String ACTUAL_NUMBER_OF_VISITORS = "actual_number_of_visitors";
    private final String ENTRY_TIME = "entry_time";
    private final String EXIT_TIME = "exit_time";
    private final String HANDLED_BY_EMPLOYEE_ID = "handled_by_employee_id";
    private final String EXIT_HANDLED_BY_EMPLOYEE_ID = "exit_handled_by_employee_id";
    private final String IDENTIFICATION_METHOD = "identification_method";

    private final String ORDER_STATUS_APPROVED = "approved";
    private final String VISIT_TYPE_ORDERED = "ordered";

    private VisitConnection() throws SQLException {
        connect();
    }

    public static VisitConnection getInstance() throws SQLException {
        if (instance == null || instance.conn == null || instance.conn.isClosed()) {
            instance = new VisitConnection();
        }

        return instance;
    }

    @Override
    protected String getTableName() {
        return ConstantsDBTableNames.VISIT;
    }

    private Integer getNullableInt(ResultSet rs, String columnName) throws SQLException {
        Object value = rs.getObject(columnName);

        if (value == null) {
            return null;
        }

        return rs.getInt(columnName);
    }

    private LocalDateTime getNullableDateTime(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);

        if (timestamp == null) {
            return null;
        }

        return timestamp.toLocalDateTime();
    }

    private Visit convertResultSetToVisit(ResultSet rs) throws SQLException {
        return new Visit(
                rs.getInt(VISIT_ID),
                getNullableInt(rs, ORDER_NUMBER),
                rs.getInt(PARK_ID),
                getNullableInt(rs, SUBSCRIBER_ID),
                rs.getString(VISIT_TYPE),
                rs.getInt(ACTUAL_NUMBER_OF_VISITORS),
                getNullableDateTime(rs, ENTRY_TIME),
                getNullableDateTime(rs, EXIT_TIME),
                getNullableInt(rs, HANDLED_BY_EMPLOYEE_ID),
                getNullableInt(rs, EXIT_HANDLED_BY_EMPLOYEE_ID),
                rs.getString(IDENTIFICATION_METHOD)
        );
    }

    /**
     * Creates a visit from an approved order.
     */
    public int createVisitFromOrder(int orderNumber, int actualNumberOfVisitors,
            int handledByEmployeeId, String identificationMethod) throws SQLException {

        ensureConnection();

        String orderSql = """
                SELECT order_number, park_id, subscriber_id
                FROM `order`
                WHERE order_number = ?
                  AND order_status = ?;
                """;

        int parkId;
        int subscriberId;

        try (PreparedStatement orderStmt = conn.prepareStatement(orderSql)) {
            orderStmt.setInt(1, orderNumber);
            orderStmt.setString(2, ORDER_STATUS_APPROVED);

            try (ResultSet orderRs = orderStmt.executeQuery()) {
                if (!orderRs.next()) {
                    return -1;
                }

                parkId = orderRs.getInt(PARK_ID);
                subscriberId = orderRs.getInt(SUBSCRIBER_ID);
            }
        }

        List<Object> values = new ArrayList<>();

        values.add(orderNumber);
        values.add(parkId);
        values.add(subscriberId);
        values.add(VISIT_TYPE_ORDERED);
        values.add(actualNumberOfVisitors);
        values.add(Timestamp.valueOf(LocalDateTime.now()));
        values.add(handledByEmployeeId);
        values.add(identificationMethod);

        return insertFieldsAndReturnGeneratedKey(
                new String[] {
                        ORDER_NUMBER,
                        PARK_ID,
                        SUBSCRIBER_ID,
                        VISIT_TYPE,
                        ACTUAL_NUMBER_OF_VISITORS,
                        ENTRY_TIME,
                        HANDLED_BY_EMPLOYEE_ID,
                        IDENTIFICATION_METHOD
                },
                values
        );
    }

    /**
     * Closes an existing visit.
     */
    public boolean closeVisit(int visitId, int exitHandledByEmployeeId) throws SQLException {
        ensureConnection();

        String sql = "UPDATE `" + getTableName() + "` "
                + "SET " + EXIT_TIME + " = ?, "
                + EXIT_HANDLED_BY_EMPLOYEE_ID + " = ? "
                + "WHERE " + VISIT_ID + " = ?;";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(2, exitHandledByEmployeeId);
            pstmt.setInt(3, visitId);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Returns a visit by id.
     */
    public Visit getVisitById(int visitId) throws SQLException {
        ensureConnection();

        String sql = selectByFields(
                new String[] { "*" },
                new String[] { VISIT_ID }
        );

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, visitId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return convertResultSetToVisit(rs);
                }
            }
        }

        return null;
    }
}