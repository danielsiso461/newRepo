package serverController;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import common.CancelOrderMessage;
import common.CommonConstants;
import common.Employee;
import common.EmployeeLoginRequest;
import common.EntryPriceReceipt;
import common.EntryPriceRequest;
import common.ExistingCustomerLoginRequest;
import common.GuideRegistrationRequest;
import common.Message;
import common.OperationResponse;
import common.Order;
import common.Park;
import common.ParkEntranceMessage;
import common.ParkParameterChangeRequest;
import common.ParkVisitorCounterSnapshot;
import common.ParkVisitorCounterUpdateRequest;
import common.Protocol;
import common.RegisterSubscriberRequest;
import common.ReportRequest;
import common.Subscriber;
import common.UpdateMessage;
import common.WaitingListMessage;
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
import databaseControllers.VisitConnection;
import databaseControllers.WaitingListConnection;
import server.Server;
import serverCommon.ServerAndControllerConnection;
import serverCommon.User;
import serverGUI.ClientConnectionTableController;
import timerController.TimerController;

/**
 * Coordinates the server networking layer, database connectors, timers, and server GUI.
 * 
 * This controller receives requests from the Server class, routes them to the
 * appropriate database connector, creates response messages for clients, updates
 * the server GUI, and manages reminder-related actions during runtime.
 */
public class ServerController implements ServerAndControllerConnection {

	private static final String PARAMETER_MAX_CAPACITY = "max_capacity";
	private static final String PARAMETER_PLACES_FOR_UNPLANNED_VISITORS =
			"places_for_unplanned_visitors";
	private static final String PARAMETER_ESTIMATED_VISIT_DURATION_HOURS =
			"estimated_visit_duration_hours";
	private static final String PARAMETER_PROMOTIONS = "promotions";

	private static final String REPORT_TYPE_VISITOR = "Visitor Report";
	private static final String REPORT_TYPE_CANCELLATION = "Cancellation Report";
	private static final String REPORT_TYPE_VISIT_DURATION = "Visit Duration Report";
	private static final String REPORT_TYPE_PARK_USAGE = "Park Usage Report";

	private static final String ORDER_STATUS_APPROVED = "approved";
	private static final String ORDER_STATUS_PENDING = "pending";

	private Server server;
	private Set<User> users = new HashSet<>();

	private OrderConnection oc;
	private ParkConnection pc;
	private SubscriberConnection sc;
	private VisitConnection vc;
	private GuideConnection gc;
	private EmployeeConnection ec;
	private ParkParameterChangeRequestConnection pcrc;
	private WaitingListConnection wlc;
	private ReportConnection rc;
	private BillConnection bc;
	private OrderExceedsParkCapacityCheck orderChecker;
	
	private TimerController tc;
	
	private int allTimeUserCount = 1;

	private ClientConnectionTableController serverGUIController;

	/**
	 * Creates a ServerController instance.
	 *
	 * The constructor initializes the server, creates the database connection
	 * objects, starts the timer controller, and begins listening for client
	 * connections.
	 *
	 * @param serverGUIController the server GUI controller used for log and table updates
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
			vc = VisitConnection.getInstance();
			rc = ReportConnection.getInstance();
			bc = BillConnection.getInstance();

			orderChecker = OrderExceedsParkCapacityCheck.getInstance(pc, oc);

			addLog("All database connection objects were created successfully.");

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to create database connection objects: " + e.getMessage());
		}
		
		try {
			tc = new TimerController(this);
			tc.start();
			addLog("Timer controller started.");
		} catch(Exception e) {
			e.printStackTrace();
			addLog("ERROR - Failed to create database connection objects: " + e.getMessage());
		}
		
		try {
			server.listen();
			addLog("Server started listening for clients.");
		} catch (Exception ex) {
			ex.printStackTrace();
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
	 * Adds a newly connected user to the server's connected users set.
	 *
	 * If the user was not already connected, the method assigns a user number,
	 * updates the GUI, and writes the connection event to the server log.
	 *
	 * @param u the connected user
	 * @return true if the user was added successfully, otherwise false
	 */
	@Override
	public boolean addUserOnUserConnected(User u) {
		if (u == null) {
			addLog("Tried to connect a null user.");
			return false;
		}

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
	 * Removes a disconnected user from the server's connected users set.
	 *
	 * The method also updates the user's status in the GUI when the user was already
	 * assigned a server user number.
	 *
	 * @param u the disconnected user
	 */
	@Override
	public void removeUserOnUserDisconnected(User u) {
		if (u == null) {
			addLog("Tried to disconnect a null user.");
			return;
		}

		if (u.getUserNumber() != null) {
			u.setStatus(false);

			if (serverGUIController != null) {
				serverGUIController.onUserDisconnected(u);
			}
		}

		users.remove(u);
		addLog("User removed from connected users set.");
	}

	/**
	 * Prints all currently connected users to the console and the server log.
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
	 *
	 * The method loads the active park list and sends it to all clients using the
	 * PARKS_UPDATED protocol.
	 */
	public void notifyParksUpdated() {
		try {
			List<Park> parks = pc.getAllActiveParksInfo();

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
	 * The method identifies the request protocol and delegates the operation to the
	 * matching handler method.
	 *
	 * @param m the message received from the client
	 * @return the response message that should be sent back to the client
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
			
		case SEARCH_USER_INFORMATION_REQUEST:
			addLog("Client requested user information search.");
			return handleSearchUserInformation(m);	

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

		case GET_WAITING_OFFERS_REQUEST:
			return handleGetWaitingOffers(m);

		case REJECT_WAITING_OFFER_REQUEST:
			return handleRejectWaitingOffer(m);

		case ACCEPT_WAITING_OFFER_REQUEST:
			return handleAcceptWaitingOffer(m);

		case CHECK_IN_ORDER_REQUEST:
			return handleCheckInOrder(m);

		case CHECK_OUT_VISIT_REQUEST:
			return handleCheckOutVisit(m);

		case OCCASIONAL_VISIT_REQUEST:
			return handleOccasionalVisit(m);

		case GET_CURRENT_VISITORS_REQUEST:
			return handleGetCurrentVisitors(m);

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
		
		case ACCEPT_ORDER_REMINDER:
			return handleAcceptOrderReminder(m);
			
		case DECLINE_ORDER_REMINDER:
			return handleDeclineOrderReminder(m);
			
		default:
			addLog("ERROR - Unknown client request: " + type);
			return null;
		}
	}

	/**
	 * Handles an order update request.
	 *
	 * @param m the client message containing an UpdateMessage
	 * @return a message indicating whether the update succeeded or failed
	 */
	private Message handleUpdateOrder(Message m) {
		Protocol typeRet = Protocol.UPDATE_ORDER_SUCCESS;
		UpdateMessage um = (UpdateMessage) m.getData();

		try {
			oc.updateOrder(um);
			addLog("Order updated successfully.");
		} catch (SQLException e) {
			typeRet = Protocol.UPDATE_ORDER_FAILURE;
			e.printStackTrace();
			addLog("ERROR - Failed to update order: " + e.getMessage());
		}

		return new Message(m.getData(), typeRet);
	}

	/**
	 * Handles a request for loading a user's active orders.
	 *
	 * @param m the client message containing the subscriber ID
	 * @return a message containing the user's orders, or null if loading failed
	 */
	private Message handleReturnOrder(Message m) {
		List<Order> orders = null;

		try {
			orders = oc.getUserOrders(m);
		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to load orders: " + e.getMessage());
		}

		if (orders != null) {
			return new Message(orders, Protocol.RETURN_ORDER);
		}

		return null;
	}

	/**
	 * Handles a request for the names of all active parks.
	 *
	 * @param m the client message
	 * @return a message containing the active park names or a failure protocol
	 */
	private Message handleGetParkNames(Message m) {
		try {
			List<String> parkNames = pc.getActiveParksNames();

			if (parkNames.isEmpty()) {
				return new Message(null, Protocol.RETURN_PARK_NAMES_FAILURE);
			}

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
	 * @param m the client message containing the park ID
	 * @return a message containing the park orders
	 */
	private Message handleGetParkOrders(Message m) {
		try {
			if (!(m.getData() instanceof Integer)) {
				return new Message(null, Protocol.GET_PARK_ORDERS_RESPONSE);
			}

			int parkId = (Integer) m.getData();

			List<Order> orders = oc.getOrdersByPark(parkId);

			return new Message(orders, Protocol.GET_PARK_ORDERS_RESPONSE);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to load park orders: " + e.getMessage());

			return new Message(null, Protocol.GET_PARK_ORDERS_RESPONSE);
		}
	}

	/**
	 * Handles a request for creating a new order.
	 *
	 * The method validates the order data, resolves the park and guide data,
	 * checks subscriber and capacity rules, books the order, and returns the result
	 * to the client.
	 *
	 * @param m the client message containing the Order object
	 * @return a success message with the created order, or a failure message
	 */
	private Message handleMakeOrder(Message m) {
		if (!(m.getData() instanceof Order)) {
			return new Message(null, Protocol.MAKE_ORDER_FAIL);
		}

		Order o = (Order) m.getData();

		if (o.getOrderDate() == null
				|| o.getVisitorNumber() == null
				|| o.getUserId() == null
				|| o.getParkName() == null
				|| o.getOrderType() == null) {
			return new Message(null, Protocol.MAKE_ORDER_FAIL);
		}

		o.setPlacementDate(LocalDate.now());

		int parkId = -1;

		try {
			parkId = pc.getParkIdByName(o.getParkName());
		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to get park ID by name.");
			return new Message(null, Protocol.MAKE_ORDER_FAIL);
		}

		if (parkId == -1) {
			return new Message(null, Protocol.MAKE_ORDER_FAIL);
		}

		o.setParkId(parkId);

		if (Order.ORDER_TYPE_ORGANIZED.equals(o.getOrderType())) {
			Integer guideId = null;

			try {
				guideId = gc.isActiveGuide(o.getUserId());
			} catch (SQLException e) {
				e.printStackTrace();
				addLog("ERROR - Failed to check guide status.");
				return new Message(null, Protocol.MAKE_ORDER_FAIL);
			}

			if (guideId == null) {
				return new Message(null, Protocol.MAKE_ORDER_FAIL_NOT_GUIDE);
			}

			o.setGuideId(guideId);

		} else if (o.getGuideId() != null) {
			o.setGuideId(null);
		}

		boolean isSubscribed = false;

		try {
			isSubscribed = sc.subscriberExists(o.getUserId());

			if (isSubscribed) {
				o.setIsSubscribedToTrue();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to check subscriber status.");
			return new Message(null, Protocol.MAKE_ORDER_FAIL);
		}

		if (o.getVisitorNumber() > 1 && !isSubscribed) {
			return new Message(null, Protocol.MAKE_ORDER_FAIL_NOT_SUBSCRIBED);
		}

		if (!checkOrderDetailsAreValid(o)) {
			return new Message(null, Protocol.MAKE_ORDER_FAIL);
		}

		int check = -1;

		try {
			check = orderChecker.check(o);
		} catch (Exception e) {
			e.printStackTrace();
			addLog("ERROR - Failed to check order capacity.");
			return new Message(null, Protocol.MAKE_ORDER_FAIL);
		}

		if (check == -1) {
			return new Message(null, Protocol.MAKE_ORDER_FAIL);
		}

		if (check == 1) {
			return new Message(null, Protocol.MAKE_ORDER_FAIL_TIME);
		}

		o.setOrderStatus(ORDER_STATUS_APPROVED);

		try {
			oc.bookOrder(o);
		} catch (SQLException e) {
			o.setOrderStatus(ORDER_STATUS_PENDING);
			e.printStackTrace();
			addLog("ERROR - Failed to book order: " + e.getMessage());
			return new Message(null, Protocol.MAKE_ORDER_FAIL);
		} catch (Exception e) {
			o.setOrderStatus(ORDER_STATUS_PENDING);
			e.printStackTrace();
			addLog("ERROR - Failed to book order: " + e.getMessage());
			return new Message(null, Protocol.MAKE_ORDER_FAIL);
		}

		String phoneNumber = null;

		if (isSubscribed) {
			try {
				phoneNumber = sc.getPhoneNumberById(o.getUserId());
			} catch (Exception e) {
				e.printStackTrace();
				addLog("ERROR - Failed to get subscriber phone number.");
				return new Message(null, Protocol.MAKE_ORDER_FAIL);
			}

			if (phoneNumber == null) {
				return new Message(null, Protocol.MAKE_ORDER_FAIL);
			}
		}

		o.setPhoneNumber(phoneNumber);

		return new Message(o, Protocol.MAKE_ORDER_SUCCESS);
	}

	/**
	 * Handles a request for loading all active parks.
	 *
	 * @return a message containing active parks or a null data response on failure
	 */
	private Message handleGetActiveParks() {
		try {
			List<Park> parks = pc.getAllActiveParksInfo();

			return new Message(parks, Protocol.ACTIVE_PARKS_RESULT);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to load active parks: " + e.getMessage());

			return new Message(null, Protocol.ACTIVE_PARKS_RESULT);
		}
	}

	/**
	 * Handles a client request to search for a subscriber by subscriber ID.
	 *
	 * @param m the message received from the client
	 * @return a message containing the subscriber search result
	 */
	private Message handleSearchSubscriber(Message m) {
		try {
			if (!(m.getData() instanceof Integer)) {
				OperationResponse response =
						new OperationResponse(false, "Invalid subscriber ID.", null);

				return new Message(response, Protocol.SEARCH_SUBSCRIBER_RESPONSE);
			}

			int subscriberId = (int) m.getData();

			Subscriber subscriber = sc.findSubscriberById(subscriberId);

			if (subscriber == null) {
				OperationResponse response =
						new OperationResponse(false, "Subscriber not found.", null);

				return new Message(response, Protocol.SEARCH_SUBSCRIBER_RESPONSE);
			}

			OperationResponse response =
					new OperationResponse(true, "Subscriber found.", subscriber);

			return new Message(response, Protocol.SEARCH_SUBSCRIBER_RESPONSE);

		} catch (SQLException e) {
			e.printStackTrace();

			OperationResponse response =
					new OperationResponse(false, "Database error while searching subscriber.", null);

			return new Message(response, Protocol.SEARCH_SUBSCRIBER_RESPONSE);
		}
	}

	/**
	 * Handles a client request to register an existing subscriber as a guide.
	 *
	 * @param m the message received from the client
	 * @return a message containing the guide registration result
	 */
	private Message handleRegisterGuide(Message m) {
		if (!(m.getData() instanceof GuideRegistrationRequest)) {
			OperationResponse response =
					new OperationResponse(false, "Invalid guide registration request.", null);

			return new Message(response, Protocol.REGISTER_GUIDE_RESPONSE);
		}

		GuideRegistrationRequest request = (GuideRegistrationRequest) m.getData();

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

			return new Message(response, Protocol.REGISTER_GUIDE_RESPONSE);

		} catch (SQLException e) {
			e.printStackTrace();

			OperationResponse response =
					new OperationResponse(false, "Database error while registering guide.", null);

			return new Message(response, Protocol.REGISTER_GUIDE_RESPONSE);
		}
	}

	/**
	 * Handles a request to cancel an order.
	 *
	 * @param m the client message containing a CancelOrderMessage
	 * @return a message indicating whether the cancellation succeeded or failed
	 */
	private Message handleCancelOrder(Message m) {
		if (!(m.getData() instanceof CancelOrderMessage)) {
			return new Message(m.getData(), Protocol.CANCEL_ORDER_FAILURE);
		}

		CancelOrderMessage cancelOrderMessage = (CancelOrderMessage) m.getData();
		return cancelOrder(cancelOrderMessage);
		
	}
	
	/**
	 * Cancels an order and offers the released places to the waiting list when possible.
	 *
	 * @param cancelOrderMessage the cancellation details
	 * @return the response message for the cancellation operation
	 */
	private Message cancelOrder(CancelOrderMessage cancelOrderMessage) {
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
				if (cancelledOrder != null
						&& cancelledOrder.getParkId() != null
						&& cancelledOrder.getOrderDate() != null
						&& cancelledOrder.getVisitorNumber() != null) {

					wlc.offerFirstMatchingWaitingRequest(
							cancelledOrder.getParkId(),
							cancelledOrder.getOrderDate(),
							cancelledOrder.getVisitorNumber()
					);
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
	 * Handles a request to join the waiting list.
	 *
	 * The method resolves the park ID when needed, checks duplicate active waiting
	 * requests, and adds the visitor to the waiting list.
	 *
	 * @param m the client message containing a WaitingListMessage
	 * @return a success or failure message for joining the waiting list
	 */
	private Message handleJoinWaitingList(Message m) {
		if (!(m.getData() instanceof WaitingListMessage)) {
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

				return new Message(waitingListMessage, Protocol.JOIN_WAITING_LIST_FAILURE);
			}

			waitingListMessage.setQueuePosition(queuePosition);
			waitingListMessage.setWaitingStatus("waiting");

			return new Message(waitingListMessage, Protocol.JOIN_WAITING_LIST_SUCCESS);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to add visitor to waiting list: " + e.getMessage());

			return new Message(waitingListMessage, Protocol.JOIN_WAITING_LIST_FAILURE);
		}
	}

	/**
	 * Handles a request to load waiting list offers for a subscriber.
	 *
	 * @param m the client message containing the subscriber ID
	 * @return a message containing the subscriber's waiting list offers
	 */
	private Message handleGetWaitingOffers(Message m) {
		if (!(m.getData() instanceof Integer)) {
			return new Message(null, Protocol.GET_WAITING_OFFERS_FAILURE);
		}

		int subscriberId = (int) m.getData();

		try {
			expireOldWaitingOffers();

			List<WaitingListMessage> offers = wlc.getOfferedRequestsForSubscriber(subscriberId);

			return new Message(offers, Protocol.GET_WAITING_OFFERS_SUCCESS);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to get waiting list offers: " + e.getMessage());

			return new Message(null, Protocol.GET_WAITING_OFFERS_FAILURE);
		}
	}

	/**
	 * Handles a client request to reject an offered waiting list request.
	 *
	 * @param m the message received from the client
	 * @return a message indicating whether the offer rejection succeeded or failed
	 */
	private Message handleRejectWaitingOffer(Message m) {
		if (!(m.getData() instanceof WaitingListMessage)) {
			return new Message(m.getData(), Protocol.REJECT_WAITING_OFFER_FAILURE);
		}

		WaitingListMessage waitingListMessage = (WaitingListMessage) m.getData();

		try {
			expireOldWaitingOffers();

			boolean rejected = wlc.rejectWaitingOfferAndOfferNext(waitingListMessage.getWaitingId());

			if (rejected) {
				waitingListMessage.setWaitingStatus("cancelled");

				return new Message(waitingListMessage, Protocol.REJECT_WAITING_OFFER_SUCCESS);
			}

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
	 * @param m the client message containing a WaitingListMessage
	 * @return a message indicating whether the offer acceptance succeeded or failed
	 */
	private Message handleAcceptWaitingOffer(Message m) {
		if (!(m.getData() instanceof WaitingListMessage)) {
			return new Message(m.getData(), Protocol.ACCEPT_WAITING_OFFER_FAILURE);
		}

		WaitingListMessage waitingListMessage = (WaitingListMessage) m.getData();

		try {
			expireOldWaitingOffers();

			boolean accepted = wlc.acceptWaitingOffer(waitingListMessage.getWaitingId());

			if (accepted) {
				waitingListMessage.setWaitingStatus("confirmed");

				return new Message(waitingListMessage, Protocol.ACCEPT_WAITING_OFFER_SUCCESS);
			}

			return new Message(waitingListMessage, Protocol.ACCEPT_WAITING_OFFER_FAILURE);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to accept waiting list offer: " + e.getMessage());

			return new Message(waitingListMessage, Protocol.ACCEPT_WAITING_OFFER_FAILURE);
		}
	}

	/**
	 * Expires old waiting list offers before waiting list operations are performed.
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
	 * The method searches for existing orders by customer ID number and returns the
	 * result to the client.
	 *
	 * @param m the message received from the client
	 * @return a message containing the access result
	 */
	private Message handleOccasionalCustomerAccess(Message m) {
		try {
			if (!(m.getData() instanceof String)) {
				OperationResponse response =
						new OperationResponse(false, "Invalid ID number.", null);

				return new Message(response, Protocol.OCCASIONAL_CUSTOMER_ACCESS_RESPONSE);
			}

			String customerIdNumber = (String) m.getData();

			ArrayList<Order> orders = oc.getOrdersByCustomerIdNumber(customerIdNumber);

			if (orders != null && !orders.isEmpty()) {
				OperationResponse response =
						new OperationResponse(true, "Orders found", orders);

				return new Message(response, Protocol.OCCASIONAL_CUSTOMER_ACCESS_RESPONSE);
			}

			OperationResponse response = new OperationResponse(
			        true,
			        "No existing orders were found. You can create a new order.",
			        orders
			);

			return new Message(response, Protocol.OCCASIONAL_CUSTOMER_ACCESS_RESPONSE);

		} catch (SQLException e) {
			e.printStackTrace();

			OperationResponse response =
					new OperationResponse(false, "Database error while searching orders.", null);

			return new Message(response, Protocol.OCCASIONAL_CUSTOMER_ACCESS_RESPONSE);

		} catch (Exception e) {
			e.printStackTrace();

			OperationResponse response =
					new OperationResponse(false, "Unexpected server error while searching orders.", null);

			return new Message(response, Protocol.OCCASIONAL_CUSTOMER_ACCESS_RESPONSE);
		}
	}

	/**
	 * Handles an employee login request.
	 *
	 * @param m the message received from the client
	 * @return a message containing the employee login result
	 */
	private Message handleEmployeeLogin(Message m) {
		if (!(m.getData() instanceof EmployeeLoginRequest)) {
			OperationResponse response =
					new OperationResponse(false, "Invalid employee login request", null);

			return new Message(response, Protocol.EMPLOYEE_LOGIN_RESPONSE);
		}

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

			return new Message(response, Protocol.EMPLOYEE_LOGIN_RESPONSE);

		} catch (SQLException e) {
			e.printStackTrace();

			OperationResponse response =
					new OperationResponse(false, "Database error while employee login", null);

			return new Message(response, Protocol.EMPLOYEE_LOGIN_RESPONSE);
		}
	}

	/**
	 * Handles an existing customer login request.
	 *
	 * @param m the message received from the client
	 * @return a message containing the customer login result
	 */
	private Message handleExistingCustomerLogin(Message m) {
		if (!(m.getData() instanceof ExistingCustomerLoginRequest)) {
			OperationResponse response =
					new OperationResponse(false, "Invalid customer login request", null);

			return new Message(response, Protocol.EXISTING_CUSTOMER_LOGIN_RESPONSE);
		}

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

			return new Message(response, Protocol.EXISTING_CUSTOMER_LOGIN_RESPONSE);

		} catch (SQLException e) {
			e.printStackTrace();

			OperationResponse response =
					new OperationResponse(false, "Database error while customer login", null);

			return new Message(response, Protocol.EXISTING_CUSTOMER_LOGIN_RESPONSE);
		}
	}

	/**
	 * Handles a subscriber registration request.
	 *
	 * The method checks for duplicate username and ID number before registering the
	 * subscriber.
	 *
	 * @param m the message received from the client
	 * @return a message containing the registration result
	 */
	private Message handleRegisterSubscriber(Message m) {
		if (!(m.getData() instanceof RegisterSubscriberRequest)) {
			OperationResponse response =
					new OperationResponse(false, "Invalid subscriber registration request", null);

			return new Message(response, Protocol.REGISTER_SUBSCRIBER_RESPONSE);
		}

		RegisterSubscriberRequest request = (RegisterSubscriberRequest) m.getData();

		try {
			if (sc.isUsernameExists(request.getUsername())) {
				OperationResponse response =
						new OperationResponse(false, "Username already exists.", null);

				return new Message(response, Protocol.REGISTER_SUBSCRIBER_RESPONSE);
			}

			if (sc.isIdNumberExists(request.getIdNumber())) {
				OperationResponse response =
						new OperationResponse(false, "ID number already exists.", null);

				return new Message(response, Protocol.REGISTER_SUBSCRIBER_RESPONSE);
			}

			sc.registerSubscriber(request);

			OperationResponse response =
					new OperationResponse(true, "Subscriber registered successfully.", null);

			return new Message(response, Protocol.REGISTER_SUBSCRIBER_RESPONSE);

		} catch (SQLException e) {
			e.printStackTrace();

			OperationResponse response =
					new OperationResponse(false, "Database error while registering subscriber.", null);

			return new Message(response, Protocol.REGISTER_SUBSCRIBER_RESPONSE);
		}
	}

	/**
	 * Handles a report request.
	 *
	 * The method validates the report request, checks employee permissions, loads the
	 * matching report data, and returns it to the client.
	 *
	 * @param m the client message containing a ReportRequest
	 * @return a message containing the report response
	 */
	private Message handleGetReport(Message m) {
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

			return new Message(response, Protocol.GET_REPORT_RESPONSE);

		} catch (SQLException e) {
			e.printStackTrace();

			OperationResponse response =
					new OperationResponse(false, "Database error while loading report", null);

			return new Message(response, Protocol.GET_REPORT_RESPONSE);

		} catch (Exception e) {
			e.printStackTrace();

			OperationResponse response =
					new OperationResponse(false, "Failed to load report", null);

			return new Message(response, Protocol.GET_REPORT_RESPONSE);
		}
	}

	/**
	 * Validates the basic fields of a report request.
	 *
	 * @param request the report request to validate
	 * @return true if the request is valid, otherwise false
	 * @throws SQLException if checking the requested park fails
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
	 * Checks whether an employee is allowed to view a requested report.
	 *
	 * Department managers may view all reports. Park managers may view only allowed
	 * report types for their own park.
	 *
	 * @param request the report request to check
	 * @return true if the employee is allowed to view the report, otherwise false
	 * @throws SQLException if employee permission data cannot be loaded
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
	 * Checks whether a report type is allowed for park managers.
	 *
	 * @param reportType the report type to check
	 * @return true if park managers may view this report type, otherwise false
	 */
	private boolean isReportAllowedForParkManager(String reportType) {
		return REPORT_TYPE_VISITOR.equals(reportType)
				|| REPORT_TYPE_CANCELLATION.equals(reportType);
	}

	/**
	 * Handles a request to create a park parameter change request.
	 *
	 * The method validates the request data, checks park manager permissions, reads
	 * the current park value, and creates a pending change request.
	 *
	 * @param m the client message containing the request data
	 * @return a message indicating whether the request was created successfully
	 */
	private Message handleCreateParkParameterChangeRequest(Message m) {
		try {
			if (!(m.getData() instanceof Object[])) {
				OperationResponse response =
						new OperationResponse(false,
								"Invalid park parameter change request data",
								null);

				return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
			}

			Object[] data = (Object[]) m.getData();

			int parkId = (int) data[0];
			int requestedByEmployeeId = (int) data[1];
			String parameterName = data[2] == null ? null : data[2].toString().trim();
			String newValue = data[3] == null ? null : data[3].toString().trim();

			if (parameterName == null || parameterName.isBlank()
					|| newValue == null || newValue.isBlank()) {

				OperationResponse response =
						new OperationResponse(false,
								"Missing park parameter change details",
								null);

				return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
			}

			if (!ec.isParkManager(requestedByEmployeeId)) {
				OperationResponse response =
						new OperationResponse(false,
								"Only park managers can request park parameter changes",
								null);

				return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
			}

			int employeeParkId = ec.getEmployeeParkId(requestedByEmployeeId);

			if (employeeParkId != parkId) {
				OperationResponse response =
						new OperationResponse(false,
								"Park manager can request changes only for his own park",
								null);

				return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
			}

			Park park = pc.getFullParkById(parkId);

			if (park == null) {
				OperationResponse response =
						new OperationResponse(false, "Park was not found", null);

				return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
			}

			String oldValue = getCurrentParkParameterValue(park, parameterName);

			if (oldValue == null) {
				OperationResponse response =
						new OperationResponse(false, "Unknown park parameter", null);

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
				OperationResponse response =
						new OperationResponse(false,
								"Failed to create park parameter change request",
								null);

				return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
			}

			OperationResponse response =
					new OperationResponse(true,
							"Park parameter change request was created successfully",
							null);

			return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_CREATED);

		} catch (SQLException e) {
			e.printStackTrace();

			OperationResponse response =
					new OperationResponse(false,
							"Database error while creating park parameter change request",
							null);

			return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);

		} catch (Exception e) {
			e.printStackTrace();

			OperationResponse response =
					new OperationResponse(false,
							"Failed to create park parameter change request",
							null);

			return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
		}
	}

	/**
	 * Returns the current value of a supported park parameter.
	 *
	 * @param park the park whose parameter value should be read
	 * @param parameterName the parameter name to read
	 * @return the current parameter value as text, or null if unsupported
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
	 * Handles a request to load pending park parameter change requests.
	 *
	 * Only department managers are allowed to view pending requests.
	 *
	 * @param m the client message containing the employee ID
	 * @return a message containing the pending requests or a failure response
	 */
	private Message handleGetPendingParkParameterChangeRequests(Message m) {
		try {
			if (!(m.getData() instanceof Integer)) {
				OperationResponse response =
						new OperationResponse(false, "Invalid employee id", null);

				return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
			}

			int employeeId = (Integer) m.getData();

			if (!ec.isDepartmentManager(employeeId)) {
				OperationResponse response =
						new OperationResponse(false,
								"Only department managers can view pending parameter change requests",
								null);

				return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
			}

			List<ParkParameterChangeRequest> requests = pcrc.getPendingRequests();

			OperationResponse response =
					new OperationResponse(true,
							"Pending park parameter change requests loaded successfully",
							requests);

			return new Message(response, Protocol.PENDING_PARK_PARAMETER_CHANGE_REQUESTS_RESULT);

		} catch (SQLException e) {
			e.printStackTrace();

			OperationResponse response =
					new OperationResponse(false,
							"Database error while loading pending requests",
							null);

			return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);

		} catch (Exception e) {
			e.printStackTrace();

			OperationResponse response =
					new OperationResponse(false, "Failed to load pending requests", null);

			return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
		}
	}

	/**
	 * Handles a department manager request to approve a park parameter change request.
	 *
	 * @param m the client message containing approval data
	 * @return a message indicating whether the request was approved successfully
	 */
	private Message handleApproveParkParameterChangeRequest(Message m) {
		try {
			if (!(m.getData() instanceof Object[])) {
				OperationResponse response =
						new OperationResponse(false, "Invalid approval request data", null);

				return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
			}

			Object[] data = (Object[]) m.getData();

			int requestId = (int) data[0];
			int approvedByEmployeeId = (int) data[1];
			String reviewNote = data.length > 2 && data[2] != null
					? data[2].toString()
					: "";

			if (!ec.isDepartmentManager(approvedByEmployeeId)) {
				OperationResponse response =
						new OperationResponse(false,
								"Only department managers can approve park parameter change requests",
								null);

				return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
			}

			boolean approved = pcrc.approveRequest(requestId, approvedByEmployeeId, reviewNote);

			if (approved) {
				notifyParksUpdated();

				OperationResponse response =
						new OperationResponse(true,
								"Park parameter change request approved",
								requestId);

				return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_APPROVED);
			}

			OperationResponse response =
					new OperationResponse(false,
							"Park parameter change request approval failed",
							requestId);

			return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);

		} catch (SQLException e) {
			e.printStackTrace();

			OperationResponse response =
					new OperationResponse(false, e.getMessage(), null);

			return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);

		} catch (Exception e) {
			e.printStackTrace();

			OperationResponse response =
					new OperationResponse(false, e.getMessage(), null);

			return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
		}
	}

	/**
	 * Handles a department manager request to reject a park parameter change request.
	 *
	 * @param m the client message containing rejection data
	 * @return a message indicating whether the request was rejected successfully
	 */
	private Message handleRejectParkParameterChangeRequest(Message m) {
		try {
			if (!(m.getData() instanceof Object[])) {
				OperationResponse response =
						new OperationResponse(false, "Invalid rejection request data", null);

				return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
			}

			Object[] data = (Object[]) m.getData();

			int requestId = (int) data[0];
			int reviewedByEmployeeId = (int) data[1];
			String reviewNote = data.length > 2 && data[2] != null
					? data[2].toString()
					: "";

			if (!ec.isDepartmentManager(reviewedByEmployeeId)) {
				OperationResponse response =
						new OperationResponse(false,
								"Only department managers can reject park parameter change requests",
								null);

				return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
			}

			boolean rejected = pcrc.rejectRequest(requestId, reviewedByEmployeeId, reviewNote);

			if (rejected) {
				OperationResponse response =
						new OperationResponse(true,
								"Park parameter change request rejected",
								requestId);

				return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_REJECTED);
			}

			OperationResponse response =
					new OperationResponse(false,
							"Park parameter change request rejection failed",
							requestId);

			return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);

		} catch (SQLException e) {
			e.printStackTrace();

			OperationResponse response =
					new OperationResponse(false, e.getMessage(), null);

			return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);

		} catch (Exception e) {
			e.printStackTrace();

			OperationResponse response =
					new OperationResponse(false, e.getMessage(), null);

			return new Message(response, Protocol.PARK_PARAMETER_CHANGE_REQUEST_FAILURE);
		}
	}

	/**
	 * Handles a request to calculate the entry price for an order.
	 *
	 * @param m the client message containing an EntryPriceRequest
	 * @return a message containing the calculated receipt or a failure response
	 */
	private Message handleCalculateEntryPrice(Message m) {
		try {
			if (!(m.getData() instanceof EntryPriceRequest)) {
				OperationResponse response =
						new OperationResponse(false, "Invalid entry price request", null);

				return new Message(response, Protocol.CALCULATE_ENTRY_PRICE_RESPONSE);
			}

			EntryPriceRequest request = (EntryPriceRequest) m.getData();

			EntryPriceReceipt receipt =
					bc.calculateReceiptByOrderNumber(request.getOrderNumber());

			OperationResponse response =
					new OperationResponse(true,
							"Entry price calculated successfully",
							receipt);

			return new Message(response, Protocol.CALCULATE_ENTRY_PRICE_RESPONSE);

		} catch (SQLException e) {
			e.printStackTrace();

			OperationResponse response =
					new OperationResponse(false, e.getMessage(), null);

			return new Message(response, Protocol.CALCULATE_ENTRY_PRICE_RESPONSE);

		} catch (Exception e) {
			e.printStackTrace();

			OperationResponse response =
					new OperationResponse(false, "Failed to calculate entry price", null);

			return new Message(response, Protocol.CALCULATE_ENTRY_PRICE_RESPONSE);
		}
	}

	/**
	 * Handles a request to load park visitor counters.
	 *
	 * The returned counters depend on the employee role and permissions.
	 *
	 * @param m the client message containing the employee ID
	 * @return a message containing the available visitor counters
	 */
	private Message handleGetParkVisitorCounters(Message m) {
		try {
			if (!(m.getData() instanceof Integer)) {
				OperationResponse response =
						new OperationResponse(false, "Invalid employee id", null);

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
				OperationResponse response =
						new OperationResponse(false,
								"Only park managers and department managers can view park visitor counters",
								null);

				return new Message(response, Protocol.PARK_VISITOR_COUNTERS_RESULT);
			}

			OperationResponse response =
					new OperationResponse(true,
							"Park visitor counters loaded successfully",
							counters);

			return new Message(response, Protocol.PARK_VISITOR_COUNTERS_RESULT);

		} catch (SQLException e) {
			e.printStackTrace();

			OperationResponse response =
					new OperationResponse(false,
							"Database error while loading park visitor counters",
							null);

			return new Message(response, Protocol.PARK_VISITOR_COUNTERS_RESULT);

		} catch (Exception e) {
			e.printStackTrace();

			OperationResponse response =
					new OperationResponse(false,
							"Failed to load park visitor counters",
							null);

			return new Message(response, Protocol.PARK_VISITOR_COUNTERS_RESULT);
		}
	}

	/**
	 * Handles a request to update a park visitor counter.
	 *
	 * The method validates employee permissions, updates the park counter, and
	 * notifies connected clients that the counters were updated.
	 *
	 * @param m the client message containing a ParkVisitorCounterUpdateRequest
	 * @return a message containing the update result
	 */
	private Message handleUpdateParkVisitorCounter(Message m) {
		try {
			if (!(m.getData() instanceof ParkVisitorCounterUpdateRequest)) {
				OperationResponse response =
						new OperationResponse(false,
								"Invalid park visitor counter update request",
								null);

				return new Message(response, Protocol.PARK_VISITOR_COUNTER_UPDATE_RESULT);
			}

			ParkVisitorCounterUpdateRequest request =
					(ParkVisitorCounterUpdateRequest) m.getData();

			if (!canEmployeeUpdateParkVisitorCounter(
					request.getEmployeeId(),
					request.getParkId())) {

				OperationResponse response =
						new OperationResponse(false,
								"Employee is not allowed to update this park visitor counter",
								null);

				return new Message(response, Protocol.PARK_VISITOR_COUNTER_UPDATE_RESULT);
			}

			boolean updated = pc.updateCurrentVisitors(
					request.getParkId(),
					request.getEmployeeId(),
					request.getActionType(),
					request.getAmount()
			);

			if (!updated) {
				OperationResponse response =
						new OperationResponse(false,
								"Failed to update park visitor counter",
								null);

				return new Message(response, Protocol.PARK_VISITOR_COUNTER_UPDATE_RESULT);
			}

			ParkVisitorCounterSnapshot updatedCounter =
					pc.getParkVisitorCounter(request.getParkId());

			OperationResponse response =
					new OperationResponse(true,
							"Park visitor counter updated successfully",
							updatedCounter);

			notifyParkVisitorCountersUpdated();

			return new Message(response, Protocol.PARK_VISITOR_COUNTER_UPDATE_RESULT);

		} catch (SQLException e) {
			e.printStackTrace();

			OperationResponse response =
					new OperationResponse(false, e.getMessage(), null);

			return new Message(response, Protocol.PARK_VISITOR_COUNTER_UPDATE_RESULT);

		} catch (Exception e) {
			e.printStackTrace();

			OperationResponse response =
					new OperationResponse(false,
							"Failed to update park visitor counter",
							null);

			return new Message(response, Protocol.PARK_VISITOR_COUNTER_UPDATE_RESULT);
		}
	}

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
	 * Checks whether the details of a given order are valid before booking.
	 *
	 * @param o the order to check
	 * @return true if the order details are valid, otherwise false
	 */
	private boolean checkOrderDetailsAreValid(Order o) {
		if (o == null) {
			return false;
		}

		if (o.getOrderDate() == null
				|| o.getVisitorNumber() == null
				|| o.getOrderStatus() == null) {
			return false;
		}

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

		return ORDER_STATUS_PENDING.equals(o.getOrderStatus());
	}

	/**
	 * Handles a service representative request to load all orders.
	 *
	 * @return a message containing all orders or a null data response on failure
	 */
	private Message handleGetAllOrdersForServiceRepresentative() {
		try {
			List<Order> orders = oc.getAllOrders();

			return new Message(orders, Protocol.GET_ALL_ORDERS_RESPONSE);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to load all customer orders: " + e.getMessage());

			return new Message(null, Protocol.GET_ALL_ORDERS_RESPONSE);
		}
	}

	/**
	 * Closes all database connectors and releases pooled database connections.
	 */
	private void closeDBConnection() {
		try {
			if (oc != null) {
				oc.close();
			}

			if (pc != null) {
				pc.close();
			}

			if (pcrc != null) {
				pcrc.close();
			}

			if (ec != null) {
				ec.close();
			}

			if (sc != null) {
				sc.close();
			}

			if (gc != null) {
				gc.close();
			}

			if (wlc != null) {
				wlc.close();
			}

			if (vc != null) {
				vc.close();
			}

			if (rc != null) {
				rc.close();
			}

			if (bc != null) {
				bc.close();
			}

			DBConnectionPool.getInstance().closeAllConnections();

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to close database connection: " + e.getMessage());
		}
	}

	/**
	 * Closes all relevant server resources safely.
	 *
	 * The method disconnects connected clients, stops listening for new clients,
	 * closes the server, releases database connections, and shuts down the timer
	 * controller.
	 */
	@Override
	public void closeServer() {
		try {
			addLog("Closing server.");

			server.sendToAllClients(new Message(null, Protocol.CLIENT_DISCONNECT_SERVER));
			server.stopListening();
			server.close();

		} catch (IOException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to close server: " + e.getMessage());
		} finally {
			closeDBConnection();
			if(tc != null)
				tc.shutDown();
		}
	}

	/**
	 * Handles a park check-in request using a confirmation code.
	 *
	 * If the confirmation code and order details are valid, the method creates a new
	 * visit record and returns the updated park visitor count.
	 *
	 * @param m the client message containing a ParkEntranceMessage
	 * @return a success or failure message for the check-in action
	 */
	private Message handleCheckInOrder(Message m) {
		if (!(m.getData() instanceof ParkEntranceMessage)) {
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

			String failureMessage = getCheckInFailureMessage(visitId);

			if (failureMessage != null) {
				entranceMessage.setResponseMessage(failureMessage);
				return new Message(entranceMessage, Protocol.CHECK_IN_ORDER_FAILURE);
			}

			int currentVisitors = vc.getCurrentVisitorsInPark(entranceMessage.getParkId());

			entranceMessage.setVisitId(visitId);
			entranceMessage.setCurrentVisitors(currentVisitors);
			entranceMessage.setResponseMessage(
					"Check-in completed successfully. Visit ID: " + visitId
			);

			return new Message(entranceMessage, Protocol.CHECK_IN_ORDER_SUCCESS);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to check in order: " + e.getMessage());

			entranceMessage.setResponseMessage("Server error while checking in order.");
			return new Message(entranceMessage, Protocol.CHECK_IN_ORDER_FAILURE);
		}
	}

	/**
	 * Converts a check-in failure code into a user-facing message.
	 *
	 * @param visitId the visit ID or negative failure code returned by the visit connector
	 * @return a failure message, or null if the code represents success
	 */
	private String getCheckInFailureMessage(int visitId) {
		switch (visitId) {

		case -1:
			return "No order was found for this confirmation code.";

		case -2:
			return "This order already has an open visit.";

		case -3:
			return "The entered number of visitors is greater than the number in the order.";

		case -4:
			return "This order does not belong to the park assigned to the logged-in employee.";

		case -5:
			return "This order is not approved and cannot be used for park entrance.";

		case -6:
			return "This order has already been completed.";

		case -7:
			return "This order is not valid for the current date and time.";

		default:
			return null;
		}
	}

	/**
	 * Handles a park check-out request.
	 *
	 * The method first tries to close the visit by confirmation code and then by
	 * visit ID, so it can support both ordered and occasional visits.
	 *
	 * @param m the client message containing a ParkEntranceMessage
	 * @return a success or failure message for the check-out action
	 */
	private Message handleCheckOutVisit(Message m) {
		if (!(m.getData() instanceof ParkEntranceMessage)) {
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
				visitId = vc.closeVisitByVisitId(
						entranceMessage.getVisitId(),
						entranceMessage.getParkId(),
						entranceMessage.getEmployeeId()
				);
			}

			if (visitId == -1) {
				entranceMessage.setResponseMessage("No open visit was found for this confirmation code or visit ID.");
				return new Message(entranceMessage, Protocol.CHECK_OUT_VISIT_FAILURE);
			}

			int currentVisitors = vc.getCurrentVisitorsInPark(entranceMessage.getParkId());

			entranceMessage.setVisitId(visitId);
			entranceMessage.setCurrentVisitors(currentVisitors);
			entranceMessage.setResponseMessage("Check-out completed successfully. Visit ID: " + visitId);

			return new Message(entranceMessage, Protocol.CHECK_OUT_VISIT_SUCCESS);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to check out visit: " + e.getMessage());

			entranceMessage.setResponseMessage("Server error while checking out visit.");
			return new Message(entranceMessage, Protocol.CHECK_OUT_VISIT_FAILURE);
		}
	}

	/**
	 * Handles an occasional visit request.
	 *
	 * The method checks park capacity and creates a visit for visitors who arrive
	 * without a reservation.
	 *
	 * @param m the client message containing a ParkEntranceMessage
	 * @return a success or failure message for the occasional visit action
	 */
	private Message handleOccasionalVisit(Message m) {
		if (!(m.getData() instanceof ParkEntranceMessage)) {
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
				return new Message(entranceMessage, Protocol.OCCASIONAL_VISIT_FAILURE);
			}

			int currentVisitors = vc.getCurrentVisitorsInPark(entranceMessage.getParkId());

			entranceMessage.setVisitId(visitId);
			entranceMessage.setCurrentVisitors(currentVisitors);
			entranceMessage.setResponseMessage("Occasional visit created successfully. Visit ID: " + visitId);

			return new Message(entranceMessage, Protocol.OCCASIONAL_VISIT_SUCCESS);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to create occasional visit: " + e.getMessage());

			entranceMessage.setResponseMessage("Server error while creating occasional visit.");
			return new Message(entranceMessage, Protocol.OCCASIONAL_VISIT_FAILURE);
		}
	}

	/**
	 * Handles a request for the current number of visitors inside a park.
	 *
	 * @param m the client message containing a ParkEntranceMessage
	 * @return a success or failure message with the current visitors count
	 */
	private Message handleGetCurrentVisitors(Message m) {
		if (!(m.getData() instanceof ParkEntranceMessage)) {
			return new Message(null, Protocol.GET_CURRENT_VISITORS_FAILURE);
		}

		ParkEntranceMessage entranceMessage = (ParkEntranceMessage) m.getData();

		try {
			int currentVisitors = vc.getCurrentVisitorsInPark(entranceMessage.getParkId());

			entranceMessage.setCurrentVisitors(currentVisitors);
			entranceMessage.setResponseMessage("Current visitors in park: " + currentVisitors);

			return new Message(entranceMessage, Protocol.GET_CURRENT_VISITORS_SUCCESS);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to get current visitors: " + e.getMessage());

			entranceMessage.setResponseMessage("Server error while loading current visitors.");
			return new Message(entranceMessage, Protocol.GET_CURRENT_VISITORS_FAILURE);
		}
	}
	
	/**
	 * Handles a service representative request to search user information.
	 * 
	 * The search supports customer lookup by ID and employee lookup by employee ID,
	 * employee number, or username.
	 * 
	 * @param m the client message containing the search value
	 * @return a message containing the user information search result
	 */
	private Message handleSearchUserInformation(Message m) {
		try {
			if (!(m.getData() instanceof String)) {
				OperationResponse response = new OperationResponse(
						false,
						"Invalid search data.",
						null
				);

				return new Message(response, Protocol.SEARCH_USER_INFORMATION_RESPONSE);
			}

			String searchValue = ((String) m.getData()).trim();

			if (searchValue.isEmpty()) {
				OperationResponse response = new OperationResponse(
						false,
						"Please enter a search value.",
						null
				);

				return new Message(response, Protocol.SEARCH_USER_INFORMATION_RESPONSE);
			}

			/*
			 * Employee search.
			 * The client sends EMPLOYEE|value so we can keep the same protocol
			 * without breaking the existing customer search.
			 */
			if (searchValue.startsWith("EMPLOYEE|")) {
				String employeeSearchValue = searchValue.substring("EMPLOYEE|".length()).trim();

				if (employeeSearchValue.isEmpty()) {
					OperationResponse response = new OperationResponse(
							false,
							"Please enter employee ID, employee number, or username.",
							null
					);

					return new Message(response, Protocol.SEARCH_USER_INFORMATION_RESPONSE);
				}

				addLog("Searching employee information for value: " + employeeSearchValue);

				String employeeInfo = ec.getEmployeeInformationText(employeeSearchValue);

				if (employeeInfo != null) {
					OperationResponse response = new OperationResponse(
							true,
							"Employee information found.",
							employeeInfo
					);

					addLog("Employee information found for value: " + employeeSearchValue);

					return new Message(response, Protocol.SEARCH_USER_INFORMATION_RESPONSE);
				}

				OperationResponse response = new OperationResponse(
						false,
						"No employee was found for this search value.",
						null
				);

				addLog("No employee information found for value: " + employeeSearchValue);

				return new Message(response, Protocol.SEARCH_USER_INFORMATION_RESPONSE);
			}

			/*
			 * Customer search.
			 */
			String userIdNumber = searchValue;

			if (!userIdNumber.matches("\\d+")) {
				OperationResponse response = new OperationResponse(
						false,
						"Customer ID must contain digits only.",
						null
				);

				return new Message(response, Protocol.SEARCH_USER_INFORMATION_RESPONSE);
			}

			addLog("Searching customer information for ID: " + userIdNumber);

			int numericId = Integer.parseInt(userIdNumber);

			/*
			 * First: check whether the user is a subscriber.
			 */
			String subscriberInfo = sc.getSubscriberInformationTextById(numericId);

			if (subscriberInfo != null) {
				OperationResponse response = new OperationResponse(
						true,
						"Subscriber information found.",
						subscriberInfo
				);

				addLog("Subscriber information found for ID: " + userIdNumber);

				return new Message(response, Protocol.SEARCH_USER_INFORMATION_RESPONSE);
			}

			/*
			 * Second: if not a subscriber, check whether this ID has occasional orders.
			 */
			ArrayList<Order> occasionalOrders = oc.getOrdersByCustomerIdNumber(userIdNumber);

			if (occasionalOrders != null && !occasionalOrders.isEmpty()) {
				String occasionalInfo =
						"User Type: Occasional Customer\n"
						+ "ID Number: " + userIdNumber + "\n"
						+ "Existing Orders Count: " + occasionalOrders.size() + "\n"
						+ "Status: Exists in the system through previous orders.";

				OperationResponse response = new OperationResponse(
						true,
						"Occasional customer information found.",
						occasionalInfo
				);

				addLog("Occasional customer information found for ID: " + userIdNumber);

				return new Message(response, Protocol.SEARCH_USER_INFORMATION_RESPONSE);
			}

			OperationResponse response = new OperationResponse(
					false,
					"No subscriber or occasional customer was found for this ID.",
					null
			);

			addLog("No customer information found for ID: " + userIdNumber);

			return new Message(response, Protocol.SEARCH_USER_INFORMATION_RESPONSE);

		} catch (SQLException e) {
			e.printStackTrace();

			addLog("ERROR - Database error while searching user information: " + e.getMessage());

			OperationResponse response = new OperationResponse(
					false,
					"Database error while searching user information.",
					null
			);

			return new Message(response, Protocol.SEARCH_USER_INFORMATION_RESPONSE);
		}
	}
	/**
	 * Stores pending order reminders by user ID.
	 * 
	 * A ConcurrentHashMap is used because the reminders collection may be accessed
	 * by different server and timer threads. Each user receives a thread-safe queue
	 * that keeps reminder insertion order.
	 */
	ConcurrentHashMap<String, ConcurrentLinkedQueue<Order>> remindersByUser = new ConcurrentHashMap<>();
	
	/**
	 * Adds order reminders for orders matching a target date, hour, and status.
	 *
	 * The method stores reminders by user, simulates sending email or phone messages,
	 * and immediately sends reminders to users who are currently connected.
	 *
	 * @param date the target order date
	 * @param hour the target order hour
	 * @param status the target order status
	 */
	public void addReminder(LocalDate date, int hour, String status) {
		List<Order> newReminders = new ArrayList<>();
		try {
			addLog("Setting reminder for orders. Date: " + date + " hour: " + hour);
			newReminders = oc.getOrdersByDateAndHourAndStatus(date, hour, status);
		} catch(Exception e) {
			addLog("Error with adding new reminders: " + e.getMessage());
		}
		
		Set<String> newUsersToRemind = new HashSet<>();
		
		//simulate messages
		String id;
		addLog("Sending reminders start (email / phone)");
		for(Order o : newReminders) {
			id = o.getUserId().toString();
			newUsersToRemind.add(id);
			remindersByUser.computeIfAbsent(id, key -> new ConcurrentLinkedQueue<>()).add(o);
			String phoneLog = o.getPhoneNumber() == null ? "" : (", " + o.getPhoneNumber());
			addLog("SIMULATION! Sending reminder messages to: " + o.getEmail() +
					phoneLog + " regarding order number: " + o.getOrderId());
		}
		addLog("Sending reminders end  (email / phone)");
		
		sendRemindersToConnectedUsers(newUsersToRemind);
	}
	
	/**
	 * Sends reminders to all relevant users who are currently connected.
	 *
	 * @param newUsersToRemind the set of user IDs that should receive reminders
	 */
	private void sendRemindersToConnectedUsers(Set<String> newUsersToRemind) {
		if(newUsersToRemind == null || newUsersToRemind.isEmpty())
			return;
		for(String id : newUsersToRemind) {
			if(!server.isUserConnected(id)) {
				addLog("User " + id + " is not connected, cannot send reminder");
				continue;
			}
			remindConnectedUser(id);	
		}
	}
	
	/**
	 * Sends all non-expired reminders for a specific connected user.
	 *
	 * @param id the user ID to remind
	 */
	private void remindConnectedUser(String id) {
		Queue<Order> orders = remindersByUser.get(id);
		if(orders == null || orders.isEmpty())
			return;
		
		for(Order o : orders) {
			if (isExpired(o)) {
			    continue;
			}
			int ret = server.sendReminderToUser(id, new Message(o, Protocol.ORDER_REMINDER));
			if(ret == -1) {
				addLog("Unknown error occurred trying to send reminder to user " + id);
			}
			else if(ret == 1) {
				addLog("Reminder sent to user " + id + " successfully");
			}
		}
	}
	
	/**
	 * Checks whether an order reminder is already expired according to the timer.
	 *
	 * @param o the order to check
	 * @return true if the order is expired, otherwise false
	 */
	private boolean isExpired(Order o) {
	    LocalDate expiredDate = tc.getExpiredDate();
	    int expiredHour = tc.getExpiredHourAsInt();

	    return o.getOrderDate().isBefore(expiredDate)
	        || (o.getOrderDate().equals(expiredDate)
	            && o.getOrderHour() <= expiredHour);
	}
	
	
	/**
	 * Removes an order from the reminder map.
	 *
	 * If the user's reminder queue becomes empty, the user entry is removed from the
	 * map as well.
	 *
	 * @param o the order to remove
	 * @return true if the order was removed, otherwise false
	 */
	private boolean removeFromMap(Order o) {
		Queue<Order> orders = remindersByUser.get(o.getUserId().toString());
		if (orders != null) {
	        orders.remove(o);

	        if (orders.isEmpty()) {
	            remindersByUser.remove(o.getUserId().toString(), orders);
	        }
	        return true;
	    }
		return false;
	}
	
	/**
	 * Checks whether a connected user has pending reminders and sends them if needed.
	 *
	 * @param id the user's ID
	 */
	public void checkForUserReminder(String id) {
		if(!remindersByUser.containsKey(id))
			return;
		remindConnectedUser(id);
	}
	
	/**
	 * Removes expired reminders and auto-cancels matching orders when needed.
	 *
	 * The method removes reminders up to the target date and hour, logs simulated
	 * cancellation messages, and updates matching orders in the database.
	 *
	 * @param date the target order date
	 * @param hour the target order hour
	 * @param status the target order status
	 */
	public void removeReminder(LocalDate date, int hour, String status) {
		boolean toRemove = false;
		
		String id;
		ConcurrentLinkedQueue<Order> orders;
		Order o;
		for(Map.Entry<String, ConcurrentLinkedQueue<Order>> entry : remindersByUser.entrySet()) {
			id = entry.getKey();
			orders = entry.getValue();
			
			while(!orders.isEmpty()) {
				o = orders.peek();
				if(o.getOrderDate().isAfter(date) ||
						   (o.getOrderDate().equals(date) && o.getOrderHour() > hour))
					break;
				if(o.getOrderDate().equals(date) && o.getOrderHour() == hour &&
						o.getOrderStatus().equals(status)) {
					toRemove = true;
					addLog("SIMULATION! Order auto cancelled: " + 
							o.getEmail() + ", " + o.getPhoneNumber() +
							" regarding order number: " + o.getOrderId());
				}
				orders.poll();
			}
			
			if (orders.isEmpty()) {
	            remindersByUser.remove(id, orders);
	        }
		}
		
		try {
			addLog("Auto cancelling orders with expired reminders.");
			if(toRemove)
				oc.autoCancelOrderList(date, hour, status);
		} catch(Exception e) {
			addLog("Error with auto cancelling orders: " + e.getMessage());
		}
	}
	
	/**
	 * Updates old orders with the given status to no-show when their visit time passed.
	 *
	 * @param status the status of orders that should be checked
	 */
	public void updateNoShows(String status) {
		try {
			addLog("Updating orders to no-shows.");
			addLog(oc.updateOrdersToNoShowsAccordingToStatus(status));
		} catch(Exception e) {
			addLog("Error with updating orders to no-shows: " + e.getMessage());
		}
	}
	
	/**
	 * Handles a client confirmation for an order reminder.
	 *
	 * The method removes the order from the reminders map and returns a confirmation
	 * response to the client.
	 *
	 * @param m the client message containing the reminded Order
	 * @return a confirmation or error message for the reminder action
	 */
	private Message handleAcceptOrderReminder(Message m) {
		boolean removedOnAccept = removeFromMap((Order) m.getData());
		if(removedOnAccept) {
	        addLog("Order acceptance confirmed, Order id: " + ((Order) m.getData()).getUserId());
	        return new Message((Order) m.getData(), Protocol.ACCEPT_ORDER_REMINDER_CONFIRMATION);
	    }
		addLog("Order acceptance failed, Order id: " + ((Order) m.getData()).getOrderId());
		return new Message((Order) m.getData(), Protocol.ERROR_ORDER_REMINDER_CONFIRMATION);
	}
	
	/**
	 * Handles a client decline for an order reminder.
	 *
	 * The method removes the order from the reminders map, cancels the related order,
	 * and returns a confirmation or error response.
	 *
	 * @param m the client message containing the reminded Order
	 * @return a confirmation or error message for the reminder action
	 */
	private Message handleDeclineOrderReminder(Message m) {
		if(!(m.getData() instanceof Order)) {
			System.out.println("Unknown error declining reminder");
			return null;
		}
		Order o = (Order) m.getData();
		boolean removedOnDecline = removeFromMap(o);
		CancelOrderMessage cancelOrderMessage = new 
				CancelOrderMessage(o.getOrderId(), -1, o.getUserId().toString(), "Reminder");
		cancelOrder(cancelOrderMessage);
		System.out.println("done with cancelOrder");
		if(removedOnDecline) {
			addLog("Order cancellation confirmed, Order id: " + o.getUserId());
			return new Message(m.getData(), Protocol.DECLINE_ORDER_REMINDER_CONFIRMATION);
		}
		addLog("Order cancellation failed, Order id: " + o.getOrderId());
		return new Message((Order) m.getData(), Protocol.ERROR_ORDER_REMINDER_CONFIRMATION);
	}
}
