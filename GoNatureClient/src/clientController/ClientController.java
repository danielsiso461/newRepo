
package clientController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import client.Client;
import clientCommon.ChatIF;
import clientCommon.EmployeeLoginObserver;
import clientCommon.EntryPriceObserver;
import clientCommon.ExistingCustomerLoginObserver;
import clientCommon.MakeOrderObserver;
import clientCommon.OccasionalCustomerAccessObserver;
import clientCommon.OrderObserver;
import clientCommon.ParkEntranceObserver;
import clientCommon.ParkObserver;
import clientCommon.ParkParameterObserver;
import clientCommon.ParkVisitorCounterObserver;
import clientCommon.RegisterGuideObserver;
import clientCommon.RegisterSubscriberObserver;
import clientCommon.ReportObserver;
import clientCommon.SearchSubscriberObserver;
import clientCommon.WaitingListObserver;
import clientGUI.MakePopUp;
import common.CancelOrderMessage;
import common.EmployeeLoginRequest;
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
import common.UpdateMessage;
import common.WaitingListMessage;
import javafx.application.Platform;

/**
 * Controls the communication between the client networking layer and the UI layer.
 * 
 * This class sends requests from the UI to the server and receives responses from
 * the server. It also uses the Observer pattern to notify the relevant UI
 * controllers when new data or operation results arrive.
 */
@SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
public class ClientController implements ChatIF {

	/**
	 * the client networking object
	 */
	private Client client;

	/**
	 * the ID of the current user
	 */
	private String id;

	/**
	 * indicates whether the disconnect was requested by the user
	 */
	private boolean userIssuedDisconnect = false;

	/**
	 * the ID of the logged-in subscriber
	 */
	private Integer loggedInSubscriberId;

	/**
	 * the list of order observers
	 */
	private List<OrderObserver> orderObservers = new ArrayList<>();

	/**
	 * the list of park observers
	 */
	private List<ParkObserver> parkObservers = new ArrayList<>();

	/**
	 * the list of make order observers
	 */
	private List<MakeOrderObserver> makeOrderObservers = new ArrayList<>();

	/**
	 * the list of waiting list observers
	 */
	private List<WaitingListObserver> waitingListObservers = new ArrayList<>();

	/**
	 * the list of park entrance observers
	 */
	private List<ParkEntranceObserver> parkEntranceObservers = new ArrayList<>();

	/**
	 * the list of occasional customer access observers
	 */
	private List<OccasionalCustomerAccessObserver> occasionalCustomerAccessObservers = new ArrayList<>();

	/**
	 * the list of employee login observers
	 */
	private List<EmployeeLoginObserver> employeeLoginObservers = new ArrayList<>();

	/**
	 * the list of existing customer login observers
	 */
	private List<ExistingCustomerLoginObsooerver> existingCustomerLoginObservers = new ArrayList<>();

	/**
	 * the list of register subscriber observers
	 */
	private List<RegisterSubscriberObserver> registerSubscriberObservers = new ArrayList<>();

	/**
	 * the list of search subscriber observers
	 */
	private List<SearchSubscriberObserver> searchSubscriberObservers = new ArrayList<>();

	/**
	 * the list of register guide observers
	 */
	private List<RegisterGuideObserver> registerGuideObservers = new ArrayList<>();

	/**
	 * the list of report observers
	 */
	private List<ReportObserver> reportObservers = new ArrayList<>();

	/**
	 * the list of park parameter observers
	 */
	private List<ParkParameterObserver> parkParameterObservers = new ArrayList<>();

	/**
	 * the list of entry price observers
	 */
	private List<EntryPriceObserver> entryPriceObservers = new ArrayList<>();

	/**
	 * the list of park visitor counter observers
	 */
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
	 * Returns the current user ID.
	 *
	 * @return the current user ID
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the current user ID.
	 *
	 * @param id the current user ID
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

	// Order observers

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

	/**
	 * Notifies order observers that orders were received.
	 *
	 * @param rows the list of orders
	 */
	private void notifyOrdersReceived(List<Order> rows) {
		for (OrderObserver observer : orderObservers) {
			observer.onOrdersReceived(rows);
		}
	}

	/**
	 * Notifies order observers that a new order was made.
	 *
	 * @param order the order that was made
	 */
	private void notifyOrderMade(Order order) {
		for (OrderObserver observer : orderObservers) {
			observer.addOrder(order);
		}
	}

	/**
	 * Notifies order observers about an order update result.
	 *
	 * @param success whether the update was successful
	 * @param updateMessage the update message data
	 */
	private void notifyUpdateResult(boolean success, UpdateMessage updateMessage) {
		for (OrderObserver observer : orderObservers) {
			observer.onUpdateResult(success, updateMessage);
		}
	}

	/**
	 * Notifies order observers about an order cancellation result.
	 *
	 * @param success whether the cancellation was successful
	 * @param cancelOrderMessage the cancellation message data
	 */
	private void notifyCancelResult(boolean success, CancelOrderMessage cancelOrderMessage) {
		for (OrderObserver observer : orderObservers) {
			observer.onCancelResult(success, cancelOrderMessage);
		}
	}

	// Waiting list observers

	/**
	 * Adds a waiting list observer.
	 *
	 * @param observer the waiting list observer to add
	 */
	public void addWaitingListObserver(WaitingListObserver observer) {
		if (observer != null && !waitingListObservers.contains(observer)) {
			waitingListObservers.add(observer);
		}
	}

	/**
	 * Removes a waiting list observer.
	 *
	 * @param observer the waiting list observer to remove
	 */
	public void removeWaitingListObserver(WaitingListObserver observer) {
		waitingListObservers.remove(observer);
	}

	/**
	 * Notifies waiting list observers about a join waiting list result.
	 *
	 * @param success whether joining the waiting list was successful
	 * @param waitingListMessage the waiting list message data
	 */
	private void notifyJoinWaitingListResult(boolean success, WaitingListMessage waitingListMessage) {
		for (WaitingListObserver observer : waitingListObservers) {
			observer.onJoinWaitingListResult(success, waitingListMessage);
		}
	}

	/**
	 * Notifies waiting list observers that waiting offers were received.
	 *
	 * @param success whether the offers were received successfully
	 * @param offers the list of waiting list offers
	 */
	private void notifyWaitingOffersReceived(boolean success, List<WaitingListMessage> offers) {
		for (WaitingListObserver observer : waitingListObservers) {
			observer.onWaitingOffersReceived(success, offers);
		}
	}

	/**
	 * Notifies waiting list observers about a reject waiting offer result.
	 *
	 * @param success whether rejecting the offer was successful
	 * @param waitingListMessage the waiting list message data
	 */
	private void notifyRejectWaitingOfferResult(boolean success, WaitingListMessage waitingListMessage) {
		for (WaitingListObserver observer : waitingListObservers) {
			observer.onRejectWaitingOfferResult(success, waitingListMessage);
		}
	}

	/**
	 * Notifies waiting list observers about an accept waiting offer result.
	 *
	 * @param success whether accepting the offer was successful
	 * @param waitingListMessage the waiting list message data
	 */
	private void notifyAcceptWaitingOfferResult(boolean success, WaitingListMessage waitingListMessage) {
		for (WaitingListObserver observer : waitingListObservers) {
			observer.onAcceptWaitingOfferResult(success, waitingListMessage);
		}
	}

	// Make order observers

	/**
	 * Adds a make order observer.
	 *
	 * @param observer the make order observer to add
	 */
	public void addMakeOrderObserver(MakeOrderObserver observer) {
		if (observer != null && !makeOrderObservers.contains(observer)) {
			makeOrderObservers.add(observer);
		}
	}

	/**
	 * Removes a make order observer.
	 *
	 * @param observer the make order observer to remove
	 */
	public void removeMakeOrderObserver(MakeOrderObserver observer) {
		makeOrderObservers.remove(observer);
	}

	/**
	 * Notifies make order observers that park names were received.
	 *
	 * @param parkNames the list of park names
	 */
	private void notifyParkNamesReceivedForMakeOrder(List<String> parkNames) {
		for (MakeOrderObserver observer : makeOrderObservers) {
			observer.onParkNamesReceived(parkNames);
		}
	}

	/**
	 * Notifies make order observers about a make order server response.
	 *
	 * @param message the message received from the server
	 */
	private void notifyMakeOrderServerResponse(Message message) {
		for (MakeOrderObserver observer : new ArrayList<>(makeOrderObservers)) {
			observer.onMakeOrderServerResponse(message);
		}
	}

	// Park observers

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
	 * Notifies park observers that parks were received.
	 *
	 * @param parks the list of parks
	 */
	private void notifyParksReceived(List parks) {
		for (ParkObserver observer : parkObservers) {
			observer.onParksReceived(parks);
		}
	}

	// Park entrance observers

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

	/**
	 * Notifies park entrance observers about a check-in order result.
	 *
	 * @param success whether the check-in was successful
	 * @param parkEntranceMessage the park entrance message data
	 */
	private void notifyCheckInOrderResult(boolean success, ParkEntranceMessage parkEntranceMessage) {
		for (ParkEntranceObserver observer : parkEntranceObservers) {
			observer.onCheckInOrderResult(success, parkEntranceMessage);
		}
	}

	/**
	 * Notifies park entrance observers about a check-out visit result.
	 *
	 * @param success whether the check-out was successful
	 * @param parkEntranceMessage the park entrance message data
	 */
	private void notifyCheckOutVisitResult(boolean success, ParkEntranceMessage parkEntranceMessage) {
		for (ParkEntranceObserver observer : parkEntranceObservers) {
			observer.onCheckOutVisitResult(success, parkEntranceMessage);
		}
	}

	/**
	 * Notifies park entrance observers about an occasional visit result.
	 *
	 * @param success whether creating the occasional visit was successful
	 * @param parkEntranceMessage the park entrance message data
	 */
	private void notifyOccasionalVisitResult(boolean success, ParkEntranceMessage parkEntranceMessage) {
		for (ParkEntranceObserver observer : parkEntranceObservers) {
			observer.onOccasionalVisitResult(success, parkEntranceMessage);
		}
	}

	/**
	 * Notifies park entrance observers that the current visitors count was received.
	 *
	 * @param success whether receiving the current visitors count was successful
	 * @param parkEntranceMessage the park entrance message data
	 */
	private void notifyCurrentVisitorsReceived(boolean success, ParkEntranceMessage parkEntranceMessage) {
		for (ParkEntranceObserver observer : parkEntranceObservers) {
			observer.onCurrentVisitorsReceived(success, parkEntranceMessage);
		}
	}

	// Occasional customer access observers

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
	 * Notifies occasional customer access observers about the access result.
	 *
	 * @param response the response received from the server
	 */
	private void notifyOccasionalCustomerAccessResult(OperationResponse response) {
		for (OccasionalCustomerAccessObserver observer : occasionalCustomerAccessObservers) {
			observer.onOccasionalCustomerAccessResult(response);
		}
	}

	// Employee login observers

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
	 * Notifies employee login observers about the login result.
	 *
	 * @param response the response received from the server
	 */
	private void notifyEmployeeLoginResult(OperationResponse response) {
		for (EmployeeLoginObserver observer : employeeLoginObservers) {
			observer.onEmployeeLoginResult(response);
		}
	}

	// Existing customer login observers

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
	 * Notifies existing customer login observers about the login result.
	 *
	 * @param response the response received from the server
	 */
	private void notifyExistingCustomerLoginResult(OperationResponse response) {
		for (ExistingCustomerLoginObserver observer : existingCustomerLoginObservers) {
			observer.onExistingCustomerLoginResult(response);
		}
	}

	// Register subscriber observers

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
	 * Notifies register subscriber observers about the register subscriber result.
	 *
	 * @param response the response received from the server
	 */
	private void notifyRegisterSubscriberResult(OperationResponse response) {
		for (RegisterSubscriberObserver observer : registerSubscriberObservers) {
			observer.onRegisterSubscriberResult(response);
		}
	}

	// Search subscriber observers

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
	 * Notifies search subscriber observers about the search result.
	 *
	 * @param response the response received from the server
	 */
	private void notifySearchSubscriberResult(OperationResponse response) {
		for (SearchSubscriberObserver observer : searchSubscriberObservers) {
			observer.onSearchSubscriberResult(response);
		}
	}

	// Register guide observers

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
	 * Notifies register guide observers about the register guide result.
	 *
	 * @param response the response received from the server
	 */
	private void notifyRegisterGuideResult(OperationResponse response) {
		for (RegisterGuideObserver observer : registerGuideObservers) {
			observer.onRegisterGuideResult(response);
		}
	}

	// Report observers

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

	/**
	 * Notifies report observers about a report response.
	 *
	 * @param response the response received from the server
	 */
	private void notifyReportResponse(OperationResponse response) {
		for (ReportObserver observer : reportObservers) {
			observer.onReportResponse(response);
		}
	}

	// Park parameter observers

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

	/**
	 * Notifies park parameter observers that pending requests were received.
	 *
	 * @param requests the list of pending park parameter change requests
	 */
	private void notifyPendingParkParameterRequestsReceived(List<ParkParameterChangeRequest> requests) {
		for (ParkParameterObserver observer : parkParameterObservers) {
			observer.onPendingParkParameterRequestsReceived(requests);
		}
	}

	/**
	 * Notifies park parameter observers about an operation response.
	 *
	 * @param response the response received from the server
	 * @param responseType the protocol type of the response
	 */
	private void notifyParkParameterOperationResponse(OperationResponse response, Protocol responseType) {
		for (ParkParameterObserver observer : parkParameterObservers) {
			observer.onParkParameterOperationResponse(response, responseType);
		}
	}

	// Entry price observers

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

	/**
	 * Notifies entry price observers that the entry price was calculated.
	 *
	 * @param response the response received from the server
	 */
	private void notifyEntryPriceCalculated(OperationResponse response) {
		for (EntryPriceObserver observer : entryPriceObservers) {
			observer.onEntryPriceCalculated(response);
		}
	}

	// Park visitor counter observers

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

	/**
	 * Notifies park visitor counter observers that counters were received.
	 *
	 * @param counters the list of park visitor counter snapshots
	 */
	private void notifyParkVisitorCountersReceived(List<ParkVisitorCounterSnapshot> counters) {
		for (ParkVisitorCounterObserver observer : parkVisitorCounterObservers) {
			observer.onParkVisitorCountersReceived(counters);
		}
	}

	/**
	 * Notifies park visitor counter observers about an operation response.
	 *
	 * @param response the response received from the server
	 * @param responseType the protocol type of the response
	 */
	private void notifyParkVisitorCounterOperationResponse(OperationResponse response, Protocol responseType) {
		for (ParkVisitorCounterObserver observer : parkVisitorCounterObservers) {
			observer.onParkVisitorCounterOperationResponse(response, responseType);
		}
	}

	/**
	 * Notifies park visitor counter observers that counters were updated.
	 */
	private void notifyParkVisitorCountersUpdated() {
		for (ParkVisitorCounterObserver observer : parkVisitorCounterObservers) {
			observer.onParkVisitorCountersUpdated();
		}
	}

	// Requests to server

	/**
	 * Requests the orders of the current user from the server.
	 */
	public void requestOrders() {
		sendMessageToServer(new Message(id, Protocol.RETURN_ORDER));
	}

	/**
	 * Requests orders by subscriber ID from the server.
	 *
	 * @param subscriberId the subscriber ID
	 */
	public void requestOrdersBySubscriberId(int subscriberId) {
		this.id = String.valueOf(subscriberId);
		sendMessageToServer(new Message(String.valueOf(subscriberId), Protocol.RETURN_ORDER));
	}

	/**
	 * Requests orders by park ID from the server.
	 *
	 * @param parkId the park ID
	 */
	public void requestOrdersByParkId(int parkId) {
		sendMessageToServer(new Message(parkId, Protocol.GET_PARK_ORDERS_REQUEST));
	}

	/**
	 * Requests all orders for a service representative.
	 */
	public void requestAllOrdersForServiceRepresentative() {
		sendMessageToServer(new Message(null, Protocol.GET_ALL_ORDERS_REQUEST));
	}

	/**
	 * Sends an order update request to the server.
	 *
	 * @param updateMessage the update request data
	 */
	public void requestUpdate(UpdateMessage updateMessage) {
		sendMessageToServer(new Message(updateMessage, Protocol.UPDATE_ORDER));
	}

	/**
	 * Sends an order cancellation request to the server.
	 *
	 * @param cancelOrderMessage the cancellation request data
	 */
	public void requestCancelOrder(CancelOrderMessage cancelOrderMessage) {
		sendMessageToServer(new Message(cancelOrderMessage, Protocol.CANCEL_ORDER));
	}

	/**
	 * Sends a join waiting list request to the server.
	 *
	 * @param waitingListMessage the waiting list request data
	 */
	public void requestJoinWaitingList(WaitingListMessage waitingListMessage) {
		sendMessageToServer(new Message(waitingListMessage, Protocol.JOIN_WAITING_LIST_REQUEST));
	}

	/**
	 * Requests waiting offers for a subscriber.
	 *
	 * @param subscriberId the subscriber ID
	 */
	public void requestWaitingOffers(int subscriberId) {
		sendMessageToServer(new Message(subscriberId, Protocol.GET_WAITING_OFFERS_REQUEST));
	}

	/**
	 * Sends a reject waiting offer request to the server.
	 *
	 * @param waitingId the waiting list request ID
	 */
	public void requestRejectWaitingOffer(int waitingId) {
		WaitingListMessage waitingListMessage = new WaitingListMessage(waitingId);
		sendMessageToServer(new Message(waitingListMessage, Protocol.REJECT_WAITING_OFFER_REQUEST));
	}

	/**
	 * Sends an accept waiting offer request to the server.
	 *
	 * @param waitingId the waiting list request ID
	 */
	public void requestAcceptWaitingOffer(int waitingId) {
		WaitingListMessage waitingListMessage = new WaitingListMessage(waitingId);
		sendMessageToServer(new Message(waitingListMessage, Protocol.ACCEPT_WAITING_OFFER_REQUEST));
	}

	/**
	 * Requests the active parks from the server.
	 */
	public void requestActiveParks() {
		sendMessageToServer(new Message(null, Protocol.GET_ACTIVE_PARKS));
	}

	/**
	 * Sends an occasional customer access request by order number.
	 *
	 * @param orderNumber the order number
	 */
	public void requestOccasionalCustomerAccess(int orderNumber) {
		this.id = String.valueOf(orderNumber);
		sendMessageToServer(new Message(orderNumber, Protocol.OCCASIONAL_CUSTOMER_ACCESS_REQUEST));
	}

	/**
	 * Sends an occasional customer access request by customer ID number.
	 *
	 * @param customerIdNumber the customer ID number
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
	 * @param request the register subscriber request data
	 */
	public void requestRegisterSubscriber(RegisterSubscriberRequest request) {
		sendMessageToServer(new Message(request, Protocol.REGISTER_SUBSCRIBER_REQUEST));
	}

	/**
	 * Sends a search subscriber request to the server.
	 *
	 * @param subscriberId the subscriber ID
	 */
	public void requestSearchSubscriber(int subscriberId) {
		sendMessageToServer(new Message(subscriberId, Protocol.SEARCH_SUBSCRIBER_REQUEST));
	}

	/**
	 * Sends a register guide request to the server.
	 *
	 * @param request the guide registration request data
	 */
	public void requestRegisterGuide(GuideRegistrationRequest request) {
		sendMessageToServer(new Message(request, Protocol.REGISTER_GUIDE_REQUEST));
	}

	/**
	 * Sends a check-in order request to the server.
	 *
	 * @param parkEntranceMessage the park entrance request data
	 */
	public void requestCheckInOrder(ParkEntranceMessage parkEntranceMessage) {
		sendMessageToServer(new Message(parkEntranceMessage, Protocol.CHECK_IN_ORDER_REQUEST));
	}

	/**
	 * Sends a check-out visit request to the server.
	 *
	 * @param parkEntranceMessage the park entrance request data
	 */
	public void requestCheckOutVisit(ParkEntranceMessage parkEntranceMessage) {
		sendMessageToServer(new Message(parkEntranceMessage, Protocol.CHECK_OUT_VISIT_REQUEST));
	}

	/**
	 * Sends an occasional visit request to the server.
	 *
	 * @param parkEntranceMessage the park entrance request data
	 */
	public void requestOccasionalVisit(ParkEntranceMessage parkEntranceMessage) {
		sendMessageToServer(new Message(parkEntranceMessage, Protocol.OCCASIONAL_VISIT_REQUEST));
	}

	/**
	 * Requests the current number of visitors in a park.
	 *
	 * @param parkEntranceMessage the park entrance request data
	 */
	public void requestCurrentVisitors(ParkEntranceMessage parkEntranceMessage) {
		sendMessageToServer(new Message(parkEntranceMessage, Protocol.GET_CURRENT_VISITORS_REQUEST));
	}

	/**
	 * Sends a report request to the server.
	 *
	 * @param request the report request data
	 */
	public void requestReport(ReportRequest request) {
		sendMessageToServer(new Message(request, Protocol.GET_REPORT_REQUEST));
	}

	/**
	 * Sends an entry price calculation request to the server.
	 *
	 * @param orderNumber the order number
	 */
	public void calculateEntryPrice(int orderNumber) {
		EntryPriceRequest request = new EntryPriceRequest(orderNumber);
		sendMessageToServer(new Message(request, Protocol.CALCULATE_ENTRY_PRICE_REQUEST));
	}

	/**
	 * Sends a create park parameter change request to the server.
	 *
	 * @param parkId the park ID
	 * @param employeeId the employee ID
	 * @param parameterName the parameter name
	 * @param newValue the new parameter value
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
	 * Requests pending park parameter change requests from the server.
	 *
	 * @param employeeId the employee ID
	 */
	public void requestPendingParkParameterChangeRequests(int employeeId) {
		sendMessageToServer(new Message(employeeId, Protocol.GET_PENDING_PARK_PARAMETER_CHANGE_REQUESTS));
	}

	/**
	 * Sends an approve park parameter change request to the server.
	 *
	 * @param requestId the request ID
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
	 * Sends a reject park parameter change request to the server.
	 *
	 * @param requestId the request ID
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
	 * Requests park visitor counters from the server.
	 *
	 * @param employeeId the employee ID
	 */
	public void requestParkVisitorCounters(int employeeId) {
		sendMessageToServer(new Message(employeeId, Protocol.GET_PARK_VISITOR_COUNTERS_REQUEST));
	}

	/**
	 * Sends a park visitor counter update request to the server.
	 *
	 * @param request the park visitor counter update request data
	 */
	public void updateParkVisitorCounter(ParkVisitorCounterUpdateRequest request) {
		sendMessageToServer(new Message(request, Protocol.UPDATE_PARK_VISITOR_COUNTER_REQUEST));
	}

	/**
	 * Sends a message to the server.
	 *
	 * @param message the message to send
	 */
	public void sendMessageToServer(Message message) {
		if (message != null) {
			client.handleMessageFromClientUI(message);
		}
	}
	
	/**
	 * Sends a logout request for the current user to the server.
	 */
	public void logoutCurrentUserFromServer() {
		sendMessageToServer(new Message(null, Protocol.CLIENT_LOGOUT_USER));
	}

	// Messages from server

	/**
	 * Displays a message received from the server.
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
	 * Handles a message received from the server.
	 *
	 * @param message the message received from the server
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
			
		case CLIENT_LOGOUT_USER_SUCCESS:
			System.out.println("User logged out successfully from server.");
			break;
			
		case ORDER_REMINDER:
			handleOrderReminder(message);
			break;
		
		case ACCEPT_ORDER_REMINDER_CONFIRMATION:
			handleOrderReminderAcceptanceConfirmation(message);
			break;
			
		case DECLINE_ORDER_REMINDER_CONFIRMATION:
			handleOrderReminderDeclineConfirmation(message);
			break;
		
		case ERROR_ORDER_REMINDER_CONFIRMATION:
			handleOrderReminderAnswerConfirmationError(message);
			break;

		default:
			System.out.println("Error: unknown server response in ClientController: " + type);
			break;
		}
	}

	/**
	 * Handles a general operation response from the server.
	 *
	 * @param data the response data
	 * @param consumer the response consumer
	 */
	private void handleOperationResponse(Object data, OperationResponseConsumer consumer) {
		if (data instanceof OperationResponse) {
			consumer.accept((OperationResponse) data);
			return;
		}

		consumer.accept(new OperationResponse(false, "Invalid operation response from server", null));
	}

	/**
	 * Handles an order response received from the server.
	 *
	 * @param data the response data
	 */
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

	/**
	 * Handles a park names response received from the server.
	 *
	 * @param data the response data
	 */
	private void handleParkNamesResponse(Object data) {
		List<String> parkNames = parseParkNamesMessage(data);

		if (parkNames != null) {
			notifyParkNamesReceivedForMakeOrder(parkNames);
		} else {
			notifyParkNamesReceivedForMakeOrder(null);
		}
	}

	/**
	 * Handles a parks response received from the server.
	 *
	 * @param data the response data
	 */
	private void handleParksResponse(Object data) {
		List parks = parseParkMessage(data);

		if (parks != null) {
			notifyParksReceived(parks);
		}
	}

	/**
	 * Handles a report response received from the server.
	 *
	 * @param data the response data
	 */
	private void handleReportResponse(Object data) {
		if (data instanceof OperationResponse) {
			notifyReportResponse((OperationResponse) data);
			return;
		}

		notifyReportResponse(new OperationResponse(false, "Invalid report response from server", null));
	}

	/**
	 * Handles an entry price response received from the server.
	 *
	 * @param data the response data
	 */
	private void handleEntryPriceResponse(Object data) {
		if (data instanceof OperationResponse) {
			notifyEntryPriceCalculated((OperationResponse) data);
			return;
		}

		notifyEntryPriceCalculated(new OperationResponse(false, "Invalid entry price response from server", null));
	}

	/**
	 * Handles pending park parameter change requests received from the server.
	 *
	 * @param data the response data
	 * @param responseType the protocol type of the response
	 */
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

	/**
	 * Handles a park parameter operation response received from the server.
	 *
	 * @param data the response data
	 * @param responseType the protocol type of the response
	 */
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

	/**
	 * Handles park visitor counters received from the server.
	 *
	 * @param data the response data
	 * @param responseType the protocol type of the response
	 */
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

	/**
	 * Handles a park visitor counter operation response received from the server.
	 *
	 * @param data the response data
	 * @param responseType the protocol type of the response
	 */
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

	// Disconnect

	/**
	 * Disconnects the client from the server.
	 */
	public void disconnectFromServer() {
		userIssuedDisconnect = true;
		sendMessageToServer(new Message(null, Protocol.CLIENT_DISCONNECT_USER));
		client.quit();
	}

	/**
	 * Handles a disconnect request sent by the server.
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
	 * Returns whether the disconnect was requested by the user.
	 *
	 * @return true if the disconnect was requested by the user
	 */
	public boolean isUserIssuedDisconnect() {
		return userIssuedDisconnect;
	}

	/**
	 * Sets whether the disconnect was requested by the user.
	 *
	 * @param userIssuedDisconnect whether the disconnect was requested by the user
	 */
	public void setUserIssuedDisconnect(boolean userIssuedDisconnect) {
		this.userIssuedDisconnect = userIssuedDisconnect;
	}

	// Parsers

	/**
	 * Parses order data received from the server.
	 *
	 * @param data the data received from the server
	 * @return the list of orders, or null if the data is invalid
	 */
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

	/**
	 * Parses waiting list offer data received from the server.
	 *
	 * @param data the data received from the server
	 * @return the list of waiting list offers, or null if the data is invalid
	 */
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

	/**
	 * Parses park names data received from the server.
	 *
	 * @param data the data received from the server
	 * @return the list of park names, or null if the data is invalid
	 */
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

	/**
	 * Parses park data received from the server.
	 *
	 * @param data the data received from the server
	 * @return the list of parks, or null if the data is invalid
	 */
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

	/**
	 * Parses park parameter change request data received from the server.
	 *
	 * @param data the data received from the server
	 * @return the list of park parameter change requests, or null if the data is invalid
	 */
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

	/**
	 * Parses park visitor counter data received from the server.
	 *
	 * @param data the data received from the server
	 * @return the list of park visitor counter snapshots, or null if the data is invalid
	 */
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

	/**
	 * Interface for handling operation responses.
	 */
	private interface OperationResponseConsumer {
		/**
		 * Accepts an operation response.
		 *
		 * @param response the operation response
		 */
		void accept(OperationResponse response);
	}
	
	/**
	 * this method notifies order observers that an order was canceled via reminder
	 * 
	 * @param o the order
	 */
	private void notifyReminderDeclined(Order o) {
		for (OrderObserver observer : orderObservers) {
			observer.reminderDeclined(o);
		}
	}
	
	/**
	 * Handles an order reminder received from the server.
	 *
	 * @param message the order reminder message
	 */
	private void handleOrderReminder(Message message) {
		MakePopUp.makeReminderPopup(this, (Order) message.getData(), "Order Reminder");
	}
	
	/**
	 * Handles the confirmation of accepting an order reminder.
	 *
	 * @param message the confirmation message
	 */
	private void handleOrderReminderAcceptanceConfirmation(Message message) {
		MakePopUp.makePopup("Confirmed: Order accepted",
				"Order reminder accepted successfully\nOrder ID: " + 
				((Order) message.getData()).getOrderId());
	}

	/**
	 * Handles the confirmation of declining an order reminder.
	 *
	 * @param message the confirmation message
	 */
	private void handleOrderReminderDeclineConfirmation(Message message) {
		System.out.println("handleOrderReminderDeclineConfirmation");
		MakePopUp.makePopup("Confirmed: Order cancelled",
				"Order cancelled successfully\nOrder ID: " + 
				((Order) message.getData()).getOrderId());
		notifyReminderDeclined((Order) message.getData());
	}

	/**
	 * Handles an error response for an order reminder answer.
	 *
	 * @param message the error message
	 */
	private void handleOrderReminderAnswerConfirmationError(Message message) {
		MakePopUp.makePopup("Error: Order Reminder",
				"Order Confirmation Error\nOrder ID: " + 
				((Order) message.getData()).getOrderId());
	}
}

