package clientCommon;

/**
 * Stores details about the currently logged-in employee on the client side.
 */
public final class ClientSession {

    /**
     * the logged-in employee ID
     */
    private static int employeeId = -1;

    /**
     * the logged-in employee role
     */
    private static String employeeRole;

    /**
     * the park ID of the logged-in employee
     */
    private static int employeeParkId = -1;

    /**
     * Private constructor to prevent creating ClientSession objects.
     */
    private ClientSession() {
    }

    /**
     * Sets the logged-in employee details.
     *
     * @param id the employee ID
     * @param role the employee role
     * @param parkId the employee park ID
     */
    public static void setLoggedEmployee(int id, String role, int parkId) {
        employeeId = id;
        employeeRole = role;
        employeeParkId = parkId;
    }

    /**
     * Returns the logged-in employee ID.
     *
     * @return the employee ID
     */
    public static int getEmployeeId() {
        return employeeId;
    }

    /**
     * Returns the logged-in employee role.
     *
     * @return the employee role
     */
    public static String getEmployeeRole() {
        return employeeRole;
    }

    /**
     * Returns the park ID of the logged-in employee.
     *
     * @return the employee park ID
     */
    public static int getEmployeeParkId() {
        return employeeParkId;
    }

    /**
     * Returns whether an employee is currently logged in.
     *
     * @return true if an employee is logged in
     */
    public static boolean isEmployeeLoggedIn() {
        return employeeId > 0;
    }

    /**
     * Clears the current employee session details.
     */
    public static void clear() {
        employeeId = -1;
        employeeRole = null;
        employeeParkId = -1;
    }
}