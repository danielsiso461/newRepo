package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import common.GuideRegistrationRequest;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import common.GuideRegistrationRequest;

/**
 * DB connector for the guide table.
 */
public class GuideConnection extends AbstractDBConnection {

    private static final GuideConnection INSTANCE = new GuideConnection();

    private final String GUIDE_ID = "guide_id";
    private final String SUBSCRIBER_ID = "subscriber_id";
    private final String AUTHORIZED_BY_EMPLOYEE_ID = "authorized_by_employee_id";
    private final String ORGANIZATION_NAME = "organization_name";
    private final String GUIDE_STATUS = "guide_status";
    private final String CREATED_AT = "created_at";

    private final String GUIDE_STATUS_ACTIVE = "active";

    private GuideConnection() {
        super();

        try {
            connect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static GuideConnection getInstance() {
        return INSTANCE;
    }

    @Override
    public String getTableName() {
        return ConstantsDBTableNames.GUIDE;
    }

    /**
     * Checks whether the subscriber already exists in the guide table.
     */
    public boolean isSubscriberAlreadyGuide(int subscriberId) throws SQLException {
        ensureConnection();

        String sql = selectByFields(
                new String[] { GUIDE_ID },
                new String[] { SUBSCRIBER_ID }
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
     */
    public boolean registerGuide(GuideRegistrationRequest request) throws SQLException {
        ensureConnection();

        if (request == null) {
            return false;
        }

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
     */
    public Integer isActiveGuide(int subscriberId) throws SQLException {
        ensureConnection();

        String sql = selectByFieldsAND(
                new String[] { GUIDE_ID },
                new String[] { SUBSCRIBER_ID, GUIDE_STATUS }
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

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}