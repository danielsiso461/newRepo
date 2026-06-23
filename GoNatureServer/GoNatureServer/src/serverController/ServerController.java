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

		server = Server.getInstance(common.CommonConstants.DEFAULT_PORT, this);
		oc = OrderConnection.getInstance();

		try {
			server.listen(); // Start listening for connections
		} catch (Exception ex) {
			System.out.println("ERROR - Could not listen for clients!");
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
		boolean userIdNotConnected = users.add(u);

		if (userIdNotConnected) {
			u.setUserNumber(allTimeUserCount++);
			serverGUIController.onUserConnected(u);
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
			return;
		}

		if (u.getUserNumber() != null) {
			u.setStatus(false);
			serverGUIController.onUserDisconnected(u);
		}

		users.remove(u);
	}

	private void printUsers(String s) {
		System.out.println(s);

		for (User u : users) {
			System.out.println(u.toString());
		}
	}

	/*
	 * this method parses the request received by server and handles it accordingly
	 * 
	 * @param m an instance Message from the client
	 */
	@Override
	public Message handleRequest(Message m) {
		Protocol type = m.getType();

		switch (type) {
		case CLIENT_DISCONNECT_USER:
			return m;

		case UPDATE_ORDER:
			Protocol typeRet = Protocol.UPDATE_ORDER_SUCCESS;
			UpdateMessage um = (UpdateMessage) m.getData();

			try {
				oc.updateOrder(um);
			} catch (SQLException e) {
				typeRet = Protocol.UPDATE_ORDER_FAILURE;
				System.out.println(e.getMessage());
			}

			return new Message(m.getData(), typeRet);

		case RETURN_ORDER:
			List<Order> req = null;

			try {
				req = oc.getUserOrders(m);
			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}

			if (req != null) {
				return new Message(req, Protocol.RETURN_ORDER);
			}

			break;

		default:
			System.out.println("Error: client request unknown");
		}

		return null;
	}

	/*
	 * this method makes sure the connection to the DB is closed properly
	 */
	private void closeDBConnection() {
		try {
			oc.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * this method takes care to close all relevant parts of the server properly
	 */
	@Override
	public void closeServer() {
		try {
			server.sendToAllClients(new Message(null, Protocol.CLIENT_DISCONNECT_SERVER));
			server.stopListening();
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeDBConnection();
		}
	}
}