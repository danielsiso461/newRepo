package server;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import common.*;
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

		if (!(msg instanceof Message)) {
			return;
		}

		Message m = (Message) msg;

		// Check if the user issued a disconnect.
		if (m.getType() == Protocol.CLIENT_DISCONNECT_USER) {
			processClientDisconnection(client);
			return;
		}

		// Register the client in the server table once we can identify him.
		if (!registerClientIfNeeded(m, client)) {
			return;
		}

		// Handling client requests.
		try {
			Message returnMessage = serverController.handleRequest(m);

			if (returnMessage != null) {
				bindClientAfterSuccessfulLogin(m, returnMessage, client);

				if (returnMessage.getType() == Protocol.UPDATE_ORDER_SUCCESS
						|| returnMessage.getType() == Protocol.UPDATE_ORDER_FAILURE) {

					User user = (User) client.getInfo("User");

					if (user != null) {
						String messageId = user.getUserId();
						ConnectionToClient c = currIdConnection.get(messageId);

						if (c != null) {
							c.sendToClient(returnMessage);
							return;
						}
					}
				}

				client.sendToClient(returnMessage);
			} else {
				System.out.println("Error: request handling failure");
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	/*
	 * Registers the client in the server user table once an identifying value is
	 * available in the received message.
	 *
	 * @param m      the message received from the client
	 * @param client the client connection
	 * @return true if the request can continue, false if the client was disconnected
	 */
	private boolean registerClientIfNeeded(Message m, ConnectionToClient client) {
		if (client == null || client.getInfo("User") != null) {
			return true;
		}

		String userId = extractUserIdFromMessage(m);

		if (userId == null || userId.isBlank()) {
			return true;
		}

		User u = makeUserFromConnectionToClient(client);
		u.setUserId(userId);

		if (!serverController.addUserOnUserConnected(u)) {
			try {
				client.sendToClient(new Message(null, Protocol.CLIENT_DISCONNECT_SERVER));
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return false;
		}

		client.setInfo("User", u);

		if (!currIdConnection.containsKey(u.getUserId())) {
			currIdConnection.put(u.getUserId(), client);
		}

		return true;
	}

	/*
	 * Extracts a user identifier from a client message.
	 *
	 * The server table should show the user once the client enters the system,
	 * even if the first request is not RETURN_ORDER.
	 *
	 * Login requests are not bound here. They are bound only after the login
	 * response succeeds, so a failed login will not appear as a connected user.
	 *
	 * @param m the message received from the client
	 * @return the user identifier, or null if the message does not contain one
	 */
	private String extractUserIdFromMessage(Message m) {
		if (m == null || m.getData() == null) {
			return null;
		}

		switch (m.getType()) {
		case RETURN_ORDER:
		case OCCASIONAL_CUSTOMER_ACCESS_REQUEST:
		case GET_WAITING_OFFERS_REQUEST:
			return String.valueOf(m.getData());

		case JOIN_WAITING_LIST_REQUEST:
			if (m.getData() instanceof WaitingListMessage) {
				WaitingListMessage waitingListMessage = (WaitingListMessage) m.getData();
				return String.valueOf(waitingListMessage.getSubscriberId());
			}
			break;

		case MAKE_ORDER:
			if (m.getData() instanceof Order) {
				Order order = (Order) m.getData();
				return String.valueOf(order.getUserId());
			}
			break;

		case UPDATE_ORDER:
			if (m.getData() instanceof UpdateMessage) {
				UpdateMessage updateMessage = (UpdateMessage) m.getData();
				return updateMessage.getOrdererId();
			}
			break;

		case CANCEL_ORDER:
			if (m.getData() instanceof CancelOrderMessage) {
				CancelOrderMessage cancelOrderMessage = (CancelOrderMessage) m.getData();
				return cancelOrderMessage.getOrdererId();
			}
			break;

		default:
			break;
		}

		return null;
	}

	/*
	 * Binds a user id to the current client connection.
	 *
	 * This method is used after successful login responses, so employees and
	 * existing customers appear in the server's connected users table only after
	 * their login was accepted.
	 *
	 * @param id the user id
	 * @param client the client connection
	 */
	private void bindIdToClientConnection(String id, ConnectionToClient client) {
		if (id == null || id.trim().isEmpty() || client == null) {
			return;
		}

		if (client.getInfo("User") != null) {
			return;
		}

		User u = makeUserFromConnectionToClient(client);
		u.setUserId(id);

		if (!serverController.addUserOnUserConnected(u)) {
			System.out.println("User ID is already connected: " + id);

			try {
				client.sendToClient(new Message(null, Protocol.CLIENT_DISCONNECT_SERVER));
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return;
		}

		client.setInfo("User", u);

		if (!currIdConnection.containsKey(id)) {
			currIdConnection.put(id, client);
		}

		System.out.println("Bound user ID " + id + " to client connection.");
	}

	/*
	 * Binds the client connection after a successful login response.
	 *
	 * @param requestMessage the original request message
	 * @param responseMessage the response returned from the server controller
	 * @param client the client connection
	 */
	private void bindClientAfterSuccessfulLogin(Message requestMessage,
			Message responseMessage,
			ConnectionToClient client) {

		if (requestMessage == null || responseMessage == null) {
			return;
		}

		if (!(responseMessage.getData() instanceof OperationResponse)) {
			return;
		}

		OperationResponse response = (OperationResponse) responseMessage.getData();

		if (!response.isSuccess() || response.getData() == null) {
			return;
		}

		if (requestMessage.getType() == Protocol.EXISTING_CUSTOMER_LOGIN_REQUEST
				&& response.getData() instanceof Subscriber) {

			Subscriber subscriber = (Subscriber) response.getData();

			bindIdToClientConnection(
					String.valueOf(subscriber.getSubscriberId()),
					client
			);

			return;
		}

		if (requestMessage.getType() == Protocol.EMPLOYEE_LOGIN_REQUEST
				&& response.getData() instanceof Employee) {

			Employee employee = (Employee) response.getData();

			bindIdToClientConnection(
					String.valueOf(employee.getEmployeeId()),
					client
			);
		}
	}

	/**
	 * This method is called when a client disconnects from the server in an orderly way.
	 * 
	 * For example, this can happen when the client calls closeConnection().
	 * The method delegates the actual disconnection handling to processClientDisconnection
	 * in order to avoid duplicate code.
	 *
	 * @param client the client connection that was disconnected
	 */
	@Override
	protected void clientDisconnected(ConnectionToClient client) {
		processClientDisconnection(client);
	}

	/**
	 * This method is called when an exception occurs in the client connection.
	 * 
	 * This usually happens when the client closes the window, the client process is
	 * terminated, or the connection is lost unexpectedly.
	 * The method delegates the actual disconnection handling to processClientDisconnection
	 * in order to close the connection safely from the server side.
	 *
	 * @param client    the client connection where the exception occurred
	 * @param exception the exception that caused the disconnection
	 */
	@Override
	protected void clientException(ConnectionToClient client, Throwable exception) {
		processClientDisconnection(client);
	}

	/**
	 * Handles client disconnection in one central place.
	 * 
	 * This method is used both for orderly disconnection and for unexpected
	 * disconnection. Since OCSF may sometimes call more than one disconnection
	 * routine for the same client, the method first checks whether this client was
	 * already processed.
	 * 
	 * If the client was not processed yet, the method marks it as disconnected,
	 * removes the related User object from the server controller, and closes the
	 * client connection safely.
	 *
	 * @param client the client connection that should be disconnected
	 */
	private void processClientDisconnection(ConnectionToClient client) {
		if (client == null) {
			return;
		}

		if (client.getInfo("Disconnected") != null) {
			return;
		}

		client.setInfo("Disconnected", true);

		User u = (User) client.getInfo("User");

		if (u != null) {
			serverController.removeUserOnUserDisconnected(u);
		}

		if (u != null && u.getUserId() != null) {
			currIdConnection.remove(u.getUserId());
		}

		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
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
