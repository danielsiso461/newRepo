package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import common.SystemUser;

/**
 * This class is the DB connector used when working with the employee table.
 * 
 * The class is implemented as a Singleton, so the server will use only one
 * database connection object for employees during runtime.
 * 
 * The employee table stores system workers, including their login details,
 * personal details, role, park affiliation, and active status.
 */
public class EmployeeConnection extends AbstractDBConnection {

	/**
	 * The single instance of EmployeeConnection.
	 */
	private static EmployeeConnection instance;

	/**
	 * The employee ID column in the employee table.
	 */
	private final String EMPLOYEE_ID = "employee_id";

	/**
	 * The employee number column in the employee table.
	 */
	private final String EMPLOYEE_NUMBER = "employee_number";

	/**
	 * The employee first name column in the employee table.
	 */
	private final String EMPLOYEE_FIRST_NAME = "employee_first_name";

	/**
	 * The employee last name column in the employee table.
	 */
	private final String EMPLOYEE_LAST_NAME = "employee_last_name";

	/**
	 * The employee email column in the employee table.
	 */
	private final String EMPLOYEE_EMAIL = "employee_email";

	/**
	 * The username column in the employee table.
	 */
	private final String USERNAME = "username";

	/**
	 * The password column in the employee table.
	 */
	private final String PASSWORD = "password";

	/**
	 * The employee role column in the employee table.
	 */
	private final String EMPLOYEE_ROLE = "employee_role";

	/**
	 * The park ID column in the employee table.
	 */
	private final String PARK_ID = "park_id";

	/**
	 * The active status column in the employee table.
	 */
	private final String IS_ACTIVE = "is_active";

	/**
	 * Employee role value for park manager.
	 */
	private final String PARK_MANAGER = "park_manager";

	/**
	 * Employee role value for park worker.
	 */
	private final String PARK_WORKER = "park_worker";

	/**
	 * Employee role value for department manager.
	 */
	private final String DEPARTMENT_MANAGER = "department_manager";

	/**
	 * Employee role value for service representative.
	 */
	private final String SERVICE_REPRESENTATIVE = "service_representative";

	/**
	 * Private constructor for Singleton.
	 * 
	 * It creates the database connection once.
	 * 
	 * @throws SQLException if the connection to the database fails
	 */
	private EmployeeConnection() throws SQLException {
		connect();
	}

	/**
	 * Returns the single instance of EmployeeConnection.
	 * 
	 * If no instance exists, or if the existing database connection is closed, a new
	 * instance is created.
	 * 
	 * @return the only EmployeeConnection instance
	 * @throws SQLException if creating the database connection fails
	 */
	public static EmployeeConnection getInstance() throws SQLException {
		if (instance == null || instance.conn == null || instance.conn.isClosed()) {
			instance = new EmployeeConnection();
		}

		return instance;
	}

	/**
	 * Returns the table name used by this DB connector.
	 * 
	 * @return the employee table name
	 */
	@Override
	protected String getTableName() {
		return ConstantsDBTableNames.EMPLOYEE;
	}

	/**
	 * This method checks that the database connection is open.
	 * 
	 * @throws SQLException if reconnecting to the database fails
	 */
	private void ensureConnection() throws SQLException {
		if (conn == null || conn.isClosed()) {
			connect();
		}
	}

	/**
	 * This method checks employee login details.
	 * 
	 * The method checks if there is an active employee with the given username and
	 * password. If the login succeeds, the method returns a SystemUser object with
	 * the employee details needed by the system.
	 * 
	 * @param username the username entered by the employee
	 * @param password the password entered by the employee
	 * @return a SystemUser object if login succeeds, otherwise null
	 * @throws SQLException if the select query fails
	 */
	public SystemUser login(String username, String password) throws SQLException {
		ensureConnection();

		if (username == null || username.isBlank() || password == null || password.isBlank()) {
			return null;
		}

		String[] columnNames = {
				EMPLOYEE_ID,
				EMPLOYEE_FIRST_NAME,
				EMPLOYEE_LAST_NAME,
				EMPLOYEE_EMAIL,
				USERNAME,
				EMPLOYEE_ROLE,
				PARK_ID
		};

		String[] keyColumns = {
				USERNAME,
				PASSWORD,
				IS_ACTIVE
		};

		String sql = selectByFields(columnNames, keyColumns);

		PreparedStatement pstmt = conn.prepareStatement(sql);

		pstmt.setString(1, username);
		pstmt.setString(2, password);
		pstmt.setInt(3, 1);

		java.sql.ResultSet rs = pstmt.executeQuery();

		SystemUser user = null;

		if (rs.next()) {
			Integer parkId = null;

			if (rs.getObject(PARK_ID) != null) {
				parkId = rs.getInt(PARK_ID);
			}

			user = new SystemUser(
					rs.getInt(EMPLOYEE_ID),
					rs.getString(EMPLOYEE_FIRST_NAME) + " " + rs.getString(EMPLOYEE_LAST_NAME),
					rs.getString(EMPLOYEE_EMAIL),
					rs.getString(USERNAME),
					"employee",
					rs.getString(EMPLOYEE_ROLE),
					parkId);
		}

		rs.close();
		pstmt.close();

		return user;
	}

	/**
	 * This method checks whether employee login details are valid.
	 * 
	 * @param username the username entered by the employee
	 * @param password the password entered by the employee
	 * @return true if login succeeds, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean isLoginValid(String username, String password) throws SQLException {
		return login(username, password) != null;
	}

	/**
	 * This method returns the role of a specific active employee.
	 * 
	 * The role is used to decide which actions the employee is allowed to perform.
	 * 
	 * @param employeeId the employee ID
	 * @return the employee role, or null if the employee was not found
	 * @throws SQLException if the select query fails
	 */
	public String getEmployeeRole(int employeeId) throws SQLException {
		ensureConnection();

		if (employeeId <= 0) {
			return null;
		}

		String[] columnNames = {
				EMPLOYEE_ROLE
		};

		String[] keyColumns = {
				EMPLOYEE_ID,
				IS_ACTIVE
		};

		String sql = selectByFields(columnNames, keyColumns);

		PreparedStatement pstmt = conn.prepareStatement(sql);

		pstmt.setInt(1, employeeId);
		pstmt.setInt(2, 1);

		java.sql.ResultSet rs = pstmt.executeQuery();

		String role = null;

		if (rs.next()) {
			role = rs.getString(EMPLOYEE_ROLE);
		}

		rs.close();
		pstmt.close();

		return role;
	}

	/**
	 * This method returns the park ID connected to a specific active employee.
	 * 
	 * This is used when checking if a park manager or park worker is allowed to work
	 * with a specific park.
	 * 
	 * @param employeeId the employee ID
	 * @return the park ID of the employee, or -1 if the employee was not found or is
	 *         not connected to a specific park
	 * @throws SQLException if the select query fails
	 */
	public int getEmployeeParkId(int employeeId) throws SQLException {
		ensureConnection();

		if (employeeId <= 0) {
			return -1;
		}

		String[] columnNames = {
				PARK_ID
		};

		String[] keyColumns = {
				EMPLOYEE_ID,
				IS_ACTIVE
		};

		String sql = selectByFields(columnNames, keyColumns);

		PreparedStatement pstmt = conn.prepareStatement(sql);

		pstmt.setInt(1, employeeId);
		pstmt.setInt(2, 1);

		java.sql.ResultSet rs = pstmt.executeQuery();

		int parkId = -1;

		if (rs.next() && rs.getObject(PARK_ID) != null) {
			parkId = rs.getInt(PARK_ID);
		}

		rs.close();
		pstmt.close();

		return parkId;
	}

	/**
	 * This method returns basic employee data as an ArrayList.
	 * 
	 * The returned ArrayList contains the following values:
	 * employee_id, employee_number, employee_first_name, employee_last_name,
	 * employee_email, username, employee_role, park_id, is_active.
	 * 
	 * The password is not returned.
	 * 
	 * @param employeeId the employee ID
	 * @return an ArrayList with employee data, or an empty ArrayList if the employee
	 *         was not found
	 * @throws SQLException if the select query fails
	 */
	public ArrayList<Object> getEmployeeDetails(int employeeId) throws SQLException {
		ensureConnection();

		ArrayList<Object> employeeDetails = new ArrayList<>();

		if (employeeId <= 0) {
			return employeeDetails;
		}

		String[] columnNames = {
				EMPLOYEE_ID,
				EMPLOYEE_NUMBER,
				EMPLOYEE_FIRST_NAME,
				EMPLOYEE_LAST_NAME,
				EMPLOYEE_EMAIL,
				USERNAME,
				EMPLOYEE_ROLE,
				PARK_ID,
				IS_ACTIVE
		};

		String[] keyColumns = {
				EMPLOYEE_ID,
				IS_ACTIVE
		};

		String sql = selectByFields(columnNames, keyColumns);

		PreparedStatement pstmt = conn.prepareStatement(sql);

		pstmt.setInt(1, employeeId);
		pstmt.setInt(2, 1);

		java.sql.ResultSet rs = pstmt.executeQuery();

		if (rs.next()) {
			employeeDetails.add(rs.getInt(EMPLOYEE_ID));
			employeeDetails.add(rs.getString(EMPLOYEE_NUMBER));
			employeeDetails.add(rs.getString(EMPLOYEE_FIRST_NAME));
			employeeDetails.add(rs.getString(EMPLOYEE_LAST_NAME));
			employeeDetails.add(rs.getString(EMPLOYEE_EMAIL));
			employeeDetails.add(rs.getString(USERNAME));
			employeeDetails.add(rs.getString(EMPLOYEE_ROLE));

			if (rs.getObject(PARK_ID) != null) {
				employeeDetails.add(rs.getInt(PARK_ID));
			} else {
				employeeDetails.add(null);
			}

			employeeDetails.add(rs.getInt(IS_ACTIVE));
		}

		rs.close();
		pstmt.close();

		return employeeDetails;
	}

	/**
	 * This method checks whether the employee is a park manager.
	 * 
	 * @param employeeId the employee ID
	 * @return true if the employee role is park_manager, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean isParkManager(int employeeId) throws SQLException {
		return PARK_MANAGER.equals(getEmployeeRole(employeeId));
	}

	/**
	 * This method checks whether the employee is a park worker.
	 * 
	 * @param employeeId the employee ID
	 * @return true if the employee role is park_worker, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean isParkWorker(int employeeId) throws SQLException {
		return PARK_WORKER.equals(getEmployeeRole(employeeId));
	}

	/**
	 * This method checks whether the employee is a department manager.
	 * 
	 * @param employeeId the employee ID
	 * @return true if the employee role is department_manager, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean isDepartmentManager(int employeeId) throws SQLException {
		return DEPARTMENT_MANAGER.equals(getEmployeeRole(employeeId));
	}

	/**
	 * This method checks whether the employee is a service representative.
	 * 
	 * @param employeeId the employee ID
	 * @return true if the employee role is service_representative, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean isServiceRepresentative(int employeeId) throws SQLException {
		return SERVICE_REPRESENTATIVE.equals(getEmployeeRole(employeeId));
	}

	/**
	 * This method checks whether an employee is allowed to access a specific park.
	 * 
	 * General rule:
	 * A department manager can access every park.
	 * Any other employee can access only the park connected to him in the employee
	 * table.
	 * 
	 * @param employeeId the employee ID
	 * @param parkId     the park ID requested for the action
	 * @return true if the employee can access the requested park, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean canAccessPark(int employeeId, int parkId) throws SQLException {
		if (employeeId <= 0 || parkId <= 0) {
			return false;
		}

		String role = getEmployeeRole(employeeId);

		if (role == null) {
			return false;
		}

		if (DEPARTMENT_MANAGER.equals(role)) {
			return true;
		}

		int employeeParkId = getEmployeeParkId(employeeId);

		return employeeParkId == parkId;
	}

	/**
	 * This method checks whether an employee can handle park entrance and exit
	 * operations.
	 * 
	 * Entrance and exit operations can be handled by:
	 * department manager - in every park,
	 * park manager - only in his own park,
	 * park worker - only in his own park.
	 * 
	 * @param employeeId the employee ID
	 * @param parkId     the park ID requested for entrance or exit handling
	 * @return true if the employee can handle entrance or exit for this park, false
	 *         otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean canHandleParkEntrance(int employeeId, int parkId) throws SQLException {
		if (employeeId <= 0 || parkId <= 0) {
			return false;
		}

		String role = getEmployeeRole(employeeId);

		if (role == null) {
			return false;
		}

		if (!DEPARTMENT_MANAGER.equals(role) && !PARK_MANAGER.equals(role) && !PARK_WORKER.equals(role)) {
			return false;
		}

		return canAccessPark(employeeId, parkId);
	}

	/**
	 * This method checks whether an employee is the manager of a specific park.
	 * 
	 * A park manager is relevant only to the park connected to him in the employee
	 * table.
	 * 
	 * @param employeeId the employee ID
	 * @param parkId     the park ID
	 * @return true if the employee is the manager of the requested park, false
	 *         otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean isParkManagerOfPark(int employeeId, int parkId) throws SQLException {
		if (employeeId <= 0 || parkId <= 0) {
			return false;
		}

		if (!isParkManager(employeeId)) {
			return false;
		}

		return canAccessPark(employeeId, parkId);
	}

	/**
	 * This method returns the park ID managed by a park manager.
	 * 
	 * If the employee is not an active park manager, or if the employee is not
	 * connected to a specific park, the method returns -1.
	 * 
	 * @param employeeId the employee ID
	 * @return the park ID managed by the park manager, or -1 if not relevant
	 * @throws SQLException if the select query fails
	 */
	public int getManagedParkId(int employeeId) throws SQLException {
		if (!isParkManager(employeeId)) {
			return -1;
		}

		return getEmployeeParkId(employeeId);
	}

	/**
	 * This method checks whether an employee can request park parameter changes.
	 * 
	 * Only a park manager can request park parameter changes, and only for the park
	 * connected to him in the employee table.
	 * 
	 * @param employeeId the employee ID
	 * @param parkId     the park ID requested for parameter change
	 * @return true if the employee can request changes for this park, false
	 *         otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean canRequestParkParameterChange(int employeeId, int parkId) throws SQLException {
		return isParkManagerOfPark(employeeId, parkId);
	}

	/**
	 * This method checks whether an employee can approve park parameter changes.
	 * 
	 * Only a department manager can approve or reject park parameter change
	 * requests.
	 * 
	 * @param employeeId the employee ID
	 * @return true if the employee can approve park parameter changes, false
	 *         otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean canApproveParkParameterChange(int employeeId) throws SQLException {
		return isDepartmentManager(employeeId);
	}

	/**
	 * This method checks whether an employee can register guides.
	 * 
	 * According to the system story, authorized guides are registered by a service
	 * representative.
	 * 
	 * @param employeeId the employee ID
	 * @return true if the employee can register guides, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean canRegisterGuide(int employeeId) throws SQLException {
		return isServiceRepresentative(employeeId);
	}

	/**
	 * This method checks whether an employee can register subscribers.
	 * 
	 * According to the system story, family subscribers are registered by a service
	 * representative.
	 * 
	 * @param employeeId the employee ID
	 * @return true if the employee can register subscribers, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean canRegisterSubscriber(int employeeId) throws SQLException {
		return isServiceRepresentative(employeeId);
	}
	
	/**
	 * This method registers a new subscriber if the employee is allowed to do it.
	 * 
	 * According to the system story, family subscribers are registered by a service
	 * representative.
	 * 
	 * The method first checks that the employee is a service representative. If the
	 * employee is not allowed to register subscribers, the method returns false.
	 * 
	 * The actual insert into the subscriber table is done by SubscriberConnection,
	 * using the general insertFields method from AbstractDBConnection.
	 * 
	 * @param employeeId         the employee ID that performs the registration
	 * @param subscriberId       the subscriber ID used as the subscriber number
	 * @param subscriberName     the full name of the subscriber
	 * @param idNumber           the personal identification number of the subscriber
	 * @param phone              the subscriber phone number
	 * @param email              the subscriber email address
	 * @param familyMembersCount the number of family members included in the subscription
	 * @param paymentMethod      the payment method
	 * @param creditCardLast4    the last four digits of the credit card, or null for cash payment
	 * @return true if the subscriber was registered successfully, false otherwise
	 * @throws SQLException if the insert query fails
	 */
	public boolean registerSubscriber(int employeeId, int subscriberId, String subscriberName, String idNumber,
			String phone, String email, int familyMembersCount, String paymentMethod, String creditCardLast4)
			throws SQLException {

		if (!canRegisterSubscriber(employeeId)) {
			return false;
		}

		return SubscriberConnection.getInstance().addSubscriber(subscriberId, subscriberName, idNumber, phone, email,
				familyMembersCount, paymentMethod, creditCardLast4);
	}
	
	
	/**
	 * This method registers a new guide if the employee is allowed to do it.
	 * 
	 * According to the system story, authorized guides are registered by a service
	 * representative.
	 * 
	 * The method first checks that the employee is allowed to register guides.
	 * If the employee is not allowed, the method returns -1.
	 * 
	 * The actual insert into the guide table is done by GuideConnection.
	 * The guide is connected to an existing subscriber.
	 * 
	 * @param employeeId        the employee ID that performs the registration
	 * @param subscriberId      the subscriber ID that should become a guide
	 * @param organizationName  the organization name of the guide
	 * @return the created active guide ID, or -1 if the registration failed
	 * @throws SQLException if the insert or select query fails
	 */
	public int registerGuide(int employeeId, int subscriberId, String organizationName) throws SQLException {

		if (!canRegisterGuide(employeeId)) {
			return -1;
		}

		return GuideConnection.getInstance().addGuide(subscriberId, employeeId, organizationName);
	}
}