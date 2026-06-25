
package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import common.GuideRegistrationRequest;

/**
 * Handles database operations related to guides.
 * 
 * This connector supports checking whether a subscriber is already registered
 * as a guide, registering new guides, and validating whether a subscriber is an
 * active guide.
 */
public class GuideConnection extends AbstractDBConnection {

    /**
     * The single instance of GuideConnection.
     */
    private static final GuideConnection INSTANCE = new GuideConnection();

    /**
     * Column name for the guide ID.
     */
    private final String GUIDE_ID = "guide_id";

    /**
     * Column name for the subscriber ID.
     */
    private final String SUBSCRIBER_ID = "subscriber_id";

    /**
     * Column name for the employee who authorized the guide.
     */
    private final String AUTHORIZED_BY_EMPLOYEE_ID = "authorized_by_employee_id";

    /**
     * Column name for the guide's organization name.
     */
    private final String ORGANIZATION_NAME = "organization_name";

    /**
     * Column name for the guide status.
     */
    private final String GUIDE_STATUS = "guide_status";

    /**
     * Column name for the guide creation timestamp.
     */
    private final String CREATED_AT = "created_at";

    /**
     * Status value that represents an active guide.
     */
    private final String GUIDE_STATUS_ACTIVE = "active";

    /**
     * Creates the GuideConnection singleton instance.
     * 
     * The constructor is private to prevent external object creation and opens the
     * database connection when the singleton is initialized.
     */
    private GuideConnection() {
        super();

        try {
            connect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the single instance of GuideConnection.
     * 
     * @return the singleton GuideConnection instance
     */
    public static GuideConnection getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the database table name used by this connector.
     * 
     * @return the guide table name
     */
    @Override
    public String getTableName() {
        return ConstantsDBTableNames.GUIDE;
    }

    /**
     * Checks whether a subscriber is already registered as a guide.
     * 
     * @param subscriberId the subscriber ID to check
     * @return true if the subscriber already has a guide record, otherwise false
     * @throws SQLException if the select query fails
     */
    public boolean isSubscriberAlreadyGuide(int subscriberId) throws SQLException {
        ensureConnection();

        String sql = selectByFields(
                new String[] {
                        GUIDE_ID
                },
                new String[] {
                        SUBSCRIBER_ID
                }
        );

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, subscriberId);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Registers a subscriber as a guide.
     * 
     * The method inserts a new guide record into the guide table using the data
     * received from the registration request.
     * 
     * @param request the guide registration request containing the guide details
     * @return true if the guide registration was inserted, or false if the request
     *         is null
     * @throws SQLException if the insert query fails
     */
    public boolean registerGuide(GuideRegistrationRequest request) throws SQLException {
        if (request == null) {
            return false;
        }

        ensureConnection();

        List<Object> values = new ArrayList<>();

        values.add(request.getSubscriberId());
        values.add(request.getAuthorizedByEmployeeId());
        values.add(request.getOrganizationName());
        values.add(request.getGuideStatus());
        values.add(Timestamp.valueOf(LocalDateTime.now()));

        insertFields(
                new String[] {
                        SUBSCRIBER_ID,
                        AUTHORIZED_BY_EMPLOYEE_ID,
                        ORGANIZATION_NAME,
                        GUIDE_STATUS,
                        CREATED_AT
                },
                values
        );

        return true;
    }

    /**
     * Returns the guide ID if the subscriber is registered as an active guide.
     * 
     * @param subscriberId the subscriber ID to check
     * @return the guide ID if the subscriber is an active guide, otherwise null
     * @throws SQLException if the select query fails
     */
    public Integer isActiveGuide(int subscriberId) throws SQLException {
        ensureConnection();

        String sql = selectByFieldsAND(
                new String[] {
                        GUIDE_ID
                },
                new String[] {
                        SUBSCRIBER_ID,
                        GUIDE_STATUS
                }
        );

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, subscriberId);
            pstmt.setString(2, GUIDE_STATUS_ACTIVE);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getObject(GUIDE_ID, Integer.class);
                }
            }
        }

        return null;
    }

    /**
     * Prevents cloning of the singleton instance.
     * 
     * @return never returns, because cloning is not supported
     * @throws CloneNotSupportedException always thrown to prevent cloning
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}

