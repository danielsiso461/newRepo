package common;

import java.io.Serializable;

/*
 * This class represents an employee login request sent from the client to the server.
 */
public class EmployeeLoginRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private String username;
	private String password;

	public EmployeeLoginRequest(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}
