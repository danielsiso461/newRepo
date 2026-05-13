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
	 */
	public abstract boolean addUserOnUserConnected(User u);

	/**
	 * Method that when overriden is used to remove user from Set when they
	 * disconnect from server
	 */
	public abstract void removeUserOnUserDisconnected(User u);

	/**
	 * Method that when overriden is used to handle user requests.
	 */
	public abstract Message handleRequest(Message m);

	/**
	 * Method that when overriden is used to close the server safely.
	 */
	public abstract void closeServer();
}
