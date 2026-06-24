package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import common.GuideRegistrationRequest;
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
    
    /**
     * This method checks whether a subscriber is already registered as a guide
     * 
     * @param subscriberId the id of the subscriber to check
     * @return true if the subscriber is already registered as a guide, otherwise false
     * @throws SQLException if the select query fails
     */
    public boolean isSubscriberAlreadyGuide(int subscriberId) throws SQLException {
    	String query = selectByFields(
    			new String[] {
    					GUIDE_ID
    			},
    			new String[] {
    					SUBSCRIBER_ID
    			}
    	);

    	try (PreparedStatement pstmt = conn.prepareStatement(query)) {
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
    public void registerGuide(GuideRegistrationRequest request) throws SQLException {
    	String sql = "INSERT INTO `" + getTableName() + "` "
    			+ "("
    			+ SUBSCRIBER_ID + ", "
    			+ ORGANIZATION_NAME + ", "
    			+ GUIDE_STATUS + ", "
    			+ AUTHORIZED_BY_EMPLOYEE_ID
    			+ ") VALUES (?, ?, ?, ?);";

    	try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
    		pstmt.setInt(1, request.getSubscriberId());
    		pstmt.setString(2, request.getOrganizationName());
    		pstmt.setString(3, request.getGuideStatus());
    		pstmt.setInt(4, request.getAuthorizedByEmployeeId());

    		pstmt.executeUpdate();
    	}
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

       ResultSet rs = pstmt.executeQuery();
       
       Integer exists = null;
       if(rs.next())
    	   exists = rs.getObject(GUIDE_ID, Integer.class);

       rs.close();
       pstmt.close();
       return exists;
    }
    
    /**
	 * Prevents cloning of the Singleton instance.
	 */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}
