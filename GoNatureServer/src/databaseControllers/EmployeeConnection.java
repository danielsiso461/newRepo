package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import common.*;

/**
 * This class is the DB connector used when working with the employee table.
 * 
 * The class is implemented as a Singleton, so the server will use only one
 * database connection object for employees during runtime.
 * 
 * The employee table stores the system workers, including their login details,
 * role, park affiliation, and active status.
 */
public class EmployeeConnection extends AbstractDBConnection {

	/**
	 * The single instance of EmployeeConnection.
	 */
	private static EmployeeConnection instance;
	/**
	 * the employee table's employee id column
	 */
	private final String EMPLOYEE_ID = "employee_id";
	/**
	 * the employee table's employee number column
	 */
	private final String EMPLOYEE_NUMBER = "employee_number";
	/**
	 * the employee table's employee first name column
	 */
	private final String EMPLOYEE_FIRST_NAME = "employee_first_name";
	/**
	 * the employee table's employee last name column
	 */
	private final String EMPLOYEE_LAST_NAME = "employee_last_name";
	/**
	 * the employee table's employee email column
	 */
	private final String EMPLOYEE_EMAIL = "employee_email";
	/**
	 * the employee table's employee username column
	 */
	private final String USERNAME = "username";
	/**
	 * the employee table's employee password column
	 */
	private final String PASSWORD = "password";
	/**
	 * the employee table's employee role column
	 */
	private final String EMPLOYEE_ROLE = "employee_role";
	/**
	 * the employee table's employee park id column
	 */
	private final String PARK_ID = "park_id";
	/**
	 * the employee table's employee activity status column
	 */
	private final String IS_ACTIVE = "is_active";

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
	 * This method checks employee login details.
	 * 
	 * The method returns an active employee whose username and password match the
	 * given values.
	 * 
	 * @param username the username entered by the employee
	 * @param password the password entered by the employee
	 * @return a ResultSet containing the employee data if login succeeds
	 * @throws SQLException if the select query fails
	 */
	public ResultSet login(String username, String password) throws SQLException {
		String sql = "SELECT * FROM employee "
				+ "WHERE username = ? AND password = ? AND is_active = 1;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, username);
		pstmt.setString(2, password);

		return pstmt.executeQuery();
	}

	/**
	 * This method returns the role of a specific employee.
	 * 
	 * @param employeeId the employee ID
	 * @return the employee role, or null if the employee was not found
	 * @throws SQLException if the select query fails
	 */
	public String getEmployeeRole(int employeeId) throws SQLException {
		String sql = "SELECT employee_role FROM employee WHERE employee_id = ?;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, employeeId);

		ResultSet rs = pstmt.executeQuery();

		if (rs.next()) {
			return rs.getString("employee_role");
		}

		return null;
	}

	/**
	 * This method checks whether the employee is a park manager.
	 * 
	 * @param employeeId the employee ID
	 * @return true if the employee role is park_manager, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean isParkManager(int employeeId) throws SQLException {
		return "park_manager".equals(getEmployeeRole(employeeId));
	}

	/**
	 * This method checks whether the employee is a department manager.
	 * 
	 * @param employeeId the employee ID
	 * @return true if the employee role is department_manager, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean isDepartmentManager(int employeeId) throws SQLException {
		return "department_manager".equals(getEmployeeRole(employeeId));
	}

	/**
	 * This method checks whether the employee is a service representative.
	 * 
	 * @param employeeId the employee ID
	 * @return true if the employee role is service_representative, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean isServiceRepresentative(int employeeId) throws SQLException {
		return "service_representative".equals(getEmployeeRole(employeeId));
	}

	/**
	 * This method checks whether the employee is a park worker.
	 * 
	 * @param employeeId the employee ID
	 * @return true if the employee role is park_worker, false otherwise
	 * @throws SQLException if the select query fails
	 */
	public boolean isParkWorker(int employeeId) throws SQLException {
		return "park_worker".equals(getEmployeeRole(employeeId));
	}

	/**
	 * This method returns the park ID connected to a specific employee.
	 * 
	 * This is useful for checking whether a park manager or park worker belongs to a
	 * specific park.
	 * 
	 * @param employeeId the employee ID
	 * @return the park ID of the employee, or -1 if the employee was not found
	 * @throws SQLException if the select query fails
	 */
	public int getEmployeeParkId(int employeeId) throws SQLException {
		String sql = "SELECT park_id FROM employee WHERE employee_id = ?;";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, employeeId);

		ResultSet rs = pstmt.executeQuery();

		if (rs.next()) {
			return rs.getInt("park_id");
		}

		return -1;
	}
	
	/**
	 * This method checks employee login details and returns the matching employee.
	 * 
	 * The method searches for an active employee whose username and password match
	 * the values entered in the login screen.
	 * 
	 * @param username the username entered by the employee
	 * @param password the password entered by the employee
	 * @return an Employee object if login succeeds, otherwise null
	 * @throws SQLException if the select query fails
	 */
	public Employee loginEmployee(String username, String password) throws SQLException {
		String query = selectByFields(
				new String[] {
						EMPLOYEE_ID,
						EMPLOYEE_NUMBER,
						EMPLOYEE_FIRST_NAME,
						EMPLOYEE_LAST_NAME,
						EMPLOYEE_EMAIL,
						USERNAME,
						EMPLOYEE_ROLE,
						PARK_ID
				},
				new String[] {
						USERNAME,
						PASSWORD,
						IS_ACTIVE
				}
		);

		try (PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setString(1, username);
			pstmt.setString(2, password);
			pstmt.setInt(3, 1);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					Integer parkId = rs.getObject(PARK_ID) == null ? null : rs.getInt(PARK_ID);

					return new Employee(
							rs.getInt(EMPLOYEE_ID),
							rs.getInt(EMPLOYEE_NUMBER),
							rs.getString(EMPLOYEE_FIRST_NAME),
							rs.getString(EMPLOYEE_LAST_NAME),
							rs.getString(EMPLOYEE_EMAIL),
							rs.getString(USERNAME),
							rs.getString(EMPLOYEE_ROLE),
							parkId
					);
				}
			}
		}

		return null;
	}
}