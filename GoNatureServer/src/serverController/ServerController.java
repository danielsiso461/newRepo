package serverController;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import databaseControllers.DBConnectionPool;
import common.GuideRegistrationRequest;
import common.Message;
import common.OperationResponse;
import common.OrderRow;
import common.Park;
import common.Protocol;
import common.Subscriber;
import common.UpdateMessage;

import databaseControllers.GuideConnection;
import databaseControllers.OrderConnection;
import databaseControllers.ParkConnection;
import databaseControllers.ParkParameterChangeRequestConnection;
import databaseControllers.SubscriberConnection;
import server.Server;
import serverCommon.ServerAndControllerConnection;
import serverCommon.User;
import serverGUI.ClientConnectionTableController;

/**
 * This class connects the networking part of the server and the server GUI.
 * 
 * It is responsible for handling client requests, updating the server GUI,
 * managing connected users, and communicating with the database connection
 * classes.
 */
public class ServerController implements ServerAndControllerConnection {

	private Server server;
	private Set<User> users = new HashSet<>();

	private OrderConnection oc;
	private ParkConnection pc;
	private SubscriberConnection sc;
	private GuideConnection gc;
	private ParkParameterChangeRequestConnection pcrc;

	private int allTimeUserCount = 1;

	private ClientConnectionTableController serverGUIController;

	/**
	 * Creates a ServerController object.
	 * 
	 * The constructor initializes the server, creates the database connection
	 * objects, and starts listening for clients.
	 * 
	 * @param serverGUIController the server GUI controller
	 */
	public ServerController(ClientConnectionTableController serverGUIController) {
		this.serverGUIController = serverGUIController;

		addLog("Initializing server controller.");

		server = Server.getInstance(common.CommonConstants.DEFAULT_PORT, this);
		addLog("Server instance created on port " + common.CommonConstants.DEFAULT_PORT + ".");

		try {
			oc = OrderConnection.getInstance();
			pc = ParkConnection.getInstance();
			pcrc = ParkParameterChangeRequestConnection.getInstance();
			sc = SubscriberConnection.getInstance();
			gc = GuideConnection.getInstance();

			addLog("Order database connection object created.");
			addLog("Park database connection object created.");
			addLog("Park parameter change request database connection object created.");
			addLog("Subscriber database connection object created.");
			addLog("Guide database connection object created.");
			addLog("All database connection objects were created successfully.");

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to create database connection objects: " + e.getMessage());
		}

		try {
			server.listen();
			addLog("Server started listening for clients.");
		} catch (Exception ex) {
			System.out.println("ERROR - Could not listen for clients!");
			addLog("ERROR - Could not listen for clients: " + ex.getMessage());
		}
	}

	/**
	 * Adds a message to the server log area in the GUI.
	 * 
	 * @param message the message to add to the server log
	 */
	public void addLog(String message) {
		if (serverGUIController != null) {
			serverGUIController.addLog(message);
		}
	}

	/**
	 * Presents the server connection details in the GUI.
	 * 
	 * @param hostName the server host name
	 * @param ip       the server IP address
	 */
	@Override
	public void presentServerConnection(String hostName, String ip) {
		if (serverGUIController != null) {
			serverGUIController.setLabels(hostName, ip);
		}

		addLog("Server connection details updated. Host: " + hostName + ", IP: " + ip);
	}

	/**
	 * Adds a connected user to the server connected users set.
	 * 
	 * If the user is not already connected, the method gives the user a server
	 * number and updates the server GUI.
	 * 
	 * @param u the connected user
	 * @return true if the user was added successfully, false otherwise
	 */
	@Override
	public boolean addUserOnUserConnected(User u) {
		if (u == null) {
			addLog("Tried to connect a null user.");
			return false;
		}

		addLog("Trying to connect user: " + u);

		boolean userIdNotConnected = users.add(u);

		if (userIdNotConnected) {
			u.setUserNumber(allTimeUserCount++);

			if (serverGUIController != null) {
				serverGUIController.onUserConnected(u);
			}

			addLog("User connected successfully: " + u);
		} else {
			addLog("User is already connected: " + u);
		}

		return userIdNotConnected;
	}

	/**
	 * Removes a disconnected user from the server connected users set.
	 * 
	 * @param u the disconnected user
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

			if (serverGUIController != null) {
				serverGUIController.onUserDisconnected(u);
			}

			addLog("User status updated to disconnected: " + u);
		}

		users.remove(u);
		addLog("User removed from connected users set.");
	}

	/**
	 * Prints all connected users to the console and to the server log.
	 * 
	 * @param message the message to print before the users list
	 */
	private void printUsers(String message) {
		System.out.println(message);
		addLog(message);

		for (User u : users) {
			System.out.println(u.toString());
			addLog("Connected user: " + u);
		}
	}

	/**
	 * Notifies all connected clients that park data was updated.
	 * 
	 * The method loads the updated active parks from the database and sends them to
	 * all connected clients.
	 */
	public void notifyParksUpdated() {
		try {
			addLog("Loading updated active parks before notifying clients.");

			List<Park> parks = pc.getAllActiveParks();

			addLog("Loaded " + parks.size() + " active parks for update notification.");

			server.sendToAllClients(new Message(parks, Protocol.PARKS_UPDATED));

			addLog("PARKS_UPDATED message was sent to all connected clients.");

		} catch (Exception e) {
			e.printStackTrace();
			addLog("ERROR - Failed to notify clients about parks update: " + e.getMessage());
		}
	}

	/**
	 * Handles a request received from a client.
	 * 
	 * @param m the message received from the client
	 * @return a response message to send back to the client
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
			return handleUpdateOrder(m);

		case RETURN_ORDER:
			return handleReturnOrder(m);

		case GET_ACTIVE_PARKS:
			return handleGetActiveParks();

		case APPROVE_PARK_PARAMETER_CHANGE_REQUEST:
			return handleApproveParkParameterChangeRequest(m);

		case REJECT_PARK_PARAMETER_CHANGE_REQUEST:
			return handleRejectParkParameterChangeRequest(m);

		case SEARCH_SUBSCRIBER_REQUEST:
			return handleSearchSubscriber(m);

		case REGISTER_GUIDE_REQUEST:
			return handleRegisterGuide(m);

		default:
			System.out.println("Error: client request unknown");
			addLog("ERROR - Unknown client request: " + type);
			return null;
		}
	}

	/**
	 * Handles an order update request.
	 * 
	 * @param m the client message
	 * @return update success or failure message
	 */
	private Message handleUpdateOrder(Message m) {
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
	}

	/**
	 * Handles a request for returning user orders.
	 * 
	 * @param m the client message
	 * @return a message containing the user's orders, or null if loading failed
	 */
	private Message handleReturnOrder(Message m) {
		addLog("Client requested orders list.");

		List<OrderRow> orders = null;

		try {
			orders = oc.getUserOrders(m);
			addLog("Orders were loaded from database.");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			addLog("ERROR - Failed to load orders: " + e.getMessage());
		}

		if (orders != null) {
			addLog("Returning " + orders.size() + " orders to client.");
			return new Message(orders, Protocol.RETURN_ORDER);
		}

		addLog("No orders returned to client.");
		return null;
	}

	/**
	 * Handles a request for all active parks.
	 * 
	 * @return a message containing active parks, or a failure message
	 */
	private Message handleGetActiveParks() {
		addLog("Client requested active parks list.");

		try {
			addLog("Loading active parks from database.");

			List<Park> parks = pc.getAllActiveParks();

			addLog("Active parks list loaded from database. Number of parks: " + parks.size());
			addLog("Returning active parks list to client.");

			return new Message(parks, Protocol.ACTIVE_PARKS_RESULT);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to load active parks: " + e.getMessage());

			return new Message(e.getMessage(), Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
		}
	}

	/**
	 * Handles a client request to search for a subscriber by subscriber ID.
	 * 
	 * @param m the message received from the client, containing the subscriber ID
	 * @return a message with the search result
	 */
	private Message handleSearchSubscriber(Message m) {
		try {
			int subscriberId = (int) m.getData();

			Subscriber subscriber = sc.findSubscriberById(subscriberId);

			if (subscriber == null) {
				OperationResponse response = new OperationResponse(false, "Subscriber not found", null);
				return new Message(response, Protocol.SEARCH_SUBSCRIBER_RESPONSE);
			}

			OperationResponse response = new OperationResponse(true, "Subscriber found", subscriber);
			return new Message(response, Protocol.SEARCH_SUBSCRIBER_RESPONSE);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Database error while searching subscriber: " + e.getMessage());

			OperationResponse response = new OperationResponse(false, "Database error while searching subscriber", null);
			return new Message(response, Protocol.SEARCH_SUBSCRIBER_RESPONSE);

		} catch (Exception e) {
			e.printStackTrace();
			addLog("ERROR - Invalid subscriber search request: " + e.getMessage());

			OperationResponse response = new OperationResponse(false, "Invalid subscriber search request", null);
			return new Message(response, Protocol.SEARCH_SUBSCRIBER_RESPONSE);
		}
	}

	/**
	 * Handles a client request to register an existing subscriber as a guide.
	 * 
	 * @param m the message received from the client, containing a guide registration
	 *          request
	 * @return a message with the registration result
	 */
	private Message handleRegisterGuide(Message m) {
		try {
			GuideRegistrationRequest request = (GuideRegistrationRequest) m.getData();

			Subscriber subscriber = sc.findSubscriberById(request.getSubscriberId());

			if (subscriber == null) {
				OperationResponse response = new OperationResponse(false, "Subscriber not found", null);
				return new Message(response, Protocol.REGISTER_GUIDE_RESPONSE);
			}

			int guideId = gc.addGuide(request.getSubscriberId(), request.getAuthorizedByEmployeeId(),
					request.getOrganizationName());

			if (guideId != -1) {
				OperationResponse response = new OperationResponse(true, "Guide registered successfully", guideId);
				return new Message(response, Protocol.REGISTER_GUIDE_RESPONSE);
			}

			OperationResponse response = new OperationResponse(false,
					"Failed to register guide. Subscriber may already be an active guide or employee is not allowed.",
					null);

			return new Message(response, Protocol.REGISTER_GUIDE_RESPONSE);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Database error while registering guide: " + e.getMessage());

			OperationResponse response = new OperationResponse(false, "Database error while registering guide", null);
			return new Message(response, Protocol.REGISTER_GUIDE_RESPONSE);

		} catch (Exception e) {
			e.printStackTrace();
			addLog("ERROR - Invalid guide registration request: " + e.getMessage());

			OperationResponse response = new OperationResponse(false, "Invalid guide registration request", null);
			return new Message(response, Protocol.REGISTER_GUIDE_RESPONSE);
		}
	}

	/**
	 * Handles approval of a park parameter change request.
	 * 
	 * @param m the client message
	 * @return approval success or failure message
	 */
	private Message handleApproveParkParameterChangeRequest(Message m) {
		addLog("Client requested approval of park parameter change request.");

		try {
			Object[] data = (Object[]) m.getData();

			int requestId = (int) data[0];
			int approvedByEmployeeId = (int) data[1];
			String reviewNote = (String) data[2];

			addLog("Approving park parameter change request. Request ID: " + requestId
					+ ", approved by employee ID: " + approvedByEmployeeId);

			boolean approved = pcrc.approveRequest(requestId, approvedByEmployeeId, reviewNote);

			if (approved) {
				addLog("Park parameter change request approved successfully. Request ID: " + requestId);
				addLog("Park data may have changed. Notifying all clients.");

				notifyParksUpdated();

				return new Message(requestId, Protocol.PARK_PARAMETER_CHANGE_REQUEST_APPROVED);
			}

			addLog("Park parameter change request approval failed. Request ID: " + requestId);
			return new Message(requestId, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to approve park parameter change request: " + e.getMessage());
			return new Message(e.getMessage(), Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);

		} catch (Exception e) {
			e.printStackTrace();
			addLog("ERROR - Invalid approval request data: " + e.getMessage());
			return new Message(e.getMessage(), Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
		}
	}

	/**
	 * Handles rejection of a park parameter change request.
	 * 
	 * @param m the client message
	 * @return rejection success or failure message
	 */
	private Message handleRejectParkParameterChangeRequest(Message m) {
		addLog("Client requested rejection of park parameter change request.");

		try {
			Object[] data = (Object[]) m.getData();

			int requestId = (int) data[0];
			int approvedByEmployeeId = (int) data[1];
			String reviewNote = (String) data[2];

			addLog("Rejecting park parameter change request. Request ID: " + requestId
					+ ", reviewed by employee ID: " + approvedByEmployeeId);

			boolean rejected = pcrc.rejectRequest(requestId, approvedByEmployeeId, reviewNote);

			if (rejected) {
				addLog("Park parameter change request rejected successfully. Request ID: " + requestId);
				return new Message(requestId, Protocol.PARK_PARAMETER_CHANGE_REQUEST_REJECTED);
			}

			addLog("Park parameter change request rejection failed. Request ID: " + requestId);
			return new Message(requestId, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to reject park parameter change request: " + e.getMessage());
			return new Message(e.getMessage(), Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);

		} catch (Exception e) {
			e.printStackTrace();
			addLog("ERROR - Invalid rejection request data: " + e.getMessage());
			return new Message(e.getMessage(), Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
		}
	}

	/**
	 * Closes all database connections safely.
	 * 
	 * Each database connector returns its connection to the connection pool.
	 * After all connectors are closed, the connection pool closes all available
	 * database connections.
	 */
	private void closeDBConnection() {
		try {
			addLog("Closing database connections.");

			if (oc != null) {
				oc.close();
				addLog("Order database connection returned to pool.");
			}

			if (pc != null) {
				pc.close();
				addLog("Park database connection returned to pool.");
			}

			if (pcrc != null) {
				pcrc.close();
				addLog("Park parameter change request database connection returned to pool.");
			}

			if (sc != null) {
				sc.close();
				addLog("Subscriber database connection returned to pool.");
			}

			if (gc != null) {
				gc.close();
				addLog("Guide database connection returned to pool.");
			}

			DBConnectionPool.getInstance().closeAllConnections();
			addLog("All database pool connections were closed.");

			addLog("Database connections closed successfully.");

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to close database connection: " + e.getMessage());
		}
	}

	/**
	 * Closes the server safely.
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