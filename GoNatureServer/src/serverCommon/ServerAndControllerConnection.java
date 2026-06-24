package serverCommon;

import common.Message;

/**
 * This interface implements the abstract methods used by the server to relay
 * information to the UI and back.
 */
public interface ServerAndControllerConnection {
	/**
	 * Method that when overriden is used to add user to Set when they connect to
	 * server
	 * @param u the user to add to the connection table
	 * @return true if user is allowed to be added, false otherwise
	 */
	public abstract boolean addUserOnUserConnected(User u);

	/**
	 * Method that when overriden is used to remove user from Set when they
	 * disconnect from server
	 * @param u the user to remove
	 */
	public abstract void removeUserOnUserDisconnected(User u);

	/**
	 * Method that when overriden is used to handle user requests.
	 * @param m the message to handle
	 * @return the response as a Message Object
	 */
	public abstract Message handleRequest(Message m);

	/**
	 * this method presents the connection details of the server
	 * 
	 * @param hostName 	the server's hostName
	 * @param ip		the server's ip
	 */
	public abstract void presentServerConnection(String hostName, String ip);
	
	/**
	 * Method that when overriden is used to close the server safely.
	 */
	public abstract void closeServer();
}
