package serverCommon;

import java.util.Objects;

// this class represents a user - holds the client data relevant to the server
public class User {
	private Integer userNumber;
	private String hostName, userIp, userId = "";
	private Boolean status;

	/*
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

	public Integer getUserNumber() {
		return userNumber;
	}

	/*
	 * this method is used to set the userNumber it is relevant since it is called
	 * only for users accepted by the server
	 * 
	 * @param userNumber the user number on the server
	 */
	public void setUserNumber(Integer userNumber) {
		this.userNumber = userNumber;
	}

	public String getStatus() {
		return status ? "Connected" : "Disconnected";
	}

	/*
	 * this method is used to change the client status
	 * 
	 * @param status true if connected, false otherwise
	 */
	public void setStatus(Boolean status) {
		this.status = status;
	}

	public String getHostName() {
		return hostName;
	}

	public String getUserIp() {
		return userIp;
	}

	public String getUserId() {
		return userId;
	}

	/*
	 * this method is used to set the user ID, triggers once the client asks for
	 * orders
	 * 
	 * @param userId the user ID
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/*
	 * this method is used for the set on the controller for the server
	 */
	@Override
	public int hashCode() {
		return Objects.hash(userId);
	}

	/*
	 * this method is used for the set on the controller for the server
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

	@Override
	public String toString() {
		return "User [userNumber=" + userNumber + ", hostName=" + hostName + ", userIp=" + userIp + ", userId=" + userId
				+ ", status=" + status + "]";
	}
}
