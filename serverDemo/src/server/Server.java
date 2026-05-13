package server;

import java.io.IOException;

import common.Message;
import common.Protocol;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import serverCommon.ServerAndControllerConnection;
import serverCommon.User;

// this class represents the networking side of the server
public class Server extends AbstractServer {
	ServerAndControllerConnection serverController;

	// Constructors ****************************************************

	/**
	 * Constructs an instance of the server.
	 *
	 * @param port             The port number to connect on.
	 * @param serverController the logic of the server and the connector to UI
	 */
	public Server(int port, ServerAndControllerConnection serverController) {
		super(port);
		this.serverController = serverController;
	}

	// Instance methods ************************************************

	/**
	 * This method handles any messages received from the client.
	 *
	 * @param msg    The message received from the client.
	 * @param client The connection from which the message originated.
	 */
	public void handleMessageFromClient(Object msg, ConnectionToClient client) {
		System.out.println("Message received: " + msg + " from " + client);
		if(msg == null)
			return;
		// makes a user instance for a client
		// if the id bound to the client is a duplicate it disconnects the client
		// otherwise binds a User instance to the client
		Message m = (Message) msg;
		if (m.getType() == Protocol.RETURN_ORDER) {
			User u = makeUserFromConnectionToClient(client);
			u.setUserId((String) m.getData());
			if (!serverController.addUserOnUserConnected(u)) {
				try {
					client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else
				client.setInfo("User", u);
		}

		// handling client requests
		try {
			Message returnMessage = serverController.handleRequest(m);
			if (returnMessage != null)
				client.sendToClient(returnMessage);
			else
				System.out.println("Error: request handling failure");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * This method handles client disconnects
	 *
	 * @param client The client disconnecting.
	 */
	@Override
	protected void clientDisconnected(ConnectionToClient client) {
		User u = (User) client.getInfo("User");
		serverController.removeUserOnUserDisconnected(u);
	}

	/**
	 * This method overrides the one in the superclass. Called when the server
	 * starts listening for connections.
	 */
	protected void serverStarted() {
		System.out.println("Server listening for connections on port " + getPort());
	}

	/**
	 * This method overrides the one in the superclass. Called when the server stops
	 * listening for connections. it calls the controller to close everything
	 * running on the server
	 */
	protected void serverStopped() {
		serverController.closeServer();
		System.out.println("Server has stopped listening for connections.");
	}

	/**
	 * This method handles client exceptions here we use it to handle disconnects
	 * that happened ungracefully
	 *
	 * @param client    The client disconnecting.
	 * @param exception the exception thrown
	 */
	@Override
	protected void clientException(ConnectionToClient client, Throwable exception) {
		if (exception instanceof java.io.EOFException) {
			User u = (User) client.getInfo("User");
			serverController.removeUserOnUserDisconnected(u);
			return;
		}
		exception.printStackTrace();
	}

	/**
	 * This method makes a User instance for a given client
	 *
	 * @param client The client whose data we save into a User instance.
	 * @return returns said User instance
	 */
	public static User makeUserFromConnectionToClient(ConnectionToClient client) {
		return new User(client.getInetAddress().getHostName(), client.getInetAddress().getHostAddress(),
				client.isAlive());
	}
}
