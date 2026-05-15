package server;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import common.Message;
import common.OrderRow;
import common.Protocol;
import common.UpdateMessage;
import databaseControllers.OrderConnection;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

/**
 * This class represents the networking side of the server.
 * 
 * The class is implemented as a Singleton, so only one server instance can exist
 * during runtime.
 */
public final class Server extends AbstractServer {

	/**
	 * The default port to listen on.
	 */
	public static final int DEFAULT_PORT = 5555;

	/**
	 * The single instance of Server.
	 */
	private static Server instance = null;

	private OrderConnection oc;

	/**
	 * Constructs an instance of the server.
	 * 
	 * The constructor is private because this class is implemented as a Singleton.
	 *
	 * @param port The port number to connect on.
	 */
	private Server(int port) {
		super(port);
		oc = OrderConnection.getInstance();
	}

	/**
	 * Returns the single instance of the server.
	 * 
	 * If the instance does not exist yet, it creates it.
	 *
	 * @param port The port number to connect on.
	 * @return the single Server instance
	 */
	public static Server getInstance(int port) {
		if (instance == null) {
			instance = new Server(port);
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

		Message m = (Message) msg;
		handleRequest(m, client);
	}

	private void handleRequest(Message m, ConnectionToClient client) {
		Protocol type = m.getType();

		switch (type) {
		case CLIENT_CONNECT:
			// placeholder
			break;

		case CLIENT_DISCONNECT:
			// placeholder
			break;

		case MAKE_ORDER:
			// not in prototype
			break;

		case UPDATE_ORDER:
			Protocol typeRet = Protocol.UPDATE_ORDER_SUCCESS;
			UpdateMessage um = (UpdateMessage) m.getData();

			try {
				oc.updateOrder(um);
			} catch (SQLException e) {
				typeRet = Protocol.UPDATE_ORDER_FAILURE;
				System.out.println(e.getMessage());
			}

			try {
				client.sendToClient(new Message(m.getData(), typeRet));
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}

			break;

		case RETURN_ORDER:
			List<OrderRow> req = null;

			try {
				req = oc.getUserOrders(m);
			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}

			try {
				if (req != null) {
					client.sendToClient(new Message(req, Protocol.RETURN_ORDER));
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}

			break;

		default:
			System.out.println("Error: client request unknown");
		}
	}

	/**
	 * This method is called when a client connects to the server.
	 * 
	 * @param client The connected client.
	 */
	@Override
	protected void clientConnected(ConnectionToClient client) {
		String hostName = client.getInetAddress().getHostName();
		String ipAddress = client.getInetAddress().getHostAddress();
		boolean connected = client.isAlive();

		System.out.println("===== Connected Client Info =====");
		System.out.println("Host: " + hostName);
		System.out.println("IP: " + ipAddress);
		System.out.println("Status: " + (connected ? "Connected" : "Disconnected"));
		System.out.println("=================================");
	}

	/**
	 * This method closes the DB connection.
	 */
	public void closeDBConnection() {
		try {
			oc.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method overrides the one in the superclass. Called when the server starts
	 * listening for connections.
	 */
	@Override
	protected void serverStarted() {
		System.out.println("Server listening for connections on port " + getPort());
	}

	/**
	 * This method overrides the one in the superclass. Called when the server stops
	 * listening for connections.
	 */
	@Override
	protected void serverStopped() {
		System.out.println("Server has stopped listening for connections.");
		closeDBConnection();
	}

	/**
	 * Prevents cloning of the Singleton instance.
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	/**
	 * This method is responsible for the creation of the server instance.
	 *
	 * @param args command line arguments. args[0] is the port number to listen on.
	 */
	public static void main(String[] args) {
		int port = 0;

		try {
			port = Integer.parseInt(args[0]);
		} catch (Throwable t) {
			port = DEFAULT_PORT;
		}

		Server sv = Server.getInstance(port);

		try {
			sv.listen();
		} catch (Exception ex) {
			System.out.println("ERROR - Could not listen for clients!");
		}
	}
}