package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DB connector for the park_parameter_change_request table.
 */
public class ParkParameterChangeRequestConnection extends AbstractDBConnection {

    private static ParkParameterChangeRequestConnection instance;

    private final String REQUEST_ID = "request_id";
    private final String PARK_ID = "park_id";
    private final String REQUESTED_BY_EMPLOYEE_ID = "requested_by_employee_id";
    private final String PARAMETER_NAME = "parameter_name";
    private final String OLD_VALUE = "old_value";
    private final String NEW_VALUE = "new_value";
    private final String REQUEST_STATUS = "request_status";
    private final String APPROVED_BY_EMPLOYEE_ID = "approved_by_employee_id";
    private final String REVIEW_NOTE = "review_note";

    private final String STATUS_PENDING = "pending";
    private final String STATUS_APPROVED = "approved";
    private final String STATUS_REJECTED = "rejected";

    private ParkParameterChangeRequestConnection() throws SQLException {
        connect();
    }

    public static ParkParameterChangeRequestConnection getInstance() throws SQLException {
        if (instance == null || instance.conn == null || instance.conn.isClosed()) {
            instance = new ParkParameterChangeRequestConnection();
        }

        return instance;
    }

    @Override
    protected String getTableName() {
        return ConstantsDBTableNames.PARK_PARAMETER_CHANGE_REQUEST;
    }

    /**
     * Creates a new park parameter change request.
     */
    public boolean createChangeRequest(int parkId, int requestedByEmployeeId,
            String parameterName, String oldValue, String newValue) throws SQLException {

        ensureConnection();

        List<Object> values = new ArrayList<>();

        values.add(parkId);
        values.add(requestedByEmployeeId);
        values.add(parameterName);
        values.add(oldValue);
        values.add(newValue);

        insertFields(
                new String[] {
                        PARK_ID,
                        REQUESTED_BY_EMPLOYEE_ID,
                        PARAMETER_NAME,
                        OLD_VALUE,
                        NEW_VALUE
                },
                values
        );

        return true;
    }

    /**
     * Approves a pending request.
     */
    public boolean approveRequest(int requestId, int approvedByEmployeeId,
            String reviewNote) throws SQLException {

        return updateRequestStatus(
                requestId,
                approvedByEmployeeId,
                reviewNote,
                STATUS_APPROVED
        );
    }

    /**
     * Rejects a pending request.
     */
    public boolean rejectRequest(int requestId, int approvedByEmployeeId,
            String reviewNote) throws SQLException {

        return updateRequestStatus(
                requestId,
                approvedByEmployeeId,
                reviewNote,
                STATUS_REJECTED
        );
    }

    /**
     * Updates a pending request to approved or rejected.
     */
    private boolean updateRequestStatus(int requestId, int approvedByEmployeeId,
            String reviewNote, String newStatus) throws SQLException {

        ensureConnection();

        String sql = "UPDATE `" + getTableName() + "` "
                + "SET " + REQUEST_STATUS + " = ?, "
                + APPROVED_BY_EMPLOYEE_ID + " = ?, "
                + REVIEW_NOTE + " = ? "
                + "WHERE " + REQUEST_ID + " = ? "
                + "AND " + REQUEST_STATUS + " = ?;";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, approvedByEmployeeId);
            pstmt.setString(3, reviewNote);
            pstmt.setInt(4, requestId);
            pstmt.setString(5, STATUS_PENDING);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Returns all pending requests.
     */
    public List<Object[]> getPendingRequests() throws SQLException {
        ensureConnection();

        String sql = "SELECT * FROM `" + getTableName() + "` "
                + "WHERE " + REQUEST_STATUS + " = ? "
                + "ORDER BY requested_at;";

        List<Object[]> pendingRequests = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, STATUS_PENDING);

            try (ResultSet rs = pstmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    Object[] row = new Object[columnCount];

                    for (int i = 1; i <= columnCount; i++) {
                        row[i - 1] = rs.getObject(i);
                    }

                    pendingRequests.add(row);
                }
            }
        }

        return pendingRequests;
    }
}