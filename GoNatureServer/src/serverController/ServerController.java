package serverController;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import common.*;

import databaseControllers.OrderConnection;
import server.Server;
import serverCommon.ServerAndControllerConnection;
import serverCommon.User;
import serverGUI.ClientConnectionTableController;

// this class is the controller that connects 
// the networking part of the server and the UI part of it. 
// It is also the logic behind it
public class ServerController implements ServerAndControllerConnection {

	private Server server;
	private Set<User> users = new HashSet<>();
	private OrderConnection oc;
	private int allTimeUserCount = 1;
	private ClientConnectionTableController serverGUIController;

	public ServerController(ClientConnectionTableController serverGUIController) {
		this.serverGUIController = serverGUIController;

		addLog("Initializing server controller.");

		server = Server.getInstance(common.CommonConstants.DEFAULT_PORT, this);
		addLog("Server instance created on port " + common.CommonConstants.DEFAULT_PORT + ".");

		oc = OrderConnection.getInstance();
		addLog("Database connection object created.");

		try {
			server.listen(); // Start listening for connections
			addLog("Server started listening for clients.");
		} catch (Exception ex) {
			System.out.println("ERROR - Could not listen for clients!");
			addLog("ERROR - Could not listen for clients: " + ex.getMessage());
		}
	}

	/*
	 * this method adds a message to the server log area in the GUI
	 * 
	 * @param message the message to add to the server log
	 */
	public void addLog(String message) {
		if (serverGUIController != null) {
			serverGUIController.addLog(message);
		}
	}

	/*
	 * this method presents the connection details of the server
	 * 
	 * @param hostName 	the server's hostName
	 * @param ip		the server's ip
	 */
	@Override
	public void presentServerConnection(String hostName, String ip) {
		serverGUIController.setLabels(hostName, ip);
		addLog("Server connection details updated. Host: " + hostName + ", IP: " + ip);
	}

	/*
	 * this method adds a user to the set of users on the server if this user is not
	 * already in the set, it sets the userNumber then it calls the UI handler for
	 * new User on the server
	 * 
	 * @param u an instance of user to add to the server
	 */
	@Override
	public boolean addUserOnUserConnected(User u) {
		addLog("Trying to connect user: " + u);

		boolean userIdNotConnected = users.add(u);

		if (userIdNotConnected) {
			u.setUserNumber(allTimeUserCount++);
			serverGUIController.onUserConnected(u);
			addLog("User connected successfully: " + u);
		} else {
			addLog("User is already connected: " + u);
		}

		return userIdNotConnected;
	}

	/*
	 * this method removes a user from the set of users on the server if this user
	 * has a userNumber it sets their status to false = disconnected then it calls
	 * the UI handler for updating User data
	 * 
	 * @param u an instance of user to remove from the server
	 */
	@Override
	public void removeUserOnUserDisconnected(User u) {
		if (u == null) {
			addLog("Tried to disconnect a null user.");
			return;
		}

		addLog("Disconnecting user: " + u);

		if (u.getUserNumber() != null) {
			u.setStatus(false);
			serverGUIController.onUserDisconnected(u);
			addLog("User status updated to disconnected: " + u);
		}

		users.remove(u);
		addLog("User removed from connected users set.");
	}

	/*
	 * this method prints all connected users to the console
	 * 
	 * @param s the message to print before the users list
	 */
	private void printUsers(String s) {
		System.out.println(s);
		addLog(s);

		for (User u : users) {
			System.out.println(u.toString());
			addLog("Connected user: " + u);
		}
	}

	/*
	 * this method parses the request received by server and handles it accordingly
	 * 
	 * @param m an instance Message from the client
	 */
	@Override
	public Message handleRequest(Message m) {
		if (m == null) {
			addLog("Received null message from client.");
			return null;
		}

		Protocol type = m.getType();
		addLog("Received request from client. Protocol: " + type);

		switch (type) {

		case CLIENT_DISCONNECT_USER:
			addLog("Client requested disconnect.");
			return m;

		case UPDATE_ORDER:
			addLog("Client requested order update.");

			Protocol typeRet = Protocol.UPDATE_ORDER_SUCCESS;
			UpdateMessage um = (UpdateMessage) m.getData();

			try {
				oc.updateOrder(um);
				addLog("Order updated successfully: " + um);
			} catch (SQLException e) {
				typeRet = Protocol.UPDATE_ORDER_FAILURE;
				System.out.println(e.getMessage());
				addLog("ERROR - Failed to update order: " + e.getMessage());
			}

			return new Message(m.getData(), typeRet);

		case RETURN_ORDER:
			addLog("Client requested orders list.");

			List<OrderRow> req = null;

			try {
				req = oc.getUserOrders(m);
				addLog("Orders were loaded from database.");
			} catch (SQLException e) {
				System.out.println(e.getMessage());
				addLog("ERROR - Failed to load orders: " + e.getMessage());
			}

			if (req != null) {
				addLog("Returning " + req.size() + " orders to client.");
				return new Message(req, Protocol.RETURN_ORDER);
			}

			addLog("No orders returned to client.");
			break;

		default:
			System.out.println("Error: client request unknown");
			addLog("ERROR - Unknown client request: " + type);
		}

		return null;
	}

	/*
	 * this method makes sure the connection to the DB is closed properly
	 */
	private void closeDBConnection() {
		try {
			addLog("Closing database connection.");
			oc.close();
			addLog("Database connection closed successfully.");
		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to close database connection: " + e.getMessage());
		}
	}

	/*
	 * this method takes care to close all relevant parts of the server properly
	 */
	@Override
	public void closeServer() {
		try {
			addLog("Closing server.");

			server.sendToAllClients(new Message(null, Protocol.CLIENT_DISCONNECT_SERVER));
			addLog("Disconnect message sent to all clients.");

			server.stopListening();
			addLog("Server stopped listening.");

			server.close();
			addLog("Server closed successfully.");

		} catch (IOException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to close server: " + e.getMessage());
		} finally {
			closeDBConnection();
		}
	}
}