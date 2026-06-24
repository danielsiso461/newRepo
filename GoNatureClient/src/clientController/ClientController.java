package clientController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import client.Client;
import clientCommon.*;
import common.*;
import javafx.application.Platform;

/**
 * This class connects the client networking side to the UI side.
 * 
 * It sends requests from the UI to the server and receives responses from
 * the server. It also uses the Observer pattern to notify the relevant UI
 * controllers when new data or operation results arrive.
 */
public class ClientController implements ChatIF {

	/**
	 * The client object responsible for handling the connection with the server.
	 */
	private Client client;

	/**
	 * The ID of the currently connected user.
	 */
	private String id = null;

	/**
	 * Indicates whether the disconnection was initiated by the user.
	 */
	private boolean userIssuedDisconnect = false;

	/**
	 * The logged-in subscriber ID.
	 */
	private Integer loggedInSubscriberId;

	/**
	 * Observers for order-related screens.
	 */
	private List<OrderObserver> observers = new ArrayList<>();

	/**
	 * Observers for park-related screens.
	 */
	private List<ParkObserver> parkObservers = new ArrayList<>();

	/**
	 * Observers for occasional customer access screens.
	 */
	private List<OccasionalCustomerAccessObserver> occasionalCustomerAccessObservers = new ArrayList<>();

	/**
	 * Observers for waiting-list screens.
	 */
	private List<WaitingListObserver> waitingListObservers = new ArrayList<>();

	/**
	 * Observers for park entrance control screens.
	 */
	private List<ParkEntranceObserver> parkEntranceObservers = new ArrayList<>();

	/**
	 * Observers for make-order screens.
	 */
	private List<MakeOrderObserver> makeOrderObservers = new ArrayList<>();

	/**
	 * Observers for employee login screens.
	 */
	private List<EmployeeLoginObserver> employeeLoginObservers = new ArrayList<>();

	/**
	 * Observers for existing customer login screens.
	 */
	private List<ExistingCustomerLoginObserver> existingCustomerLoginObservers = new ArrayList<>();

	/**
	 * Observers for subscriber registration screens.
	 */
	private List<RegisterSubscriberObserver> registerSubscriberObservers = new ArrayList<>();

	/**
	 * Observers for guide registration screens.
	 */
	private List<RegisterGuideObserver> registerGuideObservers = new ArrayList<>();

	/**
	 * Observers for subscriber search screens.
	 */
	private List<SearchSubscriberObserver> searchSubscriberObservers = new ArrayList<>();

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
	 * Adds an order observer.
	 *
	 * @param observer the order observer to add
	 */
	public void addObserver(OrderObserver observer) {
		if (observer != null && !observers.contains(observer)) {
			observers.add(observer);
		}
	}

	/**
	 * Removes an order observer.
	 *
	 * @param observer the order observer to remove
	 */
	public void removeObserver(OrderObserver observer) {
		observers.remove(observer);
	}

	/**
	 * Notifies all order observers that orders were received from the server.
	 *
	 * @param rows the list of received orders
	 */
	private void notifyOrdersReceived(List<Order> rows) {
		for (OrderObserver observer : observers) {
			observer.onOrdersReceived(rows);
		}
	}

	/**
	 * Notifies all order observers that a new order was created.
	 *
	 * @param order the newly created order
	 */
	private void notifyOrderMade(Order order) {
		for (OrderObserver observer : observers) {
			observer.addOrder(order);
		}
	}

	/**
	 * Notifies all order observers about the result of an order update request.
	 *
	 * @param success       true if the update succeeded, false otherwise
	 * @param updateMessage the update message returned from the server
	 */
	private void notifyUpdateResult(boolean success, UpdateMessage updateMessage) {
		for (OrderObserver observer : observers) {
			observer.onUpdateResult(success, updateMessage);
		}
	}

	/**
	 * Notifies all order observers about the result of an order cancellation request.
	 *
	 * @param success            true if the cancellation succeeded, false otherwise
	 * @param cancelOrderMessage the cancellation message returned from the server
	 */
	private void notifyCancelResult(boolean success, CancelOrderMessage cancelOrderMessage) {
		for (OrderObserver observer : observers) {
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

	/**
	 * Notifies all park observers that park information was received.
	 *
	 * @param parks the list of parks received from the server
	 */
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

	/**
	 * Notifies all occasional customer access observers about the server response.
	 *
	 * @param response the response received from the server
	 */
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

	/**
	 * Notifies all waiting-list observers about the result of a join waiting-list request.
	 *
	 * @param success            true if the request succeeded, false otherwise
	 * @param waitingListMessage the waiting-list message returned from the server
	 */
	private void notifyJoinWaitingListResult(boolean success, WaitingListMessage waitingListMessage) {
		for (WaitingListObserver observer : waitingListObservers) {
			observer.onJoinWaitingListResult(success, waitingListMessage);
		}
	}

	/**
	 * Notifies all waiting-list observers about loaded waiting-list offers.
	 *
	 * @param success true if the offers were loaded successfully
	 * @param offers  the waiting-list offers returned from the server
	 */
	private void notifyWaitingOffersReceived(boolean success, List<WaitingListMessage> offers) {
		for (WaitingListObserver observer : waitingListObservers) {
			observer.onWaitingOffersReceived(success, offers);
		}
	}

	/**
	 * Notifies all waiting-list observers about the result of a reject waiting offer request.
	 *
	 * @param success            true if the request succeeded, false otherwise
	 * @param waitingListMessage the waiting-list message returned from the server
	 */
	private void notifyRejectWaitingOfferResult(boolean success, WaitingListMessage waitingListMessage) {
		for (WaitingListObserver observer : waitingListObservers) {
			observer.onRejectWaitingOfferResult(success, waitingListMessage);
		}
	}

	/**
	 * Notifies all waiting-list observers about the result of an accept waiting offer request.
	 *
	 * @param success            true if the request succeeded, false otherwise
	 * @param waitingListMessage the waiting-list message returned from the server
	 */
	private void notifyAcceptWaitingOfferResult(boolean success, WaitingListMessage waitingListMessage) {
		for (WaitingListObserver observer : waitingListObservers) {
			observer.onAcceptWaitingOfferResult(success, waitingListMessage);
		}
	}

	/**
	 * Adds a park entrance observer.
	 *
	 * @param observer the observer to add
	 */
	public void addParkEntranceObserver(ParkEntranceObserver observer) {
		if (observer != null && !parkEntranceObservers.contains(observer)) {
			parkEntranceObservers.add(observer);
		}
	}

	/**
	 * Removes a park entrance observer.
	 *
	 * @param observer the observer to remove
	 */
	public void removeParkEntranceObserver(ParkEntranceObserver observer) {
		parkEntranceObservers.remove(observer);
	}

	/**
	 * Notifies all park entrance observers about a check-in result.
	 *
	 * @param success             true if check-in succeeded
	 * @param parkEntranceMessage the response data returned from the server
	 */
	private void notifyCheckInOrderResult(boolean success, ParkEntranceMessage parkEntranceMessage) {
		for (ParkEntranceObserver observer : parkEntranceObservers) {
			observer.onCheckInOrderResult(success, parkEntranceMessage);
		}
	}

	/**
	 * Notifies all park entrance observers about a check-out result.
	 *
	 * @param success             true if check-out succeeded
	 * @param parkEntranceMessage the response data returned from the server
	 */
	private void notifyCheckOutVisitResult(boolean success, ParkEntranceMessage parkEntranceMessage) {
		for (ParkEntranceObserver observer : parkEntranceObservers) {
			observer.onCheckOutVisitResult(success, parkEntranceMessage);
		}
	}

	/**
	 * Notifies all park entrance observers about an occasional visit result.
	 *
	 * @param success             true if the occasional visit was created successfully
	 * @param parkEntranceMessage the response data returned from the server
	 */
	private void notifyOccasionalVisitResult(boolean success, ParkEntranceMessage parkEntranceMessage) {
		for (ParkEntranceObserver observer : parkEntranceObservers) {
			observer.onOccasionalVisitResult(success, parkEntranceMessage);
		}
	}

	/**
	 * Notifies all park entrance observers about the current visitors count.
	 *
	 * @param success             true if the current visitors count was loaded successfully
	 * @param parkEntranceMessage the response data returned from the server
	 */
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

	/**
	 * Notifies all make-order observers that park names were received from the server.
	 *
	 * @param parkNames the list of park names received from the server
	 */
	private void notifyParkNamesReceivedForMakeOrder(List<String> parkNames) {
		for (MakeOrderObserver observer : makeOrderObservers) {
			observer.onParkNamesReceived(parkNames);
		}
	}

	/**
	 * Notifies all make-order observers about a make-order server response.
	 *
	 * @param message the message received from the server
	 */
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

	/**
	 * Notifies all employee login observers about the login result.
	 *
	 * @param response the employee login response received from the server
	 */
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

	/**
	 * Notifies all existing customer login observers about the login result.
	 *
	 * @param response the existing customer login response received from the server
	 */
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

	/**
	 * Notifies all register subscriber observers about the registration result.
	 *
	 * @param response the registration response received from the server
	 */
	private void notifyRegisterSubscriberResult(OperationResponse response) {
		for (RegisterSubscriberObserver observer : registerSubscriberObservers) {
			observer.onRegisterSubscriberResult(response);
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

	/**
	 * Notifies all register guide observers about the registration result.
	 *
	 * @param response the registration response received from the server
	 */
	private void notifyRegisterGuideResult(OperationResponse response) {
		for (RegisterGuideObserver observer : registerGuideObservers) {
			observer.onRegisterGuideResult(response);
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

	/**
	 * Notifies all search subscriber observers about the search result.
	 *
	 * @param response the search subscriber response received from the server
	 */
	private void notifySearchSubscriberResult(OperationResponse response) {
		for (SearchSubscriberObserver observer : searchSubscriberObservers) {
			observer.onSearchSubscriberResult(response);
		}
	}

	/**
	 * Sends the server a request for all orders of the current user.
	 */
	public void requestOrders() {
		client.handleMessageFromClientUI(new Message(id, Protocol.RETURN_ORDER));
	}

	/**
	 * Sends the server a request for all orders of a specific subscriber.
	 *
	 * @param subscriberId the subscriber ID whose orders should be loaded
	 */
	public void requestOrdersBySubscriberId(int subscriberId) {
		this.id = String.valueOf(subscriberId);

		client.handleMessageFromClientUI(
				new Message(String.valueOf(subscriberId), Protocol.RETURN_ORDER)
		);
	}

	/**
	 * Sends the server a request to update an order.
	 *
	 * @param um the update data
	 */
	public void requestUpdate(UpdateMessage um) {
		client.handleMessageFromClientUI(new Message(um, Protocol.UPDATE_ORDER));
	}

	/**
	 * Sends the server a request to cancel an order.
	 *
	 * @param cancelOrderMessage the cancellation request data
	 */
	public void requestCancelOrder(CancelOrderMessage cancelOrderMessage) {
		client.handleMessageFromClientUI(new Message(cancelOrderMessage, Protocol.CANCEL_ORDER));
	}

	/**
	 * Sends the server a request to join the waiting list.
	 *
	 * @param waitingListMessage the waiting-list request data
	 */
	public void requestJoinWaitingList(WaitingListMessage waitingListMessage) {
		client.handleMessageFromClientUI(
				new Message(waitingListMessage, Protocol.JOIN_WAITING_LIST_REQUEST)
		);
	}

	/**
	 * Sends a request to get waiting-list offers for a subscriber.
	 *
	 * @param subscriberId the subscriber ID
	 */
	public void requestWaitingOffers(int subscriberId) {
		client.handleMessageFromClientUI(
				new Message(subscriberId, Protocol.GET_WAITING_OFFERS_REQUEST)
		);
	}

	/**
	 * Sends a request to reject a waiting-list offer.
	 *
	 * @param waitingId the waiting-list request ID
	 */
	public void requestRejectWaitingOffer(int waitingId) {
		WaitingListMessage waitingListMessage = new WaitingListMessage(waitingId);

		client.handleMessageFromClientUI(
				new Message(waitingListMessage, Protocol.REJECT_WAITING_OFFER_REQUEST)
		);
	}

	/**
	 * Sends a request to accept a waiting-list offer.
	 *
	 * @param waitingId the waiting-list request ID
	 */
	public void requestAcceptWaitingOffer(int waitingId) {
		WaitingListMessage waitingListMessage = new WaitingListMessage(waitingId);

		client.handleMessageFromClientUI(
				new Message(waitingListMessage, Protocol.ACCEPT_WAITING_OFFER_REQUEST)
		);
	}

	/**
	 * Sends the server a request for all active parks.
	 */
	public void requestActiveParks() {
		client.handleMessageFromClientUI(new Message(null, Protocol.GET_ACTIVE_PARKS));
	}

	/**
	 * Sends a request to search subscriber by subscriber ID.
	 *
	 * @param subscriberId the subscriber ID to search
	 */
	public void requestSearchSubscriber(int subscriberId) {
		client.handleMessageFromClientUI(
				new Message(subscriberId, Protocol.SEARCH_SUBSCRIBER_REQUEST)
		);
	}

	/**
	 * Sends the server a request to check occasional customer access by order number.
	 *
	 * @param orderNumber the order number entered by the customer
	 */
	public void requestOccasionalCustomerAccess(int orderNumber) {
		this.id = String.valueOf(orderNumber);

		client.handleMessageFromClientUI(
				new Message(orderNumber, Protocol.OCCASIONAL_CUSTOMER_ACCESS_REQUEST)
		);
	}

	/**
	 * Sends the server an occasional customer access request.
	 *
	 * The occasional customer identifies himself by ID number.
	 *
	 * @param customerIdNumber the ID number entered by the occasional customer
	 */
	public void requestOccasionalCustomerAccess(String customerIdNumber) {
		this.id = customerIdNumber;

		client.handleMessageFromClientUI(
				new Message(customerIdNumber, Protocol.OCCASIONAL_CUSTOMER_ACCESS_REQUEST)
		);
	}

	/**
	 * Sends an employee login request to the server.
	 *
	 * @param username the employee username
	 * @param password the employee password
	 */
	public void requestEmployeeLogin(String username, String password) {
		EmployeeLoginRequest request = new EmployeeLoginRequest(username, password);

		client.handleMessageFromClientUI(
				new Message(request, Protocol.EMPLOYEE_LOGIN_REQUEST)
		);
	}

	/**
	 * Sends an existing customer login request to the server.
	 *
	 * @param username the customer username
	 * @param password the customer password
	 */
	public void requestExistingCustomerLogin(String username, String password) {
		ExistingCustomerLoginRequest request = new ExistingCustomerLoginRequest(username, password);

		client.handleMessageFromClientUI(
				new Message(request, Protocol.EXISTING_CUSTOMER_LOGIN_REQUEST)
		);
	}

	/**
	 * Sends a register subscriber request to the server.
	 *
	 * @param request the subscriber registration request data
	 */
	public void requestRegisterSubscriber(RegisterSubscriberRequest request) {
		client.handleMessageFromClientUI(
				new Message(request, Protocol.REGISTER_SUBSCRIBER_REQUEST)
		);
	}

	/**
	 * Sends a request to register a subscriber as a guide.
	 *
	 * @param request the guide registration request
	 */
	public void requestRegisterGuide(GuideRegistrationRequest request) {
		client.handleMessageFromClientUI(
				new Message(request, Protocol.REGISTER_GUIDE_REQUEST)
		);
	}

	/**
	 * Sends a request to check in visitors using an order confirmation code.
	 *
	 * @param parkEntranceMessage the check-in request data
	 */
	public void requestCheckInOrder(ParkEntranceMessage parkEntranceMessage) {
		client.handleMessageFromClientUI(
				new Message(parkEntranceMessage, Protocol.CHECK_IN_ORDER_REQUEST)
		);
	}

	/**
	 * Sends a request to check out visitors using an order confirmation code.
	 *
	 * @param parkEntranceMessage the check-out request data
	 */
	public void requestCheckOutVisit(ParkEntranceMessage parkEntranceMessage) {
		client.handleMessageFromClientUI(
				new Message(parkEntranceMessage, Protocol.CHECK_OUT_VISIT_REQUEST)
		);
	}

	/**
	 * Sends a request to create an occasional visit.
	 *
	 * @param parkEntranceMessage the occasional visit request data
	 */
	public void requestOccasionalVisit(ParkEntranceMessage parkEntranceMessage) {
		client.handleMessageFromClientUI(
				new Message(parkEntranceMessage, Protocol.OCCASIONAL_VISIT_REQUEST)
		);
	}

	/**
	 * Sends a request to get the current number of visitors in a park.
	 *
	 * @param parkEntranceMessage the current visitors request data
	 */
	public void requestCurrentVisitors(ParkEntranceMessage parkEntranceMessage) {
		client.handleMessageFromClientUI(
				new Message(parkEntranceMessage, Protocol.GET_CURRENT_VISITORS_REQUEST)
		);
	}

	/**
	 * Sends a general message to the server.
	 *
	 * @param message the message to send
	 */
	public void sendMessageToServer(Message message) {
		client.handleMessageFromClientUI(message);
	}

	/**
	 * Handles messages received from the server.
	 *
	 * @param m the message received from the server
	 */
	@Override
	public void display(Message m) {
		Protocol type = m.getType();

		Platform.runLater(() -> {
			switch (type) {
			case CLIENT_DISCONNECT_SERVER:
				handleServerIssuedDisconnect();
				break;

			case UPDATE_ORDER_SUCCESS:
				notifyUpdateResult(true, (UpdateMessage) m.getData());
				break;

			case UPDATE_ORDER_FAILURE:
				notifyUpdateResult(false, (UpdateMessage) m.getData());
				break;

			case CANCEL_ORDER_SUCCESS:
				notifyCancelResult(true, (CancelOrderMessage) m.getData());
				break;

			case CANCEL_ORDER_FAILURE:
				notifyCancelResult(false, (CancelOrderMessage) m.getData());
				break;

			case JOIN_WAITING_LIST_SUCCESS:
				notifyJoinWaitingListResult(true, (WaitingListMessage) m.getData());
				break;

			case JOIN_WAITING_LIST_FAILURE:
				notifyJoinWaitingListResult(false, (WaitingListMessage) m.getData());
				break;

			case GET_WAITING_OFFERS_SUCCESS:
				List<WaitingListMessage> offers = parseWaitingOffersMessage(m.getData());
				notifyWaitingOffersReceived(offers != null, offers);
				break;

			case GET_WAITING_OFFERS_FAILURE:
				notifyWaitingOffersReceived(false, null);
				break;

			case REJECT_WAITING_OFFER_SUCCESS:
				notifyRejectWaitingOfferResult(true, (WaitingListMessage) m.getData());
				break;

			case REJECT_WAITING_OFFER_FAILURE:
				notifyRejectWaitingOfferResult(false, (WaitingListMessage) m.getData());
				break;

			case ACCEPT_WAITING_OFFER_SUCCESS:
				notifyAcceptWaitingOfferResult(true, (WaitingListMessage) m.getData());
				break;

			case ACCEPT_WAITING_OFFER_FAILURE:
				notifyAcceptWaitingOfferResult(false, (WaitingListMessage) m.getData());
				break;

			case RETURN_ORDER:
				List<Order> rows = parseOrderMessage(m.getData());

				if (rows == null) {
					break;
				}

				if (!rows.isEmpty() && rows.get(0).getUserId() != null) {
					this.id = String.valueOf(rows.get(0).getUserId());
					System.out.println("ClientController ID saved from order table: " + this.id);
				}

				notifyOrdersReceived(rows);
				break;

			case RETURN_PARK_NAMES_SUCCESS:
			case GET_PARK_NAMES:
				List<String> parkNames = parseStringList(m.getData());

				if (parkNames == null) {
					break;
				}

				notifyParkNamesReceivedForMakeOrder(parkNames);
				break;

			case RETURN_PARK_NAMES_FAILURE:
				notifyParkNamesReceivedForMakeOrder(null);
				break;

			case MAKE_ORDER_SUCCESS:
				if (m.getData() instanceof Order) {
					notifyOrderMade((Order) m.getData());
				}

				notifyMakeOrderServerResponse(m);
				break;

			case MAKE_ORDER_FAIL:
			case MAKE_ORDER_FAIL_TIME:
			case MAKE_ORDER_FAIL_NOT_GUIDE:
			case MAKE_ORDER_FAIL_NOT_SUBSCRIBED:
				notifyMakeOrderServerResponse(m);
				break;

			case ACTIVE_PARKS_RESULT:
			case PARKS_UPDATED:
				List<Park> parks = parseParkMessage(m.getData());

				if (parks == null) {
					break;
				}

				notifyParksReceived(parks);
				break;

			case OCCASIONAL_CUSTOMER_ACCESS_RESPONSE:
				OperationResponse response = (OperationResponse) m.getData();
				notifyOccasionalCustomerAccessResult(response);
				break;

			case EMPLOYEE_LOGIN_RESPONSE:
				OperationResponse employeeLoginResponse = (OperationResponse) m.getData();
				notifyEmployeeLoginResult(employeeLoginResponse);
				break;

			case EXISTING_CUSTOMER_LOGIN_RESPONSE:
				OperationResponse existingCustomerLoginResponse = (OperationResponse) m.getData();
				notifyExistingCustomerLoginResult(existingCustomerLoginResponse);
				break;

			case REGISTER_SUBSCRIBER_RESPONSE:
				OperationResponse registerSubscriberResponse = (OperationResponse) m.getData();
				notifyRegisterSubscriberResult(registerSubscriberResponse);
				break;

			case SEARCH_SUBSCRIBER_RESPONSE:
				OperationResponse searchSubscriberResponse = (OperationResponse) m.getData();
				notifySearchSubscriberResult(searchSubscriberResponse);
				break;

			case REGISTER_GUIDE_RESPONSE:
				OperationResponse registerGuideResponse = (OperationResponse) m.getData();
				notifyRegisterGuideResult(registerGuideResponse);
				break;

			case CHECK_IN_ORDER_SUCCESS:
				notifyCheckInOrderResult(true, (ParkEntranceMessage) m.getData());
				break;

			case CHECK_IN_ORDER_FAILURE:
				notifyCheckInOrderResult(false, (ParkEntranceMessage) m.getData());
				break;

			case CHECK_OUT_VISIT_SUCCESS:
				notifyCheckOutVisitResult(true, (ParkEntranceMessage) m.getData());
				break;

			case CHECK_OUT_VISIT_FAILURE:
				notifyCheckOutVisitResult(false, (ParkEntranceMessage) m.getData());
				break;

			case OCCASIONAL_VISIT_SUCCESS:
				notifyOccasionalVisitResult(true, (ParkEntranceMessage) m.getData());
				break;

			case OCCASIONAL_VISIT_FAILURE:
				notifyOccasionalVisitResult(false, (ParkEntranceMessage) m.getData());
				break;

			case GET_CURRENT_VISITORS_SUCCESS:
				notifyCurrentVisitorsReceived(true, (ParkEntranceMessage) m.getData());
				break;

			case GET_CURRENT_VISITORS_FAILURE:
				notifyCurrentVisitorsReceived(false, (ParkEntranceMessage) m.getData());
				break;

			default:
				System.out.println("Error: Server Response Unknown in ClientController display "
						+ m.getType());
			}
		});
	}

	/**
	 * Parses an object into a list of waiting-list messages.
	 *
	 * @param o the object to parse
	 * @return a list of waiting-list messages if valid, otherwise null
	 */
	private List<WaitingListMessage> parseWaitingOffersMessage(Object o) {
		List<WaitingListMessage> offers = new ArrayList<>();

		if (o instanceof List<?>) {
			List<?> rawList = (List<?>) o;

			for (Object offer : rawList) {
				if (offer instanceof WaitingListMessage) {
					offers.add((WaitingListMessage) offer);
				} else {
					return null;
				}
			}
		} else {
			return null;
		}

		return offers;
	}

	/**
	 * Parses an object into a list of orders.
	 *
	 * @param o the object to parse
	 * @return a list of orders if valid, otherwise null
	 */
	private List<Order> parseOrderMessage(Object o) {
		List<Order> rows = new ArrayList<>();

		if (o instanceof List<?>) {
			List<?> rawList = (List<?>) o;

			for (Object row : rawList) {
				if (row instanceof Order) {
					rows.add((Order) row);
				} else {
					return null;
				}
			}
		} else {
			return null;
		}

		return rows;
	}

	/**
	 * Parses an object into a list of park information objects.
	 *
	 * @param o the object to parse
	 * @return a list of parks if valid, otherwise null
	 */
	private List<Park> parseParkMessage(Object o) {
		List<Park> parks = new ArrayList<>();

		if (o instanceof List<?>) {
			List<?> rawList = (List<?>) o;

			for (Object park : rawList) {
				if (park instanceof Park) {
					parks.add((Park) park);
				} else {
					return null;
				}
			}
		} else {
			return null;
		}

		return parks;
	}

	/**
	 * Parses an object into a list of strings.
	 *
	 * @param o the object to parse
	 * @return a list of strings if valid, otherwise null
	 */
	private List<String> parseStringList(Object o) {
		List<String> values = new ArrayList<>();

		if (o instanceof List<?>) {
			List<?> rawList = (List<?>) o;

			for (Object value : rawList) {
				if (value instanceof String) {
					values.add((String) value);
				} else {
					return null;
				}
			}
		} else {
			return null;
		}

		return values;
	}

	/**
	 * Disconnects the client from the server after a user-initiated disconnect request.
	 */
	public void disconnectFromServer() {
		client.handleMessageFromClientUI(new Message(null, Protocol.CLIENT_DISCONNECT_USER));
		client.quit();
	}

	/**
	 * Handles a disconnect request issued by the server and notifies relevant observers.
	 */
	public void handleServerIssuedDisconnect() {
		for (OrderObserver observer : observers) {
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
}