package serverController;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import common.*;
import java.util.ArrayList;

import databaseControllers.DBConnectionPool;
import databaseControllers.EmployeeConnection;
import databaseControllers.GuideConnection;
import databaseControllers.OrderConnection;
import databaseControllers.OrderExceedsParkCapacityCheck;
import databaseControllers.ParkConnection;
import databaseControllers.ParkParameterChangeRequestConnection;
import databaseControllers.SubscriberConnection;
import databaseControllers.WaitingListConnection;
import server.Server;
import serverCommon.ServerAndControllerConnection;
import serverCommon.User;
import serverGUI.ClientConnectionTableController;
import databaseControllers.VisitConnection;

// this class is the controller that connects 
// the networking part of the server and the UI part of it. 
// It is also the logic behind it
public class ServerController implements ServerAndControllerConnection {

	private Server server;
	private Set<User> users = new HashSet<>();
	private OrderConnection oc;
	private ParkConnection pc;
	private SubscriberConnection sc;
	private VisitConnection vc;
	private GuideConnection gc;
	private EmployeeConnection ec;
	private ParkParameterChangeRequestConnection pcrc;
	private OrderExceedsParkCapacityCheck orderChecker;
	private int allTimeUserCount = 1;
	private ClientConnectionTableController serverGUIController;
	private WaitingListConnection wlc;

	public ServerController(ClientConnectionTableController serverGUIController) {
		this.serverGUIController = serverGUIController;

		addLog("Initializing server controller.");

		server = Server.getInstance(CommonConstants.DEFAULT_PORT, this);
		addLog("Server instance created on port " + CommonConstants.DEFAULT_PORT + ".");

		try {
			oc = OrderConnection.getInstance();
			pc = ParkConnection.getInstance();
			pcrc = ParkParameterChangeRequestConnection.getInstance();
			sc = SubscriberConnection.getInstance();
			gc = GuideConnection.getInstance();
			wlc = WaitingListConnection.getInstance();
			vc = VisitConnection.getInstance();
			ec = EmployeeConnection.getInstance();
			orderChecker = OrderExceedsParkCapacityCheck.getInstance(pc, oc);

			addLog("Order database connection object created.");
			addLog("Park database connection object created.");
			addLog("Park parameter change request database connection object created.");
			addLog("Subscriber database connection object created.");
			addLog("Guide database connection object created.");
			addLog("Waiting list database connection object created.");
			addLog("Order checker object created.");
			addLog("All database connection objects were created successfully.");
			addLog("Visit database connection object created.");
		}catch (SQLException e) {
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
	 * This method notifies all connected clients that the public park data was
	 * updated.
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
			return handleUpdateOrder(m);

		case CANCEL_ORDER:
			addLog("Client requested order cancellation.");
			return handleCancelOrder(m);

		case RETURN_ORDER:
			return handleReturnOrder(m);

		case GET_PARK_NAMES:
			return handleGetParkNames(m);

		case MAKE_ORDER:
			return handleMakeOrder(m);

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

		case OCCASIONAL_CUSTOMER_ACCESS_REQUEST:
			return handleOccasionalCustomerAccess(m);

		case JOIN_WAITING_LIST_REQUEST:
			addLog("Client requested to join the waiting list.");
			return handleJoinWaitingList(m);

		case GET_WAITING_OFFERS_REQUEST:
			addLog("Client requested waiting list offers.");
			return handleGetWaitingOffers(m);

		case REJECT_WAITING_OFFER_REQUEST:
			addLog("Client requested to reject a waiting list offer.");
			return handleRejectWaitingOffer(m);

		case ACCEPT_WAITING_OFFER_REQUEST:
			addLog("Client requested to accept a waiting list offer.");
			return handleAcceptWaitingOffer(m);

		case EMPLOYEE_LOGIN_REQUEST:
			addLog("Client requested employee login.");
			return handleEmployeeLogin(m);

		case EXISTING_CUSTOMER_LOGIN_REQUEST:
			addLog("Client requested existing customer login.");
			return handleExistingCustomerLogin(m);

		case REGISTER_SUBSCRIBER_REQUEST:
			addLog("Client requested subscriber registration.");
			return handleRegisterSubscriber(m);

		case CHECK_IN_ORDER_REQUEST:
			addLog("Client requested park check-in by confirmation code.");
			return handleCheckInOrder(m);

		case CHECK_OUT_VISIT_REQUEST:
			addLog("Client requested park check-out by confirmation code.");
			return handleCheckOutVisit(m);

		case OCCASIONAL_VISIT_REQUEST:
			addLog("Client requested occasional visit entrance.");
			return handleOccasionalVisit(m);

		case GET_CURRENT_VISITORS_REQUEST:
			addLog("Client requested current visitors count.");
			return handleGetCurrentVisitors(m);
		default:
			System.out.println("Error: client request unknown");
			addLog("ERROR - Unknown client request: " + type);
			return null;
		}
	}

	/*
	 * Handles a request to update an existing order.
	 * 
	 * @param m the client message
	 * @return a message with update success or failure
	 */
	private Message handleUpdateOrder(Message m) {
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

	/*
	 * Handles a request for returning user orders.
	 * 
	 * @param m the client message
	 * @return a message containing the user's orders, or null if loading failed
	 */
	private Message handleReturnOrder(Message m) {
		addLog("Client requested orders list.");

		List<Order> orders = null;

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

	/*
	 * Handles a request for getting park names.
	 * 
	 * @param m the client message
	 * @return a message containing a list of park names
	 */
	private Message handleGetParkNames(Message m) {
		addLog("Client requested active parks name list.");

		try {
			addLog("Loading names of active parks from database.");

			List<String> parkNames = pc.getActiveParksNames();

			if (parkNames.isEmpty()) {
				addLog("Error - no park names fetched.");
				return new Message(null, Protocol.RETURN_PARK_NAMES_FAILURE);
			}

			addLog("Active parks name list loaded from database.");
			addLog("Returning active parks name list to client.");
			return new Message(parkNames, Protocol.RETURN_PARK_NAMES_SUCCESS);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to load active park names: " + e.getMessage());
			return new Message(null, Protocol.RETURN_PARK_NAMES_FAILURE);
		}
	}

	/*
	 * Handles a request for making order.
	 * 
	 * @param m the client message
	 * @return a message containing the user order on success, or a fail message
	 */
	private Message handleMakeOrder(Message m) {
		if (!(m.getData() instanceof Order)) {
			addLog("Make Order Request Unapproved - unknown error.");
			return new Message(null, Protocol.MAKE_ORDER_FAIL);
		}

		Order o = (Order) m.getData();

		o.setPlacementDate(LocalDate.now());

		int parkId = -1;

		try {
			addLog("Getting parkId from the park name from the DB.");
			parkId = pc.getParkIdByName(o.getParkName());
		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to execute query to get parkId from park name.");
			return new Message(null, Protocol.MAKE_ORDER_FAIL);
		}

		if (parkId == -1) {
			addLog("Make Order Request Unapproved - park name unknown.");
			return new Message(null, Protocol.MAKE_ORDER_FAIL);
		}

		addLog("Successfully fetched parkId from the DB.");
		o.setParkId(parkId);

		if (o.getOrderType().equals(Order.ORDER_TYPE_ORGANIZED)) {
			Integer guideId = null;

			try {
				guideId = gc.isActiveGuide(o.getUserId());
			} catch (SQLException e) {
				e.printStackTrace();
				addLog("ERROR - Failed to execute query to check if user is a guide.");
				return new Message(null, Protocol.MAKE_ORDER_FAIL);
			}

			if (guideId == null) {
				addLog("Make Order Request Unapproved - user is not a guide.");
				return new Message(null, Protocol.MAKE_ORDER_FAIL_NOT_GUIDE);
			}

			o.setGuideId(guideId);
		} else if (o.getGuideId() != null) {
			o.setGuideId(null);
		}

		addLog("Successfully handled guide checking.");

		boolean isSubscribed = false;

		try {
			isSubscribed = sc.subscriberExists(o.getUserId());

			if (isSubscribed) {
				o.setIsSubscribedToTrue();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to execute query to check if user is subscribed.");
			return new Message(null, Protocol.MAKE_ORDER_FAIL);
		}

		if (o.getVisitorNumber() > 1 && !isSubscribed) {
			addLog("Make Order Request Unapproved - user is not subscribed.");
			return new Message(null, Protocol.MAKE_ORDER_FAIL_NOT_SUBSCRIBED);
		}

		addLog("Successfully handled subscriber checking.");

		if (!checkOrderDetailsAreValid(o)) {
			addLog("Make Order Request Unapproved - order details are invalid.");
			return new Message(null, Protocol.MAKE_ORDER_FAIL);
		}

		int check = -1;

		try {
			check = orderChecker.check(o);
		} catch (Exception e) {
			e.printStackTrace();
			addLog("ERROR - Failed to execute query to check if order can be booked.");
			return new Message(null, Protocol.MAKE_ORDER_FAIL);
		}

		if (check == -1) {
			addLog("Make Order Request Unapproved - bad order.");
			return new Message(null, Protocol.MAKE_ORDER_FAIL);
		} else if (check == 1) {
			addLog("Make Order Request Unapproved - bad order time.");
			return new Message(null, Protocol.MAKE_ORDER_FAIL_TIME);
		}

		addLog("Successfully handled all checks.");

		o.setOrderStatus("approved");

		try {
			oc.bookOrder(o);
		} catch (SQLException e) {
			o.setOrderStatus("pending");
			addLog("Make Order Request Failed - SQL error.");
			e.printStackTrace();
			return new Message(null, Protocol.MAKE_ORDER_FAIL);
		} catch (Exception e) {
			o.setOrderStatus("pending");
			addLog("Make Order Request Failed - resource allocation error.");
			return new Message(null, Protocol.MAKE_ORDER_FAIL);
		}

		String phoneNumber = null;

		if (isSubscribed) {
			try {
				phoneNumber = sc.getPhoneNumberById(o.getUserId());
			} catch (Exception e) {
				e.printStackTrace();
				addLog("ERROR - Failed to execute query to get order phone number.");
				return new Message(null, Protocol.MAKE_ORDER_FAIL);
			}

			if (phoneNumber == null) {
				addLog("ERROR - phone number does not exist for existing subscriber.");
				return new Message(null, Protocol.MAKE_ORDER_FAIL);
			}
		}

		o.setPhoneNumber(phoneNumber);
		addLog("Make Order request successful - " + o.getOrderId());

		return new Message(o, Protocol.MAKE_ORDER_SUCCESS);
	}

	/*
	 * Handles a request for all active parks.
	 * 
	 * @return a message containing active parks, or a failure message
	 */
	private Message handleGetActiveParks() {
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
	}

	/*
	 * Handles approval of a park parameter change request.
	 * 
	 * @param m the client message
	 * @return approval result message
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

	/*
	 * Handles rejection of a park parameter change request.
	 * 
	 * @param m the client message
	 * @return rejection result message
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
			if (queuePosition == -1) {
				waitingListMessage.setWaitingStatus("duplicate");

				addLog("ERROR - Duplicate active waiting list request was not added.");

				return new Message(waitingListMessage, Protocol.JOIN_WAITING_LIST_FAILURE);
			}

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
	 * Handles a client request to get all offered waiting list requests for a
	 * specific subscriber.
	 *
	 * The request data is the subscriber ID. The server returns all waiting list
	 * requests that are currently in "offered" status and can still be accepted or
	 * rejected by the visitor.
	 *
	 * @param m the message received from the client, containing the subscriber ID
	 * @return a message with GET_WAITING_OFFERS_SUCCESS or GET_WAITING_OFFERS_FAILURE
	 */
	private Message handleGetWaitingOffers(Message m) {
		if (!(m.getData() instanceof Integer)) {
			addLog("ERROR - Invalid get waiting offers request data.");
			return new Message(null, Protocol.GET_WAITING_OFFERS_FAILURE);
		}

		int subscriberId = (int) m.getData();

		try {
			expireOldWaitingOffers();

			List<WaitingListMessage> offers = wlc.getOfferedRequestsForSubscriber(subscriberId);

			addLog("Returning waiting list offers for subscriber ID: "
					+ subscriberId + ". Offers count: " + offers.size());

			return new Message(offers, Protocol.GET_WAITING_OFFERS_SUCCESS);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to get waiting list offers: " + e.getMessage());

			return new Message(null, Protocol.GET_WAITING_OFFERS_FAILURE);
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
	 * Handles an occasional customer access request received from the client.
	 * 
	 * The occasional customer identifies himself using an ID number.
	 * The method searches all orders that belong to this ID number and returns
	 * them to the client.
	 * 
	 * @param m the message received from the client, containing customer ID number
	 * @return a Message with an OperationResponse containing the access result
	 */
	private Message handleOccasionalCustomerAccess(Message m) {
		String customerIdNumber = (String) m.getData();

		addLog("Checking occasional customer ID number: " + customerIdNumber);

		try {
			ArrayList<Order> orders = oc.getOrdersByCustomerIdNumber(customerIdNumber);

			if (orders != null && !orders.isEmpty()) {
				OperationResponse response =
						new OperationResponse(true, "Orders found", orders);

				addLog("Occasional customer access approved for ID number: " + customerIdNumber);

				return new Message(response, Protocol.OCCASIONAL_CUSTOMER_ACCESS_RESPONSE);
			}

			OperationResponse response =
					new OperationResponse(false, "No orders found for this ID number", null);

			addLog("Occasional customer access denied. No orders found for ID number: " + customerIdNumber);

			return new Message(response, Protocol.OCCASIONAL_CUSTOMER_ACCESS_RESPONSE);

		} catch (SQLException e) {
			e.printStackTrace();

			addLog("ERROR - Database error while searching orders by ID number: " + e.getMessage());

			OperationResponse response =
					new OperationResponse(false, "Database error while searching orders", null);

			return new Message(response, Protocol.OCCASIONAL_CUSTOMER_ACCESS_RESPONSE);
		}
	}
	
	/*
	 * Handles an employee login request received from the client.
	 * 
	 * The method receives username and password from the client, checks them
	 * against the employee table, and returns the employee data if login succeeds.
	 * 
	 * @param m the message received from the client, containing EmployeeLoginRequest
	 * @return a Message with an OperationResponse containing the login result
	 */
	private Message handleEmployeeLogin(Message m) {
		EmployeeLoginRequest request = (EmployeeLoginRequest) m.getData();

		try {
			Employee employee = ec.loginEmployee(
					request.getUsername(),
					request.getPassword()
			);

			if (employee == null) {
				OperationResponse response =
						new OperationResponse(false, "Invalid username or password", null);

				return new Message(response, Protocol.EMPLOYEE_LOGIN_RESPONSE);
			}

			OperationResponse response =
					new OperationResponse(true, "Employee login successful", employee);

			addLog("Employee login successful: " + employee.getUsername()
					+ ", role: " + employee.getRole());

			return new Message(response, Protocol.EMPLOYEE_LOGIN_RESPONSE);

		} catch (SQLException e) {
			e.printStackTrace();

			addLog("ERROR - Database error while employee login: " + e.getMessage());

			OperationResponse response =
					new OperationResponse(false, "Database error while employee login", null);

			return new Message(response, Protocol.EMPLOYEE_LOGIN_RESPONSE);
		}
	}
	
	/*
	 * Handles an existing customer login request received from the client.
	 * 
	 * The method receives username and password from the client, checks them
	 * against the subscriber table, and returns the subscriber data if login succeeds.
	 * 
	 * @param m the message received from the client, containing ExistingCustomerLoginRequest
	 * @return a Message with an OperationResponse containing the login result
	 */
	private Message handleExistingCustomerLogin(Message m) {
		ExistingCustomerLoginRequest request = (ExistingCustomerLoginRequest) m.getData();

		try {
			Subscriber subscriber = sc.loginSubscriber(
					request.getUsername(),
					request.getPassword()
			);

			if (subscriber == null) {
				OperationResponse response =
						new OperationResponse(false, "Invalid username or password", null);

				return new Message(response, Protocol.EXISTING_CUSTOMER_LOGIN_RESPONSE);
			}

			OperationResponse response =
					new OperationResponse(true, "Customer login successful", subscriber);

			addLog("Existing customer login successful: " + subscriber.getSubscriberName());

			return new Message(response, Protocol.EXISTING_CUSTOMER_LOGIN_RESPONSE);

		} catch (SQLException e) {
			e.printStackTrace();

			addLog("ERROR - Database error while existing customer login: " + e.getMessage());

			OperationResponse response =
					new OperationResponse(false, "Database error while customer login", null);

			return new Message(response, Protocol.EXISTING_CUSTOMER_LOGIN_RESPONSE);
		}
	}
	
	/*
	 * Handles a register subscriber request received from the client.
	 * 
	 * The method checks that the username and ID number are not already used.
	 * If the details are valid, it inserts a new subscriber into the database.
	 * 
	 * @param m the message received from the client, containing RegisterSubscriberRequest
	 * @return a Message with an OperationResponse containing the registration result
	 */
	private Message handleRegisterSubscriber(Message m) {
		RegisterSubscriberRequest request = (RegisterSubscriberRequest) m.getData();

		addLog("Register subscriber request received. Username: " + request.getUsername());

		try {
			if (sc.isUsernameExists(request.getUsername())) {
				OperationResponse response =
						new OperationResponse(false, "Username already exists.", null);

				addLog("Register subscriber failed. Username already exists: " + request.getUsername());

				return new Message(response, Protocol.REGISTER_SUBSCRIBER_RESPONSE);
			}

			if (sc.isIdNumberExists(request.getIdNumber())) {
				OperationResponse response =
						new OperationResponse(false, "ID number already exists.", null);

				addLog("Register subscriber failed. ID number already exists: " + request.getIdNumber());

				return new Message(response, Protocol.REGISTER_SUBSCRIBER_RESPONSE);
			}

			sc.registerSubscriber(request);

			OperationResponse response =
					new OperationResponse(true, "Subscriber registered successfully.", null);

			addLog("Subscriber registered successfully. Username: " + request.getUsername());

			return new Message(response, Protocol.REGISTER_SUBSCRIBER_RESPONSE);

		} catch (SQLException e) {
			e.printStackTrace();

			addLog("ERROR - Database error while registering subscriber: " + e.getMessage());

			OperationResponse response =
					new OperationResponse(false, "Database error while registering subscriber.", null);

			return new Message(response, Protocol.REGISTER_SUBSCRIBER_RESPONSE);
		}
	}

	/*
	 * this method checks if the details of a given order are valid
	 * 
	 * @param o the order to check
	 * @return true if valid and false otherwise
	 */
	private boolean checkOrderDetailsAreValid(Order o) {
		if (o.getOrderDate().isBefore(LocalDate.now())) {
			return false;
		}
		if (o.getVisitorNumber() > CommonConstants.MAX_VISITOR_COUNT
				|| o.getVisitorNumber() < CommonConstants.MIN_VISITOR_COUNT) {
			return false;
		}
		if (o.getOrderHour() > CommonConstants.MAX_HOUR
				|| o.getOrderHour() < CommonConstants.MIN_HOUR) {
			return false;
		}
		if (!(o.getOrderStatus().equals(Order.ORDER_STATUS_PENDING))) {
			return false;
		}

		return true;
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
			
			if (ec != null) {
				ec.close();
				addLog("Employee database connection closed.");
			}

			if (sc != null) {
				sc.close();
				addLog("Subscriber database connection returned to pool.");
			}

			if (gc != null) {
				gc.close();
				addLog("Guide database connection returned to pool.");
			}

			if (wlc != null) {
				wlc.close();
				addLog("Waiting list database connection closed.");
			}

			DBConnectionPool.getInstance().closeAllConnections();
			addLog("All database pool connections were closed.");

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
	/*
	 * Handles a park check-in request using a confirmation code.
	 *
	 * The confirmation code is used as a QR code simulation. If the order is valid,
	 * the server creates a new visit record.
	 *
	 * @param m the client message containing ParkEntranceMessage
	 * @return a success or failure message for the check-in action
	 */
	private Message handleCheckInOrder(Message m) {
		if (!(m.getData() instanceof ParkEntranceMessage)) {
			addLog("ERROR - Invalid check-in request data.");
			return new Message(null, Protocol.CHECK_IN_ORDER_FAILURE);
		}

		ParkEntranceMessage entranceMessage = (ParkEntranceMessage) m.getData();

		try {
			int visitId = vc.createVisitFromConfirmationCode(
					entranceMessage.getConfirmationCode(),
					entranceMessage.getParkId(),
					entranceMessage.getActualNumberOfVisitors(),
					entranceMessage.getEmployeeId(),
					"confirmation_code"
			);

			if (visitId == -1) {
				entranceMessage.setResponseMessage("No valid order was found for this confirmation code.");
				addLog("Check-in failed. No valid order was found.");
				return new Message(entranceMessage, Protocol.CHECK_IN_ORDER_FAILURE);
			}

			if (visitId == -2) {
				entranceMessage.setResponseMessage("This order already has an open visit.");
				addLog("Check-in failed. The order already has an open visit.");
				return new Message(entranceMessage, Protocol.CHECK_IN_ORDER_FAILURE);
			}

			if (visitId == -3) {
				entranceMessage.setResponseMessage("Invalid number of visitors for this order.");
				addLog("Check-in failed. Invalid number of visitors.");
				return new Message(entranceMessage, Protocol.CHECK_IN_ORDER_FAILURE);
			}

			int currentVisitors = vc.getCurrentVisitorsInPark(entranceMessage.getParkId());

			entranceMessage.setVisitId(visitId);
			entranceMessage.setCurrentVisitors(currentVisitors);
			entranceMessage.setResponseMessage("Check-in completed successfully. Visit ID: " + visitId);

			addLog("Check-in completed successfully. Visit ID: " + visitId);

			return new Message(entranceMessage, Protocol.CHECK_IN_ORDER_SUCCESS);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to check in order: " + e.getMessage());

			entranceMessage.setResponseMessage("Server error while checking in order.");
			return new Message(entranceMessage, Protocol.CHECK_IN_ORDER_FAILURE);
		}
	}

	/*
	 * Handles a park check-out request using a confirmation code.
	 *
	 * The confirmation code is used as a QR code simulation. If an open visit exists,
	 * the server closes it by setting exit_time.
	 *
	 * @param m the client message containing ParkEntranceMessage
	 * @return a success or failure message for the check-out action
	 */
	private Message handleCheckOutVisit(Message m) {
		if (!(m.getData() instanceof ParkEntranceMessage)) {
			addLog("ERROR - Invalid check-out request data.");
			return new Message(null, Protocol.CHECK_OUT_VISIT_FAILURE);
		}

		ParkEntranceMessage entranceMessage = (ParkEntranceMessage) m.getData();

		try {
			int visitId = vc.closeVisitByConfirmationCode(
					entranceMessage.getConfirmationCode(),
					entranceMessage.getParkId(),
					entranceMessage.getEmployeeId()
			);

			if (visitId == -1) {
				entranceMessage.setResponseMessage("No open visit was found for this confirmation code.");
				addLog("Check-out failed. No open visit was found.");
				return new Message(entranceMessage, Protocol.CHECK_OUT_VISIT_FAILURE);
			}

			int currentVisitors = vc.getCurrentVisitorsInPark(entranceMessage.getParkId());

			entranceMessage.setVisitId(visitId);
			entranceMessage.setCurrentVisitors(currentVisitors);
			entranceMessage.setResponseMessage("Check-out completed successfully. Visit ID: " + visitId);

			addLog("Check-out completed successfully. Visit ID: " + visitId);

			return new Message(entranceMessage, Protocol.CHECK_OUT_VISIT_SUCCESS);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to check out visit: " + e.getMessage());

			entranceMessage.setResponseMessage("Server error while checking out visit.");
			return new Message(entranceMessage, Protocol.CHECK_OUT_VISIT_FAILURE);
		}
	}

	/*
	 * Handles an occasional visit request.
	 *
	 * Occasional visitors do not have an order. The server checks whether the park has
	 * available capacity before creating the visit.
	 *
	 * @param m the client message containing ParkEntranceMessage
	 * @return a success or failure message for the occasional visit action
	 */
	private Message handleOccasionalVisit(Message m) {
		if (!(m.getData() instanceof ParkEntranceMessage)) {
			addLog("ERROR - Invalid occasional visit request data.");
			return new Message(null, Protocol.OCCASIONAL_VISIT_FAILURE);
		}

		ParkEntranceMessage entranceMessage = (ParkEntranceMessage) m.getData();

		try {
			boolean hasCapacity = pc.hasAvailableCapacity(
					entranceMessage.getParkId(),
					entranceMessage.getActualNumberOfVisitors()
			);

			if (!hasCapacity) {
				entranceMessage.setResponseMessage("The park does not have enough available capacity.");
				addLog("Occasional visit failed. Not enough capacity.");
				return new Message(entranceMessage, Protocol.OCCASIONAL_VISIT_FAILURE);
			}

			int visitId = vc.createOccasionalVisit(
					entranceMessage.getParkId(),
					entranceMessage.getActualNumberOfVisitors(),
					entranceMessage.getEmployeeId(),
					"id_number"
			);

			if (visitId == -1) {
				entranceMessage.setResponseMessage("Failed to create occasional visit.");
				addLog("Occasional visit failed. Visit was not created.");
				return new Message(entranceMessage, Protocol.OCCASIONAL_VISIT_FAILURE);
			}

			int currentVisitors = vc.getCurrentVisitorsInPark(entranceMessage.getParkId());

			entranceMessage.setVisitId(visitId);
			entranceMessage.setCurrentVisitors(currentVisitors);
			entranceMessage.setResponseMessage("Occasional visit created successfully. Visit ID: " + visitId);

			addLog("Occasional visit created successfully. Visit ID: " + visitId);

			return new Message(entranceMessage, Protocol.OCCASIONAL_VISIT_SUCCESS);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to create occasional visit: " + e.getMessage());

			entranceMessage.setResponseMessage("Server error while creating occasional visit.");
			return new Message(entranceMessage, Protocol.OCCASIONAL_VISIT_FAILURE);
		}
	}

	/*
	 * Handles a request for the current number of visitors inside a park.
	 *
	 * @param m the client message containing ParkEntranceMessage
	 * @return a success or failure message with the current visitors count
	 */
	private Message handleGetCurrentVisitors(Message m) {
		if (!(m.getData() instanceof ParkEntranceMessage)) {
			addLog("ERROR - Invalid current visitors request data.");
			return new Message(null, Protocol.GET_CURRENT_VISITORS_FAILURE);
		}

		ParkEntranceMessage entranceMessage = (ParkEntranceMessage) m.getData();

		try {
			int currentVisitors = vc.getCurrentVisitorsInPark(entranceMessage.getParkId());

			entranceMessage.setCurrentVisitors(currentVisitors);
			entranceMessage.setResponseMessage("Current visitors in park: " + currentVisitors);

			addLog("Current visitors in park " + entranceMessage.getParkId() + ": " + currentVisitors);

			return new Message(entranceMessage, Protocol.GET_CURRENT_VISITORS_SUCCESS);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to get current visitors: " + e.getMessage());

			entranceMessage.setResponseMessage("Server error while loading current visitors.");
			return new Message(entranceMessage, Protocol.GET_CURRENT_VISITORS_FAILURE);
		}
	}
}