package common;

import java.io.Serializable;

/**
 * This class represents an employee login request sent from the client to the server.
 */
public class EmployeeLoginRequest implements Serializable {
	/**
	 * serial version UID for serialization
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * the given username
	 */
	private String username;
	/**
	 * the given password
	 */
	private String password;
	/**
	 * constructor for the login request
	 * @param username the given username
	 * @param password the given password
	 */
	public EmployeeLoginRequest(String username, String password) {
		this.username = username;
		this.password = password;
	}
	/**
	 * getter for the given username
	 * @return the given username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * getter for the given password
	 * @return the given password
	 */
	public String getPassword() {
		return password;
	}
}
