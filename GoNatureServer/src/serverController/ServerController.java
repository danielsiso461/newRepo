package serverController;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import common.*;

import databaseControllers.OrderConnection;
import databaseControllers.ParkConnection;
import databaseControllers.ParkParameterChangeRequestConnection;
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
	private ParkConnection pc;
	private SubscriberConnection sc;
	private GuideConnection gc;
	private ParkParameterChangeRequestConnection pcrc;
	private int allTimeUserCount = 1;
	private ClientConnectionTableController serverGUIController;

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
			addLog("All database connection objects were created successfully.");

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to create database connection objects: " + e.getMessage());
		}

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

	/**
	 * This method notifies all connected clients that the public park data was
	 * updated.
	 * 
	 * The method loads the updated active parks from the database and sends them to
	 * all clients, so screens such as order creation can refresh their park list.
	 */
	public void notifyParksUpdated() {
		try {
			addLog("Loading updated active parks before notifying clients.");

			List<ParkInfo> parks = pc.getAllActiveParksInfo();

			addLog("Loaded " + parks.size() + " active parks for update notification.");

			server.sendToAllClients(new Message(parks, Protocol.PARKS_UPDATED));

			addLog("PARKS_UPDATED message was sent to all connected clients.");

		} catch (Exception e) {
			e.printStackTrace();
			addLog("ERROR - Failed to notify clients about parks update: " + e.getMessage());
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

		case GET_ACTIVE_PARKS:
			addLog("Client requested active parks list.");

			try {
				addLog("Loading active parks from database.");

				List<ParkInfo> parks = pc.getAllActiveParksInfo();

				addLog("Active parks list loaded from database. Number of parks: " + parks.size());
				addLog("Returning active parks list to client.");

				return new Message(parks, Protocol.ACTIVE_PARKS_RESULT);

			} catch (SQLException e) {
				e.printStackTrace();
				addLog("ERROR - Failed to load active parks: " + e.getMessage());
				return new Message(e.getMessage(), Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
			}

		case APPROVE_PARK_PARAMETER_CHANGE_REQUEST:
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

		case REJECT_PARK_PARAMETER_CHANGE_REQUEST:
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
		case SEARCH_SUBSCRIBER_REQUEST:
		    return handleSearchSubscriber(m);

		case REGISTER_GUIDE_REQUEST:
		    return handleRegisterGuide(m);

		default:
			System.out.println("Error: client request unknown");
			addLog("ERROR - Unknown client request: " + type);
		}

		return null;
	}
	/*
	 * Handles a client request to search for a subscriber by subscriber ID.
	 * 
	 * @param m the message received from the client, containing the subscriber ID.
	 */
	private Message handleSearchSubscriber(Message m) {
	    int subscriberId = (int) m.getData();

	    try {
	        Subscriber subscriber = sc.findSubscriberById(subscriberId);

	        if (subscriber == null) {
	            OperationResponse response =
	                    new OperationResponse(false, "Subscriber not found", null);

	            return new Message(response, Protocol.SEARCH_SUBSCRIBER_RESPONSE);
	        }

	        OperationResponse response =
	                new OperationResponse(true, "Subscriber found", subscriber);

	        return new Message(response, Protocol.SEARCH_SUBSCRIBER_RESPONSE);

	    } catch (SQLException e) {
	        e.printStackTrace();

	        OperationResponse response =
	                new OperationResponse(false, "Database error while searching subscriber", null);

	        return new Message(response, Protocol.SEARCH_SUBSCRIBER_RESPONSE);
	    }
	}
	
	/*
	 * Handles a client request to register an existing subscriber as a guide.
	 * 
	 * @param m the message received from the client, containing a guide registration request.
	 */
	private Message handleRegisterGuide(Message m) {
	    GuideRegistrationRequest request = (GuideRegistrationRequest) m.getData();

	    try {
	        Subscriber subscriber = sc.findSubscriberById(request.getSubscriberId());

	        if (subscriber == null) {
	            OperationResponse response =
	                    new OperationResponse(false, "Subscriber not found", null);

	            return new Message(response, Protocol.REGISTER_GUIDE_RESPONSE);
	        }

	        boolean alreadyGuide = gc.isSubscriberAlreadyGuide(request.getSubscriberId());

	        if (alreadyGuide) {
	            OperationResponse response =
	                    new OperationResponse(false, "Subscriber is already registered as guide", null);

	            return new Message(response, Protocol.REGISTER_GUIDE_RESPONSE);
	        }

	        boolean registered = gc.registerGuide(request);

	        if (registered) {
	            OperationResponse response =
	                    new OperationResponse(true, "Guide registered successfully", null);

	            return new Message(response, Protocol.REGISTER_GUIDE_RESPONSE);
	        }

	        OperationResponse response =
	                new OperationResponse(false, "Failed to register guide", null);

	        return new Message(response, Protocol.REGISTER_GUIDE_RESPONSE);

	    } catch (SQLException e) {
	        e.printStackTrace();

	        OperationResponse response =
	                new OperationResponse(false, "Database error while registering guide", null);

	        return new Message(response, Protocol.REGISTER_GUIDE_RESPONSE);
	    }
	}


	/*
	 * this method makes sure the connection to the DB is closed properly
	 */
	private void closeDBConnection() {
		try {
			addLog("Closing database connections.");

			if (oc != null) {
				oc.close();
				addLog("Order database connection closed.");
			}

			if (pc != null) {
				pc.close();
				addLog("Park database connection closed.");
			}

			if (pcrc != null) {
				pcrc.close();
				addLog("Park parameter change request database connection closed.");
			}

			addLog("Database connections closed successfully.");
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
