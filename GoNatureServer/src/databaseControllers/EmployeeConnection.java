package databaseControllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import common.Employee;

/**
 * DB connector for the employee table.
 */
public class EmployeeConnection extends AbstractDBConnection {

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

    private EmployeeConnection() throws SQLException {
        connect();
    }

    public static EmployeeConnection getInstance() throws SQLException {
        if (instance == null || instance.conn == null || instance.conn.isClosed()) {
            instance = new EmployeeConnection();
        }

        return instance;
    }

    @Override
    protected String getTableName() {
        return ConstantsDBTableNames.EMPLOYEE;
    }

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
     */
    public Employee login(String username, String password) throws SQLException {
        ensureConnection();

        String sql = selectByFieldsAND(
                new String[] { "*" },
                new String[] { USERNAME, PASSWORD, IS_ACTIVE }
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

    public String getEmployeeRole(int employeeId) throws SQLException {
        ensureConnection();

        String sql = selectByFields(
                new String[] { EMPLOYEE_ROLE },
                new String[] { EMPLOYEE_ID }
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

    public boolean isParkManager(int employeeId) throws SQLException {
        return "park_manager".equals(getEmployeeRole(employeeId));
    }

    public boolean isDepartmentManager(int employeeId) throws SQLException {
        return "department_manager".equals(getEmployeeRole(employeeId));
    }

    public boolean isServiceRepresentative(int employeeId) throws SQLException {
        return "service_representative".equals(getEmployeeRole(employeeId));
    }

    public boolean isParkWorker(int employeeId) throws SQLException {
        return "park_worker".equals(getEmployeeRole(employeeId));
    }

    public int getEmployeeParkId(int employeeId) throws SQLException {
        ensureConnection();

        String sql = selectByFields(
                new String[] { PARK_ID },
                new String[] { EMPLOYEE_ID }
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