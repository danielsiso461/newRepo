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

	private Server(int port, ServerAndControllerConnection serverController) {
		super(port);
		this.serverController = serverController;
	}

	public static Server getInstance(int port,
			ServerAndControllerConnection serverController) {

		if (instance == null) {
			instance = new Server(port, serverController);
		}

		return instance;
	}

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

		return true;
	}

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

		System.out.println("Bound user ID " + id + " to client connection.");

		return true;
	}

	private boolean bindClientAfterSuccessfulLogin(Message requestMessage,
			Message responseMessage, ConnectionToClient client) {

		if (requestMessage == null || responseMessage == null) {
			return true;
		}

		if (!(responseMessage.getData() instanceof OperationResponse)) {
			return true;
		}

		OperationResponse response = (OperationResponse) responseMessage.getData();

		if (!response.isSuccess() || response.getData() == null) {
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

		return true;
	}

	@Override
	protected void clientDisconnected(ConnectionToClient client) {
		processClientDisconnection(client);
	}

	@Override
	protected void clientException(ConnectionToClient client, Throwable exception) {
		processClientDisconnection(client);
	}

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

	@Override
	protected void serverStopped() {
		System.out.println("Server has stopped listening for connections.");
	}

	public static User makeUserFromConnectionToClient(ConnectionToClient client) {
		return new User(
				client.getInetAddress().getHostName(),
				client.getInetAddress().getHostAddress(),
				client.isAlive()
		);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}