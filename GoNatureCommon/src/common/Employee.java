package common;

import java.io.Serializable;

/**
 * This class represents an employee in the GoNature system.
 * 
 * The class is Serializable because Employee objects are sent between
 * the server and the client.
 */
public class Employee implements Serializable {
	/**
	 * serial version UID for serialization
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * the employee's id
	 */
	private int employeeId;
	/**
	 * the employee's number
	 */
	private int employeeNumber;
	/**
	 * the employee's first name
	 */
	private String firstName;
	/**
	 * the employee's last name
	 */
	private String lastName;
	/**
	 * the employee's email
	 */
	private String email;
	/**
	 * the employee's username
	 */
	private String username;
	/**
	 * the employee's role
	 */
	private String role;
	/**
	 * the employee's park id
	 */
	private Integer parkId;
	/**
	 * constructor used to create an employee object
	 * @param employeeId the employee's id
	 * @param employeeNumber the employee's number
	 * @param firstName	the employee's first name
	 * @param lastName the employee's last name
	 * @param email the employee's email
	 * @param username the employee's username
	 * @param role the employee's role
	 * @param parkId the employee's park id
	 */
	public Employee(int employeeId, int employeeNumber, String firstName,
			String lastName, String email, String username, String role, Integer parkId) {
		this.employeeId = employeeId;
		this.employeeNumber = employeeNumber;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.username = username;
		this.role = role;
		this.parkId = parkId;
	}
	/**
	 * getter for employee's id
	 * @return the employee's id
	 */
	public int getEmployeeId() {
		return employeeId;
	}
	/**
	 * getter for employee's number
	 * @return the employee's number
	 */
	public int getEmployeeNumber() {
		return employeeNumber;
	}
	/**
	 * getter for employee's first name
	 * @return the employee's first name
	 */
	public String getFirstName() {
		return firstName;
	}
	/**
	 * getter for employee's last name
	 * @return the employee's last name
	 */
	public String getLastName() {
		return lastName;
	}
	/**
	 * getter for employee's email
	 * @return the employee's email
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * getter for employee's username
	 * @return the employee's username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * getter for employee's role
	 * @return the employee's role
	 */
	public String getRole() {
		return role;
	}
	/**
	 * getter for employee's park id
	 * @return the employee's park id
	 */
	public Integer getParkId() {
		return parkId;
	}
	/**
	 * standard toString method
	 */
	@Override
	public String toString() {
		return "Employee{" +
				"employeeId=" + employeeId +
				", employeeNumber=" + employeeNumber +
				", firstName='" + firstName + '\'' +
				", lastName='" + lastName + '\'' +
				", email='" + email + '\'' +
				", username='" + username + '\'' +
				", role='" + role + '\'' +
				", parkId=" + parkId +
				'}';
	}
}