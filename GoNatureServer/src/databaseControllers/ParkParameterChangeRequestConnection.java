package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import common.ParkParameterChangeRequest;

/**
 * DB connector for the park_parameter_change_request table.
 */
public class ParkParameterChangeRequestConnection extends AbstractDBConnection {

    private static ParkParameterChangeRequestConnection instance;

    private final String REQUEST_ID = "request_id";
    private final String PARK_ID = "park_id";
    private final String REQUESTED_BY_EMPLOYEE_ID = "requested_by_employee_id";
    private final String APPROVED_BY_EMPLOYEE_ID = "approved_by_employee_id";
    private final String PARAMETER_NAME = "parameter_name";
    private final String OLD_VALUE = "old_value";
    private final String NEW_VALUE = "new_value";
    private final String REQUEST_STATUS = "request_status";
    private final String REQUESTED_AT = "requested_at";
    private final String REVIEWED_AT = "reviewed_at";
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

        if (parkId <= 0 || requestedByEmployeeId <= 0
                || parameterName == null || parameterName.isBlank()
                || oldValue == null || newValue == null) {
            return false;
        }

        List<Object> values = new ArrayList<>();

        values.add(parkId);
        values.add(requestedByEmployeeId);
        values.add(parameterName);
        values.add(oldValue);
        values.add(newValue);
        values.add(STATUS_PENDING);
        values.add(Timestamp.valueOf(LocalDateTime.now()));

        insertFields(
                new String[] {
                        PARK_ID,
                        REQUESTED_BY_EMPLOYEE_ID,
                        PARAMETER_NAME,
                        OLD_VALUE,
                        NEW_VALUE,
                        REQUEST_STATUS,
                        REQUESTED_AT
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
     * 
     * Uses the generic updateFields method from AbstractDBConnection.
     */
    private boolean updateRequestStatus(int requestId, int approvedByEmployeeId,
            String reviewNote, String newStatus) throws SQLException {
    	

        ensureConnection();
        
        if (reviewNote == null) {
            reviewNote = "";
        }

        if (requestId <= 0 || approvedByEmployeeId <= 0
                || newStatus == null || newStatus.isBlank()) {
            return false;
        }

        if (!isPendingRequest(requestId)) {
            return false;
        }

        return updateFields(
                new String[] {
                        REQUEST_STATUS,
                        APPROVED_BY_EMPLOYEE_ID,
                        REVIEW_NOTE,
                        REVIEWED_AT
                },
                List.of(
                        newStatus,
                        approvedByEmployeeId,
                        reviewNote,
                        Timestamp.valueOf(LocalDateTime.now())
                ),
                new String[] {
                        REQUEST_ID,
                        REQUEST_STATUS
                },
                List.of(
                        requestId,
                        STATUS_PENDING
                )
        );
    }

    /**
     * Checks whether a request exists and is still pending.
     */
    private boolean isPendingRequest(int requestId) throws SQLException {
        ensureConnection();

        String sql = selectByFields(
                new String[] { REQUEST_ID },
                new String[] { REQUEST_ID, REQUEST_STATUS }
        );

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, requestId);
            pstmt.setString(2, STATUS_PENDING);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Returns a specific request by id.
     */
    public ParkParameterChangeRequest getRequestById(int requestId) throws SQLException {
        ensureConnection();

        String sql = selectByFields(
                new String[] { "*" },
                new String[] { REQUEST_ID }
        );

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, requestId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return convertResultSetToRequest(rs);
                }
            }
        }

        return null;
    }

    /**
     * Returns all pending requests.
     */
    public List<ParkParameterChangeRequest> getPendingRequests() throws SQLException {
        ensureConnection();

        String sql = "SELECT * FROM `" + getTableName() + "` "
                + "WHERE " + REQUEST_STATUS + " = ? "
                + "ORDER BY " + REQUESTED_AT + ";";

        List<ParkParameterChangeRequest> pendingRequests = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, STATUS_PENDING);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    pendingRequests.add(convertResultSetToRequest(rs));
                }
            }
        }

        return pendingRequests;
    }

    /**
     * Converts a database row into a ParkParameterChangeRequest object.
     */
    private ParkParameterChangeRequest convertResultSetToRequest(ResultSet rs)
            throws SQLException {

        Integer approvedByEmployeeId = null;

        if (rs.getObject(APPROVED_BY_EMPLOYEE_ID) != null) {
            approvedByEmployeeId = rs.getInt(APPROVED_BY_EMPLOYEE_ID);
        }

        return new ParkParameterChangeRequest(
                rs.getInt(REQUEST_ID),
                rs.getInt(PARK_ID),
                rs.getInt(REQUESTED_BY_EMPLOYEE_ID),
                approvedByEmployeeId,
                rs.getString(PARAMETER_NAME),
                rs.getString(OLD_VALUE),
                rs.getString(NEW_VALUE),
                rs.getString(REQUEST_STATUS),
                convertTimestampToLocalDateTime(rs.getTimestamp(REQUESTED_AT)),
                convertTimestampToLocalDateTime(rs.getTimestamp(REVIEWED_AT)),
                rs.getString(REVIEW_NOTE)
        );
    }

    /**
     * Converts SQL Timestamp to LocalDateTime safely.
     */
    private LocalDateTime convertTimestampToLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }

        return timestamp.toLocalDateTime();
    }
}