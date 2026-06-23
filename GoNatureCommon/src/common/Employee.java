package common;

import java.io.Serializable;

/*
 * This class represents an employee in the GoNature system.
 * 
 * The class is Serializable because Employee objects are sent between
 * the server and the client.
 */
public class Employee implements Serializable {

	private static final long serialVersionUID = 1L;

	private int employeeId;
	private int employeeNumber;
	private String firstName;
	private String lastName;
	private String email;
	private String username;
	private String role;
	private Integer parkId;

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

	public int getEmployeeId() {
		return employeeId;
	}

	public int getEmployeeNumber() {
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

	public String getRole() {
		return role;
	}

	public Integer getParkId() {
		return parkId;
	}

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