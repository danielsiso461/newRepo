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
 * DB connector for the guide table.
 */
public class GuideConnection extends AbstractDBConnection {

    /**
     * The single instance of GuideConnection.
     */
    private static final GuideConnection INSTANCE = new GuideConnection();

    /**
     * The guide table's guide id column.
     */
    private final String GUIDE_ID = "guide_id";

    /**
     * The guide table's subscriber id column.
     */
    private final String SUBSCRIBER_ID = "subscriber_id";

    /**
     * The guide table's authorized employee id column.
     */
    private final String AUTHORIZED_BY_EMPLOYEE_ID = "authorized_by_employee_id";

    /**
     * The guide table's organization name column.
     */
    private final String ORGANIZATION_NAME = "organization_name";

    /**
     * The guide table's guide status column.
     */
    private final String GUIDE_STATUS = "guide_status";

    /**
     * The guide table's created at column.
     */
    private final String CREATED_AT = "created_at";

    /**
     * The status value that represents an active guide.
     */
    private final String GUIDE_STATUS_ACTIVE = "active";

    /**
     * Private constructor for Singleton.
     * 
     * It creates the database connection once.
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
     * @return the only GuideConnection instance
     */
    public static GuideConnection getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the table name used by this DB connector.
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
     * @param subscriberId the subscriber id to check
     * @return true if the subscriber is already registered as a guide, otherwise false
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
     * @param request the guide registration request
     * @return true if the insert request was executed, false if the request is null
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
     * Returns the guide id if the subscriber is an active guide.
     * 
     * @param subscriberId the subscriber id to check
     * @return the guide id if the subscriber is an active guide, otherwise null
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
     * Prevents cloning of the Singleton instance.
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}