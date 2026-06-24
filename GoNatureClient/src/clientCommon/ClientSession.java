package clientCommon;

/**
 * Stores details about the currently logged-in employee on the client side.
 */
public final class ClientSession {

    private static int employeeId = -1;
    private static String employeeRole;
    private static int employeeParkId = -1;

    private ClientSession() {
    }

    public static void setLoggedEmployee(int id, String role, int parkId) {
        employeeId = id;
        employeeRole = role;
        employeeParkId = parkId;
    }

    public static int getEmployeeId() {
        return employeeId;
    }

    public static String getEmployeeRole() {
        return employeeRole;
    }

    public static int getEmployeeParkId() {
        return employeeParkId;
    }

    public static boolean isEmployeeLoggedIn() {
        return employeeId > 0;
    }

    public static void clear() {
        employeeId = -1;
        employeeRole = null;
        employeeParkId = -1;
    }
}