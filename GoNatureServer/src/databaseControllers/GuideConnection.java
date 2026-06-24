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

    /*
     * Table column names used by this DB connector
     */
    /**
     * the guide table's guide id column's name
     */
    private final String GUIDE_ID = "guide_id";
    /**
     * the guide table's subscriber id column's name
     */
    private final String SUBSCRIBER_ID = "subscriber_id";
    /**
     * the guide table's employee id column's name
     */
    private final String AUTHORIZED_BY_EMPLOYEE_ID = "authorized_by_employee_id";
    /**
     * the guide table's organization name column's name
     */
    private final String ORGANIZATION_NAME = "organization_name";
    /**
     * the guide table's guide status column's name
     */
    private final String GUIDE_STATUS = "guide_status";
    /** this holds the active status of a guide*/    
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
     * This method Checks whether the subscriber already exists in the guide table.
     * 
     * @param subscriberId the id of the subscriber to check
     * @return true if the subscriber is already registered as a guide, otherwise false
     * @throws SQLException if the select query fails
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
     * this method handles registering a guide
     * @param request the guide registration request
     * @throws SQLException in case of sql error
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
    * This method checks whether a given id is an active, registered guide
    * 
    * @param id the id to check
    * @return the id of the guide if true, null otherwise
    * @throws SQLException if the select query fails
    */
    public Integer isActiveGuide(int id) throws SQLException {
       String query = selectByFieldsAND(
           new String[] { GUIDE_ID },
           new String[] { SUBSCRIBER_ID, GUIDE_STATUS }
       );
       
       PreparedStatement pstmt = conn.prepareStatement(query);
       pstmt.setInt(1, id);
       pstmt.setString(2, GUIDE_STATUS_ACTIVE);

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
    
    /**
	 * Prevents cloning of the Singleton instance.
	 */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}