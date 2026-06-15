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
import databaseControllers.GuideConnection;
import databaseControllers.SubscriberConnection;
import server.Server;
import serverCommon.ServerAndControllerConnection;
import serverCommon.User;
import serverGUI.ClientConnectionTableController;
import databaseControllers.WaitingListConnection;
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
	private WaitingListConnection wlc;

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
			wlc = WaitingListConnection.getInstance();


			addLog("Order database connection object created.");
			addLog("Park database connection object created.");
			addLog("Park parameter change request database connection object created.");
			addLog("Waiting list database connection object created.");
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
		case CANCEL_ORDER:
			addLog("Client requested order cancellation.");
			return handleCancelOrder(m);
		case RETURN_ORDER:
			addLog("Client requested orders list.");

			List<Order> req = null;

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
		    
		case OCCASIONAL_CUSTOMER_ACCESS_REQUEST:
			return handleOccasionalCustomerAccess(m);

		case JOIN_WAITING_LIST_REQUEST:
			addLog("Client requested to join the waiting list.");
			return handleJoinWaitingList(m);  
			
		case REJECT_WAITING_OFFER_REQUEST:
			addLog("Client requested to reject a waiting list offer.");
			return handleRejectWaitingOffer(m);
			
		case ACCEPT_WAITING_OFFER_REQUEST:
			addLog("Client requested to accept a waiting list offer.");
			return handleAcceptWaitingOffer(m);
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
	 * Handles a client request to cancel an existing order.
	 *
	 * The order is not deleted from the database. Instead, its order_status is
	 * updated to "cancelled" using OrderConnection.cancelOrder(...). This keeps the
	 * order available for monthly reports and order status history.
	 *
	 * @param m the message received from the client, containing CancelOrderMessage
	 * @return a message with CANCEL_ORDER_SUCCESS or CANCEL_ORDER_FAILURE
	 */
	private Message handleCancelOrder(Message m) {
		if (!(m.getData() instanceof CancelOrderMessage)) {
			addLog("ERROR - Invalid cancellation request data.");
			return new Message(m.getData(), Protocol.CANCEL_ORDER_FAILURE);
		}

		CancelOrderMessage cancelOrderMessage = (CancelOrderMessage) m.getData();

		/*
		 * The current database method requires an employee ID for status history.
		 * Since this cancellation is requested by the visitor through the client,
		 * we use employee ID 1 as a system/default updater.
		 * If the team has a specific system employee in the DB, replace this value.
		 */
		int changedByEmployeeId = 1;

		try {
			expireOldWaitingOffers();
			/*
			 * Loads the order details before cancelling it.
			 * 
			 * These details are needed after the cancellation, so the server can check if
			 * the cancelled order freed places for a visitor in the waiting list.
			 */
			Order cancelledOrder = oc.getOrderByNumber(cancelOrderMessage.getOrderId());
			boolean cancelled = oc.cancelOrder(
					cancelOrderMessage.getOrderId(),
					changedByEmployeeId,
					cancelOrderMessage.getReason()
			);

			if (cancelled) {
				addLog("Order cancelled successfully. Order ID: " + cancelOrderMessage.getOrderId());

				/*
				 * After a successful cancellation, the server checks if the cancelled order
				 * freed enough places for the first matching visitor in the waiting list.
				 */
				if (cancelledOrder != null
						&& cancelledOrder.getParkId() != null
						&& cancelledOrder.getOrderDate() != null
						&& cancelledOrder.getVisitorNumber() != null) {

					boolean offered = wlc.offerFirstMatchingWaitingRequest(
							cancelledOrder.getParkId(),
							cancelledOrder.getOrderDate(),
							cancelledOrder.getVisitorNumber()
					);

					if (offered) {
						addLog("A waiting list request was offered after order cancellation. Order ID: "
								+ cancelOrderMessage.getOrderId());
					} else {
						addLog("No matching waiting list request was found after order cancellation. Order ID: "
								+ cancelOrderMessage.getOrderId());
					}
				}

				return new Message(cancelOrderMessage, Protocol.CANCEL_ORDER_SUCCESS);
			}

			addLog("Order cancellation failed. Order was not found or was not updated. Order ID: "
					+ cancelOrderMessage.getOrderId());
			return new Message(cancelOrderMessage, Protocol.CANCEL_ORDER_FAILURE);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to cancel order: " + e.getMessage());
			return new Message(cancelOrderMessage, Protocol.CANCEL_ORDER_FAILURE);
		}
	}
	/*
	 * Handles a client request to join the waiting list.
	 *
	 * The request data is received as a WaitingListMessage.
	 * The server inserts the request into the waiting_list table, assigns the next
	 * queue position, and returns success or failure to the client.
	 *
	 * @param m the message received from the client, containing WaitingListMessage
	 * @return a message with JOIN_WAITING_LIST_SUCCESS or JOIN_WAITING_LIST_FAILURE
	 */
	private Message handleJoinWaitingList(Message m) {
		if (!(m.getData() instanceof WaitingListMessage)) {
			addLog("ERROR - Invalid waiting list request data.");
			return new Message(m.getData(), Protocol.JOIN_WAITING_LIST_FAILURE);
		}

		WaitingListMessage waitingListMessage = (WaitingListMessage) m.getData();

		try {
			expireOldWaitingOffers();
			int parkId = waitingListMessage.getParkId();

			/*
			 * If the client sent only the park name, the server resolves the matching
			 * park ID before inserting the request into the waiting_list table.
			 */
			if (parkId <= 0
					&& waitingListMessage.getParkName() != null
					&& !waitingListMessage.getParkName().trim().isEmpty()) {

				parkId = pc.getParkIdByName(waitingListMessage.getParkName().trim());
				waitingListMessage.setParkId(parkId);
			}

			/*
			 * If the park ID is still invalid, the waiting list request cannot be saved.
			 */
			if (parkId <= 0) {
				addLog("ERROR - Failed to add visitor to waiting list: invalid park ID.");
				return new Message(waitingListMessage, Protocol.JOIN_WAITING_LIST_FAILURE);
			}

			int queuePosition = wlc.addToWaitingList(
					waitingListMessage.getSubscriberId(),
					parkId,
					waitingListMessage.getRequestedOrderDate(),
					waitingListMessage.getNumberOfVisitors()
			);

			// Updates the message with the queue position assigned by the database layer.
			waitingListMessage.setQueuePosition(queuePosition);
			waitingListMessage.setWaitingStatus("waiting");

			addLog("Visitor added to waiting list successfully. Subscriber ID: "
					+ waitingListMessage.getSubscriberId()
					+ ", park ID: " + parkId
					+ ", queue position: " + queuePosition);

			return new Message(waitingListMessage, Protocol.JOIN_WAITING_LIST_SUCCESS);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to add visitor to waiting list: " + e.getMessage());

			return new Message(waitingListMessage, Protocol.JOIN_WAITING_LIST_FAILURE);
		}
	}
	/*
	 * Handles a client request to reject an offered waiting list request.
	 *
	 * The request data is received as a WaitingListMessage that contains the waiting
	 * list request ID. If the request is rejected successfully, the server also tries
	 * to offer the available place to the next matching visitor in the waiting list.
	 *
	 * @param m the message received from the client, containing WaitingListMessage
	 * @return a message with REJECT_WAITING_OFFER_SUCCESS or REJECT_WAITING_OFFER_FAILURE
	 */
	private Message handleRejectWaitingOffer(Message m) {
		if (!(m.getData() instanceof WaitingListMessage)) {
			addLog("ERROR - Invalid reject waiting offer request data.");
			return new Message(m.getData(), Protocol.REJECT_WAITING_OFFER_FAILURE);
		}

		WaitingListMessage waitingListMessage = (WaitingListMessage) m.getData();

		try {
			expireOldWaitingOffers();
			boolean rejected = wlc.rejectWaitingOfferAndOfferNext(waitingListMessage.getWaitingId());

			if (rejected) {
				waitingListMessage.setWaitingStatus("rejected");

				addLog("Waiting list offer rejected successfully. Waiting ID: "
						+ waitingListMessage.getWaitingId());

				return new Message(waitingListMessage, Protocol.REJECT_WAITING_OFFER_SUCCESS);
			}

			addLog("Reject waiting list offer failed. Waiting ID was not found or was not offered. Waiting ID: "
					+ waitingListMessage.getWaitingId());

			return new Message(waitingListMessage, Protocol.REJECT_WAITING_OFFER_FAILURE);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to reject waiting list offer: " + e.getMessage());

			return new Message(waitingListMessage, Protocol.REJECT_WAITING_OFFER_FAILURE);
		}
	}
	/*
	 * Handles a client request to accept an offered waiting list request.
	 *
	 * The request data is received as a WaitingListMessage that contains the waiting
	 * list request ID. If the request is accepted successfully, the waiting list row
	 * is changed from "offered" to "accepted".
	 *
	 * @param m the message received from the client, containing WaitingListMessage
	 * @return a message with ACCEPT_WAITING_OFFER_SUCCESS or ACCEPT_WAITING_OFFER_FAILURE
	 */
	private Message handleAcceptWaitingOffer(Message m) {
		if (!(m.getData() instanceof WaitingListMessage)) {
			addLog("ERROR - Invalid accept waiting offer request data.");
			return new Message(m.getData(), Protocol.ACCEPT_WAITING_OFFER_FAILURE);
		}

		WaitingListMessage waitingListMessage = (WaitingListMessage) m.getData();

		try {
			expireOldWaitingOffers();
			boolean accepted = wlc.acceptWaitingOffer(waitingListMessage.getWaitingId());

			if (accepted) {
				waitingListMessage.setWaitingStatus("accepted");

				addLog("Waiting list offer accepted successfully. Waiting ID: "
						+ waitingListMessage.getWaitingId());

				return new Message(waitingListMessage, Protocol.ACCEPT_WAITING_OFFER_SUCCESS);
			}

			addLog("Accept waiting list offer failed. Waiting ID was not found or was not offered. Waiting ID: "
					+ waitingListMessage.getWaitingId());

			return new Message(waitingListMessage, Protocol.ACCEPT_WAITING_OFFER_FAILURE);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to accept waiting list offer: " + e.getMessage());

			return new Message(waitingListMessage, Protocol.ACCEPT_WAITING_OFFER_FAILURE);
		}
	}
	/*
	 * Expires old waiting list offers before handling waiting list actions.
	 *
	 * This keeps the waiting list updated by moving expired offers to "expired" and
	 * offering the available places to the next matching visitors.
	 */
	private void expireOldWaitingOffers() {
		try {
			int expiredCount = wlc.expireOldOffersAndOfferNext();

			if (expiredCount > 0) {
				addLog("Expired old waiting list offers. Count: " + expiredCount);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to expire old waiting list offers: " + e.getMessage());
		}
	}
	/*
	 * Handles a client request for occasional customer access by order number.
	 * 
	 * The method receives a Message that contains the order number.
	 * It checks whether the order exists in the database.
	 * If the order exists, it returns a successful OperationResponse.
	 * Otherwise, it returns a failure response.
	 * 
	 * @param m the message received from the client, containing the order number
	 * @return a Message with an OperationResponse containing the access result
	 */
	private Message handleOccasionalCustomerAccess(Message m) {
		int orderNumber = (int) m.getData();

		try {
			boolean orderExists = oc.orderExists(orderNumber);

			if (orderExists) {
				OperationResponse response =
						new OperationResponse(true, "Order found", orderNumber);

				return new Message(response, Protocol.OCCASIONAL_CUSTOMER_ACCESS_RESPONSE);
			}

			OperationResponse response =
					new OperationResponse(false, "Order not found", null);

			return new Message(response, Protocol.OCCASIONAL_CUSTOMER_ACCESS_RESPONSE);

		} catch (SQLException e) {
			e.printStackTrace();

			OperationResponse response =
					new OperationResponse(false, "Database error while searching order", null);

			return new Message(response, Protocol.OCCASIONAL_CUSTOMER_ACCESS_RESPONSE);
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
			if (wlc != null) {
				wlc.close();
				addLog("Waiting list database connection closed.");
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
