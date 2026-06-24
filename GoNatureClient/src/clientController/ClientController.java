package clientController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import client.Client;
import clientCommon.*;
import common.*;
import javafx.application.Platform;

/**
 * Controls the communication between the client networking layer and the UI layer.
 * 
 * This class sends requests from the UI to the server and receives responses from
 * the server. It also uses the Observer pattern to notify the relevant UI
 * controllers when new data or operation results arrive.
 */
public class ClientController implements ChatIF {

	private Client client;
	private String id;
	private boolean userIssuedDisconnect = false;

	private Integer loggedInSubscriberId;

	private List<OrderObserver> orderObservers = new ArrayList<>();
	private List<ParkObserver> parkObservers = new ArrayList<>();
	private List<MakeOrderObserver> makeOrderObservers = new ArrayList<>();
	private List<WaitingListObserver> waitingListObservers = new ArrayList<>();
	private List<ParkEntranceObserver> parkEntranceObservers = new ArrayList<>();

	private List<OccasionalCustomerAccessObserver> occasionalCustomerAccessObservers = new ArrayList<>();
	private List<EmployeeLoginObserver> employeeLoginObservers = new ArrayList<>();
	private List<ExistingCustomerLoginObserver> existingCustomerLoginObservers = new ArrayList<>();
	private List<RegisterSubscriberObserver> registerSubscriberObservers = new ArrayList<>();
	private List<SearchSubscriberObserver> searchSubscriberObservers = new ArrayList<>();
	private List<RegisterGuideObserver> registerGuideObservers = new ArrayList<>();

	private List<ReportObserver> reportObservers = new ArrayList<>();
	private List<ParkParameterObserver> parkParameterObservers = new ArrayList<>();
	private List<EntryPriceObserver> entryPriceObservers = new ArrayList<>();
	private List<ParkVisitorCounterObserver> parkVisitorCounterObservers = new ArrayList<>();

	/**
	 * Constructs a new ClientController and opens a connection to the server.
	 *
	 * @param host the server host address
	 * @param port the server port
	 * @param id   the ID of the current user
	 * @throws IOException if the client connection cannot be created
	 */
	public ClientController(String host, int port, String id) throws IOException {
		try {
			client = new Client(host, port, this);
		} catch (IOException exception) {
			throw exception;
		}

		this.id = id;
	}

	/**
	 * Returns the ID of the currently connected user.
	 * 
	 * @return the current user ID
	 */
	public String getId() {
		return id;
	}

	/**
	 * Updates the ID of the currently connected user.
	 * 
	 * @param id the current user's ID
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Sets the logged-in subscriber ID.
	 * 
	 * @param subscriberId the logged-in subscriber ID
	 */
	public void setLoggedInSubscriberId(Integer subscriberId) {
		this.loggedInSubscriberId = subscriberId;
	}

	/**
	 * Returns the logged-in subscriber ID.
	 * 
	 * @return the logged-in subscriber ID
	 */
	public Integer getLoggedInSubscriberId() {
		return loggedInSubscriberId;
	}

	/**
	 * Adds an order observer.
	 *
	 * @param observer the order observer to add
	 */
	public void addObserver(OrderObserver observer) {
		if (observer != null && !orderObservers.contains(observer)) {
			orderObservers.add(observer);
		}
	}

	/**
	 * Removes an order observer.
	 *
	 * @param observer the order observer to remove
	 */
	public void removeObserver(OrderObserver observer) {
		orderObservers.remove(observer);
	}

	private void notifyOrdersReceived(List<Order> rows) {
		for (OrderObserver observer : orderObservers) {
			observer.onOrdersReceived(rows);
		}
	}

	private void notifyOrderMade(Order order) {
		for (OrderObserver observer : orderObservers) {
			observer.addOrder(order);
		}
	}

	private void notifyUpdateResult(boolean success, UpdateMessage updateMessage) {
		for (OrderObserver observer : orderObservers) {
			observer.onUpdateResult(success, updateMessage);
		}
	}

	private void notifyCancelResult(boolean success, CancelOrderMessage cancelOrderMessage) {
		for (OrderObserver observer : orderObservers) {
			observer.onCancelResult(success, cancelOrderMessage);
		}
	}

	/**
	 * Adds a park observer.
	 *
	 * @param observer the park observer to add
	 */
	public void addParkObserver(ParkObserver observer) {
		if (observer != null && !parkObservers.contains(observer)) {
			parkObservers.add(observer);
		}
	}

	/**
	 * Removes a park observer.
	 *
	 * @param observer the park observer to remove
	 */
	public void removeParkObserver(ParkObserver observer) {
		parkObservers.remove(observer);
	}

	private void notifyParksReceived(List<Park> parks) {
		for (ParkObserver observer : parkObservers) {
			observer.onParksReceived(parks);
		}
	}

	/**
	 * Adds an occasional customer access observer.
	 *
	 * @param observer the occasional customer access observer to add
	 */
	public void addOccasionalCustomerAccessObserver(OccasionalCustomerAccessObserver observer) {
		if (observer != null && !occasionalCustomerAccessObservers.contains(observer)) {
			occasionalCustomerAccessObservers.add(observer);
		}
	}

	/**
	 * Removes an occasional customer access observer.
	 *
	 * @param observer the occasional customer access observer to remove
	 */
	public void removeOccasionalCustomerAccessObserver(OccasionalCustomerAccessObserver observer) {
		occasionalCustomerAccessObservers.remove(observer);
	}

	private void notifyOccasionalCustomerAccessResult(OperationResponse response) {
		for (OccasionalCustomerAccessObserver observer : occasionalCustomerAccessObservers) {
			observer.onOccasionalCustomerAccessResult(response);
		}
	}

	/**
	 * Adds a waiting-list observer.
	 *
	 * @param observer the waiting-list observer to add
	 */
	public void addWaitingListObserver(WaitingListObserver observer) {
		if (observer != null && !waitingListObservers.contains(observer)) {
			waitingListObservers.add(observer);
		}
	}

	/**
	 * Removes a waiting-list observer.
	 *
	 * @param observer the waiting-list observer to remove
	 */
	public void removeWaitingListObserver(WaitingListObserver observer) {
		waitingListObservers.remove(observer);
	}

	private void notifyJoinWaitingListResult(boolean success, WaitingListMessage waitingListMessage) {
		for (WaitingListObserver observer : waitingListObservers) {
			observer.onJoinWaitingListResult(success, waitingListMessage);
		}
	}

	private void notifyWaitingOffersReceived(boolean success, List<WaitingListMessage> offers) {
		for (WaitingListObserver observer : waitingListObservers) {
			observer.onWaitingOffersReceived(success, offers);
		}
	}

	private void notifyRejectWaitingOfferResult(boolean success, WaitingListMessage waitingListMessage) {
		for (WaitingListObserver observer : waitingListObservers) {
			observer.onRejectWaitingOfferResult(success, waitingListMessage);
		}
	}

	private void notifyAcceptWaitingOfferResult(boolean success, WaitingListMessage waitingListMessage) {
		for (WaitingListObserver observer : waitingListObservers) {
			observer.onAcceptWaitingOfferResult(success, waitingListMessage);
		}
	}

	/**
	 * Adds a park entrance observer.
	 *
	 * @param observer the park entrance observer to add
	 */
	public void addParkEntranceObserver(ParkEntranceObserver observer) {
		if (observer != null && !parkEntranceObservers.contains(observer)) {
			parkEntranceObservers.add(observer);
		}
	}

	/**
	 * Removes a park entrance observer.
	 *
	 * @param observer the park entrance observer to remove
	 */
	public void removeParkEntranceObserver(ParkEntranceObserver observer) {
		parkEntranceObservers.remove(observer);
	}

	private void notifyCheckInOrderResult(boolean success, ParkEntranceMessage parkEntranceMessage) {
		for (ParkEntranceObserver observer : parkEntranceObservers) {
			observer.onCheckInOrderResult(success, parkEntranceMessage);
		}
	}

	private void notifyCheckOutVisitResult(boolean success, ParkEntranceMessage parkEntranceMessage) {
		for (ParkEntranceObserver observer : parkEntranceObservers) {
			observer.onCheckOutVisitResult(success, parkEntranceMessage);
		}
	}

	private void notifyOccasionalVisitResult(boolean success, ParkEntranceMessage parkEntranceMessage) {
		for (ParkEntranceObserver observer : parkEntranceObservers) {
			observer.onOccasionalVisitResult(success, parkEntranceMessage);
		}
	}

	private void notifyCurrentVisitorsReceived(boolean success, ParkEntranceMessage parkEntranceMessage) {
		for (ParkEntranceObserver observer : parkEntranceObservers) {
			observer.onCurrentVisitorsReceived(success, parkEntranceMessage);
		}
	}

	/**
	 * Adds a make-order observer.
	 *
	 * @param observer the make-order observer to add
	 */
	public void addMakeOrderObserver(MakeOrderObserver observer) {
		if (observer != null && !makeOrderObservers.contains(observer)) {
			makeOrderObservers.add(observer);
		}
	}

	/**
	 * Removes a make-order observer.
	 *
	 * @param observer the make-order observer to remove
	 */
	public void removeMakeOrderObserver(MakeOrderObserver observer) {
		makeOrderObservers.remove(observer);
	}

	private void notifyParkNamesReceivedForMakeOrder(List<String> parkNames) {
		for (MakeOrderObserver observer : makeOrderObservers) {
			observer.onParkNamesReceived(parkNames);
		}
	}

	private void notifyMakeOrderServerResponse(Message message) {
		for (MakeOrderObserver observer : new ArrayList<>(makeOrderObservers)) {
			observer.onMakeOrderServerResponse(message);
		}
	}

	/**
	 * Adds an employee login observer.
	 *
	 * @param observer the employee login observer to add
	 */
	public void addEmployeeLoginObserver(EmployeeLoginObserver observer) {
		if (observer != null && !employeeLoginObservers.contains(observer)) {
			employeeLoginObservers.add(observer);
		}
	}

	/**
	 * Removes an employee login observer.
	 *
	 * @param observer the employee login observer to remove
	 */
	public void removeEmployeeLoginObserver(EmployeeLoginObserver observer) {
		employeeLoginObservers.remove(observer);
	}

	private void notifyEmployeeLoginResult(OperationResponse response) {
		for (EmployeeLoginObserver observer : employeeLoginObservers) {
			observer.onEmployeeLoginResult(response);
		}
	}

	/**
	 * Adds an existing customer login observer.
	 *
	 * @param observer the existing customer login observer to add
	 */
	public void addExistingCustomerLoginObserver(ExistingCustomerLoginObserver observer) {
		if (observer != null && !existingCustomerLoginObservers.contains(observer)) {
			existingCustomerLoginObservers.add(observer);
		}
	}

	/**
	 * Removes an existing customer login observer.
	 *
	 * @param observer the existing customer login observer to remove
	 */
	public void removeExistingCustomerLoginObserver(ExistingCustomerLoginObserver observer) {
		existingCustomerLoginObservers.remove(observer);
	}

	private void notifyExistingCustomerLoginResult(OperationResponse response) {
		for (ExistingCustomerLoginObserver observer : existingCustomerLoginObservers) {
			observer.onExistingCustomerLoginResult(response);
		}
	}

	/**
	 * Adds a register subscriber observer.
	 *
	 * @param observer the register subscriber observer to add
	 */
	public void addRegisterSubscriberObserver(RegisterSubscriberObserver observer) {
		if (observer != null && !registerSubscriberObservers.contains(observer)) {
			registerSubscriberObservers.add(observer);
		}
	}

	/**
	 * Removes a register subscriber observer.
	 *
	 * @param observer the register subscriber observer to remove
	 */
	public void removeRegisterSubscriberObserver(RegisterSubscriberObserver observer) {
		registerSubscriberObservers.remove(observer);
	}

	private void notifyRegisterSubscriberResult(OperationResponse response) {
		for (RegisterSubscriberObserver observer : registerSubscriberObservers) {
			observer.onRegisterSubscriberResult(response);
		}
	}

	/**
	 * Adds a search subscriber observer.
	 *
	 * @param observer the search subscriber observer to add
	 */
	public void addSearchSubscriberObserver(SearchSubscriberObserver observer) {
		if (observer != null && !searchSubscriberObservers.contains(observer)) {
			searchSubscriberObservers.add(observer);
		}
	}

	/**
	 * Removes a search subscriber observer.
	 *
	 * @param observer the search subscriber observer to remove
	 */
	public void removeSearchSubscriberObserver(SearchSubscriberObserver observer) {
		searchSubscriberObservers.remove(observer);
	}

	private void notifySearchSubscriberResult(OperationResponse response) {
		for (SearchSubscriberObserver observer : searchSubscriberObservers) {
			observer.onSearchSubscriberResult(response);
		}
	}

	/**
	 * Adds a register guide observer.
	 *
	 * @param observer the register guide observer to add
	 */
	public void addRegisterGuideObserver(RegisterGuideObserver observer) {
		if (observer != null && !registerGuideObservers.contains(observer)) {
			registerGuideObservers.add(observer);
		}
	}

	/**
	 * Removes a register guide observer.
	 *
	 * @param observer the register guide observer to remove
	 */
	public void removeRegisterGuideObserver(RegisterGuideObserver observer) {
		registerGuideObservers.remove(observer);
	}

	private void notifyRegisterGuideResult(OperationResponse response) {
		for (RegisterGuideObserver observer : registerGuideObservers) {
			observer.onRegisterGuideResult(response);
		}
	}

	/**
	 * Adds a report observer.
	 *
	 * @param observer the report observer to add
	 */
	public void addReportObserver(ReportObserver observer) {
		if (observer != null && !reportObservers.contains(observer)) {
			reportObservers.add(observer);
		}
	}

	/**
	 * Removes a report observer.
	 *
	 * @param observer the report observer to remove
	 */
	public void removeReportObserver(ReportObserver observer) {
		reportObservers.remove(observer);
	}

	private void notifyReportResponse(OperationResponse response) {
		for (ReportObserver observer : reportObservers) {
			observer.onReportResponse(response);
		}
	}

	/**
	 * Adds a park parameter observer.
	 *
	 * @param observer the park parameter observer to add
	 */
	public void addParkParameterObserver(ParkParameterObserver observer) {
		if (observer != null && !parkParameterObservers.contains(observer)) {
			parkParameterObservers.add(observer);
		}
	}

	/**
	 * Removes a park parameter observer.
	 *
	 * @param observer the park parameter observer to remove
	 */
	public void removeParkParameterObserver(ParkParameterObserver observer) {
		parkParameterObservers.remove(observer);
	}

	private void notifyPendingParkParameterRequestsReceived(List<ParkParameterChangeRequest> requests) {
		for (ParkParameterObserver observer : parkParameterObservers) {
			observer.onPendingParkParameterRequestsReceived(requests);
		}
	}

	private void notifyParkParameterOperationResponse(OperationResponse response, Protocol responseType) {
		for (ParkParameterObserver observer : parkParameterObservers) {
			observer.onParkParameterOperationResponse(response, responseType);
		}
	}

	/**
	 * Adds an entry price observer.
	 *
	 * @param observer the entry price observer to add
	 */
	public void addEntryPriceObserver(EntryPriceObserver observer) {
		if (observer != null && !entryPriceObservers.contains(observer)) {
			entryPriceObservers.add(observer);
		}
	}

	/**
	 * Removes an entry price observer.
	 *
	 * @param observer the entry price observer to remove
	 */
	public void removeEntryPriceObserver(EntryPriceObserver observer) {
		entryPriceObservers.remove(observer);
	}

	private void notifyEntryPriceCalculated(OperationResponse response) {
		for (EntryPriceObserver observer : entryPriceObservers) {
			observer.onEntryPriceCalculated(response);
		}
	}

	/**
	 * Adds a park visitor counter observer.
	 *
	 * @param observer the park visitor counter observer to add
	 */
	public void addParkVisitorCounterObserver(ParkVisitorCounterObserver observer) {
		if (observer != null && !parkVisitorCounterObservers.contains(observer)) {
			parkVisitorCounterObservers.add(observer);
		}
	}

	/**
	 * Removes a park visitor counter observer.
	 *
	 * @param observer the park visitor counter observer to remove
	 */
	public void removeParkVisitorCounterObserver(ParkVisitorCounterObserver observer) {
		parkVisitorCounterObservers.remove(observer);
	}

	private void notifyParkVisitorCountersReceived(List<ParkVisitorCounterSnapshot> counters) {
		for (ParkVisitorCounterObserver observer : parkVisitorCounterObservers) {
			observer.onParkVisitorCountersReceived(counters);
		}
	}

	private void notifyParkVisitorCounterOperationResponse(OperationResponse response, Protocol responseType) {
		for (ParkVisitorCounterObserver observer : parkVisitorCounterObservers) {
			observer.onParkVisitorCounterOperationResponse(response, responseType);
		}
	}

	private void notifyParkVisitorCountersUpdated() {
		for (ParkVisitorCounterObserver observer : parkVisitorCounterObservers) {
			observer.onParkVisitorCountersUpdated();
		}
	}

	/**
	 * Sends the server a request for all orders of the current user.
	 */
	public void requestOrders() {
		sendMessageToServer(new Message(id, Protocol.RETURN_ORDER));
	}

	/**
	 * Sends the server a request for all orders of a specific subscriber.
	 *
	 * @param subscriberId the subscriber ID whose orders should be loaded
	 */
	public void requestOrdersBySubscriberId(int subscriberId) {
		this.id = String.valueOf(subscriberId);
		sendMessageToServer(new Message(String.valueOf(subscriberId), Protocol.RETURN_ORDER));
	}

	/**
	 * Sends the server a request for all orders of a specific park.
	 *
	 * @param parkId the park ID whose orders should be loaded
	 */
	public void requestOrdersByParkId(int parkId) {
		sendMessageToServer(new Message(parkId, Protocol.GET_PARK_ORDERS_REQUEST));
	}

	/**
	 * Sends the server a request for all orders for the service representative.
	 */
	public void requestAllOrdersForServiceRepresentative() {
		sendMessageToServer(new Message(null, Protocol.GET_ALL_ORDERS_REQUEST));
	}

	/**
	 * Sends the server a request to update an order.
	 *
	 * @param updateMessage the update data
	 */
	public void requestUpdate(UpdateMessage updateMessage) {
		sendMessageToServer(new Message(updateMessage, Protocol.UPDATE_ORDER));
	}

	/**
	 * Sends the server a request to cancel an order.
	 *
	 * @param cancelOrderMessage the cancellation request data
	 */
	public void requestCancelOrder(CancelOrderMessage cancelOrderMessage) {
		sendMessageToServer(new Message(cancelOrderMessage, Protocol.CANCEL_ORDER));
	}

	/**
	 * Sends the server a request to join the waiting list.
	 *
	 * @param waitingListMessage the waiting-list request data
	 */
	public void requestJoinWaitingList(WaitingListMessage waitingListMessage) {
		sendMessageToServer(new Message(waitingListMessage, Protocol.JOIN_WAITING_LIST_REQUEST));
	}

	/**
	 * Sends a request to get waiting-list offers for a subscriber.
	 *
	 * @param subscriberId the subscriber ID
	 */
	public void requestWaitingOffers(int subscriberId) {
		sendMessageToServer(new Message(subscriberId, Protocol.GET_WAITING_OFFERS_REQUEST));
	}

	/**
	 * Sends a request to reject a waiting-list offer.
	 *
	 * @param waitingId the waiting-list request ID
	 */
	public void requestRejectWaitingOffer(int waitingId) {
		WaitingListMessage waitingListMessage = new WaitingListMessage(waitingId);
		sendMessageToServer(new Message(waitingListMessage, Protocol.REJECT_WAITING_OFFER_REQUEST));
	}

	/**
	 * Sends a request to accept a waiting-list offer.
	 *
	 * @param waitingId the waiting-list request ID
	 */
	public void requestAcceptWaitingOffer(int waitingId) {
		WaitingListMessage waitingListMessage = new WaitingListMessage(waitingId);
		sendMessageToServer(new Message(waitingListMessage, Protocol.ACCEPT_WAITING_OFFER_REQUEST));
	}

	/**
	 * Sends the server a request for all active parks.
	 */
	public void requestActiveParks() {
		sendMessageToServer(new Message(null, Protocol.GET_ACTIVE_PARKS));
	}

	/**
	 * Sends the server a request to check occasional customer access by order number.
	 *
	 * @param orderNumber the order number entered by the customer
	 */
	public void requestOccasionalCustomerAccess(int orderNumber) {
		this.id = String.valueOf(orderNumber);
		sendMessageToServer(new Message(orderNumber, Protocol.OCCASIONAL_CUSTOMER_ACCESS_REQUEST));
	}

	/**
	 * Sends the server an occasional customer access request.
	 *
	 * @param customerIdNumber the ID number entered by the occasional customer
	 */
	public void requestOccasionalCustomerAccess(String customerIdNumber) {
		this.id = customerIdNumber;
		sendMessageToServer(new Message(customerIdNumber, Protocol.OCCASIONAL_CUSTOMER_ACCESS_REQUEST));
	}

	/**
	 * Sends an employee login request to the server.
	 *
	 * @param username the employee username
	 * @param password the employee password
	 */
	public void requestEmployeeLogin(String username, String password) {
		EmployeeLoginRequest request = new EmployeeLoginRequest(username, password);
		sendMessageToServer(new Message(request, Protocol.EMPLOYEE_LOGIN_REQUEST));
	}

	/**
	 * Sends an existing customer login request to the server.
	 *
	 * @param username the customer username
	 * @param password the customer password
	 */
	public void requestExistingCustomerLogin(String username, String password) {
		ExistingCustomerLoginRequest request = new ExistingCustomerLoginRequest(username, password);
		sendMessageToServer(new Message(request, Protocol.EXISTING_CUSTOMER_LOGIN_REQUEST));
	}

	/**
	 * Sends a register subscriber request to the server.
	 *
	 * @param request the subscriber registration request data
	 */
	public void requestRegisterSubscriber(RegisterSubscriberRequest request) {
		sendMessageToServer(new Message(request, Protocol.REGISTER_SUBSCRIBER_REQUEST));
	}

	/**
	 * Sends a request to search subscriber by subscriber ID.
	 *
	 * @param subscriberId the subscriber ID to search
	 */
	public void requestSearchSubscriber(int subscriberId) {
		sendMessageToServer(new Message(subscriberId, Protocol.SEARCH_SUBSCRIBER_REQUEST));
	}

	/**
	 * Sends a request to register a subscriber as a guide.
	 *
	 * @param request the guide registration request
	 */
	public void requestRegisterGuide(GuideRegistrationRequest request) {
		sendMessageToServer(new Message(request, Protocol.REGISTER_GUIDE_REQUEST));
	}

	/**
	 * Sends a request to check in visitors using an order confirmation code.
	 *
	 * @param parkEntranceMessage the check-in request data
	 */
	public void requestCheckInOrder(ParkEntranceMessage parkEntranceMessage) {
		sendMessageToServer(new Message(parkEntranceMessage, Protocol.CHECK_IN_ORDER_REQUEST));
	}

	/**
	 * Sends a request to check out visitors using an order confirmation code.
	 *
	 * @param parkEntranceMessage the check-out request data
	 */
	public void requestCheckOutVisit(ParkEntranceMessage parkEntranceMessage) {
		sendMessageToServer(new Message(parkEntranceMessage, Protocol.CHECK_OUT_VISIT_REQUEST));
	}

	/**
	 * Sends a request to create an occasional visit.
	 *
	 * @param parkEntranceMessage the occasional visit request data
	 */
	public void requestOccasionalVisit(ParkEntranceMessage parkEntranceMessage) {
		sendMessageToServer(new Message(parkEntranceMessage, Protocol.OCCASIONAL_VISIT_REQUEST));
	}

	/**
	 * Sends a request to get the current number of visitors in a park.
	 *
	 * @param parkEntranceMessage the current visitors request data
	 */
	public void requestCurrentVisitors(ParkEntranceMessage parkEntranceMessage) {
		sendMessageToServer(new Message(parkEntranceMessage, Protocol.GET_CURRENT_VISITORS_REQUEST));
	}

	/**
	 * Sends a report request to the server.
	 *
	 * @param request the report request
	 */
	public void requestReport(ReportRequest request) {
		sendMessageToServer(new Message(request, Protocol.GET_REPORT_REQUEST));
	}

	/**
	 * Sends a request to calculate the entry price for an order.
	 *
	 * @param orderNumber the order number
	 */
	public void calculateEntryPrice(int orderNumber) {
		EntryPriceRequest request = new EntryPriceRequest(orderNumber);
		sendMessageToServer(new Message(request, Protocol.CALCULATE_ENTRY_PRICE_REQUEST));
	}

	/**
	 * Sends a request to create a park parameter change request.
	 *
	 * @param parkId        the park ID
	 * @param employeeId    the employee ID
	 * @param parameterName the parameter name
	 * @param newValue      the requested new value
	 */
	public void createParkParameterChangeRequest(int parkId, int employeeId,
			String parameterName, String newValue) {

		Object[] data = new Object[] {
				parkId,
				employeeId,
				parameterName,
				newValue
		};

		sendMessageToServer(new Message(data, Protocol.CREATE_PARK_PARAMETER_CHANGE_REQUEST));
	}

	/**
	 * Sends a request to get all pending park parameter change requests.
	 *
	 * @param employeeId the employee ID
	 */
	public void requestPendingParkParameterChangeRequests(int employeeId) {
		sendMessageToServer(new Message(employeeId, Protocol.GET_PENDING_PARK_PARAMETER_CHANGE_REQUESTS));
	}

	/**
	 * Sends a request to approve a park parameter change request.
	 *
	 * @param requestId  the request ID
	 * @param employeeId the employee ID
	 * @param reviewNote the review note
	 */
	public void approveParkParameterChangeRequest(int requestId, int employeeId, String reviewNote) {
		Object[] data = new Object[] {
				requestId,
				employeeId,
				reviewNote
		};

		sendMessageToServer(new Message(data, Protocol.APPROVE_PARK_PARAMETER_CHANGE_REQUEST));
	}

	/**
	 * Sends a request to reject a park parameter change request.
	 *
	 * @param requestId  the request ID
	 * @param employeeId the employee ID
	 * @param reviewNote the review note
	 */
	public void rejectParkParameterChangeRequest(int requestId, int employeeId, String reviewNote) {
		Object[] data = new Object[] {
				requestId,
				employeeId,
				reviewNote
		};

		sendMessageToServer(new Message(data, Protocol.REJECT_PARK_PARAMETER_CHANGE_REQUEST));
	}

	/**
	 * Sends a request to get park visitor counters.
	 *
	 * @param employeeId the employee ID
	 */
	public void requestParkVisitorCounters(int employeeId) {
		sendMessageToServer(new Message(employeeId, Protocol.GET_PARK_VISITOR_COUNTERS_REQUEST));
	}

	/**
	 * Sends a request to update a park visitor counter.
	 *
	 * @param request the update request
	 */
	public void updateParkVisitorCounter(ParkVisitorCounterUpdateRequest request) {
		sendMessageToServer(new Message(request, Protocol.UPDATE_PARK_VISITOR_COUNTER_REQUEST));
	}

	/**
	 * Sends a general message to the server.
	 *
	 * @param message the message to send
	 */
	public void sendMessageToServer(Message message) {
		if (message != null) {
			client.handleMessageFromClientUI(message);
		}
	}

	/**
	 * Handles messages received from the server.
	 *
	 * @param message the message received from the server
	 */
	@Override
	public void display(Message message) {
		if (message == null) {
			return;
		}

		Platform.runLater(() -> handleServerMessage(message));
	}

	/**
	 * Handles the server message according to its protocol type.
	 *
	 * @param message the server message
	 */
	private void handleServerMessage(Message message) {
		Protocol type = message.getType();

		switch (type) {

		case CLIENT_DISCONNECT_SERVER:
			handleServerIssuedDisconnect();
			break;

		case UPDATE_ORDER_SUCCESS:
			notifyUpdateResult(true, (UpdateMessage) message.getData());
			break;

		case UPDATE_ORDER_FAILURE:
			notifyUpdateResult(false, (UpdateMessage) message.getData());
			break;

		case CANCEL_ORDER_SUCCESS:
			notifyCancelResult(true, (CancelOrderMessage) message.getData());
			break;

		case CANCEL_ORDER_FAILURE:
			notifyCancelResult(false, (CancelOrderMessage) message.getData());
			break;

		case JOIN_WAITING_LIST_SUCCESS:
			notifyJoinWaitingListResult(true, (WaitingListMessage) message.getData());
			break;

		case JOIN_WAITING_LIST_FAILURE:
			notifyJoinWaitingListResult(false, (WaitingListMessage) message.getData());
			break;

		case GET_WAITING_OFFERS_SUCCESS:
			List<WaitingListMessage> offers = parseWaitingOffersMessage(message.getData());
			notifyWaitingOffersReceived(offers != null, offers);
			break;

		case GET_WAITING_OFFERS_FAILURE:
			notifyWaitingOffersReceived(false, null);
			break;

		case REJECT_WAITING_OFFER_SUCCESS:
			notifyRejectWaitingOfferResult(true, (WaitingListMessage) message.getData());
			break;

		case REJECT_WAITING_OFFER_FAILURE:
			notifyRejectWaitingOfferResult(false, (WaitingListMessage) message.getData());
			break;

		case ACCEPT_WAITING_OFFER_SUCCESS:
			notifyAcceptWaitingOfferResult(true, (WaitingListMessage) message.getData());
			break;

		case ACCEPT_WAITING_OFFER_FAILURE:
			notifyAcceptWaitingOfferResult(false, (WaitingListMessage) message.getData());
			break;

		case RETURN_ORDER:
			handleReturnOrderResponse(message.getData());
			break;

		case GET_PARK_ORDERS_RESPONSE:
			handleReturnOrderResponse(message.getData());
			break;

		case GET_ALL_ORDERS_RESPONSE:
			handleReturnOrderResponse(message.getData());
			break;

		case RETURN_PARK_NAMES_SUCCESS:
		case GET_PARK_NAMES:
			handleParkNamesResponse(message.getData());
			break;

		case RETURN_PARK_NAMES_FAILURE:
			notifyParkNamesReceivedForMakeOrder(null);
			break;

		case MAKE_ORDER_SUCCESS:
			if (message.getData() instanceof Order) {
				notifyOrderMade((Order) message.getData());
			}

			notifyMakeOrderServerResponse(message);
			break;

		case MAKE_ORDER_FAIL_NOT_GUIDE:
		case MAKE_ORDER_FAIL_TIME:
		case MAKE_ORDER_FAIL_NOT_SUBSCRIBED:
		case MAKE_ORDER_FAIL:
			notifyMakeOrderServerResponse(message);
			break;

		case ACTIVE_PARKS_RESULT:
		case PARKS_UPDATED:
			handleParksResponse(message.getData());
			break;

		case OCCASIONAL_CUSTOMER_ACCESS_RESPONSE:
			handleOperationResponse(message.getData(), this::notifyOccasionalCustomerAccessResult);
			break;

		case EMPLOYEE_LOGIN_RESPONSE:
			handleOperationResponse(message.getData(), this::notifyEmployeeLoginResult);
			break;

		case EXISTING_CUSTOMER_LOGIN_RESPONSE:
			handleOperationResponse(message.getData(), this::notifyExistingCustomerLoginResult);
			break;

		case REGISTER_SUBSCRIBER_RESPONSE:
			handleOperationResponse(message.getData(), this::notifyRegisterSubscriberResult);
			break;

		case SEARCH_SUBSCRIBER_RESPONSE:
			handleOperationResponse(message.getData(), this::notifySearchSubscriberResult);
			break;

		case REGISTER_GUIDE_RESPONSE:
			handleOperationResponse(message.getData(), this::notifyRegisterGuideResult);
			break;

		case CHECK_IN_ORDER_SUCCESS:
			notifyCheckInOrderResult(true, (ParkEntranceMessage) message.getData());
			break;

		case CHECK_IN_ORDER_FAILURE:
			notifyCheckInOrderResult(false, (ParkEntranceMessage) message.getData());
			break;

		case CHECK_OUT_VISIT_SUCCESS:
			notifyCheckOutVisitResult(true, (ParkEntranceMessage) message.getData());
			break;

		case CHECK_OUT_VISIT_FAILURE:
			notifyCheckOutVisitResult(false, (ParkEntranceMessage) message.getData());
			break;

		case OCCASIONAL_VISIT_SUCCESS:
			notifyOccasionalVisitResult(true, (ParkEntranceMessage) message.getData());
			break;

		case OCCASIONAL_VISIT_FAILURE:
			notifyOccasionalVisitResult(false, (ParkEntranceMessage) message.getData());
			break;

		case GET_CURRENT_VISITORS_SUCCESS:
			notifyCurrentVisitorsReceived(true, (ParkEntranceMessage) message.getData());
			break;

		case GET_CURRENT_VISITORS_FAILURE:
			notifyCurrentVisitorsReceived(false, (ParkEntranceMessage) message.getData());
			break;

		case GET_REPORT_RESPONSE:
			handleReportResponse(message.getData());
			break;

		case PENDING_PARK_PARAMETER_CHANGE_REQUESTS_RESULT:
			handlePendingParkParameterRequestsResponse(message.getData(), type);
			break;

		case PARK_PARAMETER_CHANGE_REQUEST_CREATED:
		case PARK_PARAMETER_CHANGE_REQUEST_APPROVED:
		case PARK_PARAMETER_CHANGE_REQUEST_REJECTED:
		case PARK_PARAMETER_CHANGE_REQUEST_FAILURE:
			handleParkParameterOperationResponse(message.getData(), type);
			break;

		case CALCULATE_ENTRY_PRICE_RESPONSE:
			handleEntryPriceResponse(message.getData());
			break;

		case PARK_VISITOR_COUNTERS_RESULT:
			handleParkVisitorCountersResponse(message.getData(), type);
			break;

		case PARK_VISITOR_COUNTER_UPDATE_RESULT:
			handleParkVisitorCounterOperationResponse(message.getData(), type);
			break;

		case PARK_VISITOR_COUNTERS_UPDATED:
			notifyParkVisitorCountersUpdated();
			break;

		default:
			System.out.println("Error: unknown server response in ClientController: " + type);
			break;
		}
	}

	private void handleOperationResponse(Object data, OperationResponseConsumer consumer) {
		if (data instanceof OperationResponse) {
			consumer.accept((OperationResponse) data);
			return;
		}

		consumer.accept(new OperationResponse(false, "Invalid operation response from server", null));
	}

	private void handleReturnOrderResponse(Object data) {
		List<Order> rows = parseOrderMessage(data);

		if (rows != null) {
			if (!rows.isEmpty() && rows.get(0).getUserId() != null) {
				this.id = String.valueOf(rows.get(0).getUserId());
				System.out.println("ClientController ID saved from order table: " + this.id);
			}

			notifyOrdersReceived(rows);
		}
	}

	private void handleParkNamesResponse(Object data) {
		List<String> parkNames = parseParkNamesMessage(data);

		if (parkNames != null) {
			notifyParkNamesReceivedForMakeOrder(parkNames);
		} else {
			notifyParkNamesReceivedForMakeOrder(null);
		}
	}

	private void handleParksResponse(Object data) {
		List<Park> parks = parseParkMessage(data);

		if (parks != null) {
			notifyParksReceived(parks);
		}
	}

	private void handleReportResponse(Object data) {
		if (data instanceof OperationResponse) {
			notifyReportResponse((OperationResponse) data);
			return;
		}

		notifyReportResponse(new OperationResponse(false, "Invalid report response from server", null));
	}

	private void handleEntryPriceResponse(Object data) {
		if (data instanceof OperationResponse) {
			notifyEntryPriceCalculated((OperationResponse) data);
			return;
		}

		notifyEntryPriceCalculated(new OperationResponse(false, "Invalid entry price response from server", null));
	}

	private void handlePendingParkParameterRequestsResponse(Object data, Protocol responseType) {
		if (!(data instanceof OperationResponse)) {
			OperationResponse invalidResponse = new OperationResponse(
					false,
					"Invalid pending requests response from server",
					null
			);

			notifyParkParameterOperationResponse(invalidResponse, responseType);
			return;
		}

		OperationResponse response = (OperationResponse) data;

		if (!response.isSuccess()) {
			notifyParkParameterOperationResponse(response, responseType);
			return;
		}

		List<ParkParameterChangeRequest> requests = parseParkParameterRequestMessage(response.getData());

		if (requests == null) {
			OperationResponse invalidDataResponse = new OperationResponse(
					false,
					"Invalid pending requests data from server",
					null
			);

			notifyParkParameterOperationResponse(invalidDataResponse, responseType);
			return;
		}

		notifyPendingParkParameterRequestsReceived(requests);
	}

	private void handleParkParameterOperationResponse(Object data, Protocol responseType) {
		if (data instanceof OperationResponse) {
			notifyParkParameterOperationResponse((OperationResponse) data, responseType);
			return;
		}

		OperationResponse response = new OperationResponse(
				false,
				"Invalid park parameter response from server",
				null
		);

		notifyParkParameterOperationResponse(response, responseType);
	}

	private void handleParkVisitorCountersResponse(Object data, Protocol responseType) {
		if (!(data instanceof OperationResponse)) {
			OperationResponse invalidResponse = new OperationResponse(
					false,
					"Invalid park visitor counters response from server",
					null
			);

			notifyParkVisitorCounterOperationResponse(invalidResponse, responseType);
			return;
		}

		OperationResponse response = (OperationResponse) data;

		if (!response.isSuccess()) {
			notifyParkVisitorCounterOperationResponse(response, responseType);
			return;
		}

		List<ParkVisitorCounterSnapshot> counters = parseParkVisitorCounterMessage(response.getData());

		if (counters == null) {
			OperationResponse invalidDataResponse = new OperationResponse(
					false,
					"Invalid park visitor counters data from server",
					null
			);

			notifyParkVisitorCounterOperationResponse(invalidDataResponse, responseType);
			return;
		}

		notifyParkVisitorCountersReceived(counters);
	}

	private void handleParkVisitorCounterOperationResponse(Object data, Protocol responseType) {
		if (data instanceof OperationResponse) {
			notifyParkVisitorCounterOperationResponse((OperationResponse) data, responseType);
			return;
		}

		OperationResponse response = new OperationResponse(
				false,
				"Invalid park visitor counter response from server",
				null
		);

		notifyParkVisitorCounterOperationResponse(response, responseType);
	}

	/**
	 * Disconnects the client from the server after a user-initiated disconnect request.
	 */
	public void disconnectFromServer() {
		userIssuedDisconnect = true;
		sendMessageToServer(new Message(null, Protocol.CLIENT_DISCONNECT_USER));
		client.quit();
	}

	/**
	 * Handles a disconnect request issued by the server and notifies relevant observers.
	 */
	public void handleServerIssuedDisconnect() {
		for (OrderObserver observer : orderObservers) {
			observer.handleExit();
		}

		for (WaitingListObserver observer : waitingListObservers) {
			observer.handleExit();
		}

		for (ParkEntranceObserver observer : parkEntranceObservers) {
			observer.handleExit();
		}
	}

	/**
	 * Returns whether the user initiated the disconnect.
	 *
	 * @return true if the user initiated the disconnect, false otherwise
	 */
	public boolean isUserIssuedDisconnect() {
		return userIssuedDisconnect;
	}

	/**
	 * Sets whether the user initiated the disconnect.
	 *
	 * @param userIssuedDisconnect true if the user initiated the disconnect, false otherwise
	 */
	public void setUserIssuedDisconnect(boolean userIssuedDisconnect) {
		this.userIssuedDisconnect = userIssuedDisconnect;
	}

	private List<Order> parseOrderMessage(Object data) {
		if (!(data instanceof List<?>)) {
			return null;
		}

		List<?> rawList = (List<?>) data;
		List<Order> orders = new ArrayList<>();

		for (Object item : rawList) {
			if (!(item instanceof Order)) {
				return null;
			}

			orders.add((Order) item);
		}

		return orders;
	}

	private List<WaitingListMessage> parseWaitingOffersMessage(Object data) {
		if (!(data instanceof List<?>)) {
			return null;
		}

		List<?> rawList = (List<?>) data;
		List<WaitingListMessage> offers = new ArrayList<>();

		for (Object item : rawList) {
			if (!(item instanceof WaitingListMessage)) {
				return null;
			}

			offers.add((WaitingListMessage) item);
		}

		return offers;
	}

	private List<String> parseParkNamesMessage(Object data) {
		if (!(data instanceof List<?>)) {
			return null;
		}

		List<?> rawList = (List<?>) data;
		List<String> names = new ArrayList<>();

		for (Object item : rawList) {
			if (!(item instanceof String)) {
				return null;
			}

			names.add((String) item);
		}

		return names;
	}

	private List<Park> parseParkMessage(Object data) {
		if (!(data instanceof List<?>)) {
			return null;
		}

		List<?> rawList = (List<?>) data;
		List<Park> parks = new ArrayList<>();

		for (Object item : rawList) {
			if (!(item instanceof Park)) {
				return null;
			}

			parks.add((Park) item);
		}

		return parks;
	}

	private List<ParkParameterChangeRequest> parseParkParameterRequestMessage(Object data) {
		if (!(data instanceof List<?>)) {
			return null;
		}

		List<?> rawList = (List<?>) data;
		List<ParkParameterChangeRequest> requests = new ArrayList<>();

		for (Object item : rawList) {
			if (!(item instanceof ParkParameterChangeRequest)) {
				return null;
			}

			requests.add((ParkParameterChangeRequest) item);
		}

		return requests;
	}

	private List<ParkVisitorCounterSnapshot> parseParkVisitorCounterMessage(Object data) {
		if (!(data instanceof List<?>)) {
			return null;
		}

		List<?> rawList = (List<?>) data;
		List<ParkVisitorCounterSnapshot> counters = new ArrayList<>();

		for (Object item : rawList) {
			if (!(item instanceof ParkVisitorCounterSnapshot)) {
				return null;
			}

			counters.add((ParkVisitorCounterSnapshot) item);
		}

		return counters;
	}

	private interface OperationResponseConsumer {
		void accept(OperationResponse response);
	}
}