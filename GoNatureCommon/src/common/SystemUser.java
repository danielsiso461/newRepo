package common;

import java.io.Serializable;

/**
 * This class represents a system user after identification or login.
 * 
 * The class is shared between the client and the server and can represent
 * different types of users, such as employees and subscribers.
 */
public class SystemUser implements Serializable {

	/**
	 * Serial version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The user ID.
	 * 
	 * For employees, this is employee_id.
	 * For subscribers, this is subscriber_id.
	 */
	private int userId;

	/**
	 * The user's full name.
	 */
	private String fullName;

	/**
	 * The user's email.
	 */
	private String email;

	/**
	 * The username used for login.
	 * 
	 * For subscribers, this can be null because subscribers identify by subscriber
	 * ID and not by username/password.
	 */
	private String username;

	/**
	 * The user type, such as employee or subscriber.
	 */
	private String userType;

	/**
	 * The role of the user.
	 * 
	 * For employees, this is employee_role.
	 * For subscribers, this can be subscriber.
	 */
	private String role;

	/**
	 * The park ID connected to the user.
	 * 
	 * This is relevant mainly for park employees and park managers.
	 */
	private Integer parkId;

	/**
	 * Creates a new SystemUser object.
	 * 
	 * @param userId   the user ID
	 * @param fullName the user's full name
	 * @param email    the user's email
	 * @param username the username, or null if not relevant
	 * @param userType the user type
	 * @param role     the user role
	 * @param parkId   the park ID, or null if not relevant
	 */
	public SystemUser(int userId, String fullName, String email, String username, String userType, String role,
			Integer parkId) {

		this.userId = userId;
		this.fullName = fullName;
		this.email = email;
		this.username = username;
		this.userType = userType;
		this.role = role;
		this.parkId = parkId;
	}

	public int getUserId() {
		return userId;
	}

	public String getFullName() {
		return fullName;
	}

	public String getEmail() {
		return email;
	}

	public String getUsername() {
		return username;
	}

	public String getUserType() {
		return userType;
	}

	public String getRole() {
		return role;
	}

	public Integer getParkId() {
		return parkId;
	}

	public boolean isEmployee() {
		return "employee".equals(userType);
	}

	public boolean isSubscriber() {
		return "subscriber".equals(userType);
	}

	@Override
	public String toString() {
		return fullName + " (" + userType + ")";
	}
}