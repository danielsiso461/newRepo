package server;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import common.Message;
import common.Protocol;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import serverCommon.ServerAndControllerConnection;
import serverCommon.User;

/**
 * This class represents the networking side of the server.
 * 
 * The class is implemented as a Singleton, so only one server instance can exist
 * during runtime.
 */
public final class Server extends AbstractServer {

	private static Server instance = null;

	private ServerAndControllerConnection serverController;
	private Map<String, ConnectionToClient> currIdConnection = new HashMap<>();

	/**
	 * Constructs an instance of the server.
	 * 
	 * The constructor is private because this class is implemented as a Singleton.
	 *
	 * @param port             The port number to connect on.
	 * @param serverController the logic of the server and the connector to UI
	 */
	private Server(int port, ServerAndControllerConnection serverController) {
		super(port);
		this.serverController = serverController;
	}

	/**
	 * Returns the single instance of the server.
	 * 
	 * If the instance does not exist yet, it creates it.
	 *
	 * @param port             The port number to connect on.
	 * @param serverController the logic of the server and the connector to UI
	 * @return the single Server instance
	 */
	public static Server getInstance(int port, ServerAndControllerConnection serverController) {
		if (instance == null) {
			instance = new Server(port, serverController);
		}

		return instance;
	}

	/**
	 * This method handles any messages received from the client.
	 *
	 * @param msg    The message received from the client.
	 * @param client The connection from which the message originated.
	 */
	@Override
	public void handleMessageFromClient(Object msg, ConnectionToClient client) {
		System.out.println("Message received: " + msg + " from " + client);

		if (msg == null) {
			return;
		}
		if(!(msg instanceof Message))
			return;
		
		Message m = (Message) msg;

		// makes a user instance for a client
		// if the id bound to the client is a duplicate it disconnects the client
		// otherwise binds a User instance to the client
		if (m.getType() == Protocol.RETURN_ORDER) {
			User u = makeUserFromConnectionToClient(client);
			u.setUserId((String) m.getData());
			
			if (!serverController.addUserOnUserConnected(u)) {
				try {
					client.sendToClient(new Message(null, Protocol.CLIENT_DISCONNECT_SERVER));
					client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			} else {
				client.setInfo("User", u);
				if(currIdConnection.containsKey(u.getUserId()) == false)
					currIdConnection.put(u.getUserId(), client);
			}
		}

		// check if the user issued a disconnect
		if (m.getType() == Protocol.CLIENT_DISCONNECT_USER) {
			User u = (User) client.getInfo("User");
			serverController.removeUserOnUserDisconnected(u);
			currIdConnection.remove(u.getUserId());
			return;
		}

		// handling client requests
		try {
			Message returnMessage = serverController.handleRequest(m);

			if (returnMessage != null) {
				if(returnMessage.getType() == Protocol.UPDATE_ORDER_SUCCESS || 
					returnMessage.getType() == Protocol.UPDATE_ORDER_FAILURE) {
					String messageId = ((User) client.getInfo("User")).getUserId();
					ConnectionToClient c = currIdConnection.get(messageId);
					c.sendToClient(returnMessage);
					return;
				}
				client.sendToClient(returnMessage);
			} else {
				System.out.println("Error: request handling failure");
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * This method overrides the one in the superclass. Called when the server
	 * starts listening for connections.
	 */
	@Override
	protected void serverStarted() {
		System.out.println("Server listening for connections on port " + getPort());

		try {
			serverController.presentServerConnection(
					InetAddress.getLocalHost().getHostName(),
					InetAddress.getLocalHost().getHostAddress());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method overrides the one in the superclass. Called when the server stops
	 * listening for connections.
	 */
	@Override
	protected void serverStopped() {
		System.out.println("Server has stopped listening for connections.");
	}

	/**
	 * This method makes a User instance for a given client.
	 *
	 * @param client The client whose data we save into a User instance.
	 * @return returns said User instance
	 */
	public static User makeUserFromConnectionToClient(ConnectionToClient client) {
		return new User(client.getInetAddress().getHostName(), client.getInetAddress().getHostAddress(),
				client.isAlive());
	}

	/**
	 * Prevents cloning of the Singleton instance.
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}
