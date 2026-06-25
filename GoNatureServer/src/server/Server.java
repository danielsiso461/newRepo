
package server;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import common.CancelOrderMessage;
import common.Employee;
import common.Message;
import common.OperationResponse;
import common.Order;
import common.Protocol;
import common.Subscriber;
import common.UpdateMessage;
import common.WaitingListMessage;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import serverCommon.ServerAndControllerConnection;
import serverCommon.User;

/**
 * Represents the networking side of the server.
 * 
 * This class receives messages from clients, forwards requests to the server
 * controller, sends responses back to clients, and manages connected users.
 * The class is implemented as a singleton so only one server instance can exist
 * during runtime.
 */
public final class Server extends AbstractServer {

	/**
	 * The single instance of Server.
	 */
	private static Server instance = null;

	/**
	 * The controller connection used to handle server requests and user state.
	 */
	private ServerAndControllerConnection serverController;

	/**
	 * Maps connected user IDs to their client connections.
	 */
	private Map<String, ConnectionToClient> currIdConnection = new HashMap<>();

	/**
	 * Creates a new Server instance.
	 * 
	 * The constructor is private because this class is implemented as a singleton.
	 * 
	 * @param port the port on which the server listens
	 * @param serverController the controller used for handling client requests
	 */
	private Server(int port, ServerAndControllerConnection serverController) {
		super(port);
		this.serverController = serverController;
	}

	/**
	 * Returns the single instance of Server.
	 * 
	 * If no instance exists, a new server is created with the given port and
	 * controller.
	 * 
	 * @param port the port on which the server should listen
	 * @param serverController the controller used for handling client requests
	 * @return the singleton Server instance
	 */
	public static Server getInstance(int port,
			ServerAndControllerConnection serverController) {

		if (instance == null) {
			instance = new Server(port, serverController);
		}

		return instance;
	}

	/**
	 * Handles a message received from a client.
	 * 
	 * The method validates the message, handles logout or disconnection requests,
	 * registers the client if needed, sends the request to the server controller,
	 * and returns the controller response to the relevant client connection.
	 * 
	 * @param msg the message received from the client
	 * @param client the client connection that sent the message
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

		Message message = (Message) msg;

		if (message.getType() == Protocol.CLIENT_LOGOUT_USER) {
			processClientLogout(client);
			return;
		}

		if (message.getType() == Protocol.CLIENT_DISCONNECT_USER) {
			processClientDisconnection(client);
			return;
		}

		if (!registerClientIfNeeded(message, client)) {
			return;
		}

		try {
			Message returnMessage = serverController.handleRequest(message);

			if (returnMessage == null) {
				System.out.println("Error: request handling failure");
				return;
			}

			if (!bindClientAfterSuccessfulLogin(message, returnMessage, client)) {
				return;
			}

			if (returnMessage.getType() == Protocol.UPDATE_ORDER_SUCCESS
					|| returnMessage.getType() == Protocol.UPDATE_ORDER_FAILURE) {

				User user = (User) client.getInfo("User");

				if (user != null) {
					String messageId = user.getUserId();
					ConnectionToClient connection = currIdConnection.get(messageId);

					if (connection != null) {
						connection.sendToClient(returnMessage);
						return;
					}
				}
			}

			client.sendToClient(returnMessage);

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Registers a client connection with a user ID when possible.
	 * 
	 * The method extracts a user ID from the received message and associates it with
	 * the client connection if the client is not already registered.
	 * 
	 * @param message the message received from the client
	 * @param client the client connection to register
	 * @return true if the client can continue processing, otherwise false
	 */
	private boolean registerClientIfNeeded(Message message, ConnectionToClient client) {
		if (client == null || client.getInfo("User") != null) {
			return true;
		}

		String userId = extractUserIdFromMessage(message);

		if (userId == null || userId.isBlank()) {
			return true;
		}

		User user = makeUserFromConnectionToClient(client);
		user.setUserId(userId);

		if (!serverController.addUserOnUserConnected(user)) {
			try {
				client.sendToClient(new Message(null, Protocol.CLIENT_DISCONNECT_SERVER));
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return false;
		}

		client.setInfo("User", user);

		if (!currIdConnection.containsKey(user.getUserId())) {
			currIdConnection.put(user.getUserId(), client);
		}

		checkForReminderOnLogin(user.getUserId());

		return true;
	}

	/**
	 * Extracts a user ID from a client message according to the message type.
	 * 
	 * Different request types store the user ID in different data objects, so this
	 * method centralizes the extraction logic.
	 * 
	 * @param message the message from which to extract the user ID
	 * @return the extracted user ID, or null if the message does not contain one
	 */
	private String extractUserIdFromMessage(Message message) {
		if (message == null || message.getData() == null) {
			return null;
		}

		switch (message.getType()) {

		case RETURN_ORDER:
		case OCCASIONAL_CUSTOMER_ACCESS_REQUEST:
		case GET_WAITING_OFFERS_REQUEST:
			return String.valueOf(message.getData());

		case JOIN_WAITING_LIST_REQUEST:
			if (message.getData() instanceof WaitingListMessage) {
				WaitingListMessage waitingListMessage =
						(WaitingListMessage) message.getData();

				return String.valueOf(waitingListMessage.getSubscriberId());
			}
			break;

		case MAKE_ORDER:
			if (message.getData() instanceof Order) {
				Order order = (Order) message.getData();

				return String.valueOf(order.getUserId());
			}
			break;

		case UPDATE_ORDER:
			if (message.getData() instanceof UpdateMessage) {
				UpdateMessage updateMessage = (UpdateMessage) message.getData();

				return updateMessage.getOrdererId();
			}
			break;

		case CANCEL_ORDER:
			if (message.getData() instanceof CancelOrderMessage) {
				CancelOrderMessage cancelOrderMessage =
						(CancelOrderMessage) message.getData();

				return cancelOrderMessage.getOrdererId();
			}
			break;

		default:
			break;
		}

		return null;
	}

	/**
	 * Binds a user ID to a client connection.
	 * 
	 * The method creates a User object from the client connection, registers it in
	 * the server controller, and stores the connection in the active connections
	 * map.
	 * 
	 * @param id the user ID to bind
	 * @param client the client connection to bind
	 * @return true if the binding was successful or not needed, otherwise false
	 */
	private boolean bindIdToClientConnection(String id, ConnectionToClient client) {
		if (id == null || id.trim().isEmpty() || client == null) {
			return true;
		}

		if (client.getInfo("User") != null) {
			return true;
		}

		User user = makeUserFromConnectionToClient(client);
		user.setUserId(id);

		if (!serverController.addUserOnUserConnected(user)) {
			System.out.println("User ID is already connected: " + id);

			try {
				client.sendToClient(new Message(null, Protocol.CLIENT_DISCONNECT_SERVER));
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return false;
		}

		client.setInfo("User", user);

		if (!currIdConnection.containsKey(id)) {
			currIdConnection.put(id, client);
		}

		checkForReminderOnLogin(user.getUserId());

		System.out.println("Bound user ID " + id + " to client connection.");

		return true;
	}

	/**
	 * Binds a client connection after a successful login or access request.
	 * 
	 * The method checks the controller response and, if the login was successful,
	 * extracts the user ID from the returned subscriber, employee, or occasional
	 * customer request.
	 * 
	 * @param requestMessage the original request message
	 * @param responseMessage the response message returned by the controller
	 * @param client the client connection to bind
	 * @return true if the client can continue, otherwise false
	 */
	private boolean bindClientAfterSuccessfulLogin(Message requestMessage,
			Message responseMessage, ConnectionToClient client) {

		if (requestMessage == null || responseMessage == null) {
			return true;
		}

		if (!(responseMessage.getData() instanceof OperationResponse)) {
			return true;
		}

		OperationResponse response = (OperationResponse) responseMessage.getData();

		if (!response.isSuccess()) {
			return true;
		}

		if (requestMessage.getType() == Protocol.EXISTING_CUSTOMER_LOGIN_REQUEST
				&& response.getData() instanceof Subscriber) {

			Subscriber subscriber = (Subscriber) response.getData();

			return bindIdToClientConnection(
					String.valueOf(subscriber.getSubscriberId()),
					client
			);
		}

		if (requestMessage.getType() == Protocol.EMPLOYEE_LOGIN_REQUEST
				&& response.getData() instanceof Employee) {

			Employee employee = (Employee) response.getData();

			return bindIdToClientConnection(
					String.valueOf(employee.getEmployeeId()),
					client
			);
		}

		if (requestMessage.getType() == Protocol.OCCASIONAL_CUSTOMER_ACCESS_REQUEST
				&& requestMessage.getData() != null) {

			return bindIdToClientConnection(
					String.valueOf(requestMessage.getData()),
					client
			);
		}

		return true;
	}

	/**
	 * Processes a client logout request.
	 * 
	 * The method removes the user from the active users list, clears the user data
	 * from the client connection, and sends a logout success response.
	 * 
	 * @param client the client connection that requested logout
	 */
	private void processClientLogout(ConnectionToClient client) {
		if (client == null) {
			return;
		}

		User user = (User) client.getInfo("User");

		if (user != null) {
			if (user.getUserId() != null) {
				currIdConnection.remove(user.getUserId());
			}

			serverController.removeUserOnUserDisconnected(user);
			client.setInfo("User", null);
		}

		try {
			client.sendToClient(new Message(null, Protocol.CLIENT_LOGOUT_USER_SUCCESS));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles a client disconnection event.
	 * 
	 * @param client the disconnected client connection
	 */
	@Override
	protected void clientDisconnected(ConnectionToClient client) {
		processClientDisconnection(client);
	}

	/**
	 * Handles an exception that occurred in a client connection.
	 * 
	 * @param client the client connection where the exception occurred
	 * @param exception the exception that occurred
	 */
	@Override
	protected void clientException(ConnectionToClient client, Throwable exception) {
		processClientDisconnection(client);
	}

	/**
	 * Processes a client disconnection.
	 * 
	 * The method makes sure the disconnection is handled only once, removes the user
	 * from the active users list, removes the connection from the ID map, and closes
	 * the client connection.
	 * 
	 * @param client the client connection to disconnect
	 */
	private void processClientDisconnection(ConnectionToClient client) {
		if (client == null) {
			return;
		}

		if (client.getInfo("Disconnected") != null) {
			return;
		}

		client.setInfo("Disconnected", true);

		User user = (User) client.getInfo("User");

		if (user != null) {
			serverController.removeUserOnUserDisconnected(user);
		}

		if (user != null && user.getUserId() != null) {
			currIdConnection.remove(user.getUserId());
		}

		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Called when the server starts listening for client connections.
	 * 
	 * The method prints the server status and updates the server controller with the
	 * local host name and IP address.
	 */
	@Override
	protected void serverStarted() {
		System.out.println("Server listening for connections on port " + getPort());

		try {
			serverController.presentServerConnection(
					InetAddress.getLocalHost().getHostName(),
					InetAddress.getLocalHost().getHostAddress()
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Called when the server stops listening for client connections.
	 */
	@Override
	protected void serverStopped() {
		System.out.println("Server has stopped listening for connections.");
	}

	/**
	 * Creates a User object from a client connection.
	 * 
	 * The created user contains the client's host name, IP address, and connection
	 * status.
	 * 
	 * @param client the client connection used to create the user
	 * @return a User object that represents the connected client
	 */
	public static User makeUserFromConnectionToClient(ConnectionToClient client) {
		return new User(
				client.getInetAddress().getHostName(),
				client.getInetAddress().getHostAddress(),
				client.isAlive()
		);
	}

	/**
	 * Sends a reminder message to a connected user.
	 * 
	 * @param id the user's ID
	 * @param message the reminder message to send
	 * @return 1 if the reminder was sent successfully, or -1 on failure
	 */
	public int sendReminderToUser(String id, Message message) {
		ConnectionToClient connection = currIdConnection.get(id);

		if (connection == null || message == null) {
			return -1;
		}

		try {
			connection.sendToClient(message);
			return 1;
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return -1;
		}
	}

	/**
	 * Checks whether a user is currently connected to the server.
	 * 
	 * @param id the user's ID
	 * @return true if the user is connected, otherwise false
	 */
	public boolean isUserConnected(String id) {
		return currIdConnection.containsKey(id);
	}

	/**
	 * Checks whether a user has pending reminders after login.
	 * 
	 * @param id the user's ID
	 */
	private void checkForReminderOnLogin(String id) {
		if (id != null && !id.isBlank()) {
			serverController.checkForUserReminder(id);
		}
	}

	/**
	 * Prevents cloning of the singleton instance.
	 * 
	 * @return never returns, because cloning is not supported
	 * @throws CloneNotSupportedException always thrown to prevent cloning
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}

