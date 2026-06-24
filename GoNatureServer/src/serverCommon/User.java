package serverCommon;

import java.util.Objects;

/** this class represents a user - holds the client data relevant to the server*/
public class User {
	/**
	 * the user's number in the connection table
	 */
	private Integer userNumber;
	/**
	 * the user's hostname
	 */
	private String hostName; 
	/**
	 * the user's ip
	 */
	private String userIp;
	/**
	 * the user's id
	 */
	private String userId = "";
	
	/**
	 * the user's status
	 */
	private Boolean status;

	/**
	 * constructor
	 * 
	 * @param hostName holds the client hostName
	 * 
	 * @param ipAddress holds the client ip address
	 * 
	 * @param status holds the client status (connected / disconnected)
	 */
	public User(String hostName, String ipAddress, Boolean status) {
		this.hostName = hostName;
		this.userIp = ipAddress;
		this.status = status;
	}
	/**
	 * getter of the user's number in the connection table
	 * @return the user's number in the connection table
	 */
	public Integer getUserNumber() {
		return userNumber;
	}

	/**
	 * this method is used to set the userNumber it is relevant since it is called
	 * only for users accepted by the server
	 * 
	 * @param userNumber the user number on the server
	 */
	public void setUserNumber(Integer userNumber) {
		this.userNumber = userNumber;
	}
	/**
	 * getter of the user's status
	 * @return the user's status
	 */
	public String getStatus() {
		return status ? "Connected" : "Disconnected";
	}

	/**
	 * this method is used to change the client status
	 * 
	 * @param status true if connected, false otherwise
	 */
	public void setStatus(Boolean status) {
		this.status = status;
	}
	/**
	 * getter of the user's hostname
	 * @return the user's hostname
	 */
	public String getHostName() {
		return hostName;
	}
	/**
	 * getter of the user's ip
	 * @return the user ip
	 */
	public String getUserIp() {
		return userIp;
	}
	/**
	 * getter of the user's id
	 * @return the user id
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * this method is used to set the user ID, triggers once the client asks for
	 * orders
	 * 
	 * @param userId the user ID
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * this method is used for the set on the controller for the server
	 */
	@Override
	public int hashCode() {
		return Objects.hash(userId);
	}

	/**
	 * this method is used for the set on the controller for the server
	 * @param o the object to compare to
	 * @return true if equal false otherwise
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
	 * standard toString method
	 */
	@Override
	public String toString() {
		return "User [userNumber=" + userNumber + ", hostName=" + hostName + ", userIp=" + userIp + ", userId=" + userId
				+ ", status=" + status + "]";
	}
}
