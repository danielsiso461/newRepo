package common;

import java.io.Serializable;

/**
 * Represents an employee after a successful login.
 */
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    private int employeeId;
    private String employeeNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String employeeRole;
    private Integer parkId;
    private boolean active;

    public Employee(int employeeId, String employeeNumber, String firstName, String lastName,
                    String email, String username, String employeeRole,
                    Integer parkId, boolean active) {

        this.employeeId = employeeId;
        this.employeeNumber = employeeNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.employeeRole = employeeRole;
        this.parkId = parkId;
        this.active = active;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getEmployeeRole() {
        return employeeRole;
    }

    public Integer getParkId() {
        return parkId;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName + " (" + employeeRole + ")";
    }
}