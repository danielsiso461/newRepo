package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import common.GuideRegistrationRequest;

/**
 * This class is the DB connector used when working with the guide table
 * 
 * The class is implemented as a Singleton, so the server will use only one
 * database connection object for guide-related operations during runtime
 */
public class GuideConnection extends AbstractDBConnection {

	/**
     * The single instance of GuideDBController
     */
    private static GuideConnection INSTANCE = new GuideConnection();

    /**
     * Table column names used by this DB connector
     */
    private final String 
    					GUIDE_ID = "guide_id",
    					SUBSCRIBER_ID = "subscriber_id",
    					AUTHORIZED_BY_EMPLOYEE_ID = "authorized_by_employee_id",
    					ORGANIZATION_NAME = "organization_name",
    					GUIDE_STATUS = "guide_status";
    /* this holds the active status of a guide*/    
    private final String GUIDE_STATUS_ACTIVE = "active";
    
    /**
     * Private constructor for Singleton
     * 
     * It creates the database connection once
     */
    private GuideConnection() {
        super();
        try {
            this.connect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the single instance of GuideDBController
     * 
     * @return the only GuideDBController instance
     */
    public static GuideConnection getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the table name used by this DB connector
     * 
     * @return the guide table name
     */
    @Override
    public String getTableName() {
        return ConstantsDBTableNames.GUIDE;
    }
    
    /*
     * This method checks whether a subscriber is already registered as a guide
     * 
     * @param subscriberId the id of the subscriber to check
     * @return true if the subscriber is already registered as a guide, otherwise false
     * @throws SQLException if the select query fails
     */
    public boolean isSubscriberAlreadyGuide(int subscriberId) throws SQLException {
        String query = selectByFields(
            new String[] { GUIDE_ID },
            new String[] { SUBSCRIBER_ID }
        );

        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, subscriberId);

        ResultSet rs = pstmt.executeQuery();

        boolean exists = rs.next();

        rs.close();
        pstmt.close();

        return exists;
    }

    public boolean registerGuide(GuideRegistrationRequest request) throws SQLException {
        String query =
            "INSERT INTO `" + getTableName() + "` " +
            "(" + SUBSCRIBER_ID + ", " +
                  AUTHORIZED_BY_EMPLOYEE_ID + ", " +
                  ORGANIZATION_NAME + ", " +
                  GUIDE_STATUS + ", created_at) " +
            "VALUES (?, ?, ?, ?, NOW())";

        PreparedStatement pstmt = conn.prepareStatement(query);

        pstmt.setInt(1, request.getSubscriberId());
        pstmt.setInt(2, request.getAuthorizedByEmployeeId());
        pstmt.setString(3, request.getOrganizationName());
        pstmt.setString(4, request.getGuideStatus());

        int rows = pstmt.executeUpdate();

        pstmt.close();

        return rows == 1;
    }
    
    /* This method checks whether a given id is an active, registered guide
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

       ResultSet rs = pstmt.executeQuery();
       
       Integer exists = null;
       if(rs.next())
    	   exists = rs.getObject(GUIDE_ID, Integer.class);

       rs.close();
       pstmt.close();
       return exists;
    }
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}
