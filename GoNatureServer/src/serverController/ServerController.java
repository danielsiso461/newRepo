package serverController;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import common.*;

import databaseControllers.BillConnection;
import databaseControllers.DBConnectionPool;
import databaseControllers.EmployeeConnection;
import databaseControllers.GuideConnection;
import databaseControllers.OrderConnection;
import databaseControllers.OrderExceedsParkCapacityCheck;
import databaseControllers.ParkConnection;
import databaseControllers.ParkParameterChangeRequestConnection;
import databaseControllers.ReportConnection;
import databaseControllers.SubscriberConnection;
import databaseControllers.WaitingListConnection;
import server.Server;
import serverCommon.ServerAndControllerConnection;
import serverCommon.User;
import serverGUI.ClientConnectionTableController;

/**
 * This class connects the networking part of the server and the server GUI.
 *
 * It handles client requests, updates the server GUI, manages connected users,
 * and communicates with the database connection classes.
 */
public class ServerController implements ServerAndControllerConnection {

	/*
	 * Park parameter names used by park_parameter_change_request.
	 */
	private static final String PARAMETER_MAX_CAPACITY = "max_capacity";
	private static final String PARAMETER_PLACES_FOR_UNPLANNED_VISITORS =
			"places_for_unplanned_visitors";
	private static final String PARAMETER_ESTIMATED_VISIT_DURATION_HOURS =
			"estimated_visit_duration_hours";
	private static final String PARAMETER_PROMOTIONS = "promotions";

	/*
	 * Park parameter request statuses.
	 */
	private static final String REQUEST_STATUS_PENDING = "pending";

	/*
	 * Report type names.
	 */
	private static final String REPORT_TYPE_VISITOR = "Visitor Report";
	private static final String REPORT_TYPE_CANCELLATION = "Cancellation Report";
	private static final String REPORT_TYPE_VISIT_DURATION = "Visit Duration Report";
	private static final String REPORT_TYPE_PARK_USAGE = "Park Usage Report";

	private Server server;
	private Set<User> users = new HashSet<>();

	private OrderConnection oc;
	private ParkConnection pc;
	private SubscriberConnection sc;
	private GuideConnection gc;
	private EmployeeConnection ec;
	private ParkParameterChangeRequestConnection pcrc;
	private WaitingListConnection wlc;
	private ReportConnection rc;
	private BillConnection bc;
	private OrderExceedsParkCapacityCheck orderChecker;

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

		server = Server.getInstance(CommonConstants.DEFAULT_PORT, this);
		addLog("Server instance created on port " + CommonConstants.DEFAULT_PORT + ".");

		try {
			oc = OrderConnection.getInstance();
			pc = ParkConnection.getInstance();
			pcrc = ParkParameterChangeRequestConnection.getInstance();
			sc = SubscriberConnection.getInstance();
			gc = GuideConnection.getInstance();
			ec = EmployeeConnection.getInstance();
			wlc = WaitingListConnection.getInstance();
			rc = ReportConnection.getInstance();
			bc = BillConnection.getInstance();

			orderChecker = OrderExceedsParkCapacityCheck.getInstance(pc, oc);

			addLog("Order database connection object created.");
			addLog("Park database connection object created.");
			addLog("Park parameter change request database connection object created.");
			addLog("Subscriber database connection object created.");
			addLog("Guide database connection object created.");
			addLog("Employee database connection object created.");
			addLog("Waiting list database connection object created.");
			addLog("Report database connection object created.");
			addLog("Bill database connection object created.");
			addLog("Order checker object created.");
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
	 * @param ip the server IP address
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
	 * Prints all connected users to the console and log.
	 *
	 * @param message the message to print before the users list
	 */
	private void printUsers(String message) {
		System.out.println(message);
		addLog(message);

		for (User user : users) {
			System.out.println(user.toString());
			addLog("Connected user: " + user);
		}
	}

	/**
	 * Notifies all connected clients that public park data was updated.
	 */
	public void notifyParksUpdated() {
		try {
			addLog("Loading updated active parks before notifying clients.");

			List<Park> parks = pc.getAllActiveParksInfo();

			addLog("Loaded " + parks.size() + " active parks for update notification.");

			server.sendToAllClients(new Message(parks, Protocol.PARKS_UPDATED));

			addLog("PARKS_UPDATED message was sent to all connected clients.");

		} catch (Exception e) {
			e.printStackTrace();
			addLog("ERROR - Failed to notify clients about parks update: " + e.getMessage());
		}
	}

	/**
	 * Notifies all connected clients that park visitor counters were updated.
	 */
	private void notifyParkVisitorCountersUpdated() {
		try {
			server.sendToAllClients(
					new Message(null, Protocol.PARK_VISITOR_COUNTERS_UPDATED)
			);

			addLog("PARK_VISITOR_COUNTERS_UPDATED message was sent to all connected clients.");

		} catch (Exception e) {
			e.printStackTrace();
			addLog("ERROR - Failed to notify clients about visitor counter update: "
					+ e.getMessage());
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

		case CANCEL_ORDER:
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

		case EMPLOYEE_LOGIN_REQUEST:
			return handleEmployeeLogin(m);

		case EXISTING_CUSTOMER_LOGIN_REQUEST:
			return handleExistingCustomerLogin(m);

		case REGISTER_SUBSCRIBER_REQUEST:
			return handleRegisterSubscriber(m);

		case JOIN_WAITING_LIST_REQUEST:
			return handleJoinWaitingList(m);

		case REJECT_WAITING_OFFER_REQUEST:
			return handleRejectWaitingOffer(m);

		case ACCEPT_WAITING_OFFER_REQUEST:
			return handleAcceptWaitingOffer(m);

		case GET_REPORT_REQUEST:
			return handleGetReport(m);

		case CREATE_PARK_PARAMETER_CHANGE_REQUEST:
			return handleCreateParkParameterChangeRequest(m);

		case GET_PENDING_PARK_PARAMETER_CHANGE_REQUESTS:
			return handleGetPendingParkParameterChangeRequests(m);

		case CALCULATE_ENTRY_PRICE_REQUEST:
			return handleCalculateEntryPrice(m);

		case GET_PARK_VISITOR_COUNTERS_REQUEST:
			return handleGetParkVisitorCounters(m);

		case UPDATE_PARK_VISITOR_COUNTER_REQUEST:
			return handleUpdateParkVisitorCounter(m);
			
		case GET_PARK_ORDERS_REQUEST:
			return handleGetParkOrders(m);	
			
		case GET_ALL_ORDERS_REQUEST:
			return handleGetAllOrdersForServiceRepresentative();	

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

	/**
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
	
	/**
	 * Handles a park manager request for viewing orders of a specific park.
	 *
	 * @param m the client message containing park ID
	 * @return a message containing the park orders
	 */
	private Message handleGetParkOrders(Message m) {
		addLog("Client requested park orders.");

		try {
			if (!(m.getData() instanceof Integer)) {
				addLog("ERROR - Invalid park ID for park orders request.");
				return new Message(null, Protocol.GET_PARK_ORDERS_RESPONSE);
			}

			int parkId = (Integer) m.getData();

			List<Order> orders = oc.getOrdersByPark(parkId);

			addLog("Returning " + orders.size() + " orders for park ID: " + parkId);

			return new Message(orders, Protocol.GET_PARK_ORDERS_RESPONSE);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to load park orders: " + e.getMessage());

			return new Message(null, Protocol.GET_PARK_ORDERS_RESPONSE);
		}
	}

	/**
	 * Handles a request for making an order.
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
			o.setOrderStatus(REQUEST_STATUS_PENDING);
			addLog("Make Order Request Failed - SQL error.");
			e.printStackTrace();
			return new Message(null, Protocol.MAKE_ORDER_FAIL);
		} catch (Exception e) {
			o.setOrderStatus(REQUEST_STATUS_PENDING);
			addLog("Make Order Request Failed - resource allocation error.");
			e.printStackTrace();
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

	/**
	 * Handles a request for all active parks.
	 *
	 * @return a message containing active parks, or a failure message
	 */
	private Message handleGetActiveParks() {
		addLog("Client requested active parks list.");

		try {
			addLog("Loading active parks from database.");

			List<Park> parks = pc.getAllActiveParksInfo();

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
	 * @param m the message received from the client
	 * @return a message with the search result
	 */
	private Message handleSearchSubscriber(Message m) {
		int subscriberId = (int) m.getData();

		addLog("Searching subscriber by ID: " + subscriberId);

		try {
			Subscriber subscriber = sc.findSubscriberById(subscriberId);

			if (subscriber == null) {
				OperationResponse response =
						new OperationResponse(false, "Subscriber not found.", null);

				addLog("Subscriber not found. ID: " + subscriberId);

				return new Message(response, Protocol.SEARCH_SUBSCRIBER_RESPONSE);
			}

			OperationResponse response =
					new OperationResponse(true, "Subscriber found.", subscriber);

			addLog("Subscriber found. ID: " + subscriberId);

			return new Message(response, Protocol.SEARCH_SUBSCRIBER_RESPONSE);

		} catch (SQLException e) {
			e.printStackTrace();

			addLog("ERROR - Database error while searching subscriber: " + e.getMessage());

			OperationResponse response =
					new OperationResponse(false, "Database error while searching subscriber.", null);

			return new Message(response, Protocol.SEARCH_SUBSCRIBER_RESPONSE);
		}
	}

	/**
	 * Handles a client request to register an existing subscriber as a guide.
	 *
	 * @param m the message received from the client
	 * @return a message with the registration result
	 */
	private Message handleRegisterGuide(Message m) {
		GuideRegistrationRequest request = (GuideRegistrationRequest) m.getData();

		addLog("Register guide request received. Subscriber ID: " + request.getSubscriberId());

		try {
			Subscriber subscriber = sc.findSubscriberById(request.getSubscriberId());

			if (subscriber == null) {
				OperationResponse response =
						new OperationResponse(false, "Subscriber not found.", null);

				return new Message(response, Protocol.REGISTER_GUIDE_RESPONSE);
			}

			if (gc.isSubscriberAlreadyGuide(request.getSubscriberId())) {
				OperationResponse response =
						new OperationResponse(false, "Subscriber is already registered as a guide.", null);

				return new Message(response, Protocol.REGISTER_GUIDE_RESPONSE);
			}

			gc.registerGuide(request);

			OperationResponse response =
					new OperationResponse(true, "Guide registered successfully.", null);

			addLog("Guide registered successfully. Subscriber ID: " + request.getSubscriberId());

			return new Message(response, Protocol.REGISTER_GUIDE_RESPONSE);

		} catch (SQLException e) {
			e.printStackTrace();

			addLog("ERROR - Database error while registering guide: " + e.getMessage());

			OperationResponse response =
					new OperationResponse(false, "Database error while registering guide.", null);

			return new Message(response, Protocol.REGISTER_GUIDE_RESPONSE);
		}
	}

	/**
	 * Handles a client request to cancel an existing order.
	 *
	 * @param m the message received from the client
	 * @return a message with cancellation success or failure
	 */
	private Message handleCancelOrder(Message m) {
		if (!(m.getData() instanceof CancelOrderMessage)) {
			addLog("ERROR - Invalid cancellation request data.");
			return new Message(m.getData(), Protocol.CANCEL_ORDER_FAILURE);
		}

		CancelOrderMessage cancelOrderMessage = (CancelOrderMessage) m.getData();

		int changedByEmployeeId = 1;

		try {
			expireOldWaitingOffers();

			Order cancelledOrder = oc.getOrderByNumber(cancelOrderMessage.getOrderId());

			boolean cancelled = oc.cancelOrder(
					cancelOrderMessage.getOrderId(),
					changedByEmployeeId,
					cancelOrderMessage.getReason()
			);

			if (cancelled) {
				addLog("Order cancelled successfully. Order ID: " + cancelOrderMessage.getOrderId());

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

	/**
	 * Handles a client request to join the waiting list.
	 *
	 * @param m the message received from the client
	 * @return a message with waiting list success or failure
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

			if (parkId <= 0
					&& waitingListMessage.getParkName() != null
					&& !waitingListMessage.getParkName().trim().isEmpty()) {

				parkId = pc.getParkIdByName(waitingListMessage.getParkName().trim());
				waitingListMessage.setParkId(parkId);
			}

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

	/**
	 * Handles a client request to reject an offered waiting list request.
	 *
	 * @param m the message received from the client
	 * @return a message with reject success or failure
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

	/**
	 * Handles a client request to accept an offered waiting list request.
	 *
	 * @param m the message received from the client
	 * @return a message with accept success or failure
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

	/**
	 * Expires old waiting list offers before handling waiting list actions.
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

	/**
	 * Handles an occasional customer access request.
	 *
	 * @param m the message received from the client
	 * @return a message with the access result
	 */
	private Message handleOccasionalCustomerAccess(Message m) {
		try {
			String customerIdNumber = (String) m.getData();

			addLog("Checking occasional customer ID number: " + customerIdNumber);

			ArrayList<Order> orders = oc.getOrdersByCustomerIdNumber(customerIdNumber);

			if (orders != null && !orders.isEmpty()) {
				OperationResponse response =
						new OperationResponse(true, "Orders found", orders);

				addLog("Occasional customer access approved for ID number: " + customerIdNumber);

				return new Message(response, Protocol.OCCASIONAL_CUSTOMER_ACCESS_RESPONSE);
			}

			OperationResponse response = new OperationResponse(
			        true,
			        "No existing orders were found. You can create a new order.",
			        orders
			);

			addLog("Occasional customer access denied. No orders found for ID number: "
					+ customerIdNumber);

			return new Message(response, Protocol.OCCASIONAL_CUSTOMER_ACCESS_RESPONSE);

		} catch (SQLException e) {
			e.printStackTrace();

			addLog("ERROR - Database error while searching occasional customer orders: "
					+ e.getMessage());

			OperationResponse response =
					new OperationResponse(false, "Database error while searching orders.", null);

			return new Message(response, Protocol.OCCASIONAL_CUSTOMER_ACCESS_RESPONSE);

		} catch (Exception e) {
			e.printStackTrace();

			addLog("ERROR - Unexpected error while handling occasional customer access: "
					+ e.getMessage());

			OperationResponse response =
					new OperationResponse(false, "Unexpected server error while searching orders.", null);

			return new Message(response, Protocol.OCCASIONAL_CUSTOMER_ACCESS_RESPONSE);
		}
	}

	/**
	 * Handles an employee login request.
	 *
	 * @param m the message received from the client
	 * @return a message with the login result
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

	/**
	 * Handles an existing customer login request.
	 *
	 * @param m the message received from the client
	 * @return a message with the login result
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

	/**
	 * Handles a register subscriber request.
	 *
	 * @param m the message received from the client
	 * @return a message with the registration result
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

				addLog("Register subscriber failed. ID number already exists: "
						+ request.getIdNumber());

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

	/**
	 * Handles a report request.
	 *
	 * @param m the client message
	 * @return report response message
	 */
	private Message handleGetReport(Message m) {
		addLog("Client requested report.");

		try {
			if (!(m.getData() instanceof ReportRequest)) {
				OperationResponse response =
						new OperationResponse(false, "Invalid report request", null);

				return new Message(response, Protocol.GET_REPORT_RESPONSE);
			}

			ReportRequest request = (ReportRequest) m.getData();

			if (!isReportRequestValid(request)) {
				OperationResponse response =
						new OperationResponse(false, "Invalid report parameters", null);

				return new Message(response, Protocol.GET_REPORT_RESPONSE);
			}

			if (!isEmployeeAllowedToViewReport(request)) {
				addLog("Employee is not allowed to view this report. Employee ID: "
						+ request.getEmployeeId() + ", Park ID: " + request.getParkId());

				OperationResponse response =
						new OperationResponse(false,
								"You are not allowed to view this park report",
								null);

				return new Message(response, Protocol.GET_REPORT_RESPONSE);
			}

			Object reportData;

			switch (request.getReportType()) {

			case REPORT_TYPE_VISITOR:
				reportData = rc.getVisitorReport(
						request.getParkId(),
						request.getMonth(),
						request.getYear()
				);
				break;

			case REPORT_TYPE_CANCELLATION:
				reportData = rc.getCancellationReport(
						request.getParkId(),
						request.getMonth(),
						request.getYear()
				);
				break;

			case REPORT_TYPE_VISIT_DURATION:
				reportData = rc.getVisitDurationReport(
						request.getParkId(),
						request.getMonth(),
						request.getYear()
				);
				break;

			case REPORT_TYPE_PARK_USAGE:
				reportData = rc.getParkUsageReport(
						request.getParkId(),
						request.getMonth(),
						request.getYear()
				);
				break;

			default:
				OperationResponse response =
						new OperationResponse(false, "Unknown report type", null);

				return new Message(response, Protocol.GET_REPORT_RESPONSE);
			}

			OperationResponse response =
					new OperationResponse(true, "Report loaded successfully", reportData);

			addLog("Report loaded successfully.");

			return new Message(response, Protocol.GET_REPORT_RESPONSE);

		} catch (SQLException e) {
			e.printStackTrace();

			addLog("ERROR - Database error while loading report: " + e.getMessage());

			OperationResponse response =
					new OperationResponse(false, "Database error while loading report", null);

			return new Message(response, Protocol.GET_REPORT_RESPONSE);

		} catch (Exception e) {
			e.printStackTrace();

			addLog("ERROR - Failed to handle report request: " + e.getMessage());

			OperationResponse response =
					new OperationResponse(false, "Failed to load report", null);

			return new Message(response, Protocol.GET_REPORT_RESPONSE);
		}
	}

	/**
	 * Checks if the report request basic parameters are valid.
	 *
	 * @param request the report request
	 * @return true if valid, false otherwise
	 * @throws SQLException if checking the park fails
	 */
	private boolean isReportRequestValid(ReportRequest request) throws SQLException {
		if (request == null) {
			return false;
		}

		if (request.getParkId() <= 0) {
			return false;
		}

		if (request.getMonth() < 1 || request.getMonth() > 12) {
			return false;
		}

		if (request.getYear() < 2000) {
			return false;
		}

		if (request.getEmployeeId() <= 0) {
			return false;
		}

		if (request.getReportType() == null || request.getReportType().isBlank()) {
			return false;
		}

		Park park = pc.getFullParkById(request.getParkId());

		return park != null;
	}

	/**
	 * Checks whether an employee is allowed to view the requested report.
	 *
	 * @param request the report request
	 * @return true if allowed, false otherwise
	 * @throws SQLException if checking employee details fails
	 */
	private boolean isEmployeeAllowedToViewReport(ReportRequest request) throws SQLException {
		int employeeId = request.getEmployeeId();
		int requestedParkId = request.getParkId();

		if (ec.isDepartmentManager(employeeId)) {
			return true;
		}

		if (ec.isParkManager(employeeId)) {
			int employeeParkId = ec.getEmployeeParkId(employeeId);

			return employeeParkId == requestedParkId
					&& isReportAllowedForParkManager(request.getReportType());
		}

		return false;
	}

	/**
	 * Checks whether a park manager is allowed to view the requested report type.
	 *
	 * @param reportType the report type
	 * @return true if allowed
	 */
	private boolean isReportAllowedForParkManager(String reportType) {
		return REPORT_TYPE_VISITOR.equals(reportType)
				|| REPORT_TYPE_CANCELLATION.equals(reportType);
	}

	/**
	 * Handles creating a park parameter change request.
	 *
	 * @param m the client message
	 * @return success or failure response
	 */
	private Message handleCreateParkParameterChangeRequest(Message m) {
		addLog("Client requested to create park parameter change request.");

		try {
			if (!(m.getData() instanceof Object[])) {
				OperationResponse response = new OperationResponse(
						false,
						"Invalid park parameter change request data",
						null
				);

				return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
			}

			Object[] data = (Object[]) m.getData();

			int parkId = (int) data[0];
			int requestedByEmployeeId = (int) data[1];
			String parameterName = data[2] == null ? null : data[2].toString().trim();
			String newValue = data[3] == null ? null : data[3].toString().trim();

			if (!ec.isParkManager(requestedByEmployeeId)) {
				OperationResponse response = new OperationResponse(
						false,
						"Only park managers can request park parameter changes",
						null
				);

				return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
			}

			int employeeParkId = ec.getEmployeeParkId(requestedByEmployeeId);

			if (employeeParkId != parkId) {
				OperationResponse response = new OperationResponse(
						false,
						"Park manager can request changes only for his own park",
						null
				);

				return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
			}

			Park park = pc.getFullParkById(parkId);

			if (park == null) {
				OperationResponse response = new OperationResponse(
						false,
						"Park was not found",
						null
				);

				return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
			}

			String oldValue = getCurrentParkParameterValue(park, parameterName);

			if (oldValue == null) {
				OperationResponse response = new OperationResponse(
						false,
						"Unknown park parameter",
						null
				);

				return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
			}

			boolean created = pcrc.createChangeRequest(
					parkId,
					requestedByEmployeeId,
					parameterName,
					oldValue,
					newValue
			);

			if (!created) {
				OperationResponse response = new OperationResponse(
						false,
						"Failed to create park parameter change request",
						null
				);

				return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
			}

			OperationResponse response = new OperationResponse(
					true,
					"Park parameter change request was created successfully",
					null
			);

			addLog("Park parameter change request was created successfully. Parameter: "
					+ parameterName + ", old value: " + oldValue + ", new value: " + newValue);

			return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_CREATED);

		} catch (SQLException e) {
			e.printStackTrace();

			addLog("ERROR - Failed to create park parameter change request: " + e.getMessage());

			OperationResponse response = new OperationResponse(
					false,
					"Database error while creating park parameter change request",
					null
			);

			return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);

		} catch (Exception e) {
			e.printStackTrace();

			addLog("ERROR - Invalid park parameter change request data: " + e.getMessage());

			OperationResponse response = new OperationResponse(
					false,
					"Failed to create park parameter change request",
					null
			);

			return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
		}
	}

	/**
	 * Returns the current value of a park parameter before creating a change request.
	 *
	 * @param park the park
	 * @param parameterName the requested parameter name
	 * @return the current parameter value as String
	 */
	private String getCurrentParkParameterValue(Park park, String parameterName) {
		if (park == null || parameterName == null) {
			return null;
		}

		switch (parameterName) {

		case PARAMETER_MAX_CAPACITY:
			return String.valueOf(park.getMaxCapacity());

		case PARAMETER_PLACES_FOR_UNPLANNED_VISITORS:
			return String.valueOf(park.getPlacesForUnplannedVisitors());

		case PARAMETER_ESTIMATED_VISIT_DURATION_HOURS:
			return String.valueOf((int) park.getEstimatedVisitDurationHours());

		case PARAMETER_PROMOTIONS:
			return String.valueOf(park.getPromotions());

		default:
			return null;
		}
	}

	/**
	 * Handles loading pending park parameter change requests.
	 *
	 * @param m the client message
	 * @return pending requests response
	 */
	private Message handleGetPendingParkParameterChangeRequests(Message m) {
		addLog("Client requested pending park parameter change requests.");

		try {
			if (!(m.getData() instanceof Integer)) {
				OperationResponse response = new OperationResponse(
						false,
						"Invalid employee id",
						null
				);

				return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
			}

			int employeeId = (Integer) m.getData();

			if (!ec.isDepartmentManager(employeeId)) {
				OperationResponse response = new OperationResponse(
						false,
						"Only department managers can view pending parameter change requests",
						null
				);

				return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
			}

			List<ParkParameterChangeRequest> requests = pcrc.getPendingRequests();

			OperationResponse response = new OperationResponse(
					true,
					"Pending park parameter change requests loaded successfully",
					requests
			);

			return new Message(response, Protocol.PENDING_PARK_PARAMETER_CHANGE_REQUESTS_RESULT);

		} catch (SQLException e) {
			e.printStackTrace();

			addLog("ERROR - Failed to load pending park parameter change requests: "
					+ e.getMessage());

			OperationResponse response = new OperationResponse(
					false,
					"Database error while loading pending requests",
					null
			);

			return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);

		} catch (Exception e) {
			e.printStackTrace();

			addLog("ERROR - Failed to load pending park parameter change requests: "
					+ e.getMessage());

			OperationResponse response = new OperationResponse(
					false,
					"Failed to load pending requests",
					null
			);

			return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
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
			if (!(m.getData() instanceof Object[])) {
				OperationResponse response =
						new OperationResponse(false, "Invalid approval request data", null);

				return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
			}

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

				OperationResponse response =
						new OperationResponse(true, "Park parameter change request approved", requestId);

				return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_APPROVED);
			}

			addLog("Park parameter change request approval failed. Request ID: " + requestId);

			OperationResponse response =
					new OperationResponse(false, "Park parameter change request approval failed", requestId);

			return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);

		} catch (SQLException e) {
			e.printStackTrace();

			addLog("ERROR - Failed to approve park parameter change request: " + e.getMessage());

			OperationResponse response =
					new OperationResponse(false, e.getMessage(), null);

			return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);

		} catch (Exception e) {
			e.printStackTrace();

			addLog("ERROR - Invalid approval request data: " + e.getMessage());

			OperationResponse response =
					new OperationResponse(false, e.getMessage(), null);

			return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
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
			if (!(m.getData() instanceof Object[])) {
				OperationResponse response =
						new OperationResponse(false, "Invalid rejection request data", null);

				return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
			}

			Object[] data = (Object[]) m.getData();

			int requestId = (int) data[0];
			int reviewedByEmployeeId = (int) data[1];
			String reviewNote = (String) data[2];

			addLog("Rejecting park parameter change request. Request ID: " + requestId
					+ ", reviewed by employee ID: " + reviewedByEmployeeId);

			boolean rejected = pcrc.rejectRequest(requestId, reviewedByEmployeeId, reviewNote);

			if (rejected) {
				addLog("Park parameter change request rejected successfully. Request ID: " + requestId);

				OperationResponse response =
						new OperationResponse(true, "Park parameter change request rejected", requestId);

				return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_REJECTED);
			}

			addLog("Park parameter change request rejection failed. Request ID: " + requestId);

			OperationResponse response =
					new OperationResponse(false, "Park parameter change request rejection failed", requestId);

			return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);

		} catch (SQLException e) {
			e.printStackTrace();

			addLog("ERROR - Failed to reject park parameter change request: " + e.getMessage());

			OperationResponse response =
					new OperationResponse(false, e.getMessage(), null);

			return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);

		} catch (Exception e) {
			e.printStackTrace();

			addLog("ERROR - Invalid rejection request data: " + e.getMessage());

			OperationResponse response =
					new OperationResponse(false, e.getMessage(), null);

			return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
		}
	}

	/**
	 * Handles entry price calculation.
	 *
	 * @param m the client message
	 * @return entry price response
	 */
	private Message handleCalculateEntryPrice(Message m) {
		addLog("Client requested entry price calculation.");

		try {
			if (!(m.getData() instanceof EntryPriceRequest)) {
				OperationResponse response = new OperationResponse(
						false,
						"Invalid entry price request",
						null
				);

				return new Message(response, Protocol.CALCULATE_ENTRY_PRICE_RESPONSE);
			}

			EntryPriceRequest request = (EntryPriceRequest) m.getData();

			EntryPriceReceipt receipt =
					bc.calculateReceiptByOrderNumber(request.getOrderNumber());

			OperationResponse response = new OperationResponse(
					true,
					"Entry price calculated successfully",
					receipt
			);

			addLog("Entry price calculated successfully for order number: "
					+ request.getOrderNumber());

			return new Message(response, Protocol.CALCULATE_ENTRY_PRICE_RESPONSE);

		} catch (SQLException e) {
			e.printStackTrace();

			addLog("ERROR - Failed to calculate entry price: " + e.getMessage());

			OperationResponse response = new OperationResponse(
					false,
					e.getMessage(),
					null
			);

			return new Message(response, Protocol.CALCULATE_ENTRY_PRICE_RESPONSE);

		} catch (Exception e) {
			e.printStackTrace();

			addLog("ERROR - Failed to calculate entry price: " + e.getMessage());

			OperationResponse response = new OperationResponse(
					false,
					"Failed to calculate entry price",
					null
			);

			return new Message(response, Protocol.CALCULATE_ENTRY_PRICE_RESPONSE);
		}
	}

	/**
	 * Handles loading park visitor counters.
	 *
	 * @param m the client message
	 * @return visitor counters response
	 */
	private Message handleGetParkVisitorCounters(Message m) {
		addLog("Client requested park visitor counters.");

		try {
			if (!(m.getData() instanceof Integer)) {
				OperationResponse response = new OperationResponse(
						false,
						"Invalid employee id",
						null
				);

				return new Message(response, Protocol.PARK_VISITOR_COUNTERS_RESULT);
			}

			int employeeId = (Integer) m.getData();

			List<ParkVisitorCounterSnapshot> counters;

			if (ec.isDepartmentManager(employeeId)) {
				counters = pc.getAllParkVisitorCounters();

			} else if (ec.isParkManager(employeeId)) {
				int employeeParkId = ec.getEmployeeParkId(employeeId);

				ParkVisitorCounterSnapshot counter =
						pc.getParkVisitorCounter(employeeParkId);

				counters = new ArrayList<>();

				if (counter != null) {
					counters.add(counter);
				}

			} else {
				OperationResponse response = new OperationResponse(
						false,
						"Only park managers and department managers can view park visitor counters",
						null
				);

				return new Message(response, Protocol.PARK_VISITOR_COUNTERS_RESULT);
			}

			OperationResponse response = new OperationResponse(
					true,
					"Park visitor counters loaded successfully",
					counters
			);

			return new Message(response, Protocol.PARK_VISITOR_COUNTERS_RESULT);

		} catch (SQLException e) {
			e.printStackTrace();

			addLog("ERROR - Failed to load park visitor counters: " + e.getMessage());

			OperationResponse response = new OperationResponse(
					false,
					"Database error while loading park visitor counters",
					null
			);

			return new Message(response, Protocol.PARK_VISITOR_COUNTERS_RESULT);

		} catch (Exception e) {
			e.printStackTrace();

			addLog("ERROR - Failed to load park visitor counters: " + e.getMessage());

			OperationResponse response = new OperationResponse(
					false,
					"Failed to load park visitor counters",
					null
			);

			return new Message(response, Protocol.PARK_VISITOR_COUNTERS_RESULT);
		}
	}

	/**
	 * Handles updating park visitor counter.
	 *
	 * @param m the client message
	 * @return update response
	 */
	private Message handleUpdateParkVisitorCounter(Message m) {
		addLog("Client requested park visitor counter update.");

		try {
			if (!(m.getData() instanceof ParkVisitorCounterUpdateRequest)) {
				OperationResponse response = new OperationResponse(
						false,
						"Invalid park visitor counter update request",
						null
				);

				return new Message(response, Protocol.PARK_VISITOR_COUNTER_UPDATE_RESULT);
			}

			ParkVisitorCounterUpdateRequest request =
					(ParkVisitorCounterUpdateRequest) m.getData();

			if (!canEmployeeUpdateParkVisitorCounter(
					request.getEmployeeId(),
					request.getParkId())) {

				OperationResponse response = new OperationResponse(
						false,
						"Employee is not allowed to update this park visitor counter",
						null
				);

				return new Message(response, Protocol.PARK_VISITOR_COUNTER_UPDATE_RESULT);
			}

			boolean updated = pc.updateCurrentVisitors(
					request.getParkId(),
					request.getEmployeeId(),
					request.getActionType(),
					request.getAmount()
			);

			if (!updated) {
				OperationResponse response = new OperationResponse(
						false,
						"Failed to update park visitor counter",
						null
				);

				return new Message(response, Protocol.PARK_VISITOR_COUNTER_UPDATE_RESULT);
			}

			ParkVisitorCounterSnapshot updatedCounter =
					pc.getParkVisitorCounter(request.getParkId());

			OperationResponse response = new OperationResponse(
					true,
					"Park visitor counter updated successfully",
					updatedCounter
			);

			addLog("Park visitor counter updated successfully. Park ID: "
					+ request.getParkId()
					+ ", action: " + request.getActionType()
					+ ", amount: " + request.getAmount());

			notifyParkVisitorCountersUpdated();

			return new Message(response, Protocol.PARK_VISITOR_COUNTER_UPDATE_RESULT);

		} catch (SQLException e) {
			e.printStackTrace();

			addLog("ERROR - Failed to update park visitor counter: " + e.getMessage());

			OperationResponse response = new OperationResponse(
					false,
					e.getMessage(),
					null
			);

			return new Message(response, Protocol.PARK_VISITOR_COUNTER_UPDATE_RESULT);

		} catch (Exception e) {
			e.printStackTrace();

			addLog("ERROR - Failed to update park visitor counter: " + e.getMessage());

			OperationResponse response = new OperationResponse(
					false,
					"Failed to update park visitor counter",
					null
			);

			return new Message(response, Protocol.PARK_VISITOR_COUNTER_UPDATE_RESULT);
		}
	}

	/**
	 * Checks whether an employee may update a specific park visitor counter.
	 *
	 * @param employeeId the employee ID
	 * @param parkId the park ID
	 * @return true if allowed, otherwise false
	 * @throws SQLException if checking employee details fails
	 */
	private boolean canEmployeeUpdateParkVisitorCounter(int employeeId, int parkId)
			throws SQLException {

		if (ec.isDepartmentManager(employeeId)) {
			return true;
		}

		if (ec.isParkManager(employeeId) || ec.isParkWorker(employeeId)) {
			int employeeParkId = ec.getEmployeeParkId(employeeId);

			return employeeParkId == parkId;
		}

		return false;
	}

	/**
	 * Checks if the details of a given order are valid.
	 *
	 * @param o the order to check
	 * @return true if valid, false otherwise
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
	
	private Message handleGetAllOrdersForServiceRepresentative() {
		addLog("Service representative requested all customer orders.");

		try {
			List<Order> orders = oc.getAllOrders();

			addLog("Returning " + orders.size() + " customer orders to service representative.");

			return new Message(orders, Protocol.GET_ALL_ORDERS_RESPONSE);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to load all customer orders: " + e.getMessage());

			return new Message(null, Protocol.GET_ALL_ORDERS_RESPONSE);
		}
	}

	/**
	 * Closes all database connections safely.
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
				addLog("Employee database connection returned to pool.");
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
				addLog("Waiting list database connection returned to pool.");
			}

			if (rc != null) {
				rc.close();
				addLog("Report database connection returned to pool.");
			}

			if (bc != null) {
				bc.close();
				addLog("Bill database connection returned to pool.");
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
	 * Closes all relevant server parts properly.
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