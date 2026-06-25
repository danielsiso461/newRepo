
package serverCommon;

import java.util.Objects;

/**
 * Represents a connected user on the server side.
 * 
 * This class stores client information that is relevant to the server, such as
 * host name, IP address, user ID, connection status, and the user's number in
 * the connected users list.
 */
public class User {
	private Integer userNumber;
	private String hostName, userIp, userId = "";
	private Boolean status;

	/**
	 * Creates a new User object with basic client connection details.
	 * 
	 * @param hostName the client's host name
	 * @param ipAddress the client's IP address
	 * @param status the client's connection status
	 */
	public User(String hostName, String ipAddress, Boolean status) {
		this.hostName = hostName;
		this.userIp = ipAddress;
		this.status = status;
	}

	/**
	 * Returns the user's number in the server connection list.
	 * 
	 * @return the user number
	 */
	public Integer getUserNumber() {
		return userNumber;
	}

	/**
	 * Sets the user's number in the server connection list.
	 * 
	 * This number is assigned only to users that were accepted by the server.
	 * 
	 * @param userNumber the user number assigned by the server
	 */
	public void setUserNumber(Integer userNumber) {
		this.userNumber = userNumber;
	}

	/**
	 * Returns the user's connection status as display text.
	 * 
	 * @return "Connected" if the user is connected, otherwise "Disconnected"
	 */
	public String getStatus() {
		return status ? "Connected" : "Disconnected";
	}

	/**
	 * Updates the user's connection status.
	 * 
	 * @param status true if the user is connected, otherwise false
	 */
	public void setStatus(Boolean status) {
		this.status = status;
	}

	/**
	 * Returns the client's host name.
	 * 
	 * @return the client host name
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * Returns the client's IP address.
	 * 
	 * @return the client IP address
	 */
	public String getUserIp() {
		return userIp;
	}

	/**
	 * Returns the logical user ID associated with this connection.
	 * 
	 * @return the user ID
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Sets the logical user ID associated with this connection.
	 * 
	 * This ID is assigned after the server identifies the connected client.
	 * 
	 * @param userId the user ID to assign
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * Generates a hash code based on the user ID.
	 * 
	 * This allows User objects to be stored and compared correctly in collections
	 * such as Set.
	 * 
	 * @return the hash code of this user
	 */
	@Override
	public int hashCode() {
		return Objects.hash(userId);
	}

	/**
	 * Compares this user with another object by user ID.
	 * 
	 * Users are considered equal when they have the same user ID.
	 * 
	 * @param o the object to compare with this user
	 * @return true if both objects represent the same user ID, otherwise false
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof User))
			return false;
		User u = (User) o;
		if (Objects.equals(userId, u.getUserId()))
			return true;
		return false;
	}

	/**
	 * Returns a string representation of this user.
	 * 
	 * @return a string containing the user's main connection details
	 */
	@Override
	public String toString() {
		return "User [userNumber=" + userNumber + ", hostName=" + hostName + ", userIp=" + userIp + ", userId=" + userId
				+ ", status=" + status + "]";
	}
}
