
package serverCommon;

import common.Message;

/**
 * Defines the communication contract between the server networking layer and
 * the server controller.
 * 
 * Implementing classes use this interface to update the UI about server state,
 * manage connected users, handle client requests, close the server safely, and
 * check reminders for connected users.
 */
public interface ServerAndControllerConnection {
	/**
	 * Adds a user to the connected users collection when the user connects to the
	 * server.
	 * 
	 * @param u the user that connected to the server
	 * @return true if the user was added successfully, otherwise false
	 */
	public abstract boolean addUserOnUserConnected(User u);

	/**
	 * Removes a user from the connected users collection when the user disconnects
	 * from the server.
	 * 
	 * @param u the user that disconnected from the server
	 */
	public abstract void removeUserOnUserDisconnected(User u);

	/**
	 * Handles a request received from a connected client.
	 * 
	 * @param m the message received from the client
	 * @return the response message that should be sent back to the client
	 */
	public abstract Message handleRequest(Message m);

	/**
	 * Presents the server connection details in the user interface.
	 * 
	 * @param hostName the server host name
	 * @param ip the server IP address
	 */
	public abstract void presentServerConnection(String hostName, String ip);
	
	/**
	 * Closes the server safely.
	 */
	public abstract void closeServer();
	
	/**
	 * Checks whether a specific user has pending reminders.
	 * 
	 * @param id the user's ID
	 */
	void checkForUserReminder(String id);
}
