package common;

import java.io.Serializable;

/**
 * This class represents an existing customer login request sent from the client
 * to the server.
 */
public class ExistingCustomerLoginRequest implements Serializable {
	/**
	 * serial version UID for serialization
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * the given username in the login screen
	 */
	private String username;
	/**
	 * the given password in the login screen
	 */
	private String password;
	/**
	 * constructor that creates a login request for an existing customer
	 * @param username the given username
	 * @param password the given password
	 */
	public ExistingCustomerLoginRequest(String username, String password) {
		this.username = username;
		this.password = password;
	}
	/**
	 * getter that returns the given username
	 * @return the given username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * getter that returns the given password
	 * @return the given password
	 */
	public String getPassword() {
		return password;
	}
}