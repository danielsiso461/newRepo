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
import databaseControllers.VisitConnection;
import databaseControllers.WaitingListConnection;
import server.Server;
import serverCommon.ServerAndControllerConnection;
import serverCommon.User;
import serverGUI.ClientConnectionTableController;

/**
 * This class connects the networking part of the server and the server GUI.
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

	private int allTimeUserCount = 1;

	private ClientConnectionTableController serverGUIController;

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
			server.listen();
			addLog("Server started listening for clients.");
		} catch (Exception ex) {
			ex.printStackTrace();
			addLog("ERROR - Could not listen for clients: " + ex.getMessage());
		}
	}

	public void addLog(String message) {
		if (serverGUIController != null) {
			serverGUIController.addLog(message);
		}
	}

	@Override
	public void presentServerConnection(String hostName, String ip) {
		if (serverGUIController != null) {
			serverGUIController.setLabels(hostName, ip);
		}

		addLog("Server connection details updated. Host: " + hostName + ", IP: " + ip);
	}

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

		default:
			addLog("ERROR - Unknown client request: " + type);
			return null;
		}
	}

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

	private Message handleCancelOrder(Message m) {
		if (!(m.getData() instanceof CancelOrderMessage)) {
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

			return new Message(cancelOrderMessage, Protocol.CANCEL_ORDER_FAILURE);

		} catch (SQLException e) {
			e.printStackTrace();
			addLog("ERROR - Failed to cancel order: " + e.getMessage());

			return new Message(cancelOrderMessage, Protocol.CANCEL_ORDER_FAILURE);
		}
	}

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

			OperationResponse response =
					new OperationResponse(false, "No orders found for this ID number", null);

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

	private boolean isReportAllowedForParkManager(String reportType) {
		return REPORT_TYPE_VISITOR.equals(reportType)
				|| REPORT_TYPE_CANCELLATION.equals(reportType);
	}

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
		}
	}

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
}