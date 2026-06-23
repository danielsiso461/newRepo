package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import common.Employee;

/**
 * DB connector for the employee table.
 */
public class EmployeeConnection extends AbstractDBConnection {

	/**
	 * The single instance of EmployeeConnection.
	 */
	private static EmployeeConnection instance;

	private final String EMPLOYEE_ID = "employee_id";
	private final String EMPLOYEE_NUMBER = "employee_number";
	private final String FIRST_NAME = "employee_first_name";
	private final String LAST_NAME = "employee_last_name";
	private final String EMAIL = "employee_email";
	private final String USERNAME = "username";
	private final String PASSWORD = "password";
	private final String EMPLOYEE_ROLE = "employee_role";
	private final String PARK_ID = "park_id";
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
	 * Converts the current ResultSet row into an Employee object.
	 * 
	 * @param rs the ResultSet positioned on the current employee row
	 * @return an Employee object
	 * @throws SQLException if reading data from the ResultSet fails
	 */
	private Employee convertResultSetToEmployee(ResultSet rs) throws SQLException {
		Integer parkId = rs.getObject(PARK_ID) == null
				? null
				: rs.getInt(PARK_ID);

		return new Employee(
				rs.getInt(EMPLOYEE_ID),
				rs.getString(EMPLOYEE_NUMBER),
				rs.getString(FIRST_NAME),
				rs.getString(LAST_NAME),
				rs.getString(EMAIL),
				rs.getString(USERNAME),
				rs.getString(EMPLOYEE_ROLE),
				parkId,
				rs.getInt(IS_ACTIVE) == 1
		);
	}

	/**
	 * Returns the active employee if login succeeds, otherwise null.
	 * 
	 * The method searches for an active employee whose username and password match
	 * the values entered in the login screen.
	 * 
	 * @param username the username entered by the employee
	 * @param password the password entered by the employee
	 * @return an Employee object if login succeeds, otherwise null
	 * @throws SQLException if the select query fails
	 */
	public Employee login(String username, String password) throws SQLException {
		ensureConnection();

		String sql = selectByFieldsAND(
				new String[] {
						"*"
				},
				new String[] {
						USERNAME,
						PASSWORD,
						IS_ACTIVE
				}
		);

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, username);
			pstmt.setString(2, password);
			pstmt.setInt(3, 1);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return convertResultSetToEmployee(rs);
				}
			}
		}

		return null;
	}

	/**
	 * Checks employee login details and returns the matching employee.
	 * 
	 * This method is kept for compatibility with older code that already calls
	 * loginEmployee instead of login.
	 * 
	 * @param username the username entered by the employee
	 * @param password the password entered by the employee
	 * @return an Employee object if login succeeds, otherwise null
	 * @throws SQLException if the select query fails
	 */
	public Employee loginEmployee(String username, String password) throws SQLException {
		return login(username, password);
	}

	/**
	 * Returns the role of an employee.
	 * 
	 * @param employeeId the employee ID
	 * @return the employee role, or null if the employee does not exist
	 * @throws SQLException if the select query fails
	 */
	public String getEmployeeRole(int employeeId) throws SQLException {
		ensureConnection();

		String sql = selectByFields(
				new String[] {
						EMPLOYEE_ROLE
				},
				new String[] {
						EMPLOYEE_ID
				}
		);

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, employeeId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getString(EMPLOYEE_ROLE);
				}
			}
		}

		return null;
	}

	/**
	 * Checks whether the employee is a park manager.
	 * 
	 * @param employeeId the employee ID
	 * @return true if the employee is a park manager, otherwise false
	 * @throws SQLException if the select query fails
	 */
	public boolean isParkManager(int employeeId) throws SQLException {
		return "park_manager".equals(getEmployeeRole(employeeId));
	}

	/**
	 * Checks whether the employee is a department manager.
	 * 
	 * @param employeeId the employee ID
	 * @return true if the employee is a department manager, otherwise false
	 * @throws SQLException if the select query fails
	 */
	public boolean isDepartmentManager(int employeeId) throws SQLException {
		return "department_manager".equals(getEmployeeRole(employeeId));
	}

	/**
	 * Checks whether the employee is a service representative.
	 * 
	 * @param employeeId the employee ID
	 * @return true if the employee is a service representative, otherwise false
	 * @throws SQLException if the select query fails
	 */
	public boolean isServiceRepresentative(int employeeId) throws SQLException {
		return "service_representative".equals(getEmployeeRole(employeeId));
	}

	/**
	 * Checks whether the employee is a park worker.
	 * 
	 * @param employeeId the employee ID
	 * @return true if the employee is a park worker, otherwise false
	 * @throws SQLException if the select query fails
	 */
	public boolean isParkWorker(int employeeId) throws SQLException {
		return "park_worker".equals(getEmployeeRole(employeeId));
	}

	/**
	 * Returns the park ID assigned to the employee.
	 * 
	 * If the employee does not have a park, or if the employee was not found, the
	 * method returns -1.
	 * 
	 * @param employeeId the employee ID
	 * @return the employee park ID, or -1 if there is no assigned park
	 * @throws SQLException if the select query fails
	 */
	public int getEmployeeParkId(int employeeId) throws SQLException {
		ensureConnection();

		String sql = selectByFields(
				new String[] {
						PARK_ID
				},
				new String[] {
						EMPLOYEE_ID
				}
		);

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, employeeId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					if (rs.getObject(PARK_ID) == null) {
						return -1;
					}

					return rs.getInt(PARK_ID);
				}
			}
		}

		return -1;
	}
}